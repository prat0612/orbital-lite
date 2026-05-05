package com.orbital.lite.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String message,
        Instant createdAt,
        boolean read
) {
}
