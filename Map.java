import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Map {

    private char[][] map = new char[40][40];      // CHANGE THIS TO 160 is used so that no matter where we start in the map, we can always fit every single possible map combination
    private Pathing pathing = new Pathing(this);
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasTreasure = false;
    private boolean hasRaft = false;
    private boolean onRaft = false;
    private boolean hasGold = false;
    private boolean offMap = false;
    private int movesMade = 0;

    private HashMap<Character, List<int[]>> toolCoords = new HashMap<>();

    private int nrows;     // number of rows in environment
    private int[] startCoords = {-1, -1}; // initial row and column

    private final static int NORTH = 0;
    private final static int EAST = 1;
    private final static int SOUTH = 2;
    private final static int WEST = 3;

    private static final char EMPTY = ' ';
    private static final char TREE = 'T';
    private static final char AXE = 'a';
    private static final char STONE = 'o';
    private static final char PLACEDSTONE = 'O';
    private static final char WALL = '*';
    private static final char GOLD = '$';
    private static final char KEY = 'k';
    private static final char DOOR = '-';
    private static final char WATER = '~';
    private static final char UNEXPLORED = '#';

    private int curDir = SOUTH;
    private int iOffset = 0;
    private int jOffset = 0;
    private int[] playerCoords = {map.length / 2 + 2, map.length / 2 + 2};

    private int numStones = 0;

    public Map() {
        startCoords[0] = map.length / 2 + 2;
        startCoords[1] = map.length / 2 + 2;
        toolCoords.put(GOLD, new ArrayList<>());
        toolCoords.put(AXE, new ArrayList<>());
        toolCoords.put(KEY, new ArrayList<>());
        toolCoords.put(DOOR, new ArrayList<>());
        toolCoords.put(TREE, new ArrayList<>());
        toolCoords.put(STONE, new ArrayList<>());
    }

    public Map(HashMap<Character, List<int[]>> toolCoords, char[][] map) {
        startCoords[0] = map.length / 2 + 2;
        startCoords[1] = map.length / 2 + 2;
        this.toolCoords = toolCoords;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                this.map[i][j] = map[i][j];
            }
        }
    }

    public void setPlayer() {
        int i = playerCoords[0];
        int j = playerCoords[1];
        char prevChar;
        if (curDir == NORTH) {
            map[i][j] = '^';
            /*prevChar = map[i + 1][j];
            if (prevChar == '^') {
                map[i + 1][j] = EMPTY;
            }*/
        } else if (curDir == SOUTH) {
            map[i][j] = 'v';
            /*prevChar = map[i - 1][j];
            if (prevChar == 'v') {
                map[i - 1][j] = EMPTY;
            }*/
        } else if (curDir == EAST) {
            map[i][j] = '>';
            /*prevChar = map[i][j - 1];
            if (prevChar == '>') {
                map[i][j - 1] = EMPTY;
            }*/
        } else if (curDir == WEST) {
            map[i][j] = '<';
            /*prevChar = map[i][j + 1];
            if (prevChar == '<') {
                map[i][j + 1] = EMPTY;
            }*/
        }
    }

    public void buildMap(char view[][]) {
        int mapI = playerCoords[0] - 2;
        int mapJ = playerCoords[1] - 2;
        if (curDir == NORTH) {
            for (int i = 0; i < view.length; i++) {
                for (int j = 0; j < view.length; j++) {
                    map[mapI + i][mapJ + j] = view[i][j];
                }
            }
        } else if (curDir == SOUTH) {
            for (int i = 0; i < view.length; i++) {
                for (int j = view.length - 1; j >= 0; j--) {
                    map[mapI + view.length - 1 - i][mapJ + j] = view[i][4 - j];
                }
            }
        } else if (curDir == EAST) {
            for (int i = 0; i < view.length; i++) {
                for (int j = view.length - 1; j >= 0; j--) {
                    map[mapI + j][mapJ + view.length - 1 - i] = view[i][j];
                }
            }
        } else if (curDir == WEST) {
            for (int i = 0; i < view.length; i++) {
                for (int j = view.length - 1; j >= 0; j--) {
                    map[mapI + j][mapJ + i] = view[i][4 - j];
                }
            }
        }
    }

    public boolean canChop(char[][] view) {
        int i = 1;
        int j = 2;
        char nextTile = view[i][j];
        return hasAxe && nextTile == TREE;
    }

    public boolean canUnlock(char[][] view) {
        int i = 1;
        int j = 2;
        char nextTile = view[i][j];
        return hasKey && nextTile == DOOR;
    }

    public void initialiseMap(char[][] view) {
        int mapI = map.length / 2;
        int mapJ = map.length / 2;
        int initialMapJ = mapJ;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = UNEXPLORED;
            }
        }
        for (int i = view.length - 1; i >= 0; i--) {
            for (int j = view[0].length - 1; j >= 0; j--) {
                if ((i == 2) && (j == 2)) {
                    map[mapI][mapJ] = 'v';
                } else {
                    map[mapI][mapJ] = view[i][j];
                }
                mapJ++;
            }
            mapI++;
            mapJ = initialMapJ;
        }
    }

    private boolean seenToolCoords(List<int[]> toolCoordsList, int[] mapCoords) {
        for (int[] toolCoords : toolCoordsList) {
            if (toolCoords[0] == mapCoords[0] && toolCoords[1] == mapCoords[1]) {
                return true;
            }
        }
        return false;
    }

    public void setToolCoords() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                int[] coords = new int[]{i, j};
                if (toolCoords.containsKey(map[i][j]) && !seenToolCoords(toolCoords.get(map[i][j]), coords)) {
                    toolCoords.get(map[i][j]).add(coords);
                }
            }
        }
    }

    public void setTools(char[][] view) {
        int i = 1;
        int j = 2;
        char nextTile = view[i][j];
        switch (nextTile) {
            case AXE:
                System.out.println("HAS AXE");
                hasAxe = true;
                break;
            case TREE:
                if (hasAxe) {
                    System.out.println("HAS RAFT");
                    hasRaft = true;
                }
                break;
            case KEY:
                System.out.println("HAS KEY");
                hasKey = true;
                break;
            case STONE:
                System.out.println("HAS STONE");
                numStones++;
                break;
            case GOLD:
                System.out.println("HAS GOLD");
                hasGold = true;
                break;
            case WATER:
                if (numStones > 0) {
                    System.out.println("USED A STONE");
                    numStones--;
                } else if (hasRaft) {
                    System.out.println("USED A RAFT");
                    onRaft = true;
                }
                break;
            case EMPTY:
                if (onRaft) {
                    System.out.println("GETTING OFF RAFT");
                    onRaft = false;
                    hasRaft = false;
                }
                break;
        }
    }

    private char getReachableObject() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {

            }
        }
        return ' ';
    }

    public boolean canMove() {
        int i = playerCoords[0];
        int j = playerCoords[1];
        char nextTile = map[i][j];
        if (curDir == NORTH) {
            nextTile = map[i - 1][j];
        } else if (curDir == SOUTH) {
            nextTile = map[i + 1][j];
        } else if (curDir == EAST) {
            nextTile = map[i][j + 1];
        } else if (curDir == WEST) {
            nextTile = map[i][j - 1];
        }
        //System.out.println("Next tile is " + nextTile);
        return nextTile == EMPTY || nextTile == STONE || nextTile == PLACEDSTONE || nextTile == AXE || nextTile == KEY || nextTile == GOLD
                || ((hasRaft || numStones > 0) && nextTile == WATER);
    }

    public boolean tileIsWalkable(char tile) {
        return tile == EMPTY || tile == STONE || tile == PLACEDSTONE || tile == AXE || tile == KEY || tile == GOLD
                || ((onRaft || hasRaft || numStones > 0) && tile == WATER) || (hasAxe && tile == TREE) || (hasKey && tile == DOOR);
    }

    public void setPlayerCoords(int i, int j) {
        this.playerCoords = new int[]{i, j};
    }

    public Set<Map> getNeighbourMoves() {
        int i = playerCoords[0];
        int j = playerCoords[1];
        Set<Map> neighbours = new HashSet<>();
        char nextTile = map[i + 1][j];
        Map curNeighbour;
        if (tileIsWalkable(nextTile)) {
            curNeighbour = new Map(toolCoords, map);
            curNeighbour.map[i][j] = ' ';
            curNeighbour.setPlayerCoords(i + 1, j);
            curNeighbour.setPlayer();
            neighbours.add(curNeighbour);
            //curNeighbour.printMap();
        }
        nextTile = map[i - 1][j];
        if (tileIsWalkable(nextTile)) {
            curNeighbour = new Map(toolCoords, map);
            curNeighbour.map[i][j] = ' ';
            curNeighbour.setPlayerCoords(i - 1, j);
            curNeighbour.setPlayer();
            neighbours.add(curNeighbour);
            //curNeighbour.printMap();
        }
        nextTile = map[i][j + 1];
        if (tileIsWalkable(nextTile)) {
            curNeighbour = new Map(toolCoords, map);
            curNeighbour.map[i][j] = ' ';
            curNeighbour.setPlayerCoords(i, j + 1);
            curNeighbour.setPlayer();
            neighbours.add(curNeighbour);
        }
        nextTile = map[i][j - 1];
        if (tileIsWalkable(nextTile)) {
            curNeighbour = new Map(toolCoords, map);
            curNeighbour.map[i][j] = ' ';
            curNeighbour.setPlayerCoords(i, j - 1);
            curNeighbour.setPlayer();
            neighbours.add(curNeighbour);
        }
        return neighbours;
    }

    private boolean hasCorrectTools(char tile) {
        if (tile == TREE) return hasAxe;
        if (tile == DOOR) return hasKey;
        return true;
    }

    public char getBestMove() {
        Map bestMap = null;
        char bestTool = '.';
        int[] bestToolCoords = {-1, -1};
        if (hasGold) {
            if (pathing.search(startCoords, true) != null) {
                bestMap = pathing.search(startCoords, false);
            }
        }
        if (bestMap == null) {
            for (char tool : toolCoords.keySet()) {
                //System.out.println("Searching for tool: " + tool + ": " + toolCoords.get(tool).size());
                if (!toolCoords.get(tool).isEmpty()) {
                    bestToolCoords = toolCoords.get(tool).get(0);
                    if (hasCorrectTools(map[bestToolCoords[0]][bestToolCoords[1]]) && pathing.search(bestToolCoords, true) != null) {
                        bestMap = pathing.search(bestToolCoords, false);
                        bestTool = tool;
                        break;
                    }
                }
            }
        }
        if (bestMap == null) {
            int[] bestUnexploredCoords = getBestUnexploredCoords();
            if (bestUnexploredCoords != null) {
                bestMap = pathing.search(bestUnexploredCoords, false);
            }
        }
        if (bestMap != null) {
            int[] bestMoveCoords = bestMap.getPlayerCoords();
            System.out.println("Player coords: " + playerCoords[0] + ", " + playerCoords[1]);
            System.out.println("Move coords: " + bestMoveCoords[0] + ", " + bestMoveCoords[1]);
            if (arraysAreEqual(bestMoveCoords, bestToolCoords)) {
                System.out.println("Removing tool from coords");
                toolCoords.get(bestTool).remove(0);
            }
            int direction = getDirection(bestMap.getPlayerCoords());
            int directionDiff = curDir - direction;
            if (directionDiff == 0) {
                char nextTile = map[bestMoveCoords[0]][bestMoveCoords[1]];
                if (nextTile == TREE) {
                    return 'c';
                } else if (nextTile == DOOR) {
                    return 'u';
                }
                return 'f';
            } else if (directionDiff == -1 || directionDiff == -2 || directionDiff == 3) {
                return 'r';
            } else if (directionDiff == 1 || directionDiff == 2 ||  directionDiff == -3) {
                return 'l';
            }
        }
        /*if (canMove()) {
            return 'f';
        } else {
            return 'r';
        }*/
        System.out.println("No moves");
        return ' ';
    }

    private boolean arraysAreEqual(int[] a1, int[] a2) {
        if (a1.length != a2.length) return false;

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) return false;
        }
        return true;
    }

    public int[] getBestUnexploredCoords() {
        int curLength = 1;
        while (curLength < map.length - Math.max(playerCoords[0], playerCoords[1])) {
            for (int i = playerCoords[0] - curLength; i <= playerCoords[0] + curLength; i++) {
                for (int j = playerCoords[1] - curLength; j <= playerCoords[1] + curLength; j++) {
                    if (map[i][j] == UNEXPLORED && pathing.search(new int[]{i, j}, true) != null) {
                        return new int[]{i, j};
                    }
                }
            }
            curLength++;
        }
        return null;
    }

    public int[] getPlayerCoords() {
        return playerCoords;
    }

    public int getDirection(int[] coords) {
        int myI = playerCoords[0];
        int myJ = playerCoords[1];
        int i = coords[0];
        int j = coords[1];
        if (myI - i == 1) {
            return NORTH;
        } else if (i - myI == 1) {
            return SOUTH;
        } else if (myJ - j == 1) {
            return WEST;
        } else if (j - myJ == 1) {
            return EAST;
        }
        if (myJ == j) {         //NORTH OR SOUTH
            if (myI < i) {
                System.out.println("South");
                return SOUTH;
            }
            System.out.println("North");
            return NORTH;
        } else {
            if (myJ < j) {
                System.out.println("East");
                return EAST;
            }
            System.out.println("West");
            return WEST;
        }
    }

    public char[][] getMap() {
        return map;
    }

    public void setMap(char[][] map) {
        this.map = map;
    }



    public void setHasRaft(boolean hasRaft) {
        this.hasRaft = hasRaft;
    }

    public boolean isOnRaft() {
        return onRaft;
    }


    public int getCurDir() {
        return curDir;
    }

    public void setCurDir(int curDir) {
        this.curDir = curDir;
    }

    public int getiOffset() {
        return iOffset;
    }

    public void setiOffset(int iOffset) {
        this.iOffset = iOffset;
    }

    public int getjOffset() {
        return jOffset;
    }

    public void setjOffset(int jOffset) {
        this.jOffset = jOffset;
    }

    public int getNumStones() {
        return numStones;
    }

    public void setNumStones(int numStones) {
        this.numStones = numStones;
    }

    public void printMap() {
        for (int i = 0; i < map.length; i++) {
            for(int j = 0; j < map.length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    public int getMovesMade() {
        return movesMade;
    }

    public void setMovesMade(int movesMade) {
        this.movesMade = movesMade;
    }

    public int getManhattanDistance(int[] start, int[] goal) {
        return Math.abs(start[0] - goal[0]) + Math.abs(start[1] - goal[1]);
    }

    public int getF(int[] start, int[] goal) {
        return movesMade + getManhattanDistance(start, goal);
    }
}
