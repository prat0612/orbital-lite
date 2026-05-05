package com.orbital.lite.dto;

public record LeaveReportResponse(
        long pending,
        long approved,
        long rejected,
        long total
) {
}
