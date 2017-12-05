package gomoku;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class AIConnection
{
    private static String hostName;
    private static int portNo;
    private static PrintWriter output; //output stream for sockets
    private static BufferedReader input; //input reading
    private static AIConnection instance = null;
    public static GuiLayout layout;
    public static AIclient client; 
    public static int[][] board = new int[15][15];
    
    private AIConnection(String hostName, int portNo, AIclient ai) {
    		AIConnection.hostName = hostName;
    		AIConnection.portNo = portNo;
    		AIConnection.client = ai;
        connectToServer(hostName, portNo);
        try{
            boolean DEBUG = false;
            if(DEBUG)System.out.println("WAITING FOR DATA!");
            while (!input.ready());   //wait until we receive data
            if(DEBUG)System.out.println("RECEIVED DATA!");

        }catch(IOException ioe){}

    }

    /**
     * send a move to the gomoku server
     * @param play the desired play, 'column row' separated by space
     */
    public void makePlay(String play) {
        if (output == null) {
            output = getOutputWriter();
        }
        
        int col = play.charAt(0);
        int row = play.charAt(2);
        int val = 0;
        
        if(client.isBlack)
        		val = 2;
        else
        		val = 1;
         
        
        System.out.println("MakePlay called: "+play);
        layout.placeGamePiece(row, col, val);
       
        String rplay = "/play" + val + row + col;
        
        String s = play+"\n";
        output.print("/play" + s);
        output.flush();

        //GameState.increaseCount();
    }

    //Making this class a 'Singleton' - only one instance.
    public static AIConnection getInstance(){
        if(instance == null){
            System.err.println("getInstance() called on a null GomokuConnector.");
            return null;
        }
        return instance;
    }

    public static String getHostName() {
        return hostName;
    }

    public static int getPortNo() {
        return portNo;
    }

    public static AIConnection newInstance(String hostname, int portNo, AIclient ai){
        if(instance != null){
            return instance;
        }else{
            instance = new AIConnection(hostname, portNo, ai);
            return instance;
        }
    }

    public static BufferedReader getInputReader(){
        return input;
    }

    public static PrintWriter getOutputWriter(){
        return output;
    }

    private void connectToServer(String hostName, int portNo)
    {
        try {
        	
        		layout = new GuiLayout(client);
            Socket socket = new Socket(hostName, portNo);
            output = new PrintWriter(socket.getOutputStream()); //output stream to communicate with server
            input = new BufferedReader(new InputStreamReader(socket.getInputStream())); //read input from server

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostName);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostName);
            e.printStackTrace();
            System.exit(1);
        }
    }

}