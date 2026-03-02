package bustrack.example.bustrack.services;

import bustrack.example.bustrack.exception.UserNotFoundException;
import bustrack.example.bustrack.models.*;
import bustrack.example.bustrack.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SalarieService {

    @Autowired
    private SalarieRepository salarieRepository;
    @Autowired
    private BusRepository busRepository;  // Add this autowired field

    @Autowired
    private StationRepository stationRepository;  // Add this autowired field

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<Salarie> getAllSalaries() {
        return salarieRepository.findAll();
    }

    public Optional<Salarie> getSalarieById(Long id) {
        return salarieRepository.findById(id);
    }

    public void addSalarie(Salarie salarie) {
        // Fetch the Bus and Traget entities from the database based on the provided IDs
        Bus bus = busRepository.findById(salarie.getBus().getId())
                .orElseThrow(() -> new EntityNotFoundException("Bus not found"));

        Station station = stationRepository.findById(salarie.getStation().getId())
                .orElseThrow(() -> new EntityNotFoundException("Traget not found"));

        // Set the fetched Bus and Traget for the Salarie entity before saving
        salarie.setBus(bus);
        salarie.setStation(station);

        salarieRepository.save(salarie);
    }

    public void updateSalarie(Long id, Salarie updatedSalarie) {
        Salarie existingSalarie = salarieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Salarie not found"));

        // Update existing Salarie properties with the ones from updatedSalarie
        existingSalarie.setMatricule(updatedSalarie.getMatricule());
        existingSalarie.setPassword(updatedSalarie.getPassword());
        existingSalarie.setNom(updatedSalarie.getNom());
        existingSalarie.setPrenom(updatedSalarie.getPrenom());

        // Fetch the updated Bus entity from the repository
        Bus updatedBus = busRepository.findById(updatedSalarie.getBus().getId())
                .orElseThrow(() -> new EntityNotFoundException("Bus not found"));

        // Set the updated Bus entity to the existing Salarie entity
        existingSalarie.setBus(updatedBus);

        // Fetch the updated Station entity from the repository
        Station updatedStation = stationRepository.findByLibelle(updatedSalarie.getStation().getLibelle())
                .orElseThrow(() -> new EntityNotFoundException("Station not found"));

        // Set the updated Station entity to the existing Salarie entity
        existingSalarie.setStation(updatedStation);

        // Save the updated Salarie entity
        salarieRepository.save(existingSalarie);
    }


    @Transactional
    public void deleteSalarie(Long id, String providedMatricule) {
        // Fetch the Salarie entity by id
        Salarie salarie = salarieRepository.findById(id).orElse(null);

        // Check if the Salarie entity exists
        if (salarie != null) {
            // Verify matricule before deleting
            if (salarie.getMatricule().equals(providedMatricule)) {
                // Set related foreign keys to null
                salarie.setStation(null);
                salarie.setBus(null);
                feedbackRepository.deleteBySalarieId(id);

                // Perform deletion if matricule matches
                salarieRepository.deleteById(id);
            } else {
                // Handle case where matricule does not match
                throw new RuntimeException("Matricule verification failed. Deletion aborted.");
            }
        } else {
            // Handle case where Salarie entity with the given id does not exist
            throw new RuntimeException("Salarie with id " + id + " not found.");
        }
    }




    public Salarie findBymatricule(String matricule) {
        // Implement the logic to fetch the user by matricule and password
        return this.salarieRepository.findBymatricule(matricule)
                .orElseThrow(()->new EntityNotFoundException("salarie with matricule "+matricule+" was not found"));
}
    public long getSalariesCount() {
        return salarieRepository.count();
    }
    public void resetPassword(String matricule, String newPassword) throws UserNotFoundException {
        // Retrieve the user by matricule
        Salarie user = salarieRepository.findBymatricule(matricule)
                .orElseThrow(() -> new UserNotFoundException("User with matricule " + matricule + " not found"));

        // Update the password
        user.setPassword(newPassword);

        // Save the updated user
        salarieRepository.save(user);
    }
}