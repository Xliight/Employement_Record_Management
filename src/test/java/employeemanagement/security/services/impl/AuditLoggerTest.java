package employeemanagement.security.services.impl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import employeemanagement.security.Repository.AuditLogRepository;
import employeemanagement.security.model.AuditLog;

public class AuditLoggerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditLogger auditLogger;

    private String action = "CREATE_USER";
    private String details = "User created successfully";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@company.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);    }

    @Test
    public void testResolvePerformedByWhenAuthenticated() {
        String expectedUser = "user@company.com";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(expectedUser);

        String performedBy = auditLogger.resolvePerformedBy();

        assertEquals(expectedUser, performedBy);
    }

    @Test
    public void testResolvePerformedByWhenNotAuthenticated() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String performedBy = auditLogger.resolvePerformedBy();

        assertEquals("SYSTEM", performedBy);
    }

    @Test
    public void testLogAudit() {
        String performedBy = "user@company.com";
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        auditLogger.logAudit(action, performedBy, details);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
