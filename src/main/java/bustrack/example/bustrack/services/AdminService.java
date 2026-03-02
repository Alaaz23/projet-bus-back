package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Admin;
import bustrack.example.bustrack.repositories.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public void addAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    public void updateAdmin(Long id, Admin updatedAdmin) {
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));
        // Update existing admin properties with the ones from updatedAdmin
        existingAdmin.setMatricule(updatedAdmin.getMatricule());
        existingAdmin.setPassword(updatedAdmin.getPassword());
        adminRepository.save(existingAdmin);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
    public Admin findBymatricule(String matricule) {
        // Implement the logic to fetch the user by matricule and password
        return this.adminRepository.findBymatricule(matricule)
                .orElseThrow(()->new EntityNotFoundException("Admin with matricule "+matricule+" was not found" ));
    }
}
