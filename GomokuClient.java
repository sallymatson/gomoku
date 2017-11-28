package gomoku;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.*;
import java.io.*;

public class GomokuClient implements Runnable {
    private static Socket socket = null;
    private static PrintStream outputStream = null;
    private static BufferedReader inputStream = null;
    private static boolean closed = false;
    private static GuiLayout layout;
    private static boolean hasName = false;

    public void setupConnection(String host, int portNumber) {
        layout = new GuiLayout(this);

        // int portNumber = 5155;
        // String host = "localhost";

        // Open a socket on a given host and port. Open input and output streams.

        try {
            // open socket on the host and port,
            // datainput stream to receive messages from server,
            // dataoutput stream to send messages to server
            socket = new Socket(host, portNumber);
            outputStream = new PrintStream(socket.getOutputStream());
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        }

        // If all initialized correctly
        if (socket != null && outputStream != null && inputStream != null) {
            try {
                // Thread to read from server
                new Thread(new GomokuClient()).start();
                while (!closed) {
                    // sends to the server
//                    String message = "";
//                    if (line != null) {
//                        if (!hasName) {
//                            // setting name! (which server requires a M= prepended)
//                            message = "M=" + line.trim();
//                        } else if (line.startsWith("/nick") || line.startsWith("/q")){
//                            // sending a command
//                            message = "C=" + line.trim();
//                        } else if (layout.clientList.isSelectionEmpty()) {
//                            // no selection -> send to everyone
//                            message = "M=" + line.trim();
//                        } else if (layout.clientList.getSelectedIndex() == 0) {
//                            // "Everyone" selection -> send to everyone
//                            message = "M=" + line.trim();
//                        } else {
//                            // specific selection -> send to that recipient
//                            Object recipient = layout.clientList.getSelectedValue();
//                            message = "P=" + recipient + "=" + line.trim();
//                        }
//                        outputStream.println(message);
//                    }
                }

                // Close streams that were opened
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("IOException: " + e);
            }
        }
    }

    public void placeGamePiece(String text) {
        System.out.println("message to server: " + text);
        outputStream.println(text);
    }

    public void run() {
        /*
         * Keep on reading from the socket till we receive "C=quit" from the
         * server. Once we received that then we want to break.
         */
        String responseLine;
        try {
            // gets information from the server:
            while ((responseLine = inputStream.readLine()) != null) {
                if (responseLine.equals("C=quit")) {
                    break;
                } else if (responseLine.startsWith("C=add=")){
                    String newName = responseLine.substring(6);
                    // layout.clients.addElement(newName);
                } else if (responseLine.startsWith("C=remove=")) {
                    String oldName = responseLine.substring(9);
                    // layout.clients.removeElement(oldName);
                } else if (responseLine.startsWith("C=join")) {
                    hasName = true;
                } else if (responseLine.startsWith("M=")){
                    // sends to the layout, get rid of M=
                    // layout.putString(responseLine.substring(2));
                } else {
                    // only messages that are hardcoded in will start with no prefix
                    // layout.putString(responseLine);
                }
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}

