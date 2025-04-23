# Your first line of Python code

graph = {}
graph["start"] = {}
graph["start"]["a"] = 6
graph["start"]["b"] = 2
graph["a"] = {}
graph["a"]["fin"] = 1
graph["b"] = {}
graph["b"]["a"] = 3
graph["b"]["fin"] = 5
graph["fin"] = {}

parents = {}
parents["a"] = "start"
parents["b"] = "start"
parents["fin"] = None

infinity = float("inf")
costs = {}
costs["a"] = 6
costs["b"] = 2
costs["fin"] = infinity

processed = []

shortest_path = []
shortest_path.append("start")


def find_lowest_cost_node(costs):
    # print("costs = ", costs)
    lowest_cost = float("inf")
    lowest_cost_node = None
    for node in costs:
        cost = costs[node]
        # print("node = ", node, "cost = ", cost)
        if cost < lowest_cost and node not in processed:
            lowest_cost = cost
            lowest_cost_node = node
    # print("\n")
    return lowest_cost_node


node = find_lowest_cost_node(costs)
while node is not None:
    cost = costs[node]
    neighbors = graph[node]
    # print("neigbors = ", neighbors)
    for n in neighbors.keys():
        new_cost = cost + neighbors[n]
        if costs[n] > new_cost:
            costs[n] = new_cost
            # shortest_path.append(new_cost)
            # shortest_path.append(n)
    processed.append(node)
    node = find_lowest_cost_node(costs)

print(processed)
print("lowest_cost_way = ", shortest_path)


