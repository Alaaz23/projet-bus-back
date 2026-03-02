package bustrack.example.bustrack.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    @Column(name = "password")
    private String password;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_b")
    private Bus bus;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_st")
    private Station station;



}