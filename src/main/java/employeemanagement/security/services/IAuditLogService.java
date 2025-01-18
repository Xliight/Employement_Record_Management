package employeemanagement.security.services;

import employeemanagement.security.model.AuditLog;

import java.util.List;

public interface IAuditLogService {
    List<AuditLog> getAllAuditLogs();
}