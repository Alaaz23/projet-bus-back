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
@Table(name = "station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_st")
    private Long id;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @ManyToOne
    @JoinColumn(name = "id_t")
    @JsonIgnoreProperties({"stations", "hibernateLazyInitializer", "handler"})
    private Traget traget;

}
