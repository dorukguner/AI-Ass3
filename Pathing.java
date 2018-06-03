import java.util.*;

public class Pathing {

    private Map map;

    public Pathing(Map map) {
        this.map = map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map search(final int[] goalCoords, boolean complete) {
        Map currentMap = map;
        Map bestMap = map;
        int i = 0;

        PriorityQueue<Map> openQueue = new PriorityQueue<>(new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                if (complete) {
                    return o1.getManhattanDistance(o1.getPlayerCoords(), goalCoords) - o2.getManhattanDistance(o1.getPlayerCoords(), goalCoords);
                }
                return o1.getF(o1.getPlayerCoords(), goalCoords) - o2.getF(o1.getPlayerCoords(), goalCoords);
            }
        });
        openQueue.add(currentMap);
        Set<char[][]> closedSet = new HashSet<>();
        List<Character> path = new ArrayList<>();

        while (openQueue.peek() != null) {
            currentMap = openQueue.poll();

            if (complete) {
                //System.out.println(currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords));
                //System.out.println(map.getMap()[currentMap.getPlayerCoords()[0]][currentMap.getPlayerCoords()[1]]);
                if (currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords) <= 1) {
                    return currentMap;
                }
            } else {
                if (map.tileIsWalkable(map.getMap()[currentMap.getPlayerCoords()[0]][currentMap.getPlayerCoords()[1]]) && currentMap.getManhattanDistance(currentMap.getPlayerCoords(), goalCoords) < map.getManhattanDistance(map.getPlayerCoords(), goalCoords)) {
                    //System.out.println("AYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY " + currentMap.getPlayerCoords()[0] + ", " + currentMap.getPlayerCoords()[1]);
                    return currentMap;
                }
            }

            closedSet.add(currentMap.getMap());
            for (Map neighbour : currentMap.getNeighbourMoves()) {
                i++;
                //neighbour.printMap();
                //neighbour.setMap(neighbour.getMap());

                if (setContains(closedSet, neighbour.getMap())) {
                    //System.out.println("Closed set contains neighbour");
                    continue;
                }

                neighbour.setMovesMade(currentMap.getMovesMade() + 1);
                if (!openQueue.contains(neighbour)) {
                    openQueue.add(neighbour);
                }

                int weight = currentMap.getMovesMade() + neighbour.getManhattanDistance(currentMap.getPlayerCoords(), neighbour.getPlayerCoords());

                if (weight > neighbour.getMovesMade()) continue;

                bestMap = neighbour;


            }
        }
        return null;
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
