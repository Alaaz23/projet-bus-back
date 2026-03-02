package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Bus;
import bustrack.example.bustrack.models.Points;
import bustrack.example.bustrack.services.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePoint(@PathVariable Long id, @RequestBody Points updatedPoint) {
        pointsService.updatePoint(id, updatedPoint);
        return ResponseEntity.ok("Point updated successfully");
    }
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addPoint(@RequestBody Points points) {
        Long pointId = pointsService.addPoint(points);

        // Prepare the response body
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("pointId", pointId); // Add point ID to the response
        responseBody.put("message", "Point added successfully");

        // Return the response with the point ID and success message
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Points>> getAllPoints() {
        List<Points> P = pointsService.getAllPoints();
        return ResponseEntity.ok(P);
    }
}
