#!/usr/bin/env python3
"""
GPS Sender – Simulation temps réel (HTTP → WebSocket STOMP)
Route : Borj Cedriya → Sofrecom Tunisie (bus 6)

Le script envoie les positions via HTTP POST au backend Spring Boot.
Le backend les sauvegarde en BDD et les diffuse automatiquement via
WebSocket STOMP sur /topic/gps/{busId} → le frontend Angular les reçoit
en temps réel.

Usage :
  python gps_sender.py                    # trajet complet (bus-id 6, OSRM)
  python gps_sender.py --loop             # boucle en continu
  python gps_sender.py --bus-id 8        # changer l'id du bus (ex: Bizerte)
  python gps_sender.py --interval 2      # envoyer toutes les 2 s
  python gps_sender.py --fallback        # forcer interpolation linéaire
  python gps_sender.py --start-at 15    # démarrer au 15e km du trajet (simulation position avancée)

IDs de bus en base :
  6 → Sofrecom ↔ Borj Cedriya   (EXACT_STATIONS — 11 arrêts)
  7 → Sofrecom ↔ Ariana         (SOFRECOM_ARIANA — 4 arrêts)
  8 → Bizerte  ↔ Sofrecom       (BIZERTE_SOFRECOM — 3 arrêts)
"""

import argparse
import math
import sys
import time

import requests

# ─── Configuration ────────────────────────────────────────────────────────────

BACKEND_URL = "http://localhost:8081/Bus-tracking/gps/position"
OSRM_URL    = "https://router.project-osrm.org/route/v1/driving"

# Waypoints par bus ID — doivent correspondre à BUS_ROUTES dans map.component.ts
ROUTES = {
    6: [
        (36.705199, 10.407781),  #  1. Borj Cedriya (départ)
        (36.713843, 10.368674),  #  2. Hammem Chatt
        (36.727068, 10.336807),  #  3. Hammem Lif
        (36.736568, 10.313460),  #  4. Ezzahra lycée
        (36.740769, 10.302511),  #  5. Ezzahra ville
        (36.756550, 10.278947),  #  6. Radès
        (36.764836, 10.277623),  #  7. Pont Radès
        (36.772570, 10.287141),  #  8. Radès chatt (côte est)
        (36.818500, 10.303000),  #  9. TGM La Goulette
        (36.836000, 10.259000),  # 10. Lac 0 — rive nord
        (36.831374, 10.232228),  # 11. Lac 1 — Les Berges du Lac (arrivée)
    ],
    7: [
        (36.862000, 10.193500),  # 1. Ariana (départ)
        (36.849500, 10.198000),  # 2. Cité Ettadhamen
        (36.840200, 10.213000),  # 3. Centre Urbain Nord
        (36.831585, 10.232803),  # 4. Sofrecom (arrivée)
    ],
    8: [
        (37.269527,  9.874099),  # 1. Bizerte (départ)
        (37.264961,  9.885155),  # 2. Bizerte Zarzouna
        (36.831585, 10.232803),  # 3. Sofrecom (arrivée)
    ],
}

# Waypoints par défaut (bus 6 = Borj Cedriya → Sofrecom)
WAYPOINTS = ROUTES[6]

# ─── Utilitaires géographiques ────────────────────────────────────────────────

