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

        // assign player threads to the game:
        this.p1 = p1arg;
        this.p2 = p2arg;

        // set both player's game to be this game
        p1.myGame = this;
        p2.myGame = this;

        while (!p1.gameStarted || !p2.gameStarted){
            continue;
        }
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
        } else {
            p2.outputStream.println(GomokuProtocol.generateSetBlackColorMessage());
            p1.outputStream.println(GomokuProtocol.generateSetWhiteColorMessage());
        }

    }

    // TODO: make this
    public int checkWinState(){
        // if black wins,
        //return 2;
        // if white wins,
        //return 1;
        // if nobody wins,
        return 0;
    }
}

class clientThread extends Thread {
    private BufferedReader inputStream = null;
    public PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final clientThread[] clientConns;
    private int maxConnections;
    private int playerNumber = 0;
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
                continue;
            }


            System.out.println("Game has started.");


            while (true) {
                String line = inputStream.readLine();
                System.out.println("message from client: " + line);


                if (GomokuProtocol.isChatMessage(line)){
                    for (int i = 0; i<maxConnections; i++){
                        if (clientConns[i] != null){
                            outputStream.println(line);
                        }
                    }
                }
                else if (GomokuProtocol.isChangeNameMessage(line)){
                    String[] detail = GomokuProtocol.getChangeNameDetail(line);
                    if (nameIsUnique(detail[1])){
                        for (int i = 0; i<maxConnections; i++){
                            if (clientConns[i] != null){
                                clientConns[i].outputStream.println(line);
                            }
                        }
                    }
                }

                else if (GomokuProtocol.isPlayMessage(line)){
                    int detail[] = GomokuProtocol.getPlayDetail(line);
                    // black = 2, white = 1, empty = 0
                    myGame.gameboard[detail[1]][detail[2]] = detail[0] + 1;
                    int winState = myGame.checkWinState();
                    if (winState == 0){
                       // game continues with the new play
                       for (int i = 0; i<maxConnections; i++){
                           if (clientConns[i] != null){
                               clientConns[i].outputStream.println(line);
                           }
                       }
                    }
                    else if (winState == 1 && detail[0] == 0 || winState == 2 && detail[0] == 1){
                        // the player who JUST played (and thus sent the gameplay message) has WON
                        outputStream.println(GomokuProtocol.generateWinMessage());
                        for (int i = 0; i<maxConnections; i++){
                            if (clientConns[i] != null && clientConns[i] != this){
                                clientConns[i].outputStream.println(GomokuProtocol.generateLoseMessage());
                            }
                        }
                    }
                    else{
                        // the player who JUST played (and thus sent the gameplay message) has LOST
                        outputStream.println(GomokuProtocol.generateLoseMessage());
                        for (int i = 0; i<maxConnections; i++){
                            if (clientConns[i] != null && clientConns[i] != this){
                                clientConns[i].outputStream.println(GomokuProtocol.generateWinMessage());
                            }
                        }
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
