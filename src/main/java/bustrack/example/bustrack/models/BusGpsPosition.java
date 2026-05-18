package bustrack.example.bustrack.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bus_gps_position", indexes = {
    @Index(name = "idx_gps_bus_ts", columnList = "bus_id, timestamp DESC")
})
public class BusGpsPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_id")
    private Long busId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** Vitesse en km/h */
    @Column
    private Double speed;

    /** Cap du bus en degrés (0-359). 0=Nord, 90=Est, 180=Sud, 270=Ouest */
    @Column
    private Double bearing;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_id", length = 50)
    private String deviceId;
}
