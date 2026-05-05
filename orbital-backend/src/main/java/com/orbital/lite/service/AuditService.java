package com.orbital.lite.service;

import com.orbital.lite.dto.AuditLogResponse;
import com.orbital.lite.entity.AuditLog;
import com.orbital.lite.repository.AuditLogRepository;
import com.orbital.lite.security.CurrentUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditService(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCurrentUser(String action, String entity, Long entityId) {
        log(currentUserService.username(), action, entity, entityId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String user, String action, String entity, Long entityId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntity(entity);
        auditLog.setEntityId(entityId);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> recent(int limit) {
        return auditLogRepository.findByOrderByTimestampDesc(PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser(),
                auditLog.getAction(),
                auditLog.getEntity(),
                auditLog.getEntityId(),
                auditLog.getTimestamp()
        );
    }
}
