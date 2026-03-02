package bustrack.example.bustrack.repositories;

import bustrack.example.bustrack.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findBymatricule(String matricule);
}
