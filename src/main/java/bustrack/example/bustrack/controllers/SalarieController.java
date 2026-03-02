package bustrack.example.bustrack.controllers;

import bustrack.example.bustrack.exception.UserNotFoundException;
import bustrack.example.bustrack.models.Admin;
import bustrack.example.bustrack.models.LoginResponse;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.models.UserLoginRequest;
import bustrack.example.bustrack.services.PasswordHasher;
import bustrack.example.bustrack.services.SalarieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")

@RestController
@RequestMapping("/salaries")
public class SalarieController {


    private final SalarieService salarieService;
    private final PasswordHasher passwordHasher;
    @Autowired
    public SalarieController(SalarieService salarieService, PasswordHasher passwordHasher) {
        this.salarieService = salarieService;
        this.passwordHasher = passwordHasher;
    }



    @GetMapping("/all")
    public List<Salarie> getAllSalaries() {
        return salarieService.getAllSalaries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Salarie> getSalarieById(@PathVariable Long id) {
        return salarieService.getSalarieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addSalarie(@RequestBody Salarie salarie) {
        String plainPassword = salarie.getPassword();
        String hashedPassword = passwordHasher.hashPassword(plainPassword);
        salarie.setPassword(hashedPassword);

        salarieService.addSalarie(salarie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Salarie added successfully");
        return ResponseEntity.ok(responseBody);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateSalarie(@PathVariable Long id, @RequestBody Salarie updatedSalarie) {
        try {
            salarieService.updateSalarie(id, updatedSalarie);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Salarie updated successfully");
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteSalarie(@PathVariable Long id, @RequestParam String matricule) {
        try {
            salarieService.deleteSalarie(id, matricule);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Salarie deleted successfully");
            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", e.getMessage());
            return ResponseEntity.status(400).body(responseBody);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginRequest loginRequest) {
        String matricule = loginRequest.getMatricule();
        String password = loginRequest.getPassword();

        try {
            Salarie user = salarieService.findBymatricule(matricule);
            if (user.getPassword().equals(passwordHasher.hashPassword(password))) {
                // Authentication successful
                Long id = user.getId();
                String nom = user.getNom();
                Long id_st = user.getStation().getId();
                Long id_b = user.getBus().getId();
                String prenom = user.getPrenom();
                return ResponseEntity.ok(new LoginResponse(true, id, nom, prenom, id_st, id_b, password));
            } else {
                // Incorrect password
                return ResponseEntity.ok(new LoginResponse(false, null, null, null, null, null, password));
            }
        } catch (UserNotFoundException e) {
            // User not found
            return ResponseEntity.ok(new LoginResponse(false, null, null, null, null, null, null));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getSalariesCount() {
        long count = salarieService.getSalariesCount();
        return ResponseEntity.ok(count);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody UserLoginRequest resetRequest) {
        String matricule = resetRequest.getMatricule();
        String newPassword = resetRequest.getPassword();

        try {
            Salarie user = salarieService.findBymatricule(matricule);
            String hashedPassword = passwordHasher.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            salarieService.resetPassword(matricule, hashedPassword);
            return ResponseEntity.ok("Password reset successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
