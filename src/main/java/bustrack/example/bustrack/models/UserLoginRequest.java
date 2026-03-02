package bustrack.example.bustrack.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserLoginRequest {
    private String matricule;
    private String password;

    // Constructors, getters, and setters
}
