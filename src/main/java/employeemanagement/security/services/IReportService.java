package employeemanagement.security.services;

import employeemanagement.security.DTO.EmployyeDTO;
import employeemanagement.security.model.AuditLog;
import employeemanagement.security.model.Employee;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public interface IReportService {
//    void generateEmployeeReportCSV(List<Employee> employees, FileOutputStream outputStream) throws IOException;

    void generateEmployeeReportCSV(List<Employee> employees, String filePath) throws IOException;

    void generateAuditLogReport(List<AuditLog> auditLogs, String fileName) throws IOException;
}
