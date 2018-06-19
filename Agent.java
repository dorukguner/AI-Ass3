import java.io.*;
import java.net.*;

public class Agent {

    /**
     * Our Agent will only perform one move at a time, always searching for the best possible move each time. This is
     * done instead of performing multiple moves at once due to the fact that while uncovering new parts of the map, a
     * better move can be discovered before fulling completing the Agent's walk to whatever it was previously walking to.
     *
     * The Agent checks whether or not we can walk to a certain goal coordinate by using a flood fill algorithm
     * The flood fill algorithm is used because for this step we don't need to provide the most optimal route,
     * we just need relatively quick computation time.
     *
     * Once we have determined which goal coordinate to walk to, we perform an AStar search to get the most optimal
     * path to follow.
     * 
     * We prioritise tools in the following order: 
     *      Axe
     *      Key
     *      Door
     *      Tree
     *      Stone
     *      Treasure
     * This order is used as we found through testing that there were times when extra tools were needed to reach
     * the starting coordinates once the treasure was picked up. 
     *
     * Our Agent calculates which move to make next by following these rules:
     *
     *      1. If the agent has treasure and there is a valid path that the agent can take back to the starting point,
     *          we follow this path till the game is won.
     *
     *      2. Otherwise, if there is a tool that has been seen that also has a valid path that we can take to interact
     *          with it, we follow this path till the tool is picked up or a better move is found.
     *
     *      3. Otherwise, the agent will find the closest unexplored area by checking each square by using a modified
     *          flood fill algorithm.
     *
     *      4. Otherwise, there are no logical moves that the Agent can make, so now we check if it is safe/possible for
     *          the Agent to move forward, otherwise if it is not safe for the Agent to move forward we randomly turn left
     *          or right.
     *
     */
    private Map map = new Map();

    private final static int NORTH = 0;
    private final static int EAST = 1;
    private final static int SOUTH = 2;
    private final static int WEST = 3;


    public char get_action(char view[][]) {


        int ch = 0;

        while (ch != -1) {
            ch = map.getBestMove();
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
                        map.setHasRaft(true);
                    }
                    map.buildMap(view);
                    return ((char) ch);
                case 'U':
                case 'u':
                    map.buildMap(view);
                    return ((char) ch);
            }
        }

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
                }
                agent.print_view(view);
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