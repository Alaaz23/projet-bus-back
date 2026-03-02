package bustrack.example.bustrack.services;

import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class PasswordHasher {


    public static String hashPassword(String password) {
        try {
            // Créer un objet de hachage SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            // Convertir le mot de passe en bytes
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

            // Calculer le hachage
            byte[] hashedBytes = sha256.digest(passwordBytes);

            // Convertir le hachage en représentation hexadécimale
            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                hexStringBuilder.append(String.format("%02x", hashedByte));
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;

        }
    }
}
