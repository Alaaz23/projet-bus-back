package bustrack.example.bustrack.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginResponse {
    private boolean success;
    private String nom;
    private String prenom;
    private Long id;
    private Long id_st;
    private Long id_b;

    public LoginResponse(boolean success, Long id, String nom, String prenom, Long id_st, Long id_b) {
        this.success = success;
        this.nom = nom;
        this.prenom = prenom;
        this.id = id;
        this.id_st = id_st;
        this.id_b = id_b;
    }
}