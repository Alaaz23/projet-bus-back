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

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
