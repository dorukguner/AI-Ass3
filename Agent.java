import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class Agent {

    private Map map = new Map();

    private final static int NORTH = 0;
    private final static int EAST = 1;
    private final static int SOUTH = 2;
    private final static int WEST = 3;

    // current row, column and direction of agent
    private int row, col, dirn;


    public char get_action(char view[][]) throws InterruptedException {


        int ch = 0;

        //System.out.print("Enter Action(s): ");
        //try {

            while (ch != -1) {
                // read character from keyboard
                //ch = System.in.read();
                //if (ch == 'a') {
                Thread.sleep(100);
                    ch = map.getBestMove();
                //}
                //system.out.printf("%c\n", ch);
                map.setMovesMade(map.getMovesMade() + 1);
                switch (ch) { // if character is a valid action, return it
                    case 'F':
                    case 'f':
                        if (map.canMove()) {
                            int[] playerCoords = map.getPlayerCoords();
                            switch (map.getCurDir()) {
                                case EAST:
                                    map.setjOffset(map.getjOffset() + 1);
                                    map.setPlayerCoords(playerCoords[0], playerCoords[1] + 1);
                                    break;
                                case WEST:
                                    map.setjOffset(map.getjOffset() - 1);
                                    map.setPlayerCoords(playerCoords[0], playerCoords[1] - 1);
                                    break;
                                case NORTH:
                                    map.setiOffset(map.getiOffset() - 1);
                                    map.setPlayerCoords(playerCoords[0] - 1, playerCoords[1]);
                                    break;
                                case SOUTH:
                                    map.setiOffset(map.getiOffset() + 1);
                                    map.setPlayerCoords(playerCoords[0] + 1, playerCoords[1]);
                                    break;
                            }
                            map.setTools(view);
                        }
                        return ((char) ch);
                    case 'L':
                    case 'l':
                        switch (map.getCurDir()) {
                            case EAST:
                                map.setCurDir(NORTH);
                                break;
                            case WEST:
                                map.setCurDir(SOUTH);
                                break;
                            case NORTH:
                                map.setCurDir(WEST);
                                break;
                            case SOUTH:
                                map.setCurDir(EAST);
                                break;
                        }
                        map.buildMap(view);
                        return ((char) ch);
                    case 'R':
                    case 'r':
                        switch (map.getCurDir()) {
                            case EAST:
                                map.setCurDir(SOUTH);
                                break;
                            case WEST:
                                map.setCurDir(NORTH);
                                break;
                            case NORTH:
                                map.setCurDir(EAST);
                                break;
                            case SOUTH:
                                map.setCurDir(WEST);
                                break;
                        }
                        map.buildMap(view);
                        return ((char) ch);
                    case 'C':                       //HANDLE CHOPPING AND OPENING OF DOORS
                    case 'c':
                        if (map.canChop(view)) {
                            System.out.println("NOW HAVE RAFT");
                            map.setHasRaft(true);
                        }
                        map.buildMap(view);
                        return ((char) ch);
                    case 'U':
                    case 'u':
                        if (map.canUnlock(view)) {
                            System.out.println("UNLOCKED DOOR");
                        }
                        map.buildMap(view);
                        return ((char) ch);
                }
            }
        //} catch (IOException e) {
        //    System.out.println("IO error:" + e);
        //}

        return 0;
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

    public static void main(String[] args) throws InterruptedException {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        Agent agent = new Agent();
        boolean first = true;
        char view[][] = new char[5][5];
        char action = 'F';
        int port;
        int ch;
        int i, j;

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

        try { // scan 5-by-5 window around current location
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
                if (!first) {
                    agent.map.buildMap(view);
                    agent.map.setPlayer();
                    agent.map.setToolCoords();
                    agent.map.printMap();
                    System.out.println(agent.map.isHasAxe());
                }
                agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                if (first) {
                    agent.map.initialiseMap(view);
                    first = false;
                }
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