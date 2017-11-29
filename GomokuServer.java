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
    public static int gameboard[][] = new int[15][15];

    // TODO: make this *shrug*
    public static int checkWinState(){
        // if black wins,
        //return 2;
        // if white wins,
        //return 1;
        // if nobody wins,
        return 0;
    }

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
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
}

class clientThread extends Thread {
    private BufferedReader inputStream = null;
    private PrintStream outputStream = null;
    private Socket clientSocket = null;
    private final clientThread[] clientConns;
    private int maxConnections;
    private boolean hasName = false;



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
            for (int i = 0; i<maxConnections; i++){
                if (clientConns[i] != null && clientConns[i] != this){
                    if (clientConns[i].hasName){
                        outputStream.println("C=add=" + clientConns[i].getName());
                    }
                }
            }


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

                }

                else if (GomokuProtocol.isPlayMessage(line)){
                    int detail[] = GomokuProtocol.getPlayDetail(line);
                    // black = 2, white = 1, empty = 0
                    GomokuServer.gameboard[detail[1]][detail[2]] = detail[0] + 1;
                    int winState = GomokuServer.checkWinState();
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
