package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    List<Station> findByTragetId(Long tragetId);
    Optional<Station> findByLibelle(String libelle);
}
