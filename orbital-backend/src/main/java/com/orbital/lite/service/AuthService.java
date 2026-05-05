package com.orbital.lite.service;

import com.orbital.lite.dto.AuthRequest;
import com.orbital.lite.dto.AuthResponse;
import com.orbital.lite.dto.RegisterRequest;
import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.Role;
import com.orbital.lite.repository.RoleRepository;
import com.orbital.lite.repository.UserRepository;
import com.orbital.lite.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new IllegalStateException("EMPLOYEE role is missing"));

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(Set.of(employeeRole));

        AppUser saved = userRepository.save(user);
        auditService.log(saved.getUsername(), "REGISTER", "USER", saved.getId());
        return toAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String username = normalize(request.username());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        auditService.log(user.getUsername(), "LOGIN", "USER", user.getId());
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getUsername(),
                user.getRoles().stream().map(Role::getName).sorted().toList()
        );
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
