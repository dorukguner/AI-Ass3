import java.util.*;

public class AStar {

    private Map map;

    public AStar(Map map) {
        this.map = map;
    }

    private HashMap<Map, Integer> gScore;
    private HashMap<Map, Integer> fScore;

    /**
     * Performs an AStar search from the given Map object's player coordinates to the goal coordinates input.
     * @param goalCoords The goal coordinates of this AStar search
     * @return The path of Map objects that the AStar search has found
     */

    public List<Map> search(final int[] goalCoords) {
        Map currentMap = map;
        gScore = new HashMap<>();
        fScore = new HashMap<>();

        PriorityQueue<Map> openQueue = new PriorityQueue<>(new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                return fScore.get(o1) - fScore.get(o2);
            }
        });

        gScore.put(currentMap, 0);

        fScore.put(currentMap, currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords));

        openQueue.add(currentMap);
        Set<char[][]> closedSet = new HashSet<>();
        HashMap<Map, Map> cameFrom = new HashMap<>();

        while (openQueue.peek() != null) {
            currentMap = openQueue.poll();

            if (currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords) <= 0) {
                return getPath(cameFrom, currentMap);
            }

            closedSet.add(currentMap.getMap());
            for (Map neighbour : currentMap.getNeighbourMoves()) {

                if (setContains(closedSet, neighbour.getMap())) {
                    continue;
                }

                if (!gScore.containsKey(neighbour)) {
                    gScore.put(neighbour, Integer.MAX_VALUE);
                }


                int weight = gScore.get(currentMap) + 1;

                if (weight >= gScore.get(neighbour)) continue;

                cameFrom.put(neighbour, currentMap);
                gScore.put(neighbour, weight);
                fScore.put(neighbour, weight + neighbour.getManhattanDistance(neighbour.getPlayerCoords(), goalCoords));

                if (!openQueue.contains(neighbour)) {
                    openQueue.add(neighbour);
                }
            }
        }
        return null;
    }

    /**
     * Gets the path given the map cameFrom and the map and the current Map object
     * @param cameFrom The Map that contains all Map objects and there predecessors in the AStar search
     * @param current The current Map object that is the goal state
     * @return The path of Map objects that the AStar search has found
     */

    private List<Map> getPath(HashMap<Map, Map> cameFrom, Map current) {
        List<Map> path = new ArrayList<>();
        path.add(current);
        while (cameFrom.get(current) != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private boolean setContains(Set<char[][]> closedSet, char[][] map) {
        for (char[][] closedMap : closedSet) {
            if (arraysAreEqual(closedMap, map)) {
                return true;
            }
        }
        return false;
    }

    private boolean arraysAreEqual(char[][] a1, char[][] a2) {
        if (a1.length == a2.length && a1[0].length == a2[0].length) {
            for (int i = 0; i < a1.length; i++) {
                for (int j = 0; j < a1[0].length; j++) {
                    if (a1[i][j] != a2[i][j]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
