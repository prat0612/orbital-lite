package com.orbital.lite.service;

import com.orbital.lite.dto.EmployeeReportResponse;
import com.orbital.lite.dto.LeaveReportResponse;
import com.orbital.lite.entity.Employee;
import com.orbital.lite.entity.LeaveStatus;
import com.orbital.lite.repository.EmployeeRepository;
import com.orbital.lite.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public ReportService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public LeaveReportResponse leaveReport() {
        long pending = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
        long rejected = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);
        return new LeaveReportResponse(pending, approved, rejected, pending + approved + rejected);
    }

    @Transactional(readOnly = true)
    public EmployeeReportResponse employeeReport() {
        var employees = employeeRepository.findAll();
        Map<String, Long> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(employee -> valueOrUnassigned(employee.getDepartment()), Collectors.counting()));
        Map<String, Long> byRole = employees.stream()
                .collect(Collectors.groupingBy(employee -> valueOrUnassigned(employee.getRole()), Collectors.counting()));
        return new EmployeeReportResponse(employees.size(), byDepartment, byRole);
    }

    private String valueOrUnassigned(String value) {
        return value == null || value.isBlank() ? "Unassigned" : value;
    }
}
