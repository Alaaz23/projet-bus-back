-- ============================================================
-- Table : bus_gps_position
-- Description : stockage des positions GPS en temps réel
--               envoyées par le Raspberry Pi / script de simulation
-- ============================================================

CREATE TABLE IF NOT EXISTS bus_gps_position (
    id          BIGSERIAL PRIMARY KEY,
    bus_id      BIGINT        NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    speed       DOUBLE PRECISION,            -- vitesse en km/h
    timestamp   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    device_id   VARCHAR(50),

    CONSTRAINT fk_gps_bus FOREIGN KEY (bus_id) REFERENCES bus(id_b) ON DELETE CASCADE
);

-- Index pour accélérer les requêtes "dernière position d'un bus"
CREATE INDEX IF NOT EXISTS idx_gps_bus_ts
    ON bus_gps_position (bus_id, timestamp DESC);

-- ── Vue pratique : dernière position de chaque bus ────────────
CREATE OR REPLACE VIEW v_bus_last_position AS
SELECT DISTINCT ON (bus_id)
    g.id,
    g.bus_id,
    b.designation AS bus_designation,
    g.latitude,
    g.longitude,
    g.speed,
    g.timestamp,
    g.device_id
FROM bus_gps_position g
JOIN bus b ON b.id_b = g.bus_id
ORDER BY bus_id, timestamp DESC;

-- ── Nettoyage automatique : garder 24 h d'historique ─────────
-- (optionnel – à activer si le volume de données devient important)
-- DELETE FROM bus_gps_position WHERE timestamp < NOW() - INTERVAL '24 hours';
