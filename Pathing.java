import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Pathing {

    private char[][] map;

    public Pathing() {

    }

    public void setMap(char[][] map) {
        this.map = map;
    }

    public int getManhattanDistance(int i0, int j0, int i1, int j1) {
        return Math.abs(i0 - i1) + Math.abs(j0 - j1);
    }

    /*public boolean astar() {
        char[][] currentMap = map;
        PriorityQueue<Map> openQueue = new PriorityQueue<>(new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                return o1.get
            }
        });
        openQueue.add(currentMap);
        Set<int[][]> closedSet = new HashSet<>();

        while (!openQueue.isEmpty() && openQueue.size() < 3000) {
            currentMap = openQueue.poll();
            if (currentMap.getAmountOfCarsToMove() <= 0) {
                map.setCompletedMap(currentMap.getMap());

            }

            closedSet.add(currentMap.getMap());

            for (int[][] nextMap : currentMap.getAllPossibleMoves()) {
                BitMap nextBitMap = new BitMap(nextMap);
                nextBitMap.setMovesMade(currentMap.getMovesMade() + 1);

                if (setContains(closedSet, nextBitMap.getMap())) {
                    continue;
                }

                //if (greedy) {
                if (nextBitMap.getAmountOfCarsToMove() > currentMap.getAmountOfCarsToMove())
                    continue;
                // } else {
                //   if (nextBitMap.getF() > currentMap.getF())
                //        continue;
                // }

                if (!openQueue.contains(nextBitMap)) {
                    openQueue.add(nextBitMap);
                }

            }
        }
        return false;
    }*/

    /**
     * Checks if a given set of bitmaps contains a given bitmap
     * @param closedSet
     * @param map
     * @return true if set contains bitmap
     *          false if otherwise
     */
    private boolean setContains(Set<int[][]> closedSet, int[][] map) {
        for (int[][] closedMap : closedSet) {
            if (arraysAreEqual(closedMap, map)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if 2 bitmaps are equal
     * @param a1
     * @param a2
     * @return true if bitmaps are equal
     *          false if otherwise
     */
    private boolean arraysAreEqual(int[][] a1, int[][] a2) {
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
