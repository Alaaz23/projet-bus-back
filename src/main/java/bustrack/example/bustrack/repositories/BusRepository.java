package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Long> {

}

