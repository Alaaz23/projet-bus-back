package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Feedback;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.repositories.FeedbackRepository;
import bustrack.example.bustrack.repositories.SalarieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SalarieRepository salarieRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, SalarieRepository salarieRepository) {
        this.feedbackRepository = feedbackRepository;
        this.salarieRepository = salarieRepository;
    }

    public Feedback addFeedback(Feedback feedback) {
        // Fetch the Salarie entity from the database to ensure it's managed
        Optional<Salarie> salarieOptional = salarieRepository.findById(feedback.getSalarie().getId());
        if (salarieOptional.isPresent()) {
            feedback.setSalarie(salarieOptional.get());
            return feedbackRepository.save(feedback);
        } else {
            throw new IllegalArgumentException("Salarie not found");
        }
    }

    public List<Feedback> getFeedbackByChecked(boolean checked) {
        return feedbackRepository.findByChecked(checked);
    }

    public List<Feedback> getfeeds() {
        return feedbackRepository.findAll();
    }

    public Feedback updateFeedbackCheckedStatus(Long id, boolean checked) {
        Optional<Feedback> feedbackOptional = feedbackRepository.findById(id);
        if (feedbackOptional.isPresent()) {
            Feedback feedback = feedbackOptional.get();
            feedback.setChecked(checked);
            return feedbackRepository.save(feedback);
        } else {
            return null;
        }
    }
}
