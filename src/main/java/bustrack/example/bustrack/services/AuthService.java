package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Admin;
import bustrack.example.bustrack.models.AuthResponse;
import bustrack.example.bustrack.models.RefreshToken;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.repositories.AdminRepository;
import bustrack.example.bustrack.repositories.SalarieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SalarieRepository salarieRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthResponse login(String username, String password) {
        // Try to authenticate as Admin (role: ADMIN)
        Optional<Admin> adminOpt = adminRepository.findBymatricule(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(passwordHasher.hashPassword(password))) {
                String accessToken = jwtTokenProvider.generateAccessToken(username, "ADMIN");
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(username, "ADMIN");
                AuthResponse resp = new AuthResponse(
                        true,
                        "Login successful",
                        username,
                        admin.getNom() + " " + admin.getPrenom(),
                        "ADMIN",
                        "Bearer " + accessToken,
                        refreshToken.getToken()
                );
                return resp;
            }
        }

        // Try to authenticate as Salarie (role: USER)
        Optional<Salarie> salarieOpt = salarieRepository.findBymatricule(username);
        if (salarieOpt.isPresent()) {
            Salarie salarie = salarieOpt.get();
            if (salarie.getPassword().equals(passwordHasher.hashPassword(password))) {
                String accessToken = jwtTokenProvider.generateAccessToken(username, "USER");
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(username, "USER");
                Long busId = (salarie.getBus() != null) ? salarie.getBus().getId() : null;
                AuthResponse resp = new AuthResponse(
                        true,
                        "Login successful",
                        username,
                        salarie.getNom() + " " + salarie.getPrenom(),
                        "USER",
                        "Bearer " + accessToken,
                        refreshToken.getToken()
                );
                resp.setBusId(busId);
                return resp;
            }
        }

        return new AuthResponse(false, "Invalid credentials");
    }

    public Optional<UserPrincipal> validateToken(String token) {
        try {
            if (token == null) return Optional.empty();

            String raw = token.startsWith("Bearer ") ? token.substring(7) : token;

            if (!jwtTokenProvider.validateToken(raw)) return Optional.empty();

            String username = jwtTokenProvider.getUsernameFromToken(raw);
            String role = jwtTokenProvider.getRoleFromToken(raw);

            if (!("ADMIN".equals(role) || "USER".equals(role))) return Optional.empty();

            UserPrincipal principal = new UserPrincipal();
            principal.setUsername(username);
            principal.setRole(role);
            return Optional.of(principal);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class UserPrincipal {
        private String username;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isAdmin() { return "ADMIN".equals(role); }
        public boolean isUser() { return "USER".equals(role); }
    }
}

