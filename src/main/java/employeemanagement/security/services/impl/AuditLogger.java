package employeemanagement.security.services.impl;
import employeemanagement.security.Repository.AuditLogRepository;
import employeemanagement.security.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogger {
    private final AuditLogRepository auditLogRepository;

    public String resolvePerformedBy() {
        String performedBy = "SYSTEM";  // Default system user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            performedBy = authentication.getName(); // Get username or user details
        }

        return performedBy;
    }

    public void logAudit(String action, String performedBy, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }
}
