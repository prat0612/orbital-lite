package com.orbital.lite.config;

import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.Permission;
import com.orbital.lite.entity.Role;
import com.orbital.lite.repository.PermissionRepository;
import com.orbital.lite.repository.RoleRepository;
import com.orbital.lite.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final List<String> PERMISSIONS = List.of(
            "CREATE_EMPLOYEE",
            "READ_EMPLOYEE",
            "UPDATE_EMPLOYEE",
            "DELETE_EMPLOYEE",
            "MANAGE_USERS",
            "APPLY_LEAVE",
            "VIEW_ALL_LEAVES",
            "APPROVE_LEAVE",
            "VIEW_DASHBOARD",
            "VIEW_REPORTS"
    );

    @Bean
    @Transactional
    public CommandLineRunner seedData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            for (String permissionName : PERMISSIONS) {
                permissionRepository.findByName(permissionName).orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setName(permissionName);
                    return permissionRepository.save(permission);
                });
            }

            Map<String, Set<String>> rolePermissions = Map.of(
                    "ADMIN", new LinkedHashSet<>(PERMISSIONS),
                    "MANAGER", Set.of(
                            "CREATE_EMPLOYEE",
                            "READ_EMPLOYEE",
                            "UPDATE_EMPLOYEE",
                            "APPLY_LEAVE",
                            "VIEW_ALL_LEAVES",
                            "APPROVE_LEAVE",
                            "VIEW_DASHBOARD",
                            "VIEW_REPORTS"
                    ),
                    "EMPLOYEE", Set.of(
                            "READ_EMPLOYEE",
                            "APPLY_LEAVE"
                    )
            );

            for (Map.Entry<String, Set<String>> entry : rolePermissions.entrySet()) {
                Role role = roleRepository.findByName(entry.getKey()).orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(entry.getKey());
                    return roleRepository.save(newRole);
                });

                Set<Permission> permissions = new LinkedHashSet<>();
                for (String permissionName : entry.getValue()) {
                    permissionRepository.findByName(permissionName).ifPresent(permissions::add);
                }
                role.setPermissions(permissions);
                roleRepository.save(role);
            }

            createUserIfMissing(userRepository, roleRepository, passwordEncoder, "admin", "admin123", "ADMIN");
            createUserIfMissing(userRepository, roleRepository, passwordEncoder, "manager", "manager123", "MANAGER");
            createUserIfMissing(userRepository, roleRepository, passwordEncoder, "employee", "employee123", "EMPLOYEE");
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            String roleName
    ) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role missing: " + roleName));

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }
}
