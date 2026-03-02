package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Bus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import bustrack.example.bustrack.repositories.BusRepository;
import bustrack.example.bustrack.repositories.TragetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BusService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private TragetRepository tragetRepository;

    public Long addBus(Bus bus) {
        Bus savedBus = busRepository.save(bus);
        return savedBus.getId(); // Return the ID of the newly added bus
    }
    public void updateBus(Long id, Bus updatedBus) {

      Bus existingBus = (Bus) busRepository.findById(id)
              .orElseThrow(() -> new EntityNotFoundException("Bus not found"));
        // Update existing bus properties with the ones from updatedBus
      existingBus.setDesignation(updatedBus.getDesignation());
      existingBus.setCapacite(updatedBus.getCapacite());
      existingBus.setTraget(updatedBus.getTraget());
      existingBus.setPoints(updatedBus.getPoints());
      busRepository.save(existingBus);
    }

    public void deleteBus(Long id) {
        // You can add additional logic here, e.g., check if the bus is associated with any routes
        busRepository.deleteById(id);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
    public long getBusCount() {
        return busRepository.count();
    }
    public Optional<Bus> getBusById(Long id) {
        return busRepository.findById(id);
    }

}