def haversine_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Distance en km entre deux points GPS (formule de Haversine)."""
    R = 6371.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return 2 * R * math.asin(math.sqrt(a))


# ─── Route via OSRM (vraies routes) ──────────────────────────────────────────

def fetch_osrm_route(waypoints: list) -> list:
    """
    Appelle l'API OSRM avec tous les waypoints et retourne
    la liste des coordonnées GPS réelles de la route.
    Format retourné : liste de (lat, lng).
    """
    # OSRM attend les coords au format lng,lat séparées par ;
    coords_str   = ";".join(f"{lng},{lat}" for lat, lng in waypoints)
    radiuses_str = ";".join(["500"] * len(waypoints))
    url = f"{OSRM_URL}/{coords_str}?overview=full&geometries=geojson&radiuses={radiuses_str}"

    print(f"  Appel OSRM : {url[:90]}...")
    try:
        resp = requests.get(url, timeout=15)
        resp.raise_for_status()
        data = resp.json()

        if data.get("code") != "Ok" or not data.get("routes"):
            print("[ERREUR] OSRM n'a pas retourné de route valide.", file=sys.stderr)
            return []

        # geometry.coordinates = liste de [lng, lat]
        coords = data["routes"][0]["geometry"]["coordinates"]
        route_points = [(lat, lng) for lng, lat in coords]

        # Filtrer les points hors de la zone du trajet (+/- 0.15 deg ~ 16 km de marge)
        lat_min = min(lat for lat, _ in waypoints) - 0.15
        lat_max = max(lat for lat, _ in waypoints) + 0.15
        lng_min = min(lng for _, lng in waypoints) - 0.15
        lng_max = max(lng for _, lng in waypoints) + 0.15
        before  = len(route_points)
        route_points = [
            (lat, lng) for lat, lng in route_points
            if lat_min <= lat <= lat_max and lng_min <= lng <= lng_max
        ]
        if len(route_points) < before:
            print(f"  [FILTRE] {before - len(route_points)} points hors zone supprimes ({before} -> {len(route_points)})")
        if len(route_points) < 2:
            print("[ERREUR] Route OSRM vide apres filtrage — fallback.", file=sys.stderr)
            return []

        total_dist = data["routes"][0]["distance"] / 1000  # km
        total_dur  = data["routes"][0]["duration"] / 60    # minutes
        print(f"  [OK] Route OSRM : {len(route_points)} points GPS | {total_dist:.1f} km | ~{total_dur:.0f} min")
        return route_points

    except requests.exceptions.ConnectionError:
        print("[ERREUR] Impossible de joindre OSRM – vérifiez votre connexion internet.", file=sys.stderr)
        return []
    except requests.exceptions.Timeout:
        print("[ERREUR] Timeout OSRM (15 s).", file=sys.stderr)
        return []
    except Exception as exc:
        print(f"[ERREUR] OSRM : {exc}", file=sys.stderr)
        return []


def build_route_with_speed(osrm_points: list, interval_s: float,
                            avg_speed_kmh: float = 30.0) -> list:
    """
    Génère des positions régulières le long de la route OSRM à vitesse constante.
    Interpolation fluide — une position toutes les interval_s secondes.
    Identique au déplacement simulé dans Flutter (_kSpeedMps) et Angular.

    avg_speed_kmh : vitesse moyenne simulée (défaut 30 km/h = bus urbain Tunis).
    """
    if len(osrm_points) < 2:
        return [(osrm_points[0][0], osrm_points[0][1], 0.0, 0.0)] if osrm_points else []

    # ── 1. Distances cumulées entre les points OSRM (mètres) ─────────────────
    cumul_m = [0.0]
    for i in range(1, len(osrm_points)):
        d = haversine_km(
            osrm_points[i - 1][0], osrm_points[i - 1][1],
            osrm_points[i][0],     osrm_points[i][1]
        ) * 1000.0
        cumul_m.append(cumul_m[-1] + d)

    total_m = cumul_m[-1]
    step_m  = (avg_speed_kmh / 3.6) * interval_s   # mètres parcourus par intervalle
    n_steps = max(1, int(total_m / step_m))
    print(f"  Interpolation : {total_m / 1000:.2f} km  |  {avg_speed_kmh} km/h  |"
          f"  pas {step_m:.1f} m  |  → ~{n_steps} positions")

    # ── 2. Interpolation continue le long de la route ────────────────────────
    result  = []
    seg_idx = 0
    pos_m   = 0.0

    while pos_m < total_m:
        # Avancer l'indice de segment jusqu'au bon
        while seg_idx < len(osrm_points) - 2 and cumul_m[seg_idx + 1] <= pos_m:
            seg_idx += 1

        a       = osrm_points[seg_idx]
        b       = osrm_points[seg_idx + 1]
        seg_len = cumul_m[seg_idx + 1] - cumul_m[seg_idx]

        if seg_len < 0.01:          # points superposés — sauter
            pos_m += step_m
            continue

        t   = (pos_m - cumul_m[seg_idx]) / seg_len
        lat = a[0] + (b[0] - a[0]) * t
        lng = a[1] + (b[1] - a[1]) * t
        bearing = calc_bearing(a[0], a[1], b[0], b[1])
        result.append((lat, lng, avg_speed_kmh, bearing))
        pos_m += step_m

    # ── 3. Toujours terminer à la destination exacte ──────────────────────────
    last = osrm_points[-1]
    result.append((last[0], last[1], 0.0, 0.0))
    return result


def calc_bearing(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Cap en degrés (0=Nord, 90=Est, 180=Sud, 270=Ouest)."""
    import math
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dl = math.radians(lon2 - lon1)
    x = math.sin(dl) * math.cos(phi2)
    y = math.cos(phi1) * math.sin(phi2) - math.sin(phi1) * math.cos(phi2) * math.cos(dl)
    return (math.atan2(x, y) * 180 / math.pi + 360) % 360


