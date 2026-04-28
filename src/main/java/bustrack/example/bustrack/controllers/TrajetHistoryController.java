package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.TrajetHistory;
import bustrack.example.bustrack.repositories.TrajetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trajets")
public class TrajetHistoryController {

    @Autowired
    private TrajetHistoryRepository trajetHistoryRepository;

    @GetMapping("/history")
    public ResponseEntity<List<TrajetHistory>> getHistory(
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) Long busId,
            @RequestParam(required = false) Long salarieId) {

        PageRequest page = PageRequest.of(0, limit);
        List<TrajetHistory> result;

        if (salarieId != null) {
            result = trajetHistoryRepository.findBySalarieIdOrderByDateDepartDesc(salarieId, page);
        } else if (busId != null) {
            result = trajetHistoryRepository.findByBusIdOrderByDateDepartDesc(busId, page);
        } else {
            result = trajetHistoryRepository.findAllByOrderByDateDepartDesc(page);
        }

        return ResponseEntity.ok(result);
    }
}
