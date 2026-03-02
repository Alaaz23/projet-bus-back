package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Bus;
import bustrack.example.bustrack.models.Points;
import bustrack.example.bustrack.repositories.PointsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointsService {

    @Autowired
    private PointsRepository pointsRepository;

    public void updatePoint(Long id, Points updatedPoint) {
        Points existingPoint = pointsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Point not found"));
        // Update existing point properties with the ones from updatedPoint
        existingPoint.setLatitude(updatedPoint.getLatitude());
        existingPoint.setLongitude(updatedPoint.getLongitude());
        pointsRepository.save(existingPoint);
    }
    public Long addPoint(Points points) {
        Points savedPoint = pointsRepository.save(points);
        return savedPoint.getId(); // Return the ID of the newly added point
    }    public List<Points> getAllPoints() {
        return pointsRepository.findAll();
    }

}
