package bustrack.example.bustrack.models;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salarie_id")
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
