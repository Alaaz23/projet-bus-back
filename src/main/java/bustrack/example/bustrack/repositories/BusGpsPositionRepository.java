package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.BusGpsPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusGpsPositionRepository extends JpaRepository<BusGpsPosition, Long> {

    Optional<BusGpsPosition> findTopByBusIdOrderByTimestampDesc(Long busId);

    List<BusGpsPosition> findTop100ByBusIdOrderByTimestampDesc(Long busId);

    /** Dernière position GPS toutes lignes confondues */
    Optional<BusGpsPosition> findTopByOrderByTimestampDesc();
}
