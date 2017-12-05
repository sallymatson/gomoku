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
    private String name = "";
    private String opponent_name = "";
    private boolean isBlack;
    public int gameboard[][] = new int[15][15];

    public void setupConnection(String host, int portNumber) {
        layout = new GuiLayout(this);

        // int portNumber = 5155;
        // String host = "localhost";

        // Open a socket on a given host and port. Open input and output streams.

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
            new Thread(this).start();
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

    /**********************************************************************/
    /* These Functions handle all 4 player use cases */
    public void placeGamePiece(int row, int col) {
        System.out.println("playing a " + (isBlack ? "black" : "white") + " tile");
        outputStream.println(GomokuProtocol.generatePlayMessage(isBlack, row, col));
    }

    // this will handle all messages coming from the chat box, including name changes
    public void sendChat(String text) {
        if (text.startsWith("/nick")){
            outputStream.println(GomokuProtocol.generateChangeNameMessage(name, text.substring(6)));
        }
        else {
            outputStream.println(GomokuProtocol.generateChatMessage(name, text));
        }
    }

    // TODO: make a give up button that calls this funciton when clicked
    public void quit() {
        outputStream.println(GomokuProtocol.generateGiveupMessage());
    }

    // TODO: make a reset button
    public void resetGame(){
        outputStream.println(GomokuProtocol.generateResetMessage());
    }
    /**************************************************************************/

    public void run() {
        /*
         * Keep on reading from the socket to process messages from the
         * server, and react accordingly.
         */
        String responseLine;
        try {
            // gets information from the server:
            while ((responseLine = inputStream.readLine()) != null) {

                if (GomokuProtocol.isSetBlackColorMessage(responseLine)){
                    isBlack = true;
                    layout.startGame(isBlack);
                    layout.chatMessage("Server", "You have been randomly assigned black.");
                }
                else if (GomokuProtocol.isSetWhiteColorMessage(responseLine)){
                    isBlack = false;
                    layout.startGame(isBlack);
                    layout.chatMessage("Server", "You have been randomly assigned white.");
                }
                else if (GomokuProtocol.isChangeNameMessage(responseLine)){
                    String[] detail = GomokuProtocol.getChangeNameDetail(responseLine);
                    // on start, they both have the same name, which is ""
                    // the server sends a change name message, which gives a default name
                    // this will be caught by the first if statement, so it will change the player's own name
                    // once there are two players, it then sends the change name messages to the other 2
                    if (detail[0].equals(name)){
                        name = detail[1];
                    } else if (detail[0].equals(opponent_name)){
                        opponent_name = detail[1];
                    }
                    if (!detail[0].equals("")) {
                        layout.chatMessage("Server", detail[0] + " has changed name to " + detail[1]);
                    }
                }
                else if (GomokuProtocol.isChatMessage(responseLine)){
                    String[] detail = GomokuProtocol.getChatDetail(responseLine);
                    String sender = detail[0];
                    String msg = detail[1];
                    layout.chatMessage(sender, msg);
                }
                else if (GomokuProtocol.isPlayMessage(responseLine)){
                    int[] detail = GomokuProtocol.getPlayDetail(responseLine);
                    int color = detail[0];
                    int row = detail[1];
                    int col = detail[2];
                    gameboard[row][col] = color; // probably off by 1
                    // send message to gameboard that the opponent has played
                    layout.placeGamePiece(row, col, color);
                }
                else if (GomokuProtocol.isGiveupMessage(responseLine)){
                    System.out.println("A player has quit the game.");
                    layout.chatMessage("Server", responseLine);
                    closeConnection();
                }
                else if (GomokuProtocol.isLoseMessage(responseLine)){
                    System.out.println("Sorry, you lost :(");
                    layout.chatMessage("Server", responseLine);
                    closeConnection();
                }
                else if (GomokuProtocol.isWinMessage(responseLine)){
                    System.out.println("Congrats, you won!");
                    layout.chatMessage("Server", responseLine);
                    closeConnection();
                }
                else if (GomokuProtocol.isResetMessage(responseLine)){
                    // send to gui or AI
                    layout.chatMessage("Server", responseLine);
                }
            }
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}

