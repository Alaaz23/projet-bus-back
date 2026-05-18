#!/usr/bin/env python3
"""
bus_simulator.py
Simule un bus suivant les VRAIES routes (OSRM) entre 11 stations.
Envoie les positions GPS au backend toutes les secondes.

Usage:
  python bus_simulator.py [options]

Options:
  --backend-url URL   URL POST backend (défaut: http://localhost:8081/Bus-tracking/gps/position)
  --bus-id ID         ID du bus (défaut: 7)
  --speed KMH         Vitesse km/h (défaut: 40)
  --interval SEC      Intervalle secondes entre envois (défaut: 1.0)
  --loop              Répéter le trajet en boucle
  --fallback          Forcer interpolation linéaire (sans OSRM)
"""

import argparse
import math
import time
import requests

# ─── 11 Stations dans l'ordre EXACT — coordonnées imposées par le trajet réel ──
STATIONS = [
    {"name": "Borj Cedriya",    "lat": 36.705199, "lon": 10.407781},
    {"name": "Hammem Chatt",    "lat": 36.713843, "lon": 10.368674},
    {"name": "Hammem Lif",      "lat": 36.727068, "lon": 10.336807},
    {"name": "Ezzahra lycée",   "lat": 36.736568, "lon": 10.313460},
    {"name": "Ezzahra ville",   "lat": 36.740769, "lon": 10.302511},
    {"name": "Radès",           "lat": 36.756550, "lon": 10.278947},
    {"name": "Pont Radès",      "lat": 36.764836, "lon": 10.277623},
    {"name": "Radès chatt",     "lat": 36.772570, "lon": 10.287141},
    {"name": "TGM",             "lat": 36.801808, "lon": 10.195353},  # via Pont de Radès
    {"name": "Lac 0",           "lat": 36.809757, "lon": 10.193572},
    {"name": "Lac 1",           "lat": 36.831374, "lon": 10.232228},
]

OSRM_URL       = "https://router.project-osrm.org/route/v1/driving"
DEFAULT_BACKEND = "http://localhost:8081/Bus-tracking/gps/position"


# ─── Géométrie ────────────────────────────────────────────────────────────────

def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Distance en km entre deux points GPS (formule de Haversine)."""
    R = 6371.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi    = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return 2 * R * math.asin(math.sqrt(a))


def bearing(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Cap (0–360°) du point 1 vers le point 2."""
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dl = math.radians(lon2 - lon1)
    x  = math.sin(dl) * math.cos(phi2)
    y  = math.cos(phi1) * math.sin(phi2) - math.sin(phi1) * math.cos(phi2) * math.cos(dl)
    return (math.degrees(math.atan2(x, y)) + 360) % 360


# ─── OSRM ─────────────────────────────────────────────────────────────────────

def fetch_osrm_route(stations: list) -> list | None:
    """
    Appelle OSRM avec toutes les stations et retourne la liste de points
    (lat, lon) de la route routière réelle.
    Retourne None si OSRM est indisponible.
    """
    coords = ";".join(f"{s['lon']},{s['lat']}" for s in stations)
    url = f"{OSRM_URL}/{coords}?overview=full&geometries=geojson"
    try:
        r = requests.get(url, timeout=20)
        r.raise_for_status()
        data = r.json()
        if data.get("code") != "Ok" or not data.get("routes"):
            print(f"  OSRM returned: {data.get('code')}")
            return None
        # OSRM retourne [lon, lat] → convertir en (lat, lon)
        raw = data["routes"][0]["geometry"]["coordinates"]
        return [(c[1], c[0]) for c in raw]
    except Exception as e:
        print(f"  OSRM exception: {e}")
        return None


def linear_fallback(stations: list) -> list:
    """
    Fallback : interpolation linéaire entre stations.
    Génère ~60 points par segment pour un mouvement fluide.
    """
    points = []
    for i in range(len(stations) - 1):
        s1, s2 = stations[i], stations[i + 1]
        dist = haversine_km(s1["lat"], s1["lon"], s2["lat"], s2["lon"])
        n = max(10, int(dist * 15))  # ~15 points par km
        for j in range(n):
            t = j / n
            points.append((
                s1["lat"] + t * (s2["lat"] - s1["lat"]),
                s1["lon"] + t * (s2["lon"] - s1["lon"]),
            ))
    last = stations[-1]
    points.append((last["lat"], last["lon"]))
    return points


# ─── Interpolation à vitesse constante ────────────────────────────────────────

def build_timed_route(route_points: list, speed_kmh: float, interval_s: float) -> list:
    """
    Transforme une liste de points géométriques en positions horodatées
    à vitesse constante (speed_kmh), espacées de interval_s secondes.
    Retourne [(lat, lon, bearing_deg), ...]
    """
    dist_step_km = speed_kmh * interval_s / 3600.0
    timed = []

    seg_idx  = 0
    seg_frac = 0.0  # distance déjà parcourue dans le segment courant (km)
    last_brg = 0.0

    while seg_idx < len(route_points) - 1:
        p1 = route_points[seg_idx]
        p2 = route_points[seg_idx + 1]
        seg_len = haversine_km(p1[0], p1[1], p2[0], p2[1])

        if seg_len < 1e-9:
            seg_idx += 1
            seg_frac = 0.0
            continue

        # Position interpolée dans ce segment
        t    = seg_frac / seg_len
        lat  = p1[0] + t * (p2[0] - p1[0])
        lon  = p1[1] + t * (p2[1] - p1[1])
        brg  = bearing(p1[0], p1[1], p2[0], p2[1])
        last_brg = brg
        timed.append((lat, lon, brg))

        # Avancer d'un pas
        seg_frac += dist_step_km

        # Changer de segment si on dépasse la fin
        while seg_frac >= seg_len and seg_idx < len(route_points) - 1:
            seg_frac -= seg_len
            seg_idx  += 1
            if seg_idx < len(route_points) - 1:
                p1 = route_points[seg_idx]
                p2 = route_points[seg_idx + 1]
                seg_len = haversine_km(p1[0], p1[1], p2[0], p2[1])
                if seg_len < 1e-9:
                    seg_frac = 0.0

    # Dernier point
    last = route_points[-1]
    timed.append((last[0], last[1], last_brg))
    return timed


