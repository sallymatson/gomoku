//
//  Created by:
//      ad1229  Aissatou Diallo
//      pj202   Peter Johnston
//      sam439  Sally Matson
//
//  Disclosures:
//

package gomoku;

import sun.font.TrueTypeFont;

import java.net.*;
import java.io.*;
import java.lang.*;


public class GomokuServer {

    private static ServerSocket serverSocket = null;
    // maximum number of connected users
    private static final int maxConnections = 20;
    private static final clientThread[] clientConns = new clientThread[maxConnections];
    private static final gomokuGame[] currentGames = new gomokuGame[maxConnections/2];
    private static final clientThread[] waitingClient = new clientThread[1];

    public static void main(String args[]) {

        if (args.length < 1) {
			System.out.println("Please pass port number as command line argument");
			System.exit(0);
		}
		
		int portNumber = Integer.parseInt(args[0]);
		
        // open socket on server
        //int portNumber = 5155;
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }

        // Create a connection and new client thread for each new client
        while (true) {
            try {
                System.out.println("Listening for connections...");
                Socket conn = serverSocket.accept();
                int i = 0;
                // create player's thread, add to the server's list of client connections
                for (i = 0; i < maxConnections; i++) {
                    if (clientConns[i] == null) {
                        (clientConns[i] = new clientThread(conn, clientConns)).start();
                        break;
                    }
                }
                // if there is not enough space left:
                if (i == maxConnections) {
                    PrintStream outputStream = new PrintStream(conn.getOutputStream());
                    outputStream.println("At capacity, please try again later");
                    outputStream.close();
                    conn.close();
                }
                // if there is a waiting client, start a new game
                else if (waitingClient[0] != null){
                    for (int j = 0; j<maxConnections/2; j++){
                        if (currentGames[j] == null){
                            currentGames[j] = new gomokuGame(waitingClient[0], clientConns[i]);
                            waitingClient[0] = null;
                            break;
                        }
                    }
                }
                // if there is no waiting client, add the current client to the list of waiting clients
                else {
                    waitingClient[0] = clientConns[i];
                }
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
}

class gomokuGame {
    public clientThread p1;
    public clientThread p2;
    public int gameboard[][] = new int[15][15];

    gomokuGame(clientThread p1arg, clientThread p2arg){

        // waits for the threads to both get set up:
        while (!p1arg.gameStarted || !p2arg.gameStarted){
            continue;
        }

        // assign player threads to the game:
        this.p1 = p1arg;
        this.p2 = p2arg;


        System.out.println("p1 myGame: " + p1.myGame);
        System.out.println("p2 myGame: " + p2.myGame);
        // set both player's game to be this game
        p1.myGame = this;
        p2.myGame = this;
        System.out.println("p1 myGame: " + p1.myGame);
        System.out.println("p2 myGame: " + p2.myGame);


        // set the names for both players
        p1.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 1"));
        p2.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 2"));
        p1.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 2"));
        p2.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 1"));
        p1.setName("Player 1");
        p2.setName("Player 2");

        // set them to be eachothers opponents
        p1.opponent = p2;
        p2.opponent = p1;

        // randomly assign one black and one white
        int random = (int) (Math.random() * 2);
        if (random == 0) {
            p1.outputStream.println(GomokuProtocol.generateSetBlackColorMessage());
            p2.outputStream.println(GomokuProtocol.generateSetWhiteColorMessage());
            p1.colorNum = 2;
        } else {
            p2.outputStream.println(GomokuProtocol.generateSetBlackColorMessage());
            p1.outputStream.println(GomokuProtocol.generateSetWhiteColorMessage());
            p2.colorNum = 2;
        }

    }

    public int checkWinState(int row, int col, int colorNum){
        String winstate = (colorNum == 1) ? "11111" : "22222";
        StringBuilder checkRow = new StringBuilder();
        for (int i = 0; i<=14; i++){
            checkRow.append(gameboard[row][i]);
        }
        StringBuilder checkCol = new StringBuilder();
        for (int i = 0; i<=14; i++){
            checkCol.append(gameboard[i][col]);
        }
        StringBuilder posDiagonal = new StringBuilder();
        if (row+col < 14 && row+col > 3){
            int rowStart = row+col;
            int colStart = 0;
            int rowNum = rowStart;
            while (colStart <= rowStart){
                posDiagonal.append(gameboard[rowNum][colStart]);
                colStart++;
                rowNum--;
            }
        }
        else if (row+col >= 14 && row+col < 25){
            int rowStart = 14;
            int colStart = row + col - 14;
            int colNum = colStart;
            while (rowStart >= colStart){
                posDiagonal.append(gameboard[rowStart][colNum]);
                rowStart--;
                colNum++;
            }
        }
        StringBuilder negDiagonal = new StringBuilder();
        if (row>col && row < 11){
            int rowStart = row-col;
            int colStart = 0;
            while (rowStart <= 14){
                negDiagonal.append(gameboard[rowStart][colStart]);
                rowStart++;
                colStart++;
            }
        }
        else if (col>=row && col < 11){
            int rowStart = 0;
            int colStart = col-row;
            while (colStart <= 14){
                negDiagonal.append(gameboard[rowStart][colStart]);
                rowStart++;
                colStart++;
            }
        }
        // check if any of the win conditions have been found:
        if (checkRow.toString().contains(winstate) || checkCol.toString().contains(winstate) ||
                posDiagonal.toString().contains(winstate) || negDiagonal.toString().contains(winstate)) {
            return colorNum;
        }
        return 0;
    }
}

class clientThread extends Thread {
    private BufferedReader inputStream = null;
    public PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final clientThread[] clientConns;
    private int maxConnections;
    public int colorNum = 1; // 2 for black, 1 for white
    public gomokuGame myGame;
    public clientThread opponent;
    public boolean gameStarted = false;

    public clientThread(Socket clientSocket, clientThread[] clientConns) {
        this.clientSocket = clientSocket;
        this.clientConns = clientConns;
        maxConnections = clientConns.length;
    }

    private boolean nameIsUnique(String name){
        for (int i = 0; i < maxConnections; i++){
            if (clientConns[i] != null && clientConns[i].getName().equals(name)){
                return false;
            }
        }
        return true;
    }

    public void run() {
        int maxConnections = this.maxConnections;
        clientThread[] clientConns = this.clientConns;

        try {
            // open for one client, adds them to the list and informs everyone
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputStream = new PrintStream(clientSocket.getOutputStream());
            gameStarted = true;

            while (myGame == null){
                System.out.println("null");
            }

            System.out.println("Game has started.");
            System.out.println("my color isssssss: " + colorNum);

            while (true) {
                String line = inputStream.readLine();
                System.out.println("message from client: " + line);

                if (GomokuProtocol.isChatMessage(line)){
                    outputStream.println(line);
                    opponent.outputStream.println(line);
                }
                else if (GomokuProtocol.isChangeNameMessage(line)){
                    String[] detail = GomokuProtocol.getChangeNameDetail(line);
                    if (!detail[1].equals(opponent.getName())){
                        this.setName(detail[1]);
                        outputStream.println(line);
                        opponent.outputStream.println(line);
                    }
                }

                else if (GomokuProtocol.isPlayMessage(line)){
                    int detail[] = GomokuProtocol.getPlayDetail(line);
                    // black = 2, white = 1, empty = 0
                    myGame.gameboard[detail[1]][detail[2]] = colorNum;
                  
                    int winState = myGame.checkWinState(detail[1], detail[2], colorNum);

                    if (winState == 0){
                       // game continues with the new play
                       outputStream.println(line);
                       opponent.outputStream.println(line);
                    }
                    // TODO: sometimes sends win/lose messages to wrong player
                    else if (winState == 1 && detail[0] == 0 || winState == 2 && detail[0] == 1){
                        // the player who JUST played (and thus sent the gameplay message) has WON
                        outputStream.println(GomokuProtocol.generateWinMessage());
                        opponent.outputStream.print(GomokuProtocol.generateLoseMessage());
                    }
                    else {
                        // the player who JUST played (and thus sent the gameplay message) has LOST
                        outputStream.println(GomokuProtocol.generateLoseMessage());
                        opponent.outputStream.println(GomokuProtocol.generateWinMessage());
                    }
                }

                else if (GomokuProtocol.isResetMessage(line)){
                    for (int i = 0; i<maxConnections; i++){
                        if (clientConns[i] != null){
                            clientConns[i].outputStream.println(GomokuProtocol.generateResetMessage());
                        }
                    }
                }

                else if (GomokuProtocol.isGiveupMessage(line)){
                    for (int i = 0; i<maxConnections; i++){
                        if (clientConns[i] != null){
                            clientConns[i].outputStream.println(GomokuProtocol.generateGiveupMessage());
                        }
                    }
                    break;
                }
            }

            System.out.println("stopped one of the threads");

            //free the current thread
            for (int i = 0; i < maxConnections; i++) {
                if (clientConns[i] == this) {
                    clientConns[i] = null;
                }
            }

            // close all opened
            inputStream.close();
            outputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}
