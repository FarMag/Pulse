states_needed = set(["mt", "wa", "or", "id", "nv", "ut", "ca", "az"])

# Словарь с радиостанциями и штатом, которые они покрывают
stations = {}
stations["kone"] = set(["id", "nv", "ut"])
stations["ktwo"] = set(["wa", "id", "mt"])
stations["kthree"] = set(["or", "nv", "ca"])  # Исправлено "са" на "ca"
stations["kfour"] = set(["nv", "ut"])
stations["kfive"] = set(["ca", "az"])

final_stations = set()

# Набор для финальных радиостанций
while states_needed:
    best_station = None
    covered = set()

    for station, covered_state in stations.items():
        covered_station = covered_state & states_needed

        if len(covered) < len(covered_station):
            covered = covered_station
            best_station = covered
            states_needed -= covered_station
            final_stations.add(station)

print(final_stations)