# ─── Envoi au backend ─────────────────────────────────────────────────────────

def send_position(backend_url: str, bus_id: int, lat: float, lon: float,
                  speed: float, brg: float) -> str:
    """Envoie la position GPS au backend Spring Boot."""
    payload = {
        "busId":     bus_id,
        "latitude":  round(lat, 7),
        "longitude": round(lon, 7),
        "speed":     round(speed, 1),
        "bearing":   round(brg, 1),
        "deviceId":  f"SIM-{bus_id:03d}",
    }
    try:
        r = requests.post(backend_url, json=payload, timeout=3)
        return str(r.status_code)
    except requests.exceptions.ConnectionError:
        return "ERR(connexion refusée)"
    except Exception as e:
        return f"ERR({type(e).__name__})"


# ─── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Simulateur GPS bus – route réelle via OSRM",
        formatter_class=argparse.RawTextHelpFormatter,
    )
    parser.add_argument("--backend-url", default=DEFAULT_BACKEND,
                        help=f"URL POST GPS (défaut: {DEFAULT_BACKEND})")
    parser.add_argument("--bus-id",   type=int,   default=7,    help="ID bus (défaut: 7)")
    parser.add_argument("--speed",    type=float, default=40.0, help="Vitesse km/h (défaut: 40)")
    parser.add_argument("--interval", type=float, default=1.0,  help="Intervalle secondes (défaut: 1)")
    parser.add_argument("--loop",     action="store_true",      help="Boucler le trajet")
    parser.add_argument("--fallback", action="store_true",      help="Forcer interpolation linéaire (sans OSRM)")
    args = parser.parse_args()

    print("=" * 62)
    print(f"  Simulateur Bus  |  ID={args.bus_id}  |  Vitesse={args.speed} km/h")
    print(f"  Backend  : {args.backend_url}")
    print(f"  Intervalle : {args.interval}s  |  Boucle : {'oui' if args.loop else 'non'}")
    print(f"  Stations : {len(STATIONS)}")
    for i, s in enumerate(STATIONS, 1):
        print(f"    {i:2}. {s['name']:<20} ({s['lat']:.6f}, {s['lon']:.6f})")
    print("=" * 62)

    # ── 1. Obtenir la route géométrique ──────────────────────────
    route_points = None
    if not args.fallback:
        print("\n[OSRM] Calcul de la route réelle...", end="", flush=True)
        route_points = fetch_osrm_route(STATIONS)
        if route_points:
            print(f" ✓  {len(route_points)} points GPS")
        else:
            print(" ✗  OSRM indisponible → fallback interpolation")

    if route_points is None:
        print("[Fallback] Interpolation linéaire entre stations...")
        route_points = linear_fallback(STATIONS)
        print(f"  {len(route_points)} points générés")

    # ── 2. Construire la séquence à vitesse constante ─────────────
    timed_route = build_timed_route(route_points, args.speed, args.interval)
    total = len(timed_route)
    duration_min = total * args.interval / 60
    print(f"\n[Route] {total} positions | Durée estimée : {duration_min:.0f} min\n")

    # ── 3. Boucle d'envoi ──────────────────────────────────────────
    run = 0
    try:
        while True:
            run += 1
            if run > 1:
                print(f"\n\n[Loop #{run}] Redémarrage du trajet depuis Borj Cedriya...\n")

            for i, (lat, lon, brg) in enumerate(timed_route, 1):
                t0 = time.time()

                # Légère variation de vitesse (réaliste)
                speed_now = args.speed * (0.85 + 0.30 * abs(math.sin(i * 0.07)))

                status = send_position(args.backend_url, args.bus_id, lat, lon, speed_now, brg)

                # Barre de progression
                pct = i / total * 100
                filled = int(pct / 5)
                bar = "█" * filled + "░" * (20 - filled)
                print(
                    f"\r  [{bar}] {pct:5.1f}%  {i}/{total}"
                    f"  lat={lat:.5f} lon={lon:.5f}"
                    f"  cap={brg:5.1f}°  {speed_now:4.1f}km/h"
                    f"  HTTP={status}   ",
                    end="", flush=True,
                )

                elapsed = time.time() - t0
                sleep_t = max(0.0, args.interval - elapsed)
                if sleep_t > 0:
                    time.sleep(sleep_t)

            print(f"\n\n✓  Trajet terminé  (bus {args.bus_id})")
            if not args.loop:
                break

    except KeyboardInterrupt:
        print("\n\nSimulateur arrêté par l'utilisateur.")


if __name__ == "__main__":
    main()
