package bustrack.example.bustrack.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@Table(name = "salarie")
public class Salarie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_s")
    private Long id;

    @Column(name = "matricule")
    private String matricule;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    private String password;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_b")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "points"})
    @EqualsAndHashCode.Exclude
    private Bus bus;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_st")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "traget"})
    @EqualsAndHashCode.Exclude
    private Station station;



}