# ─── Fallback : interpolation linéaire si OSRM indisponible ──────────────────

def interpolate(p1: tuple, p2: tuple, t: float) -> tuple:
    return (p1[0] + (p2[0] - p1[0]) * t, p1[1] + (p2[1] - p1[1]) * t)


def build_fallback_route(waypoints: list, steps_between: int, interval_s: float) -> list:
    """Route de secours si OSRM est indisponible (interpolation entre waypoints)."""
    route = []
    for i in range(len(waypoints) - 1):
        p1, p2 = waypoints[i], waypoints[i + 1]
        dist_km = haversine_km(p1[0], p1[1], p2[0], p2[1])
        speed_kmh = min(dist_km / (steps_between * interval_s / 3600.0), 80.0)
        bearing = calc_bearing(p1[0], p1[1], p2[0], p2[1])
        for step in range(steps_between):
            t = step / steps_between
            lat, lng = interpolate(p1, p2, t)
            route.append((lat, lng, speed_kmh, bearing))
    last = waypoints[-1]
    route.append((last[0], last[1], 0.0, 0.0))
    return route


# ─── Envoi HTTP ───────────────────────────────────────────────────────────────

def send_position(lat: float, lng: float, speed: float, bus_id: int, device_id: str, bearing: float = 0.0) -> None:
    payload = {
        "busId": bus_id,
        "latitude": round(lat, 6),
        "longitude": round(lng, 6),
        "speed": round(speed, 1),
        "bearing": round(bearing, 1),
        "deviceId": device_id,
    }
    try:
        resp = requests.post(BACKEND_URL, json=payload, timeout=3)
        ts = time.strftime('%H:%M:%S')
        print(
            f"[{ts}] bus={bus_id:>3}  lat={payload['latitude']:>10.6f}  "
            f"lng={payload['longitude']:>10.6f}  {payload['speed']:>5.1f} km/h  "
            f"cap={payload['bearing']:>6.1f} deg  -> HTTP {resp.status_code}"
        )
    except requests.exceptions.ConnectionError:
        print(f"[ERREUR] Impossible de joindre {BACKEND_URL} – backend démarré ?", file=sys.stderr)
    except requests.exceptions.Timeout:
        print("[ERREUR] Timeout – le backend ne répond pas.", file=sys.stderr)
    except requests.exceptions.RequestException as exc:
        print(f"[ERREUR] {exc}", file=sys.stderr)


