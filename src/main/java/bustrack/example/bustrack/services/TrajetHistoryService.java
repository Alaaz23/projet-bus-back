package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.BusGpsPosition;
import bustrack.example.bustrack.models.Bus;
import bustrack.example.bustrack.models.TrajetHistory;
import bustrack.example.bustrack.repositories.BusRepository;
import bustrack.example.bustrack.repositories.SalarieRepository;
import bustrack.example.bustrack.repositories.TrajetHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service qui crée et met à jour automatiquement les enregistrements TrajetHistory
 * à partir des positions GPS reçues en temps réel.
 *
 * Logique :
 *  - Première position reçue pour un bus (ou après fin du précédent trajet) → nouveau TrajetHistory
 *  - Chaque position → cumul distance + mise à jour durée
 *  - Bus à moins de 300 m de sa destination → trajet complété automatiquement
 */
@Service
public class TrajetHistoryService {

    @Autowired private TrajetHistoryRepository trajetRepo;
    @Autowired private BusRepository           busRepo;
    @Autowired private SalarieRepository       salarieRepo;

    // ── Noms des stations par bus ID ─────────────────────────────────────────
    private static final Map<Long, String> DEP_NAMES = Map.of(
        6L, "Borj Cedriya",
        7L, "Ariana",
        8L, "Bizerte"
    );
    private static final Map<Long, String> DEST_NAMES = Map.of(
        6L, "Sofrecom",
        7L, "Sofrecom",
        8L, "Sofrecom"
    );

    /** Coordonnées GPS de la destination finale par bus (lat, lng). */
    private static final Map<Long, double[]> DEST_COORDS = Map.of(
        6L, new double[]{36.831374, 10.232228},
        7L, new double[]{36.831585, 10.232803},
        8L, new double[]{36.831585, 10.232803}
    );

    // ── État en mémoire (per-bus, thread-safe) ────────────────────────────────
    private final Map<Long, BusGpsPosition> lastPos      = new ConcurrentHashMap<>();
    private final Map<Long, Double>         cumDistKm    = new ConcurrentHashMap<>();
    private final Map<Long, Long>           activeTripId = new ConcurrentHashMap<>();

    // ── Entrée principale appelée par GpsController ───────────────────────────

    @Transactional
    public void onPositionReceived(BusGpsPosition pos) {
        if (pos == null || pos.getBusId() == null) return;
        Long busId = pos.getBusId();

        // 1. Trouver ou créer le trajet actif ──────────────────────────────────
        TrajetHistory trip = findOrCreateTrip(busId, pos);
        if (trip == null) return;

        // 2. Mettre à jour la distance cumulée ─────────────────────────────────
        BusGpsPosition prev = lastPos.get(busId);
        if (prev != null) {
            double segKm = haversineKm(
                prev.getLatitude(), prev.getLongitude(),
                pos.getLatitude(),  pos.getLongitude()
            );
            double total = cumDistKm.getOrDefault(busId, 0.0) + segKm;
            cumDistKm.put(busId, total);
            trip.setDistance(Math.round(total * 100.0) / 100.0);
        }
        lastPos.put(busId, pos);

        // 3. Mettre à jour la durée ─────────────────────────────────────────────
        LocalDateTime now = pos.getTimestamp() != null ? pos.getTimestamp() : LocalDateTime.now();
        if (trip.getDateDepart() != null) {
            long minutes = Duration.between(trip.getDateDepart(), now).toMinutes();
            trip.setDuree((int) Math.max(0, minutes));
        }
        trajetRepo.save(trip);

        // 4. Auto-compléter si proche de la destination ─────────────────────────
        double[] dest = DEST_COORDS.get(busId);
        if (dest != null) {
            double d = haversineKm(pos.getLatitude(), pos.getLongitude(), dest[0], dest[1]);
            if (d < 0.3) {   // moins de 300 m — arrivée détectée
                trip.setDateArrivee(now);
                trajetRepo.save(trip);
                activeTripId.remove(busId);
                cumDistKm.remove(busId);
                lastPos.remove(busId);
            }
        }
    }

    // ── Helpers privés ────────────────────────────────────────────────────────

    private TrajetHistory findOrCreateTrip(Long busId, BusGpsPosition pos) {
        // Vérifier l'état en mémoire
        Long activeId = activeTripId.get(busId);
        if (activeId != null) {
            Optional<TrajetHistory> opt = trajetRepo.findById(activeId);
            if (opt.isPresent() && opt.get().getDateArrivee() == null) {
                return opt.get();
            }
        }

        // Vérifier la BD (en cas de redémarrage du serveur)
        Optional<TrajetHistory> dbTrip = trajetRepo.findFirstByBus_IdAndDateArriveeIsNull(busId);
        if (dbTrip.isPresent()) {
            activeTripId.put(busId, dbTrip.get().getId());
            return dbTrip.get();
        }

        // Créer un nouveau trajet
        Optional<Bus> busOpt = busRepo.findById(busId);
        if (busOpt.isEmpty()) return null;

        LocalDateTime departureTime = pos.getTimestamp() != null ? pos.getTimestamp() : LocalDateTime.now();

        TrajetHistory trip = new TrajetHistory();
        trip.setBus(busOpt.get());
        trip.setDateDepart(departureTime);
        trip.setDepart(DEP_NAMES.getOrDefault(busId, "Départ"));
        trip.setDestination(DEST_NAMES.getOrDefault(busId, "Arrivée"));
        trip.setDistance(0.0);
        trip.setDuree(0);
        // Associer le salarié lié à ce bus (s'il en existe un)
        salarieRepo.findFirstByBus_Id(busId).ifPresent(trip::setSalarie);

        trip = trajetRepo.save(trip);
        activeTripId.put(busId, trip.getId());
        cumDistKm.put(busId, 0.0);
        return trip;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
