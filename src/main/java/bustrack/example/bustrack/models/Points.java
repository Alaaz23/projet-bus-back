package bustrack.example.bustrack.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@Table(name = "points")
public class Points {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_p")
    private Long id;

    @Column(name = "longitude")
    private Double longitude; // Use wrapper class Double instead of primitive double

    @Column(name = "latitude")
    private Double latitude; // Use wrapper class Double instead of primitive double

}
