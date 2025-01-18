package employeemanagement.security.services.impl;

import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.model.AuditLog;
import employeemanagement.security.model.Employee;
import employeemanagement.security.services.IReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService implements IReportService {

    @Value("${report.file.save.location}")
    private String reportSaveLocation;

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY = 500;

    @Override
    public void generateEmployeeReportCSV(List<Employee> employees, String filePath) throws IOException {
        // Ensure parent directories exist
        File file = new File(filePath);
        File parentDirectory = file.getParentFile();
        if (parentDirectory != null && !parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            writeEmployeeCSV(employees, fileOutputStream);
        }
    }

    public void writeEmployeeCSV(List<Employee> employees, OutputStream outputStream) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream);
        writer.write("ID,Full Name,Job Title,Hire Date,Employment Status,Department\n");

        for (Employee employee : employees) {
            writer.write(employee.getId() + ","
                    + employee.getFullName() + ","
                    + employee.getJobTitle() + ","
                    + employee.getHireDate() + ","
                    + employee.getEmploymentStatus() + ","
                    + (employee.getDepartment() != null ? employee.getDepartment().getName() : "No Department") + "\n");
        }
        writer.flush();
    }

    @Override
    public void generateAuditLogReport(List<AuditLog> auditLogs, String fileName) throws IOException {
        String filePath = Paths.get(reportSaveLocation, fileName).toString();

        // Ensure parent directories exist
        Path parentDirectory = Paths.get(filePath).getParent();
        if (parentDirectory != null && !Files.exists(parentDirectory)) {
            Files.createDirectories(parentDirectory);
        }

        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
                writer.write("ID,Performed By,Timestamp,Details\n");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (AuditLog auditLog : auditLogs) {
                    String formattedDateTime = auditLog.getTimestamp() != null
                            ? auditLog.getTimestamp().format(dateTimeFormatter)
                            : "";

                    writer.write(String.format("%d,%s,%s,%s\n",
                            auditLog.getId(),
                            auditLog.getPerformedBy() != null ? auditLog.getPerformedBy() : "",
                            formattedDateTime,
                            auditLog.getDetails() != null ? auditLog.getDetails() : ""));
                }

                System.out.println("Audit log report generated successfully!");
                return;
            } catch (IOException e) {
                if (e.getMessage().contains("The process cannot access the file")) {
                    System.err.println("File is in use by another process. Retrying...");
                    retryCount++;
                    try {
                        // Wait before retrying
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    e.printStackTrace();
                    throw e;
                }
            }
        }

        throw new ResourceNotFoundException("File is in use by another process");
    }
}
