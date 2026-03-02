package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Station;
import bustrack.example.bustrack.models.Traget;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bustrack.example.bustrack.services.TragetService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/tragets")
public class TragetController {

    @Autowired
    private TragetService tragetService;

    @PostMapping("/add")
    public ResponseEntity<Long> addTraget(@RequestBody Traget traget) {
        Long id = tragetService.addTraget(traget);
        return ResponseEntity.ok(id);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Long> updateTraget(@PathVariable Long id, @RequestBody Traget traget) {
        Long id_t = tragetService.updateTraget(id, traget);
        return ResponseEntity.ok(id_t);
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Traget>> getAllTragets() {
        List<Traget> tragets = tragetService.getAllTragets();
        return ResponseEntity.ok(tragets);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Long> deleteTraget(@PathVariable Long id) {
        tragetService.deleteTraget(id);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> TragetCount() {
        long count = tragetService.getTragetCount();
        return ResponseEntity.ok(count);
    }
    @GetMapping("/stations/{stationId}")
    public ResponseEntity<?> getAllStationsByStationId(@PathVariable Long stationId) {
        try {
            List<Station> stations = tragetService.getAllStationsByStationId(stationId);
            return ResponseEntity.ok(stations);
        } catch (EntityNotFoundException e) {
            // Handle the case where the station ID does not exist
            return ResponseEntity.notFound().build();
        }
    }
}

