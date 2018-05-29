import java.util.*;
import java.io.*;
import java.net.*;

public class Agent {

    private final static int EAST = 0;
    private final static int NORTH = 1;
    private final static int WEST = 2;
    private final static int SOUTH = 3;

    private static final char TREE = 'T';
    private static final char AXE = 'a';
    private static final char STONE = 'o';
    private static final char PLACEDSTONE = 'O';
    private static final char WALL = '*';
    private static final char GOLD = '$';
    private static final char KEY = 'k';
    private static final char DOOR = '_';
    private static final char WATER = '~';

    private char[][] map = new char[80][80];      // 160 is used so that no matter where we start in the map, we can always fit every single possible map combination

    private int nrows;     // number of rows in environment
    private int startRow, startCol; // initial row and column

    // current row, column and direction of agent
    private int row, col, dirn;

    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasTreasure = false;
    private boolean hasRaft = false;
    private boolean onRaft = false;
    private boolean offMap = false;

    private boolean gameWon = false;
    private boolean gameLost = false;

    private int numDynamites = 0;
    private int numStones = 0;

    public char get_action(char view[][]) {


        int ch = 0;

        System.out.print("Enter Action(s): ");

        try {

            while (ch != -1) {
                // read character from keyboard
                ch = System.in.read();

                switch (ch) { // if character is a valid action, return it
                    case 'F':
                    case 'L':
                    case 'R':
                    case 'C':
                    case 'U':
                    case 'f':
                    case 'l':
                    case 'r':
                    case 'c':
                    case 'u':
                        buildMap(view);
                        printMap();
                        return ((char) ch);
                }
            }
        } catch (IOException e) {
            System.out.println("IO error:" + e);
        }

        return 0;
    }

    private void printMap() {
        for (int i = 0; i < map.length; i++) {
            for(int j = 0; j < map.length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    void print_view(char view[][]) {
        int i, j;

        System.out.println("\n+-----+");
        for (i = 0; i < 5; i++) {
            System.out.print("|");
            for (j = 0; j < 5; j++) {
                if ((i == 2) && (j == 2)) {
                    System.out.print('^');
                } else {
                    System.out.print(view[i][j]);
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    private void buildMap(char view[][]) {
        int mapI = map.length / 2 - 2;
        int mapJ = map.length / 2 - 2;
        int initialMapJ = mapJ;
        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view[0].length; j++) {
                if ((i == 2) && (j == 2)) {
                    map[mapI][mapJ] = '^';
                } else {
                    map[mapI][mapJ] = view[i][j];
                }
                mapJ++;
            }
            mapI++;
            mapJ = initialMapJ;
        }
    }

    private void initialiseMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = '#';
            }
        }
    }

    public static void main(String[] args) {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        Agent agent = new Agent();
        char view[][] = new char[5][5];
        char action = 'F';
        int port;
        int ch;
        int i, j;
        agent.initialiseMap();

        if (args.length < 2) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt(args[1]);

        try { // open socket to Game Engine
            socket = new Socket("localhost", port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Could not bind to port: " + port);
            System.exit(-1);
        }

        try { // scan 5-by-5 wintow around current location
            while (true) {
                for (i = 0; i < 5; i++) {
                    for (j = 0; j < 5; j++) {
                        if (!((i == 2) && (j == 2))) {
                            ch = in.read();
                            if (ch == -1) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                action = agent.get_action(view);
                out.write(action);
            }
        } catch (IOException e) {
            System.out.println("Lost connection to port: " + port);
            System.exit(-1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
