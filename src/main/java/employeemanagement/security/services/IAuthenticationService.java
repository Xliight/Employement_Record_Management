package employeemanagement.security.services;

import employeemanagement.security.model.User;
import employeemanagement.security.payload.AuthenticationRequest;
import employeemanagement.security.payload.AuthenticationResponse;
import employeemanagement.security.payload.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import java.io.IOException;

public interface IAuthenticationService {
    @Transactional
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void saveUserToken(User user, String jwtToken);

    void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException;
}
