package com.orbital.lite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 80, message = "Username must be 80 characters or fewer")
        String username,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        Boolean enabled,

        Set<String> roles
) {
}
