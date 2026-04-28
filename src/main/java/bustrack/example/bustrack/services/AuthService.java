package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Admin;
import bustrack.example.bustrack.models.AuthResponse;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.repositories.AdminRepository;
import bustrack.example.bustrack.repositories.SalarieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SalarieRepository salarieRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    public AuthResponse login(String username, String password) {
        // Try to authenticate as Admin (role: ADMIN)
        Optional<Admin> adminOpt = adminRepository.findBymatricule(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(passwordHasher.hashPassword(password))) {
                String token = generateToken(username, "ADMIN");
                return new AuthResponse(
                        true,
                        "Login successful",
                        username,
                        admin.getNom() + " " + admin.getPrenom(),
                        "ADMIN",
                        token
                );
            }
        }

        // Try to authenticate as Salarie (role: USER)
        Optional<Salarie> salarieOpt = salarieRepository.findBymatricule(username);
        if (salarieOpt.isPresent()) {
            Salarie salarie = salarieOpt.get();
            if (salarie.getPassword().equals(passwordHasher.hashPassword(password))) {
                String token = generateToken(username, "USER");
                return new AuthResponse(
                        true,
                        "Login successful",
                        username,
                        salarie.getNom() + " " + salarie.getPrenom(),
                        "USER",
                        token
                );
            }
        }

        return new AuthResponse(false, "Invalid credentials");
    }

    public String generateToken(String username, String role) {
        String payload = role + ":" + username;
        return "Bearer role-" + Base64.getEncoder().encodeToString(payload.getBytes());
    }

    public Optional<UserPrincipal> validateToken(String token) {
        try {
            if (token == null || !token.startsWith("Bearer role-")) {
                return Optional.empty();
            }

            String encoded = token.replace("Bearer role-", "");
            String payload = new String(Base64.getDecoder().decode(encoded));
            String[] parts = payload.split(":");

            if (parts.length != 2) {
                return Optional.empty();
            }

            String role = parts[0];
            String username = parts[1];

            if (!("ADMIN".equals(role) || "USER".equals(role))) {
                return Optional.empty();
            }

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

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }

        public boolean isUser() {
            return "USER".equals(role);
        }
    }
}
