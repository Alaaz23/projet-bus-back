package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.AuthResponse;
import bustrack.example.bustrack.models.RefreshToken;
import bustrack.example.bustrack.models.UserLoginRequest;
import bustrack.example.bustrack.services.AuthService;
import bustrack.example.bustrack.services.JwtTokenProvider;
import bustrack.example.bustrack.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

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
    public ResponseEntity<AuthResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "No token provided"));
        }

        Optional<AuthService.UserPrincipal> principal = authService.validateToken(token);
        if (principal.isEmpty()) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Invalid token"));
        }
        return ResponseEntity.ok(new AuthResponse(true, "Token is valid"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            return ResponseEntity.status(400).body(new AuthResponse(false, "Missing refreshToken"));
        }

        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(refreshTokenStr);
        if (tokenOpt.isEmpty() || !refreshTokenService.isValid(tokenOpt.get())) {
            return ResponseEntity.status(401).body(new AuthResponse(false, "Invalid or expired refresh token"));
        }

        RefreshToken old = tokenOpt.get();
        // Rotation : révoquer l'ancien, créer un nouveau
        refreshTokenService.revokeToken(old);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(old.getUsername(), old.getRole());
        String newAccessToken = jwtTokenProvider.generateAccessToken(old.getUsername(), old.getRole());

        AuthResponse resp = new AuthResponse(
                true, "Token refreshed",
                old.getUsername(), null, old.getRole(),
                "Bearer " + newAccessToken,
                newRefreshToken.getToken()
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        if (refreshTokenStr != null) {
            refreshTokenService.findByToken(refreshTokenStr)
                    .ifPresent(refreshTokenService::revokeToken);
        }
        return ResponseEntity.ok(new AuthResponse(true, "Logged out successfully"));
    }
}

