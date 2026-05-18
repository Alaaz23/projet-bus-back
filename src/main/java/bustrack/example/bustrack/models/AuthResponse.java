package bustrack.example.bustrack.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String username;
    private String displayName;
    private String role;
    private String token;
    private String refreshToken;
    private Long busId;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String username, String displayName,
                        String role, String token, String refreshToken) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
