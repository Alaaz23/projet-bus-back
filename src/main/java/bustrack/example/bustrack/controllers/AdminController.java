package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.exception.UserNotFoundException;
import bustrack.example.bustrack.models.Admin;
import bustrack.example.bustrack.models.LoginResponse;
import bustrack.example.bustrack.models.UserLoginRequest;
import bustrack.example.bustrack.services.AdminService;
import bustrack.example.bustrack.services.PasswordHasher;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;
    private final PasswordHasher passwordHasher;
    private final HttpSession httpSession;

    @Autowired
    public AdminController(AdminService adminService, PasswordHasher passwordHasher, HttpSession httpSession) {
        this.adminService = adminService;
        this.passwordHasher = passwordHasher;
        this.httpSession = httpSession;
    }

    @GetMapping("/all")
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        return adminService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAdmin(@RequestBody Admin admin) {
        String plainPassword = admin.getPassword();
        String hashedPassword = passwordHasher.hashPassword(plainPassword);
        admin.setPassword(hashedPassword);

        adminService.addAdmin(admin);
        return ResponseEntity.ok("Admin added successfully");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateAdmin(@PathVariable Long id, @RequestBody Admin updatedAdmin) {
        String plainPassword = updatedAdmin.getPassword();
        String hashedPassword = passwordHasher.hashPassword(plainPassword);
        updatedAdmin.setPassword(hashedPassword);

        adminService.updateAdmin(id, updatedAdmin);
        return ResponseEntity.ok("Admin updated successfully");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok("Admin deleted successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Admin> login(@RequestBody UserLoginRequest loginRequest) {
        String matricule = loginRequest.getMatricule();
        String password = loginRequest.getPassword();

        try {
            Admin user = adminService.findBymatricule(matricule);
            if (user.getPassword().equals(passwordHasher.hashPassword(password))) {
                // Store logged-in user information in session
                httpSession.setAttribute("loggedInMatricule", matricule);
                return ResponseEntity.ok(user); // Return user details upon successful login
            } else {
                throw new UserNotFoundException("Invalid password");
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/loggedInAdmin/{matricule}")
    public ResponseEntity<Admin> getAdminByMatricule(@PathVariable String matricule) {
        try {
            Admin admin = adminService.findBymatricule(matricule);
            return ResponseEntity.ok(admin);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
