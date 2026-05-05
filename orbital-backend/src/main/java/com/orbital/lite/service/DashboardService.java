package com.orbital.lite.service;

import com.orbital.lite.dto.DashboardSummaryResponse;
import com.orbital.lite.entity.LeaveStatus;
import com.orbital.lite.repository.EmployeeRepository;
import com.orbital.lite.repository.LeaveRequestRepository;
import com.orbital.lite.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AuditService auditService;

    public DashboardService(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            LeaveRequestRepository leaveRequestRepository,
            AuditService auditService
    ) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                employeeRepository.count(),
                userRepository.count(),
                leaveRequestRepository.countByStatus(LeaveStatus.PENDING),
                auditService.recent(10)
        );
    }
}
