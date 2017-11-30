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
                for (i = 0; i < maxConnections; i++) {
                    if (clientConns[i] == null) {
                        (clientConns[i] = new clientThread(conn, clientConns)).start();
                        break;
                    }
                }
                if (i == maxConnections) {
                    PrintStream outputStream = new PrintStream(conn.getOutputStream());
                    outputStream.println("At capacity, please try again later");
                    outputStream.close();
                    conn.close();
                }
                else if (waitingClient[0] != null){
                    System.out.println("There are now two players");
                    for (int j = 0; j<maxConnections/2; j++){
                        if (currentGames[j] == null){
                            currentGames[j] = new gomokuGame(waitingClient[0], clientConns[i]);
                            waitingClient[0] = null;
                            break;
                        }
                    }
                }
                else {
                    System.out.println("waiting for another player");
                    waitingClient[0] = clientConns[i];
                }
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
}

class gomokuGame {
    public boolean full = false;
    public clientThread p1;
    public clientThread p2;
    public int gameboard[][] = new int[15][15];

    gomokuGame() {
        // dummy constructor
    }

    gomokuGame(clientThread p1arg, clientThread p2arg){
        this.p1 = p1arg;
        this.p2 = p2arg;
        System.out.println("IN constructor.");
        System.out.println(p1arg);
        System.out.println(p2arg);
        p1.myGame = this;
        p2.myGame = this;
        p1.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 1"));
        p2.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 2"));
        p1.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 2"));
        p2.outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player 1"));
        p1.setName("Player 1");
        p2.setName("Player 2");
        p1.opponent = p2;
        p2.opponent = p1;
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
    public boolean gameStarted = false;
    public gomokuGame myGame;
    public clientThread opponent;


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

            while (!gameStarted){
                continue;
            }


            System.out.println("Game has started.");
            // game starts:
            // set self name
            // set opponent name
            // set white/black




            /*
            this.setName("Player" + playerNumber);
            outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player " + playerNumber));
            // if the player is an even number, set up a game:
            while (!gameStarted) {
                if (playerNumber % 2 == 0) {
                    // sets the other players names:
                    clientConns[playerNumber - 1].outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player " + playerNumber));
                    clientConns[playerNumber - 2].outputStream.println(GomokuProtocol.generateChangeNameMessage("", "Player " + (playerNumber - 1)));
                    int random = (int) (Math.random() * 2);
                    if (random == 0) {
                        clientConns[playerNumber - 1].outputStream.println(GomokuProtocol.generateSetBlackColorMessage());
                        clientConns[playerNumber - 2].outputStream.println(GomokuProtocol.generateSetWhiteColorMessage());
                    } else {
                        clientConns[playerNumber - 2].outputStream.println(GomokuProtocol.generateSetBlackColorMessage());
                        clientConns[playerNumber - 1].outputStream.println(GomokuProtocol.generateSetWhiteColorMessage());
                    }
                    clientConns[playerNumber-2].gameStarted = true;
                    gameStarted = true;
                }
            }*/

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
