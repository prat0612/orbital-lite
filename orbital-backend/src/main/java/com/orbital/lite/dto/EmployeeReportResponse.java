package com.orbital.lite.dto;

import java.util.Map;

public record EmployeeReportResponse(
        long totalEmployees,
        Map<String, Long> byDepartment,
        Map<String, Long> byRole
) {
}
