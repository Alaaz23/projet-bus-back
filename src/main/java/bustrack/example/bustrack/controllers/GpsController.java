package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.models.BusGpsPosition;
import bustrack.example.bustrack.services.GpsPositionService;
import bustrack.example.bustrack.services.TrajetHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur GPS — REST + WebSocket STOMP temps réel.
 *
 * Flux de données :
 *   Raspberry Pi → POST /gps/position
 *       → sauvegarde PostgreSQL
 *       → broadcast STOMP /topic/gps/{busId}
 *       → broadcast STOMP /topic/gps/all
 *   Angular/Flutter ← s'abonnent via WebSocket STOMP
 */
@RestController
@RequestMapping("/gps")
public class GpsController {

    @Autowired
    private GpsPositionService gpsPositionService;

    @Autowired
    private TrajetHistoryService trajetHistoryService;

    /** Template STOMP — injecté automatiquement par Spring WebSocket */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * POST /gps/position
     * Reçoit une position GPS depuis le Raspberry Pi / script de simulation.
     * Sauvegarde en BDD et diffuse immédiatement via WebSocket STOMP.
     *
     * JSON attendu :
     * {
     *   "busId": 6,
     *   "latitude": 36.7052,
     *   "longitude": 10.4078,
     *   "speed": 40.0,
     *   "bearing": 315.0,
     *   "deviceId": "RPI-001"
     * }
     */
    @PostMapping("/position")
    public ResponseEntity<BusGpsPosition> receivePosition(@RequestBody BusGpsPosition position) {
        // Horodatage serveur pour cohérence (pas de dérive d'horloge côté Raspberry Pi)
        position.setTimestamp(LocalDateTime.now());
        BusGpsPosition saved = gpsPositionService.savePosition(position);

        // ── Suivi automatique des trajets ────────────────────────────────────
        trajetHistoryService.onPositionReceived(saved);

        // ── Broadcast WebSocket STOMP ────────────────────────────────────────
        // 1. Topic filtré par bus (Angular/Flutter s'abonnent ici)
        messagingTemplate.convertAndSend("/topic/gps/" + saved.getBusId(), saved);
        // 2. Topic global (dashboard multi-bus)
        messagingTemplate.convertAndSend("/topic/gps/all", saved);

        return ResponseEntity.ok(saved);
    }

    /**
     * GET /gps/bus/{busId}/latest
     * Retourne la dernière position connue du bus.
     */
    @GetMapping("/bus/{busId}/latest")
    public ResponseEntity<BusGpsPosition> getLatestPosition(@PathVariable Long busId) {
        return gpsPositionService.getLatestPosition(busId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /gps/bus/{busId}/history
     * Retourne les 100 dernières positions du bus (ordre décroissant).
     */
    @GetMapping("/bus/{busId}/history")
    public ResponseEntity<List<BusGpsPosition>> getHistory(@PathVariable Long busId) {
        return ResponseEntity.ok(gpsPositionService.getHistory(busId));
    }

    /**
     * GET /gps/latest
     * Retourne la position GPS la plus récente tous bus confondus.
     * Utile pour auto-détecter quel bus est actif.
     */
    @GetMapping("/latest")
    public ResponseEntity<BusGpsPosition> getGlobalLatest() {
        return gpsPositionService.getGlobalLatestPosition()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
