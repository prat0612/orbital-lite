package com.orbital.lite.audit;

import com.orbital.lite.service.AuditService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void audit(JoinPoint joinPoint, Auditable auditable, Object result) {
        auditService.logCurrentUser(auditable.action(), auditable.entity(), resolveEntityId(joinPoint, result));
    }

    private Long resolveEntityId(JoinPoint joinPoint, Object result) {
        Long resultId = extractId(result);
        if (resultId != null) {
            return resultId;
        }

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long id) {
                return id;
            }
        }
        return null;
    }

    private Long extractId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            Method idMethod = result.getClass().getMethod("id");
            Object value = idMethod.invoke(result);
            if (value instanceof Long id) {
                return id;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }
}
