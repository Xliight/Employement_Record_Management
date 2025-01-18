package employeemanagement.security.services.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.EmployeeRepository;
import employeemanagement.security.Repository.RoleRepository;
import employeemanagement.security.Repository.TokenRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.config.JwtService;
import employeemanagement.security.enums.TokenType;
import employeemanagement.security.model.Role;
import employeemanagement.security.model.Token;
import employeemanagement.security.model.User;
import employeemanagement.security.payload.AuthenticationRequest;
import employeemanagement.security.payload.AuthenticationResponse;
import employeemanagement.security.payload.RegisterRequest;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogger auditLogger;
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        
        registerRequest = new RegisterRequest("John", "john@example.com", "password123", new Role("USER"));
        authRequest = new AuthenticationRequest("john@example.com", "password123");
    }

    @Test
    public void testRegisterSuccess() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role("ADMINISTRATOR")));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any())).thenReturn("mockJwtToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("mockRefreshToken");
        registerRequest.setRole(new Role("ADMINISTRATOR"));
        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogger, times(1)).logAudit(anyString(), anyString(), anyString());
    }

    @Test
    public void testRegisterEmailAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThrows(ResourceNotFoundException.class, () -> authenticationService.register(registerRequest));
    }

    @Test
    public void testAuthenticateSuccess() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password123");
        Role role = new Role("USER");
        role.setId(1L);
        user.setRole(role);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("mockJwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mockRefreshToken");

        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
    }

    @Test
    public void testAuthenticateInvalidUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    public void testRefreshTokenSuccess() throws Exception {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password123");
        Role role = new Role("USER");
        role.setId(1L);
        user.setRole(role);
        String validRefreshToken = "mockValidRefreshToken";
        String username = "john@example.com";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public boolean isReady() {
                return true;
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validRefreshToken);
        when(jwtService.extractUsername(validRefreshToken)).thenReturn(username);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(validRefreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("newMockJwtToken");
        when(jwtService.generateRefreshToken(user)).thenReturn(validRefreshToken);

        authenticationService.refreshToken(request, response);

        ObjectMapper objectMapper = new ObjectMapper();
        AuthenticationResponse expectedResponse = new AuthenticationResponse("newMockJwtToken", validRefreshToken);
        String actualResponse = byteArrayOutputStream.toString("UTF-8");

        assertEquals(objectMapper.writeValueAsString(expectedResponse), actualResponse);

    }

    @Test
    public void testRevokeAllUserTokens() {
        User mockUser = new User();
        mockUser.setEmail("john@example.com");
        mockUser.setPassword("password123");
        Role role = new Role("USER");
        role.setId(1L);
        mockUser.setRole(role);
        Token mockToken =new Token();
        mockToken.setToken("mockJwtToken");
        mockToken.setUser(mockUser);
        mockToken.setTokenType(TokenType.BEARER);
        mockToken.setRevoked(false);
        mockToken.setExpired(false);
        when(tokenRepository.findAllValidTokenByUser(mockUser.getId())).thenReturn(List.of(mockToken));
        authenticationService.revokeAllUserTokens(mockUser);
        
        verify(tokenRepository, times(1)).saveAll(anyList());
        assertTrue(mockToken.isExpired());
        assertTrue(mockToken.isRevoked());
    }

    @Test
    public void testSaveUserToken() {
        User mockUser = new User();
        mockUser.setEmail("john@example.com");
        mockUser.setPassword("password123");
        Role role = new Role("USER");
        role.setId(1L);
        mockUser.setRole(role);        String jwtToken = "newJwtToken";
        authenticationService.saveUserToken(mockUser, jwtToken);

        
        verify(tokenRepository, times(1)).save(any(Token.class));
    }
}
