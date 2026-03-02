package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Salarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalarieRepository extends JpaRepository<Salarie, Long> {
    Optional<Salarie> findBymatricule(String matricule);
}
