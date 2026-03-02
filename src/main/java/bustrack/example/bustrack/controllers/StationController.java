package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Station;
import bustrack.example.bustrack.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping("/all")
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Long> addStation(@RequestBody Station station) {
        Long id=stationService.addStation(station);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Long> updateStation(@PathVariable Long id, @RequestBody Station updatedStation) {
        Long id_s=stationService.updateStation(id, updatedStation);
        return ResponseEntity.ok(id_s);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Long> deleteStation(@PathVariable Long id) {
        Long id_t=stationService.deleteStation(id);
        return ResponseEntity.ok(id_t);
    }
    @GetMapping("/by-traget/{id}")
    public List<Station> getByTragetId(@PathVariable Long id) {
        return stationService.getByTragetId(id);
    }
}
