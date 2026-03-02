package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Bus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bustrack.example.bustrack.services.BusService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/buses")
public class BusController {

    @Autowired
    private BusService busService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addBus(@RequestBody Bus bus) {
        // Add logic to save the bus and obtain its ID
        Long busId = busService.addBus(bus);

        // Prepare the response body
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("busId", busId); // Add bus ID to the response
        responseBody.put("message", "Bus added successfully");

        // Return the response with the bus ID and success message
        return ResponseEntity.ok(responseBody);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateBus(@PathVariable Long id, @RequestBody Bus updatedBus) {
        try {
            busService.updateBus(id, updatedBus);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Bus updated successfully");
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/getAll")
    public ResponseEntity<List<Bus>> getAllBuses() {
        List<Bus> buses = busService.getAllBuses();
        return ResponseEntity.ok(buses);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteBus(@PathVariable Long id) {
        try {
            // Call your service method to delete the bus by id
            busService.deleteBus(id);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Bus deleted successfully");
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            // Handle the case where the bus with the given id does not exist
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (Exception e) {
            // Handle other exceptions that may occur during deletion
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getBusCount() {
        long count = busService.getBusCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Double>> getBusLocation(@PathVariable Long id) {
        Optional<Bus> busOptional = busService.getBusById(id);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            if (bus.getPoints() != null) {
                Double latitude = bus.getPoints().getLatitude();
                Double longitude = bus.getPoints().getLongitude();
                Map<String, Double> location = new HashMap<>();
                location.put("latitude", latitude);
                location.put("longitude", longitude);
                return ResponseEntity.ok(location);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("des/{id}")
    public ResponseEntity<Map<String, Object>> getBusLoc(@PathVariable Long id) {
        Optional<Bus> busOptional = busService.getBusById(id);
        if (busOptional.isPresent()) {
            Bus bus = busOptional.get();
            if (bus.getPoints() != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", bus.getId());
                response.put("designation", bus.getDesignation());
                response.put("points", bus.getPoints());
                response.put("capacite", bus.getCapacite());
                response.put("traget", bus.getTraget());
                // Add more attributes as needed

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}

