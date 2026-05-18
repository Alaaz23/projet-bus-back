package bustrack.example.bustrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuration WebSocket STOMP.
 *
 * Topics disponibles (push serveur → clients) :
 *   /topic/gps/{busId}  → positions d'un bus spécifique
 *   /topic/gps/all      → toutes les positions (tous bus confondus)
 *
 * Endpoints :
 *   /ws          → SockJS (Angular web, avec fallback HTTP long-polling)
 *   /ws-native   → WebSocket pur (Flutter mobile, Raspberry Pi)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker simple en mémoire — remplacer par RabbitMQ/ActiveMQ en production
        config.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés par les clients vers le serveur
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint SockJS pour Angular (fallback polling si WS indisponible)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint WebSocket natif pour Flutter et Raspberry Pi
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}
