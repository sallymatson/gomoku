package gomoku;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.*;
import java.io.*;

class GomokuClientMain {
    public static void main(String[] args) {

        if(args.length < 2) {
            System.out.println("Please pass host name and port number as command line arguments");
            System.exit(0);
        }

        String host = args[0];
        int portNumber = Integer.parseInt(args[1]);

        GomokuClient client = new GomokuClient();
        client.setupConnection(host, portNumber);
    }
}

public class GomokuClient implements Runnable {
    private static Socket socket = null;
    private static PrintStream outputStream = null;
    private static BufferedReader inputStream = null;
    private static boolean closed = false;
    private static GuiLayout layout;
    private static boolean hasName = false;
    private String name = "My Name";

    public void setupConnection(String host, int portNumber) {
        layout = new GuiLayout(this);

        // int portNumber = 5155;
        // String host = "localhost";

        try {
            // open socket on the host and port,
            // inputStream to receive messages from server,
            // outputStream to send messages to server
            socket = new Socket(host, portNumber);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        }

        // If all initialized correctly
        if (socket != null && outputStream != null && inputStream != null) {
            // Thread to read from server
            new Thread(new GomokuClient()).start();
        }
    }

    private void closeConnection() {
        try {
            // Close streams that were opened
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        }
    }

    public void placeGamePiece(String text) {
        System.out.println("message to server: " + text);
        outputStream.println(text);
    }

    public void sendMessage(String text) {
        // TODO: only allow these methods to execute if connected
        outputStream.println(GomokuProtocol.generateChatMessage(this.name, text));
    }

    public void quit() {
        outputStream.println(GomokuProtocol.generateGiveupMessage());
    }

    public void run() {
        /*
         * Keep on reading from the socket to process messages from the
         * server, and react accordingly.
         */
        String responseLine;
        try {
            // gets information from the server:
            while ((responseLine = inputStream.readLine()) != null) {
                if (GomokuProtocol.isWinMessage(responseLine)) {
                    System.out.println("Congrats, you won!");
                    closeConnection();
                }
                else if (GomokuProtocol.isLoseMessage(responseLine)) {
                    System.out.println("Sorry, you lost :(");
                    closeConnection();
                }
                else {
                    System.out.println("Nonstandard message from server: " + responseLine);
                }
            }
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}

