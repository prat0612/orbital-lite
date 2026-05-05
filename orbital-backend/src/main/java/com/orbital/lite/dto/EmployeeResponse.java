package com.orbital.lite.dto;

import java.time.Instant;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String role,
        String department,
        Instant createdAt
) {
}
