package employeemanagement.security.services.impl;

import employeemanagement.security.Repository.AuditLogRepository;
import employeemanagement.security.model.AuditLog;
import employeemanagement.security.services.IAuditLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Constructor injection for the repository
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
