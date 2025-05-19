# states_needed = set(["mt", "wa", "or", "id", "nv", "ut", "ca", "az"])
#
# stations = {}
# stations ["kone"] = set (["id", "nv", "ut"])
# stations ["ktwo"] = set (["wa", "id", "mt"])
# stations ["kthree"] = set (["or" ," nv", "са"])
# stations ["kfour"] = set (["nv", "ut"])
# stations ["kfive"] = set (["ca", "az"])
#
# final_stations = set()
#
# arr = [1, 2, 2, 3, 3, 3]
# print(set(arr))
#
# while states_needed :
#     best_station = None
#     states_covered = set()
#     for station, states_for_station in stations.items():
#         covered = states_needed & states_for_station
#         print(covered)
#         if len (covered) > len (states_covered):
#             best_station = station
#             states_covered = covered
# states_needed -= states_covered
# final_stations.add(best_station)
#
# print(final_stations)









# Набор необходимых штатов
states_needed = set(["mt", "wa", "or", "id", "nv", "ut", "ca", "az"])

# Словарь с радиостанциями и штатом, которые они покрывают
stations = {}
stations["kone"] = set(["id", "nv", "ut"])
stations["ktwo"] = set(["wa", "id", "mt"])
stations["kthree"] = set(["or", "nv", "ca"])  # Исправлено "са" на "ca"
stations["kfour"] = set(["nv", "ut"])
stations["kfive"] = set(["ca", "az"])


# Набор для финальных радиостанций
final_stations = set()

# arr = [1, 2, 2, 3, 3, 3]
# print(set(arr))

while states_needed:
    best_station = None
    states_covered = set()

    # Ищем радиостанцию, покрывающую наибольшее количество оставшихся штатов
    for station, states_for_station in stations.items():
        covered = states_needed & states_for_station
        # print(covered)  # Для отладки, выводим покрытые штаты

        # Если нашли лучшую станцию по количеству покрытых штатов
        if len(covered) > len(states_covered):
            best_station = station
            states_covered = covered

    # Убираем покрытые штаты из необходимых
    if best_station is not None:  # Проверяем, что лучшая станция найдено
        states_needed -= states_covered
        final_stations.add(best_station)

print(final_stations)
#
#
# final_stations1 = set()
# final_stations1.add("one")
# final_stations1.add("two")
# final_stations1.add("three")
# final_stations1.add("four")
# print(final_stations1)
