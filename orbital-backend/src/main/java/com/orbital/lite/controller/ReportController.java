package com.orbital.lite.controller;

import com.orbital.lite.dto.EmployeeReportResponse;
import com.orbital.lite.dto.LeaveReportResponse;
import com.orbital.lite.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/leaves")
    public ResponseEntity<LeaveReportResponse> leaveReport() {
        return ResponseEntity.ok(reportService.leaveReport());
    }

    @GetMapping("/employees")
    public ResponseEntity<EmployeeReportResponse> employeeReport() {
        return ResponseEntity.ok(reportService.employeeReport());
    }
}