# ─── Point d'entrée ───────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(description="GPS Sender – simulation temps réel (HTTP→WebSocket STOMP)")
    parser.add_argument("--bus-id",   type=int,   default=6,             help="ID du bus (6=Cedriya, 7=Ariana, 8=Bizerte)")
    parser.add_argument("--device",   type=str,   default="RPI-BUS-001", help="Identifiant de l'appareil")
    parser.add_argument("--interval", type=float, default=2.0,           help="Intervalle en secondes (défaut: 2)")
    parser.add_argument("--steps",    type=int,   default=30,            help="Pas entre waypoints si fallback (défaut: 30)")
    parser.add_argument("--loop",     action="store_true",               help="Boucler la route en continu")
    parser.add_argument("--fallback", action="store_true",               help="Forcer le mode fallback (sans OSRM)")
    parser.add_argument("--speed",    type=float, default=30.0,
                        help="Vitesse simulée en km/h (défaut: 30 = bus urbain Tunis)")
    parser.add_argument("--start-at", type=float, default=0.0,
                        help="Démarrer à N km depuis le début du trajet (simule bus en cours de route)")
    args = parser.parse_args()

    waypoints = ROUTES.get(args.bus_id, ROUTES[6])
    dep_name  = {6: "Borj Cedriya", 7: "Ariana",   8: "Bizerte"}.get(args.bus_id, "Depart")
    arr_name  = {6: "Sofrecom",     7: "Sofrecom", 8: "Sofrecom"}.get(args.bus_id, "Arrivee")

    print("=" * 65)
    print(f"  Départ  : {dep_name}")
    print(f"  Arrivée : {arr_name}")
    print(f"  Bus ID  : {args.bus_id}  |  Intervalle : {args.interval} s")
    if args.start_at > 0:
        print(f"  ⚡ Reprise depuis : {args.start_at} km")
    print(f"  Backend : {BACKEND_URL}")
    print("=" * 65)

    # Récupérer la route OSRM ou mode fallback
    if args.fallback:
        print("  Mode fallback activé (interpolation linéaire entre waypoints)\n")
        route = build_fallback_route(waypoints, args.steps, args.interval)
    else:
        print("  Récupération de la route via OSRM (vraies routes routières)...")
        osrm_points = fetch_osrm_route(waypoints)
        if osrm_points:
            route = build_route_with_speed(osrm_points, args.interval, args.speed)
            print(f"  ✅ Mode OSRM actif – {len(route)} positions interpolées à {args.speed} km/h\n")
        else:
            print("  ⚠️  OSRM indisponible – basculement sur interpolation linéaire\n")
            route = build_fallback_route(waypoints, args.steps, args.interval)

    # Trouver l'index de départ si --start-at est spécifié
    start_idx = 0
    if args.start_at > 0:
        cumul_km = 0.0
        for i in range(1, len(route)):
            seg_km = haversine_km(route[i-1][0], route[i-1][1], route[i][0], route[i][1])
            cumul_km += seg_km
            if cumul_km >= args.start_at:
                start_idx = i
                break
        print(f"  ⚡ Démarrage à l'index {start_idx}/{len(route)} (~{cumul_km:.1f} km depuis le départ)")

    total = len(route)
    remaining = total - start_idx
    print(f"  Total points : {total}  |  Points à envoyer : {remaining}")
    print("=" * 65)
    print()

    try:
        while True:
            for idx, point in enumerate(route[start_idx:], start_idx + 1):
                lat, lng, speed, bearing = point
                print(f"  [{idx:>4}/{total}]", end=" ")
                send_position(lat, lng, speed, args.bus_id, args.device, bearing)
                time.sleep(args.interval)

            start_idx = 0  # après la 1ère boucle, reprendre depuis le début
            if not args.loop:
                print("\nTrajet terminé.")
                break
            print("\n── Redémarrage de la route ──\n")

    except KeyboardInterrupt:
        print("\nSimulation arrêtée.")


if __name__ == "__main__":
    main()
