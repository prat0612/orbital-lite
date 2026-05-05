package com.orbital.lite.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long totalEmployees,
        long totalUsers,
        long pendingLeaves,
        List<AuditLogResponse> recentAuditLogs
) {
}
