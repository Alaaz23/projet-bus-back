package bustrack.example.bustrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Data
@Getter
@Setter
@Table(name = "traget")
public class Traget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_t")
    private Long id;

    @Column(name = "libelle")
    private String libelle;
    @OneToMany(mappedBy = "traget", cascade = CascadeType.ALL)
    @JsonIgnore // Exclude stations field from serialization to avoid circular references
    private List<Station> stations;


}

