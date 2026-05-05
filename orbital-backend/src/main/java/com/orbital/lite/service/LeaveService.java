package com.orbital.lite.service;

import com.orbital.lite.audit.Auditable;
import com.orbital.lite.dto.LeaveRequestDto;
import com.orbital.lite.dto.LeaveResponse;
import com.orbital.lite.entity.AppUser;
import com.orbital.lite.entity.LeaveRequest;
import com.orbital.lite.entity.LeaveStatus;
import com.orbital.lite.exception.ResourceNotFoundException;
import com.orbital.lite.repository.LeaveRequestRepository;
import com.orbital.lite.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserService currentUserService;
    private final LeaveWorkflowService leaveWorkflowService;
    private final NotificationService notificationService;

    public LeaveService(
            LeaveRequestRepository leaveRequestRepository,
            CurrentUserService currentUserService,
            LeaveWorkflowService leaveWorkflowService,
            NotificationService notificationService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserService = currentUserService;
        this.leaveWorkflowService = leaveWorkflowService;
        this.notificationService = notificationService;
    }

    @Auditable(action = "APPLY_LEAVE", entity = "LEAVE")
    public LeaveResponse applyLeave(LeaveRequestDto request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        AppUser user = currentUserService.user();
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(user);
        leaveRequest.setStartDate(request.startDate());
        leaveRequest.setEndDate(request.endDate());
        leaveRequest.setReason(request.reason().trim());
        leaveRequest.setStatus(leaveWorkflowService.initialStatus(request.startDate(), request.endDate()));

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        if (saved.getStatus() == LeaveStatus.APPROVED) {
            notificationService.notify(user, "Your leave request #" + saved.getId() + " was auto-approved.");
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> myLeaves() {
        return leaveRequestRepository.findByUserOrderByCreatedAtDesc(currentUserService.user())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> allLeaves() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Auditable(action = "APPROVE_LEAVE", entity = "LEAVE")
    public LeaveResponse approveLeave(Long id) {
        LeaveRequest leaveRequest = findLeave(id);
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notificationService.notify(saved.getUser(), "Your leave request #" + saved.getId() + " was approved.");
        return toResponse(saved);
    }

    @Auditable(action = "REJECT_LEAVE", entity = "LEAVE")
    public LeaveResponse rejectLeave(Long id) {
        LeaveRequest leaveRequest = findLeave(id);
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        notificationService.notify(saved.getUser(), "Your leave request #" + saved.getId() + " was rejected.");
        return toResponse(saved);
    }

    private LeaveRequest findLeave(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private LeaveResponse toResponse(LeaveRequest leaveRequest) {
        return new LeaveResponse(
                leaveRequest.getId(),
                leaveRequest.getUser().getId(),
                leaveRequest.getUser().getUsername(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getStatus(),
                leaveRequest.getReason(),
                leaveRequest.getCreatedAt()
        );
    }
}
