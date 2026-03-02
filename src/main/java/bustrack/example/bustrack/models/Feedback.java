package bustrack.example.bustrack.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Entity
@Data
@Getter
@Setter
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "checked", columnDefinition = "boolean default false")
    private boolean checked;

    @Column(name = "time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP") // Renommer la colonne en "time"
    private Date time;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "salarie_id")
    private Salarie salarie;

    @PrePersist
    protected void onCreate() {
        time = new Date(); // Initialisation de la date et l'heure d'insertion
    }


}
