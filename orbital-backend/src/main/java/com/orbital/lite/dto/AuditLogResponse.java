package com.orbital.lite.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String user,
        String action,
        String entity,
        Long entityId,
        Instant timestamp
) {
}
