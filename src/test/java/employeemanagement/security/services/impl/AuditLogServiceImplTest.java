import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import employeemanagement.security.Repository.AuditLogRepository;
import employeemanagement.security.model.AuditLog;
import employeemanagement.security.services.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;

public class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog auditLog1;
    private AuditLog auditLog2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        auditLog1 = new AuditLog();
        auditLog1.setAction("LOGIN");
        auditLog1.setPerformedBy("admin@example.com");

        auditLog2 = new AuditLog();
        auditLog2.setAction("LOGOUT");
        auditLog2.setPerformedBy("user@example.com");
    }

    @Test
    public void testGetAllAuditLogsSuccess() {
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(auditLog1, auditLog2));

        List<AuditLog> auditLogs = auditLogService.getAllAuditLogs();

        verify(auditLogRepository, times(1)).findAll();

        assertNotNull(auditLogs);
        assertEquals(2, auditLogs.size());
        assertTrue(auditLogs.contains(auditLog1));
        assertTrue(auditLogs.contains(auditLog2));
    }

    @Test
    public void testGetAllAuditLogsEmptyList() {
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList());

        List<AuditLog> auditLogs = auditLogService.getAllAuditLogs();
        verify(auditLogRepository, times(1)).findAll();
        assertNotNull(auditLogs);
        assertEquals(0, auditLogs.size());
    }
}