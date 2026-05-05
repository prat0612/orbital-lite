package com.orbital.lite.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        boolean enabled,
        Set<String> roles,
        Instant createdAt
) {
}
