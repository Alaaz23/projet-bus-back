package bustrack.example.bustrack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "trajet_history")
public class TrajetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bus_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "traget", "points"})
    private Bus bus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salarie_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bus", "station", "password"})
    private Salarie salarie;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    private LocalDateTime dateArrivee;

    private Double distance; // en km

    private Integer duree; // en minutes

    @Column(length = 100)
    private String depart;

    @Column(length = 100)
    private String destination;
}
