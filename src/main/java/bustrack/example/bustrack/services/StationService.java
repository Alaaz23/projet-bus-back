package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.models.Station;
import bustrack.example.bustrack.repositories.SalarieRepository;
import bustrack.example.bustrack.repositories.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private SalarieRepository salarieRepository;

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    public Long addStation(Station station) {
        Station savedStation=stationRepository.save(station);
        return savedStation.getId();
    }

    public Long updateStation(Long id, Station updatedStation) {
        Station existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Station not found"));
        // Update existing station properties with the ones from updatedStation
        existingStation.setLibelle(updatedStation.getLibelle());
        existingStation.setTraget(updatedStation.getTraget());
        existingStation.setLongitude(updatedStation.getLongitude());
        existingStation.setLatitude(updatedStation.getLatitude());
        Station newStation = stationRepository.save(existingStation);
        return newStation.getId();
    }

    @Transactional
    public Long deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station with id " + id + " not found."));

        // 1. Délier tous les salariés qui référencent cette station
        List<Salarie> salaries = salarieRepository.findByStationId(id);
        for (Salarie s : salaries) {
            s.setStation(null);
        }
        salarieRepository.saveAll(salaries);

        // 2. Délier la station de son trajet
        station.setTraget(null);
        stationRepository.save(station);

        // 3. Supprimer la station
        stationRepository.deleteById(id);
        return id;
    }
    public List<Station> getByTragetId(Long tragetId) {
        return stationRepository.findByTragetId(tragetId);
    }
}
