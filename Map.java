import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Map {

    private char[][] map = new char[165][165];      // Assuming a maximum map size of 80 chars, so no matter where the player starts in the map we have enough room to move.
    private AStar AStar = new AStar(this);
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasRaft = false;
    private boolean onRaft = false;
    private boolean hasTreasure = false;

    private HashMap<Character, List<int[]>> toolCoords = new HashMap<>();

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
    private static final char TREASURE = '$';
    private static final char KEY = 'k';
    private static final char DOOR = '-';
    private static final char WATER = '~';
    private static final char UNEXPLORED = '#';

    private int curDir = SOUTH;
    private int iOffset = 0;
    private int jOffset = 0;
    private int[] playerCoords = {map.length / 2 + 2, map.length / 2 + 2};

    private int numStones = 0;


    /**
     * Constructor for Map, we set the start coordinates to be the length of the map/2 to allow
     * for enough room to build the whole map.
     */
    public Map() {
        startCoords[0] = map.length / 2 + 2;
        startCoords[1] = map.length / 2 + 2;
        toolCoords.put(AXE, new ArrayList<>());
        toolCoords.put(KEY, new ArrayList<>());
        toolCoords.put(DOOR, new ArrayList<>());
        toolCoords.put(TREE, new ArrayList<>());
        toolCoords.put(STONE, new ArrayList<>());
        toolCoords.put(TREASURE, new ArrayList<>());
    }

    /**
     * Constructor 2 for Map, we use this when building neighbour maps for searching to pass any important variables along
     * @param toolCoords The tool coordinates of the previous map
     * @param map The char map of the previous map
     * @param hasAxe Whether or not the previous map has an axe
     * @param hasKey Whether or not the previous map has a key
     * @param hasTreasure Whether or not the previous map has the treasure
     * @param hasRaft Whether or not the previous map has a raft
     * @param onRaft Whether or not the previous map has is on a raft
     * @param numStones The number of stones the previous map has
     */
    public Map(HashMap<Character, List<int[]>> toolCoords, char[][] map, boolean hasAxe, boolean hasKey, boolean hasTreasure, boolean hasRaft, boolean onRaft, int numStones) {
        startCoords[0] = map.length / 2 + 2;
        startCoords[1] = map.length / 2 + 2;
        this.toolCoords = toolCoords;
        this.hasAxe = hasAxe;
        this.hasKey = hasKey;
        this.hasTreasure = hasTreasure;
        this.hasRaft = hasRaft;
        this.onRaft = onRaft;
        this.numStones = numStones;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                this.map[i][j] = map[i][j];
            }
        }
    }


    /**
     * Sets the coordinates of the player in the char map that is built by us.
     */
    public void setPlayer() {
        int i = playerCoords[0];
        int j = playerCoords[1];
        char prevChar;
        if (curDir == NORTH && i < map.length - 1) {
            map[i][j] = '^';
            prevChar = map[i + 1][j];
            if (prevChar == '^') {
                map[i + 1][j] = EMPTY;
            }
        } else if (curDir == SOUTH && i > 0) {
            map[i][j] = 'v';
            prevChar = map[i - 1][j];
            if (prevChar == 'v') {
                map[i - 1][j] = EMPTY;
            }
        } else if (curDir == EAST && j > 0) {
            map[i][j] = '>';
            prevChar = map[i][j - 1];
            if (prevChar == '>') {
                map[i][j - 1] = EMPTY;
            }
        } else if (curDir == WEST && j < map.length - 1) {
            map[i][j] = '<';
            prevChar = map[i][j + 1];
            if (prevChar == '<') {
                map[i][j + 1] = EMPTY;
            }
        }
    }

    /**
     * Builds our char map according to the given view
     * @param view The view that is given to us through the socket by the Step
     */

    public void buildMap(char view[][]) {
        int mapI = playerCoords[0] - 2;
        int mapJ = playerCoords[1] - 2;
        if (curDir == NORTH) {
            for (int i = 0; i < view.length && i < map.length - mapI; i++) {
                for (int j = 0; j < view[i].length && j < map[i].length - mapJ; j++) {
                    map[mapI + i][mapJ + j] = view[i][j];
                }
            }
        } else if (curDir == SOUTH) {
            for (int i = 0; i < view.length && i < map.length - mapI; i++) {
                for (int j = view[i].length - 1; j >= 0; j--) {
                    map[mapI + view.length - 1 - i][mapJ + j] = view[i][4 - j];
                }
            }
        } else if (curDir == EAST) {
            for (int i = 0; i < view.length && i < map.length - mapI; i++) {
                for (int j = view[i].length - 1; j >= 0; j--) {
                    map[mapI + j][mapJ + view.length - 1 - i] = view[i][j];
                }
            }
        } else if (curDir == WEST) {
            for (int i = 0; i < view.length && i < map.length - mapI; i++) {
                for (int j = view[i].length - 1; j >= 0; j--) {
                    map[mapI + j][mapJ + i] = view[i][4 - j];
                }
            }
        }
    }

    /**
     * Initialises the map to be unexplored except for the given view, this is used when the game first starts.
     * @param view The view given to us by the socket when the game starts
     */
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

    /**
     * Checks if the mapCoords argument is part of the toolCoordsList List
     * @param toolCoordsList The list of previously seen tool coordinates
     * @param mapCoords The map coordinates currently being checked for
     * @return true if toolCoordsList contains an integer array that matches mapCoords
     *          false otherwise
     */
    private boolean seenToolCoords(List<int[]> toolCoordsList, int[] mapCoords) {
        for (int[] toolCoords : toolCoordsList) {
            if (toolCoords[0] == mapCoords[0] && toolCoords[1] == mapCoords[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the tool coordinates seen on the map, this method is run every time a move is made for accuracy
     */
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

    /**
     * Sets whether or not the player has a certain tool
     * @param view The view used to see if a player is about to pick up a tool
     */
    public void setTools(char[][] view) {
        int i = 1;
        int j = 2;
        char nextTile = view[i][j];
        switch (nextTile) {
            case AXE:
                hasAxe = true;
                break;
            case TREE:
                if (hasAxe) {
                    hasRaft = true;
                }
                break;
            case KEY:
                hasKey = true;
                break;
            case STONE:
                numStones++;
                break;
            case TREASURE:
                hasTreasure = true;
                break;
            case WATER:
                if (numStones > 0) {
                    numStones--;
                } else if (hasRaft) {
                    onRaft = true;
                }
                break;
            case EMPTY:
                if (onRaft) {
                    onRaft = false;
                    hasRaft = false;
                }
                break;
        }
    }

    /**
     * Checked when there are no moves available, returns whether or not the player can move forward
     * @return true if the player can move forward
     *          false otherwise
     */
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
        return nextTile == EMPTY || nextTile == STONE || nextTile == PLACEDSTONE || nextTile == AXE || nextTile == KEY || nextTile == TREASURE
                || ((hasRaft || numStones > 0) && nextTile == WATER);
    }

    /**
     * Checks whether or not the input tile is walkable
     * @param tile The tile being checked
     * @param prevTile The previous tile, used to make sure we don't set unexplored areas as walkable
     * @return True if the tile is able to be walked through
     *          false otherwise
     */

    public boolean tileIsWalkable(char tile, char prevTile) {
        return (prevTile != UNEXPLORED && tile == UNEXPLORED) || tile == EMPTY || tile == STONE || tile == PLACEDSTONE || tile == AXE || tile == KEY || tile == TREASURE
                || (tile == WATER && (onRaft || hasRaft || numStones > 0)) || (tile == TREE && hasAxe) || (tile == DOOR && hasKey);
    }

    /**
     * Sets the player's coordinates
     * @param i The i value of the player's current coordinates
     * @param j The j value of the player's current coordinates
     */

    public void setPlayerCoords(int i, int j) {
        this.playerCoords = new int[]{i, j};
    }

    /**
     * Gets all possible neighbour moves of the current map.
     * @return The Set of all possible neighbour moves of the current map.
     */

    public Set<Map> getNeighbourMoves() {
        int i = playerCoords[0];
        int j = playerCoords[1];
        Set<Map> neighbours = new HashSet<>();
        char prevTile = map[i][j];
        char nextTile;
        Map curNeighbour;
        if (i < map.length - 1) {
            nextTile = map[i + 1][j];
            if (tileIsWalkable(nextTile, prevTile)) {
                curNeighbour = new Map(toolCoords, map, hasAxe, hasKey, hasTreasure, hasRaft, onRaft, numStones);
                curNeighbour.map[i][j] = ' ';
                curNeighbour.setPlayerCoords(i + 1, j);
                curNeighbour.setPlayer();
                neighbours.add(curNeighbour);
                //curNeighbour.printMap();
            }
        }
        if (i > 0) {
            nextTile = map[i - 1][j];
            if (tileIsWalkable(nextTile, prevTile)) {
                curNeighbour = new Map(toolCoords, map, hasAxe, hasKey, hasTreasure, hasRaft, onRaft, numStones);
                curNeighbour.map[i][j] = ' ';
                curNeighbour.setPlayerCoords(i - 1, j);
                curNeighbour.setPlayer();
                neighbours.add(curNeighbour);
                //curNeighbour.printMap();
            }
        }
        if (j < map.length - 1) {
            nextTile = map[i][j + 1];
            if (tileIsWalkable(nextTile, prevTile)) {
                curNeighbour = new Map(toolCoords, map, hasAxe, hasKey, hasTreasure, hasRaft, onRaft, numStones);
                curNeighbour.map[i][j] = ' ';
                curNeighbour.setPlayerCoords(i, j + 1);
                curNeighbour.setPlayer();
                neighbours.add(curNeighbour);
            }
        }
        if (j > 0) {
            nextTile = map[i][j - 1];
            if (tileIsWalkable(nextTile, prevTile)) {
                curNeighbour = new Map(toolCoords, map, hasAxe, hasKey, hasTreasure, hasRaft, onRaft, numStones);
                curNeighbour.map[i][j] = ' ';
                curNeighbour.setPlayerCoords(i, j - 1);
                curNeighbour.setPlayer();
                neighbours.add(curNeighbour);
            }
        }
        return neighbours;
    }

    /**
     * Checks whether or not the end coordinates can be reached from the start coordinates using a spread method
     * @param start The starting coordinates
     * @param end The end coordinates
     * @return true if the end coordinates can be reached from the start coordinates
     *          false if otherwise
     */

    public boolean canReach(int[] start, int[] end) {
        List<int[]> queue = new ArrayList<>();
        Set<int[]> connected = new HashSet<>();
        char prevTile = ' ';

        queue.add(start);

        while (!queue.isEmpty()) {
            int[] cur = queue.remove(0);

            char nextTile = map[cur[0]][cur[1]];

            if (!setContainsArray(connected, cur)) {
                if (!arraysAreEqual(start, cur) && !tileIsWalkable(nextTile, prevTile))
                    continue;

                connected.add(cur);

                int i = cur[0];
                int j = cur[1];
                prevTile = map[i][j];
                int nextI = i + 1;
                int nextJ = j;
                if (nextI < map.length && tileIsWalkable(map[nextI][nextJ], prevTile)) {
                    queue.add(new int[]{nextI, nextJ});
                }
                nextI = i - 1;
                if (nextI >= 0 && tileIsWalkable(map[nextI][nextJ], prevTile)) {
                    queue.add(new int[]{nextI, nextJ});
                }
                nextI = i;
                nextJ = j + 1;
                if (nextJ < map.length && tileIsWalkable(map[nextI][nextJ], prevTile)) {
                    queue.add(new int[]{nextI, nextJ});
                }
                nextJ = j - 1;
                if (nextJ >= 0 && tileIsWalkable(map[nextI][nextJ], prevTile)) {
                    queue.add(new int[]{nextI, nextJ});
                }
            }
            prevTile = nextTile;
        }
        return setContainsArray(connected, end);
    }

    /**
     * Returns whether or not an integer array set contains an integer array
     * @param set The integer array set that is being searched
     * @param array The array that is being searched for
     * @return true if the set contains the array
     *          false if otherwise
     */

    private boolean setContainsArray(Set<int[]> set, int[] array) {
        for (int[] a : set) {
            if (a.length == array.length && a[0] == array[0] && a[1] == array[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the best move given the logic described in Agent.java
     * @return The best possible move as a char
     */

    public char getBestMove() {
        List<Map> bestMoves = null;
        char bestTool = '.';
        int[] bestToolCoords = {-1, -1};
        if (hasTreasure && canReach(playerCoords, startCoords)) {
            bestMoves = AStar.search(startCoords);
        }
        if (bestMoves == null) {
            for (char tool : toolCoords.keySet()) {
                if (!toolCoords.get(tool).isEmpty()) {
                    bestToolCoords = toolCoords.get(tool).get(0);
                    if (canReach(playerCoords, bestToolCoords)) {
                        bestMoves = AStar.search(bestToolCoords);
                        bestTool = tool;
                        break;
                    }
                }
            }
        }
        if (bestMoves == null && !hasTreasure) {
            bestMoves = getBestUnexploredCoords();
        }
        if (bestMoves != null && !bestMoves.isEmpty()) {
            Map bestMap = bestMoves.get(0);
            int[] bestMoveCoords = bestMap.getPlayerCoords();
            int direction = getDirection(bestMap.getPlayerCoords());
            int directionDiff = curDir - direction;
            if (directionDiff == 0) {
                char nextTile = map[bestMoveCoords[0]][bestMoveCoords[1]];
                if (nextTile == TREE && hasAxe) {
                    return 'c';
                } else if (nextTile == DOOR && hasKey) {
                    return 'u';
                }
                if (arraysAreEqual(bestMoveCoords, bestToolCoords)) {
                    toolCoords.get(bestTool).remove(0);
                }
                return 'f';
            } else if (directionDiff == -1 || directionDiff == -2 || directionDiff == 3) {
                return 'r';
            } else if (directionDiff == 1 || directionDiff == 2 || directionDiff == -3) {
                return 'l';
            }
        }
        if (canMove()) {
            return 'f';
        } else {
            int i = ThreadLocalRandom.current().nextInt(0,2);
            if (i == 0) {
                return 'r';
            }
            return 'l';
        }
    }

    /**
     * Checks if two arrays are equal
     * @param a1 The first array
     * @param a2 The second array
     * @return true if arrays are equal
     *          false if otherwise
     */

    private boolean arraysAreEqual(int[] a1, int[] a2) {
        if (a1.length != a2.length) return false;

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) return false;
        }
        return true;
    }

    /**
     * Returns the best unexplored coordinates by using a modified flood fill algorithm
     * @return A List of maps that contains the moves required to get to the best unexplored coordinates
     */

    public List<Map> getBestUnexploredCoords() {
        int curLength = 1;
        int odd = 0;
        while (curLength < map.length - Math.max(playerCoords[0], playerCoords[1])) {
            int i = playerCoords[0];
            while (i <= playerCoords[0] + curLength) {
                for (int j = playerCoords[1] - curLength; j <= playerCoords[1] + curLength; j++) {
                    if (map[i][j] == UNEXPLORED && canReach(playerCoords, new int[]{i, j})) {
                        List<Map> bestMoves = AStar.search(new int[]{i, j});
                        if (bestMoves != null) {
                            return bestMoves;
                        }
                    }
                }
                if (odd == 0) {
                    i = playerCoords[0] + curLength;
                    odd = 1;
                } else if (odd == 1) {
                    i = playerCoords[0] - curLength;
                    odd = 2;
                } else {
                    i = playerCoords[0] + curLength + 1;
                    odd = 0;
                }
            }
            curLength++;
        }
        return null;
    }

    /**
     * Gets the player's current coordinates
     * @return
     */

    public int[] getPlayerCoords() {
        return playerCoords;
    }


    /**
     * Gets the direction that the player is required to be in to reach the input coordinates
     * @param coords The coordinates trying to be reached
     * @return An integer that is hard coded to a certain direction.
     */

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
        if (myJ == j) {
            if (myI < i) {
                return SOUTH;
            }
            return NORTH;
        } else {
            if (myJ < j) {
                return EAST;
            }
            return WEST;
        }
    }

    /**
     * Returns whether or not there is a tree in front of the player that we can chop
     * @param view The view that is used to determine the next tile
     * @return true if the player has an axe and there is a tree in front of the player
     *          false if otherwise
     */

    public boolean canChop(char[][] view) {
        int i = 1;
        int j = 2;
        return hasAxe && view[i][j] == TREE;
    }

    /**
     * Getting for this Map object's char map
     * @return
     */

    public char[][] getMap() {
        return map;
    }

    /**
     * Sets hasRaft to the input boolean
     * @param hasRaft
     */

    public void setHasRaft(boolean hasRaft) {
        this.hasRaft = hasRaft;
    }

    /**
     * Getter for curDir
     * @return The current direction of the player
     */

    public int getCurDir() {
        return curDir;
    }

    /**
     * Setter for curDir
     * @param curDir The direction that the player is to be set
     */

    public void setCurDir(int curDir) {
        this.curDir = curDir;
    }

    /**
     * Getter for iOffset
     * @return The current value for iOffset
     */

    public int getiOffset() {
        return iOffset;
    }

    /**
     * Setter for iOffset
     * @param iOffset the value at which iOffset should be set to
     */

    public void setiOffset(int iOffset) {
        this.iOffset = iOffset;
    }

    /**
     * Getter for jOffset
     * @return The current value for jOffset
     */

    public int getjOffset() {
        return jOffset;
    }

    /**
     * Setter for jOffset
     * @param jOffset the value at which jOffset should be set to
     */

    public void setjOffset(int jOffset) {
        this.jOffset = jOffset;
    }

    /**
     * Prints the current char map of this Map object, used for debugging
     */

    public void printMap() {
        for (int i = 0; i < map.length; i++) {
            for(int j = 0; j < map.length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Gets the manhattan distance between the start coordinates and the goal coordinates
     * @param start The starting coordinates
     * @param goal The goal coordiantes
     * @return The integer value of the manhattan distance between start and goal
     */

    public int getManhattanDistance(int[] start, int[] goal) {
        return Math.abs(start[0] - goal[0]) + Math.abs(start[1] - goal[1]);
    }

}
