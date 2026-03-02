package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Points;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {
}
