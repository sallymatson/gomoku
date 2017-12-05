package gomoku;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.*;
import java.io.*;



class AIClientMain {
	
	private static Socket socket = null;
	private static PrintStream outputStream = null;
	private static BufferedReader inputStream = null;
	
    public static void main(String[] args) {

        if(args.length < 2) {
            System.out.println("Please pass host name and port number as command line arguments");
            System.exit(0);
        }
        
        String host = args[0];
        int portNumber = Integer.parseInt(args[1]);

       
        AIclient AIclient = new AIclient();
        
        //AIclient.setup();
        
   
    }
}


public class AIclient implements Runnable {

    private  AIConnection connector =  AIConnection.newInstance("localhost", 5005, this);

	public static Socket socket = null;
	public static PrintStream outputStream = null;
	public static BufferedReader inputStream = null;
	public static GuiLayout layout;
	private static String name = "";
	private static String opponent_name = "";
	public static boolean isBlack;
    public static int gameboard[][] = new int[15][15];

    
  
    
    public void setup() {
    	
		layout = new GuiLayout(this);

    }
    
	GameState currentState = new GameState(this);

	private Object runner;
	



	public void run() {
		
		Evaluator moveSearch;
		int lastMoveCount = 0;
		gomokuGame game;

		String responseLine; 

	    System.out.println("1");

		System.out.println("2");
		
		while(currentState.getStatus()) {

		//while((responseLine = (AIConnection.getInputReader()).readLine()) != null){

			
		//while(currentState.getStatus() && (responseLine = inputStream.readLine()) != null){
			do{
				//if(currentState.getMoveCount() == 0) connector.makePlay(currentState.getBoardSize()/2 + " " + currentState.getBoardSize()/2);
				lastMoveCount = currentState.getMoveCount();
				
				if(currentState.myTurn()){ //currently this client's turn
					//moveSearch = new Evaluator(currentState);
					//Thread searchThread = new Thread(moveSearch, "moveSearch");

					//searchThread.start(); //search for moves

					//choose a random play (for now)
					//currentState.makePlay(moveSearch.getMoves()[rand.nextInt(moveSearch.getMoves().length)]);
					

					//FIGURE OUT HOW TO SEND SERVER THE MOVE
					//makePlay(moveSearch.getMove());
					
				    System.out.println("about to make play");
				   String play = "0 1";
		            connector.makePlay(play);
		            
				}
			}while(currentState.update().getStatus());
		}
		
		
	}


		


	private static void closeConnection() {
		try {
			// Close streams that were opened
			(AIConnection.getOutputWriter()).close();
			(AIConnection.getInputReader()).close();
			socket.close();
		} catch (IOException e) {
			System.err.println("I/O error: " + e);
		}
	}

	public void setupConnection(String host, int portNumber) {
        layout = new GuiLayout(this);

        // Open a socket on a given host and port. Open input and output streams.

        try {
            // open socket on the host and port,
            // inputStream to receive messages from server,
            // outputStream to send messages to server
            socket = new Socket(host, portNumber);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintStream(socket.getOutputStream());
            //System.out.println("connected");
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
        }

        // If all initialized correctly
        if (socket != null && outputStream != null && inputStream != null) {
            // Thread to read from server
            new Thread(new AIclient()).start();
        }
    }


	 // TODO: make a give up button that calls this funciton when clicked
    public void quit() {
        outputStream.println(GomokuProtocol.generateGiveupMessage());
    }
    public void placeGamePiece(int row, int col) {
        System.out.println("playing a " + (isBlack ? "black" : "white") + " tile");
        outputStream.println(GomokuProtocol.generatePlayMessage(isBlack, row, col));
    }
    
    public void sendChat(String text) {
        if (text.startsWith("/nick")){
            outputStream.println(GomokuProtocol.generateChangeNameMessage(name, text.substring(6)));
        }
        else {
            outputStream.println(GomokuProtocol.generateChatMessage(name, text));
        }
    }

   

	//default constructor
    public AIclient() {
    		this.runner = new Thread(this);
    		((Thread) this.runner).start();
    	
    }


}
