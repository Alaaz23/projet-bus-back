package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.Bus;
import bustrack.example.bustrack.models.Station;
import bustrack.example.bustrack.services.TragetService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import bustrack.example.bustrack.services.BusService;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/buses")
public class BusController {

    @Autowired
    private BusService busService;

    @Autowired
    private TragetService tragetService;

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
    /** ── STATUT BUS ── **/
    @PutMapping("/{id}/statut")
    public ResponseEntity<Map<String, String>> updateStatut(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String statut = body.get("statut");
        if (statut == null || !java.util.List.of("EN_ROUTE", "A_LARRET", "HORS_SERVICE").contains(statut)) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Statut invalide"));
        }
        return busService.getBusById(id).map(bus -> {
            bus.setStatut(statut);
            busService.saveBus(bus);
            return ResponseEntity.ok(java.util.Map.of("statut", statut));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** ── ETA INTELLIGENT (Haversine) ── **/
    @GetMapping("/{id}/eta")
    public ResponseEntity<Map<String, Object>> getEta(
            @PathVariable Long id,
            @RequestParam double stationLat,
            @RequestParam double stationLon) {
        return busService.getBusById(id).map(bus -> {
            Map<String, Object> result = new java.util.HashMap<>();
            if (bus.getPoints() == null) {
                result.put("eta", null);
                result.put("message", "Position GPS non disponible");
                return ResponseEntity.ok(result);
            }
            double busLat = bus.getPoints().getLatitude();
            double busLon = bus.getPoints().getLongitude();
            // Formule Haversine
            final double R = 6371.0;
            double dLat = Math.toRadians(stationLat - busLat);
            double dLon = Math.toRadians(stationLon - busLon);
            double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                    + Math.cos(Math.toRadians(busLat))*Math.cos(Math.toRadians(stationLat))
                    * Math.sin(dLon/2)*Math.sin(dLon/2);
            double distanceKm = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            int etaMinutes = (int) Math.ceil((distanceKm / 30.0) * 60); // 30 km/h vitesse moyenne
            result.put("distanceKm", Math.round(distanceKm * 10.0) / 10.0);
            result.put("etaMinutes", etaMinutes);
            result.put("statut", bus.getStatut());
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.notFound().build());
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

    /**
     * Retourne la configuration complète de la route pour un bus donné.
     * Source de vérité partagée entre l'application web (Angular) et mobile (Flutter).
     *
     * Réponse JSON :
     * {
     *   "busId": 7,
     *   "depName": "Ariana",
     *   "destName": "Sofrecom",
     *   "waypoints": [
     *     { "name": "Ariana", "lat": 36.862, "lng": 10.1935 },
     *     ...
     *   ]
     * }
     *
     * Retourne 404 si le bus ou son trajet est introuvable.
     * Retourne 204 si le trajet a moins de 2 stations.
     *
     * Note : la BDD stocke les colonnes latitude/longitude inversées.
     * Ce endpoint corrige l'inversion (longitude_col → lat, latitude_col → lng)
     * et valide que les coordonnées sont en Tunisie (bbox 30-38N, 7.5-11.5E).
     */
    @GetMapping("/{id}/route")
    public ResponseEntity<Map<String, Object>> getBusRoute(@PathVariable Long id) {
        try {
            List<Station> stations = tragetService.getAllStationsByBusId(id);
            if (stations.size() < 2) {
                return ResponseEntity.noContent().build();
            }

            // Correction inversion lat/lng dans la BDD :
            // colonne 'longitude' contient la latitude réelle, et vice-versa
            List<Map<String, Object>> waypoints = stations.stream().map(s -> {
                double actualLat = s.getLongitude() != null ? s.getLongitude() : 0.0;
                double actualLng = s.getLatitude()  != null ? s.getLatitude()  : 0.0;
                // Validation bbox Tunisie
                boolean valid = actualLat >= 30 && actualLat <= 38
                        && actualLng >= 7.5 && actualLng <= 11.5;
                if (!valid) {
                    // Coordonnées hors Tunisie — retourner null pour signaler l'erreur
                    return (Map<String, Object>) null;
                }
                Map<String, Object> wp = new LinkedHashMap<>();
                wp.put("name", s.getLibelle() != null ? s.getLibelle() : "");
                wp.put("lat", actualLat);
                wp.put("lng", actualLng);
                return wp;
            }).collect(Collectors.toList());

            // Si des stations ont des coordonnées invalides, refuser la réponse
            boolean anyInvalid = waypoints.stream().anyMatch(w -> w == null);
            if (anyInvalid) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("busId", id);
            result.put("depName",  waypoints.get(0).get("name"));
            result.put("destName", waypoints.get(waypoints.size() - 1).get("name"));
            result.put("waypoints", waypoints);

            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


}

