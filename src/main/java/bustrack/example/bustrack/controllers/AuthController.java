package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.AuthResponse;
import bustrack.example.bustrack.models.UserLoginRequest;
import bustrack.example.bustrack.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequest loginRequest) {
        String username = loginRequest.getMatricule();
        String password = loginRequest.getPassword();

        AuthResponse response = authService.login(username, password);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "No token provided"));
        }

        authService.validateToken(token).ifPresentOrElse(
                principal -> {
                    // Token is valid
                },
                () -> {
                    throw new RuntimeException("Invalid token");
                }
        );

        return ResponseEntity.ok(new AuthResponse(true, "Token is valid"));
    }
}
