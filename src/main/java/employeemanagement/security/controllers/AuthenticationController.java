package employeemanagement.security.controllers;

import employeemanagement.security.payload.AuthenticationRequest;
import employeemanagement.security.payload.AuthenticationResponse;
import employeemanagement.security.payload.RegisterRequest;
import employeemanagement.security.services.IAuthenticationService;
import employeemanagement.security.services.impl.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final IAuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    try {
      AuthenticationResponse authenticationResponse = service.register(request);
      return ResponseEntity.ok(authenticationResponse);
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed: " + ex.getMessage());
    }
  }

    @PostMapping("/login")
  public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request) {
    try {
      AuthenticationResponse authenticationResponse = service.authenticate(request);
      return ResponseEntity.ok(authenticationResponse);
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed .");
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    try {
      service.refreshToken(request, response);
      return ResponseEntity.ok("Token refreshed successfully.");
    } catch (IOException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token refresh failed.");
    }
  }
}
