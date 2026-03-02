package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Station;
import bustrack.example.bustrack.models.Traget;
import bustrack.example.bustrack.repositories.StationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import bustrack.example.bustrack.repositories.PointsRepository;
import bustrack.example.bustrack.repositories.TragetRepository;

import java.util.List;


@Service
public class TragetService {

    @Autowired
    private TragetRepository tragetRepository;

    @Autowired
    private PointsRepository pointsRepository;
@Autowired
private StationRepository stationRepository;
    public Long addTraget(Traget traget) {
        Traget savedTraget = tragetRepository.save(traget);
        return savedTraget.getId();
    }


    public Long updateTraget(Long id, Traget updatedTraget) {

        Traget existingTraget = tragetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Traget not found"));

        // Update existing traget properties with the ones from updatedTraget
        existingTraget.setLibelle(updatedTraget.getLibelle());

        Traget newTraget = tragetRepository.save(existingTraget);
        return newTraget.getId();
    }

    public void deleteTraget(Long id) {
        tragetRepository.deleteById(id);

    }

    public List<Traget> getAllTragets() {
        return tragetRepository.findAll();
    }
    public long getTragetCount() {
        return tragetRepository.count();
    }
    public List<Station> getAllStationsByStationId(Long stationId) {
        // Find the station by its ID
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found with id: " + stationId));

        // Get the Traget associated with the station
        Traget traget = station.getTraget();

        // Retrieve all stations belonging to the same Traget
        List<Station> stations = traget.getStations();

        return stations;
    }}