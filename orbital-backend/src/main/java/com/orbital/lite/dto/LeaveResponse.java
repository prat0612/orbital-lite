package com.orbital.lite.dto;

import com.orbital.lite.entity.LeaveStatus;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveResponse(
        Long id,
        Long userId,
        String username,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status,
        String reason,
        Instant createdAt
) {
}
