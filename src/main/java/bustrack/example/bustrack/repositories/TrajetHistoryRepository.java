package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.TrajetHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrajetHistoryRepository extends JpaRepository<TrajetHistory, Long> {
    List<TrajetHistory> findBySalarieIdOrderByDateDepartDesc(Long salarieId, Pageable pageable);
    List<TrajetHistory> findByBusIdOrderByDateDepartDesc(Long busId, Pageable pageable);
    List<TrajetHistory> findAllByOrderByDateDepartDesc(Pageable pageable);
    /** Trajet actif (non terminé) pour un bus donné. */
    Optional<TrajetHistory> findFirstByBus_IdAndDateArriveeIsNull(Long busId);
}
