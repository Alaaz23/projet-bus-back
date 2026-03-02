package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Feedback;
import bustrack.example.bustrack.models.Salarie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // You can add custom query methods if needed
    List<Feedback> findByChecked(boolean checked);
    void deleteBySalarieId(Long salarieId);

}
