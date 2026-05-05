package com.orbital.lite.service;

import com.orbital.lite.audit.Auditable;
import com.orbital.lite.dto.UserRequest;
import com.orbital.lite.dto.UserResponse;
import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.Role;
import com.orbital.lite.exception.ResourceNotFoundException;
import com.orbital.lite.repository.RoleRepository;
import com.orbital.lite.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Auditable(action = "CREATE", entity = "USER")
    public UserResponse createUser(UserRequest request) {
        String username = normalize(request.username());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required for new users");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(request.enabled() == null || request.enabled());
        user.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(user));
    }

    @Auditable(action = "UPDATE", entity = "USER")
    public UserResponse updateUser(Long id, UserRequest request) {
        AppUser user = findUser(id);
        String username = normalize(request.username());

        userRepository.findByUsernameIgnoreCase(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Username already exists");
                });

        user.setUsername(username);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        user.setRoles(resolveRoles(request.roles()));
        return toResponse(userRepository.save(user));
    }

    @Auditable(action = "DELETE", entity = "USER")
    public void deleteUser(Long id) {
        userRepository.delete(findUser(id));
    }

    public AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<String> requestedRoles = roleNames == null || roleNames.isEmpty()
                ? Set.of("EMPLOYEE")
                : roleNames;

        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : requestedRoles) {
            String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
            Role role = roleRepository.findByName(normalizedRole)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + normalizedRole));
            roles.add(role);
        }
        return roles;
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                user.getCreatedAt()
        );
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
