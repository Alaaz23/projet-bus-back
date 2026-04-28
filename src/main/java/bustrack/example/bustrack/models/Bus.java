package bustrack.example.bustrack.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Entity
@Data
@Getter
@Setter

@Table(name = "bus")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_b")
    private Long id;

    @Column(name = "designation")
    private String designation;

    @Column(name = "capacite")
    private int capacite;

    @ManyToOne
    @JoinColumn(name = "id_p")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Points points;

    @ManyToOne
    @JoinColumn(name = "id_t")
    @JsonIgnoreProperties({"stations", "hibernateLazyInitializer", "handler"})
    private Traget traget;

}

