import java.util.*;

public class AStar{

    private int totalTimeTaken = 0;
    private int nodesExpanded = 0;
    private GraphImpl graph;

    private void setTotalTimeTaken(List<City> path) {
        int i = 1;
        while (i < path.size()) {
            totalTimeTaken += path.get(i).getRefuelTime();
            totalTimeTaken += graph.getWeight(path.get(i - 1), path.get(i));
            i++;
        }
    }

    public void printPath(List<City> path) {
        setTotalTimeTaken(path);
        System.out.println(nodesExpanded + " nodes expanded");
        System.out.println("cost = " + totalTimeTaken);
        boolean printFlag = true;
        int i = 0;
        while (i < path.size()) {
            if (printFlag) {
                if (i < path.size() - 1) {
                    System.out.print("Ship " + path.get(i).getCityName() + " to ");
                }
                printFlag = false;
                i++;
            } else {
                System.out.println(path.get(i).getCityName());
                printFlag = true;
            }
        }
    }

    public List<City> path(Map<City, List<City>> path, City start) {
        final List<City> pathList = new ArrayList<>();
        List<City> curList = path.get(start);
        pathList.add(start);
        while(curList != null && curList.size() > 0) {
            pathList.add(curList.remove(0));
            curList = path.get(pathList.get(pathList.size() - 1));
        }
        return pathList;
    }

    public List<City> search(State goalState) {
        State currentState = new State();

        currentState.setCurrentCity(graph.getNode("Sydney"));

        PriorityQueue<State> openQueue = new PriorityQueue<>(new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getF(graph, goalState) - o2.getF(graph, goalState);
            }
        });
        openQueue.add(currentState);
        Set<State> closedSet = new HashSet<>();
        Map<City, List<City>> path = new HashMap<>();

        while (openQueue.peek() != null) {
            currentState = openQueue.poll();
            //System.out.println(currentState.getHeuristic(graph, goalState));

            nodesExpanded++;
            if (isGoalState(currentState.getShipments(), goalState)) {
                return path(currentState.getShipments(), graph.getNode("Sydney"));
            }

            closedSet.add(currentState);

            for (City neighbour : graph.getNeighbours(currentState.getCurrentCity()).keySet()) {
                State nextState = new State();
                nextState.setShipments(currentState.getShipments());
                nextState.addShipment(currentState.getCurrentCity(), neighbour);
                nextState.setCurrentCity(neighbour);

                if (closedSet.contains(nextState)) continue;

                if (!openQueue.contains(nextState)) {
                    openQueue.add(nextState);
                }


                int weight = currentState.getG(graph) + currentState.getHeuristic(graph, nextState);

                //System.out.println(currentState.getG(graph) + " " + weight + " " + nextState.getG(graph));
                if (weight >= nextState.getG(graph)) continue;

                if (path.containsKey(currentState.getCurrentCity())) {
                    path.get(currentState.getCurrentCity()).add(neighbour);
                } else {
                    List<City> toList = new ArrayList<>();
                    toList.add(neighbour);
                    path.put(currentState.getCurrentCity(), toList);
                }


            }
        }
        return null;
    }

    private boolean isGoalState(Map<City, List<City>> path, State goalState) {
        //currentState.getHeuristic(graph, goalState) <= 0;
        for (City key : goalState.getShipments().keySet()) {
            for (City city : goalState.getShipments().get(key)) {
                if (!path.containsKey(key) || !path.get(key).contains(city)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setGraph(GraphImpl graph) {
        this.graph = graph;
    }
}
