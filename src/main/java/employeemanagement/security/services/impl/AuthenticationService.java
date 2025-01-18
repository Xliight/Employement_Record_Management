package employeemanagement.security.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import employeemanagement.security.Exceptions.ResourceNotFoundException;
import employeemanagement.security.Repository.EmployeeRepository;
import employeemanagement.security.Repository.RoleRepository;
import employeemanagement.security.Repository.TokenRepository;
import employeemanagement.security.Repository.UserRepository;
import employeemanagement.security.config.JwtService;
import employeemanagement.security.enums.TokenType;
import employeemanagement.security.model.Employee;
import employeemanagement.security.model.Role;
import employeemanagement.security.model.Token;
import employeemanagement.security.model.User;
import employeemanagement.security.payload.AuthenticationRequest;
import employeemanagement.security.payload.AuthenticationResponse;
import employeemanagement.security.payload.RegisterRequest;
import employeemanagement.security.services.IAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
  private final UserRepository repository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final EmployeeRepository employeeRepository;
  private final RoleRepository roleRepository;
  private final AuditLogger auditLogger;
  @Transactional
  @Override
  public AuthenticationResponse register(RegisterRequest request) {
  List<String> roles = new ArrayList<>();
    roles.add("ADMINISTRATOR");
    roles.add("HR_PERSONNEL");
    roles.add("MANAGER");

    Optional<User> userbyemail = userRepository.findByEmail(request.getEmail());
    if (userbyemail.isPresent() ) {
      throw new ResourceNotFoundException("Email Already Exist");
    }
    if (!roles.contains(request.getRole().getName())) {throw new ResourceNotFoundException("Role  Dont Exist");}

    Optional<Role> existingRole = roleRepository.findByName(request.getRole().getName());
    Role role;
    if (existingRole.isPresent()) {
      role = existingRole.get();
    } else {
      role = request.getRole();
      roleRepository.save(role);
    }

    Employee employee = Employee.builder()
            .fullName(request.getFirstname())
            .contactInfo(request.getEmail())
            .jobTitle("New Hire")
            .employmentStatus("Active")
            .hireDate(LocalDate.now())
            .address("to change later")
            .department(null)
            .build();

    User user = User.builder()
            .firstname(request.getFirstname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

    user.setEmployee(employee);
    employee.setUser(user);
    var savedUser = userRepository.save(user);

    employeeRepository.save(employee);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    auditLogger.logAudit("CREATE", request.getEmail(), "NEW USER CREATED: "+request.getEmail());
    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .build();
  }

  @Override
  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
        )
    );
    var user = repository.findByEmail(request.getEmail())
        .orElseThrow();
    String role= user.getRole().getName();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    auditLogger.logAudit("UPDATE", request.getEmail(), " USER Login: "+request.getEmail() );

    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
        .build();
  }
  public void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  public void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }
  @Override
  public void refreshToken(
          HttpServletRequest request,
          HttpServletResponse response
  ) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);
    if (userEmail != null) {
      var user = this.repository.findByEmail(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
