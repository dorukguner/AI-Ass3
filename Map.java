import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Map {

    private char[][] map = new char[40][40];      // CHANGE THIS TO 160 is used so that no matter where we start in the map, we can always fit every single possible map combination
    private Pathing pathing = new Pathing();
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasTreasure = false;
    private boolean hasRaft = false;
    private boolean onRaft = false;
    private boolean hasGold = false;
    private boolean offMap = false;

    private HashMap<Character, List<int[]>> toolCoords = new HashMap<>();

    private int nrows;     // number of rows in environment
    private int startRow, startCol; // initial row and column

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

    private int curDir = SOUTH;
    private int iOffset = 0;
    private int jOffset = 0;

    private boolean gameWon = false;
    private boolean gameLost = false;

    private int numDynamites = 0;
    private int numStones = 0;

    public Map() {
        toolCoords.put(GOLD, new ArrayList<>());
        toolCoords.put(AXE, new ArrayList<>());
        toolCoords.put(KEY, new ArrayList<>());
        toolCoords.put(TREE, new ArrayList<>());
        toolCoords.put(STONE, new ArrayList<>());
    }

    public void setPlayer() {
        int i = map.length / 2 + iOffset + 2;
        int j = map.length / 2 + jOffset + 2;
        char prevChar;
        if (curDir == NORTH) {
            map[i][j] = '^';
            prevChar = map[i + 1][j];
            if (prevChar == '^') {
                map[i + 1][j] = EMPTY;
            }
        } else if (curDir == SOUTH) {
            map[i][j] = 'v';
            prevChar = map[i - 1][j];
            if (prevChar == 'v') {
                map[i - 1][j] = EMPTY;
            }
        } else if (curDir == EAST) {
            map[i][j] = '>';
            prevChar = map[i][j - 1];
            if (prevChar == '>') {
                map[i][j - 1] = EMPTY;
            }
        } else if (curDir == WEST) {
            map[i][j] = '<';
            prevChar = map[i][j + 1];
            if (prevChar == '<') {
                map[i][j + 1] = EMPTY;
            }
        }
    }

    public void buildMap(char view[][]) {
        int mapI = map.length / 2 + iOffset;
        int mapJ = map.length / 2 + jOffset;
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
        startRow = map.length / 2;
        startCol = map[0].length / 2;
        int mapI = map.length / 2;
        int mapJ = map.length / 2;
        int initialMapJ = mapJ;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = '#';
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

    public void setToolCoords() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                int[] coords = new int[]{i, j};
                if (toolCoords.containsKey(map[i][j])/* && toolCoords.get(map[i][j]).contains(coords)*/) {
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
                System.out.println(toolCoords.get(KEY).size());
                toolCoords.get(KEY).remove(0);
                System.out.println(toolCoords.get(KEY).size());
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
        int i = map.length / 2 + iOffset + 2;
        int j = map.length / 2 + jOffset + 2;
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
        return nextTile == EMPTY || nextTile == STONE || nextTile == PLACEDSTONE || nextTile == AXE || nextTile == KEY || nextTile == GOLD
                || ((hasRaft || numStones > 0) && nextTile == WATER);
    }

    public char getBestMove() {
        for (char tool : toolCoords.keySet()) {
            if (!toolCoords.get(tool).isEmpty()) {
                int direction = getDirection(toolCoords.get(tool).get(0));
                if (direction == curDir) {
                    return 'f';
                } else if (Math.abs(direction - curDir) == 1) {
                    return 'r';
                } else if (Math.abs(direction - curDir) == 2) {
                    return 'r';
                } else {
                    return 'l';
                }
            }
        }
        if (canMove()) {
            return 'f';
        } else {
            return 'r';
        }
    }

    public int[] getPlayerCoords() {
        int i = map.length / 2 + iOffset + 2;
        int j = map.length / 2 + jOffset + 2;
        return new int[]{i, j};
    }

    public int getDirection(int[] coords) {
        int myI = getPlayerCoords()[0];
        int myJ = getPlayerCoords()[1];
        int i = coords[0];
        int j = coords[1];
        if (myJ == j) {         //NORTH OR SOUTH
            if (myI < i) {
                return SOUTH;
            }
            return NORTH;
        } else {
            if (myJ < i) {
                return EAST;
            }
            return WEST;
        }
    }

    public char[][] getMap() {
        return map;
    }

    public void setMap(char[][] map) {
        this.map = map;
    }

    public boolean isHasAxe() {
        return hasAxe;
    }

    public void setHasAxe(boolean hasAxe) {
        this.hasAxe = hasAxe;
    }

    public boolean isHasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public boolean isHasTreasure() {
        return hasTreasure;
    }

    public void setHasTreasure(boolean hasTreasure) {
        this.hasTreasure = hasTreasure;
    }

    public boolean isHasRaft() {
        return hasRaft;
    }

    public void setHasRaft(boolean hasRaft) {
        this.hasRaft = hasRaft;
    }

    public boolean isOnRaft() {
        return onRaft;
    }

    public void setOnRaft(boolean onRaft) {
        this.onRaft = onRaft;
    }

    public boolean isHasGold() {
        return hasGold;
    }

    public void setHasGold(boolean hasGold) {
        this.hasGold = hasGold;
    }

    public boolean isOffMap() {
        return offMap;
    }

    public void setOffMap(boolean offMap) {
        this.offMap = offMap;
    }

    public static int getEAST() {
        return EAST;
    }

    public static int getNORTH() {
        return NORTH;
    }

    public static int getWEST() {
        return WEST;
    }

    public static int getSOUTH() {
        return SOUTH;
    }

    public static char getTREE() {
        return TREE;
    }

    public static char getAXE() {
        return AXE;
    }

    public static char getSTONE() {
        return STONE;
    }

    public static char getPLACEDSTONE() {
        return PLACEDSTONE;
    }

    public static char getWALL() {
        return WALL;
    }

    public static char getGOLD() {
        return GOLD;
    }

    public static char getKEY() {
        return KEY;
    }

    public static char getDOOR() {
        return DOOR;
    }

    public static char getWATER() {
        return WATER;
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

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }

    public int getNumDynamites() {
        return numDynamites;
    }

    public void setNumDynamites(int numDynamites) {
        this.numDynamites = numDynamites;
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
}
