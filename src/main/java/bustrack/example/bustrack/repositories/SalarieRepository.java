package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Salarie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalarieRepository extends JpaRepository<Salarie, Long> {
    Optional<Salarie> findBymatricule(String matricule);
    List<Salarie> findByStationId(Long stationId);
    /** Premier salarié assigné à un bus donné. */
    Optional<Salarie> findFirstByBus_Id(Long busId);
}
