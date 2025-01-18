package employeemanagement.security.controllers;


import employeemanagement.security.DTO.EmployyeDTO;
import employeemanagement.security.model.AuditLog;
import employeemanagement.security.model.Employee;
import employeemanagement.security.services.IAuditLogService;
import employeemanagement.security.services.impl.AuditLogger;
import employeemanagement.security.services.impl.ReportService;
import employeemanagement.security.services.impl.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;


@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AuditLogger auditLogger;
    private final ReportService reportService;
    private final EmployeeServiceImpl employeeService;
    private final IAuditLogService auditLogService;

    @Value("${report.file.save.location:/path/to/save/reports}") // Default save location (override via properties)
    private String reportSaveLocation;
    @GetMapping("/employee/report/csv")
    public ResponseEntity<String> generateCSVReportAndSave() throws IOException {
        List<Employee> employees = employeeService.getAllEmployees();

        String fileName = "employees_report.csv";
        String filePath = Paths.get(reportSaveLocation, fileName).toString();

        // Generate and save the CSV report
        try {
            reportService.generateEmployeeReportCSV(employees, filePath);
            String performedBy = auditLogger.resolvePerformedBy();
            auditLogger.logAudit("CREATE", performedBy, String.format("Generated Report: %s", filePath));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate report due to file access error: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Report saved at: " + filePath);
    }
    @GetMapping("/logs")
    public ResponseEntity<String> generateAuditLogReport() {
        try {
            List<AuditLog> auditLogs = auditLogService.getAllAuditLogs();

            String fileName = "auditlog_report.csv";
            reportService.generateAuditLogReport(auditLogs, fileName);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Audit log report has been generated successfully and saved saved at:" + reportSaveLocation+"/"+fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate report due to file access error: " + e.getMessage());
        }
    }


}
