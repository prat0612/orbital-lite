package com.orbital.lite.security;

import com.orbital.lite.entity.AppUser;
import com.orbital.lite.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String username() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    public AppUser user() {
        return userRepository.findByUsernameIgnoreCase(username())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }
}
