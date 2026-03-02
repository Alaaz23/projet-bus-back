package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Feedback;
import bustrack.example.bustrack.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/add")
    public ResponseEntity<Feedback> addFeedback(@RequestBody Feedback feedback) {
        try {
            Feedback savedFeedback = feedbackService.addFeedback(feedback);
            return new ResponseEntity<>(savedFeedback, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/by-checked/{checked}")
    public List<Feedback> getFeedbackByChecked(@PathVariable boolean checked) {
        return feedbackService.getFeedbackByChecked(checked);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Feedback>> getfeeds() {
        List<Feedback> P = feedbackService.getfeeds();
        return ResponseEntity.ok(P);
    }

    @PutMapping("/{id}/check")
    public ResponseEntity<Feedback> updateFeedbackCheckedStatus(@PathVariable Long id, @RequestBody Feedback feedback) {
        Feedback updatedFeedback = feedbackService.updateFeedbackCheckedStatus(id, feedback.isChecked());
        if (updatedFeedback != null) {
            return new ResponseEntity<>(updatedFeedback, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
