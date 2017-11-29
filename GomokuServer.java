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
            outputStream.println("Please enter your name.");


            while (true) {
                String line = inputStream.readLine();
                System.out.println("message from client: " + line);


                if (GomokuProtocol.isChatMessage(line)){

                }
                else if (GomokuProtocol.isChangeNameMessage(line)){

                }
                else if (GomokuProtocol.isPlayMessage(line)){

                }
                else if (GomokuProtocol.isResetMessage(line)){
                    
                }

                // user has performed a command:
                if (line.startsWith("C=")){
                    if (line.startsWith("/q", 2)){
                        // QUIT THE PROGRAM
                        break;
                    }
                    else if (line.startsWith("/nick", 2)){
                        // CHANGE NICKNAME
                        String newName = line.substring(8);
                        if (!nameIsUnique(newName)) {
                            outputStream.println("M=This username has already been taken.");
                        } else if (newName.contains("=")) {
                            outputStream.println("M=Please choose a username without a = character.");
                        } else {
                            for (int i = 0; i < maxConnections; i++) {
                                if (clientConns[i] != null) {
                                    clientConns[i].outputStream.println("M="+ this.getName()
                                            + " has changed name to " + newName);
                                    if (clientConns[i] != this){
                                        clientConns[i].outputStream.println("C=add=" + newName);
                                        clientConns[i].outputStream.println("C=remove=" + this.getName());
                                    }
                                }
                            }
                            this.setName(newName);
                        }
                    }
                }

                // user has started a private message:
                else if (line.startsWith("P=")){
                    line = line.substring(2);
                    String recipient = line.split("=")[0];
                    String message = line.split("=")[1];
                    outputStream.println("Private msg to " + recipient + ": " + message);
                    for (int i = 0; i<maxConnections; i++){
                        if (clientConns[i] != null && clientConns[i].getName().equals(recipient)){
                            clientConns[i].outputStream.println("M=" + "Private msg from " + this.getName() + ": " + message);
                        }
                    }
                }

                // user has send a message to everyone:
                else if (line.startsWith("M=")){
                    for (int i = 0; i < maxConnections; i++) {
                        if (clientConns[i] != null && clientConns[i] != this) {
                            clientConns[i].outputStream.println(GomokuProtocol.generateWinMessage());
                        }
                    }
                    break;
                }
            }

            // INSIDE CHAT :
//            while (true) {
//                String line = inputStream.readLine();
//                if (line == null) {
//                    continue;
//                }
//
//                // user has performed a command:
//                if (line.startsWith("C=")){
//                    if (line.startsWith("/q", 2)){
//                        // QUIT THE PROGRAM
//                        break;
//                    }
//                    else if (line.startsWith("/nick", 2)){
//                        // CHANGE NICKNAME
//                        String newName = line.substring(8);
//                        if (!nameIsUnique(newName)) {
//                            outputStream.println("M=This username has already been taken.");
//                        } else if (newName.contains("=")) {
//                            outputStream.println("M=Please choose a username without a = character.");
//                        } else {
//                            for (int i = 0; i < maxConnections; i++) {
//                                if (clientConns[i] != null) {
//                                    clientConns[i].outputStream.println("M="+ this.getName()
//                                            + " has changed name to " + newName);
//                                    if (clientConns[i] != this){
//                                        clientConns[i].outputStream.println("C=add=" + newName);
//                                        clientConns[i].outputStream.println("C=remove=" + this.getName());
//                                    }
//                                }
//                            }
//                            this.setName(newName);
//                        }
//                    }
//                }
//
//                // user has started a private message:
//                else if (line.startsWith("P=")){
//                    line = line.substring(2);
//                    String recipient = line.split("=")[0];
//                    String message = line.split("=")[1];
//                    outputStream.println("Private msg to " + recipient + ": " + message);
//                    for (int i = 0; i<maxConnections; i++){
//                        if (clientConns[i] != null && clientConns[i].getName().equals(recipient)){
//                            clientConns[i].outputStream.println("M=" + "Private msg from " + this.getName() + ": " + message);
//                        }
//                    }
//                }
//
//                // user has send a message to everyone:
//                else if (line.startsWith("M=")){
//                    for (int i = 0; i < maxConnections; i++) {
//                        if (clientConns[i] != null) {
//                            clientConns[i].outputStream.println("M=" + this.getName() + ": " + line.substring(2));
//                        }
//                    }
//                }
//            }

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
