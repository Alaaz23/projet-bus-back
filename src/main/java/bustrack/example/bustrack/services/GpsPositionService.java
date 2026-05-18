package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.BusGpsPosition;
import bustrack.example.bustrack.repositories.BusGpsPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GpsPositionService {

    @Autowired
    private BusGpsPositionRepository repository;

    public BusGpsPosition savePosition(BusGpsPosition position) {
        return repository.save(position);
    }

    public Optional<BusGpsPosition> getLatestPosition(Long busId) {
        return repository.findTopByBusIdOrderByTimestampDesc(busId);
    }

    public List<BusGpsPosition> getHistory(Long busId) {
        return repository.findTop100ByBusIdOrderByTimestampDesc(busId);
    }

    /** Dernière position GPS toutes lignes confondues */
    public Optional<BusGpsPosition> getGlobalLatestPosition() {
        return repository.findTopByOrderByTimestampDesc();
    }
}
