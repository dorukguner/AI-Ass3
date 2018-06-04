import java.util.*;

public class AStar {

    private Map map;

    public AStar(Map map) {
        this.map = map;
    }

    private HashMap<Map, Integer> gScore;
    private HashMap<Map, Integer> fScore;

    public List<Map> search(final int[] goalCoords) {
        Map currentMap = map;
        gScore = new HashMap<>();
        fScore = new HashMap<>();

        PriorityQueue<Map> openQueue = new PriorityQueue<>(new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                return fScore.get(o1) - fScore.get(o2);
                //return o1.getF(o1.getPlayerCoords(), goalCoords) - o2.getF(o2.getPlayerCoords(), goalCoords);
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

                //neighbour.setMovesMade(currentMap.getMovesMade());

                int weight = gScore.get(currentMap) + 1;

                if (weight >= gScore.get(neighbour)) continue;
                //int weight = currentMap.getMovesMade() + currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords);
                //int neighbourWeight = neighbour.getMovesMade() + neighbour.getManhattanDistance(neighbour.getPlayerCoords(), goalCoords);

                //if (weight > neighbourWeight) continue;

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

    public List<Map> getPath(HashMap<Map, Map> cameFrom, Map current) {
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
