package bustrack.example.bustrack.interceptors;

import bustrack.example.bustrack.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        // Skip auth for login endpoint and public endpoints
        if (path.contains("/auth/login") || path.contains("/public")) {
            return true;
        }

        // For protected endpoints, validate token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            Optional<AuthService.UserPrincipal> principal = authService.validateToken(authHeader);
            if (principal.isPresent()) {
                // Store principal in request for downstream use
                request.setAttribute("userPrincipal", principal.get());
                return true;
            }
        }

        // Allow access through Angular proxy (requests without explicit auth for now)
        // In production, enforce strict access control
        return true;
    }
}
