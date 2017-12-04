package gomoku;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.net.*;
import java.io.*;

public class AIclient implements Runnable{

	private static Socket socket = null;
	private static PrintStream outputStream = null;
	private static BufferedReader inputStream = null;
	private static GuiLayout layout;
	private String name = "";
	private String opponent_name = "";
	private boolean isBlack;

	private static gomokuGame game;
	static GameState currentState = new GameState();


	public static void main(String[] args) {
		GameState currentState = new GameState();
		Evaluator moveSearch;
		int lastMoveCount = 0;
		String host = "localhost";
		int portNumber = 5000;
		
		setupConnection(host, portNumber);

		while(currentState.getStatus()){
			do{
				//if(currentState.getMoveCount() == 0) connector.makePlay(currentState.getBoardSize()/2 + " " + currentState.getBoardSize()/2);
				lastMoveCount = currentState.getMoveCount();
				if(currentState.myTurn()){ //currently this client's turn
					moveSearch = new Evaluator(currentState);
					Thread searchThread = new Thread(moveSearch, "moveSearch");

					searchThread.start(); //search for moves

					//choose a random play (for now)
					//currentState.makePlay(moveSearch.getMoves()[rand.nextInt(moveSearch.getMoves().length)]);


					//FIGURE OUT HOW TO SEND SERVER THE MOVE
					//connector.makePlay(moveSearch.getMove());
				}
			} while(currentState.update().getStatus() && lastMoveCount != currentState.getMoveCount());
		}

	}



	public void run() {

		//??keep a move count?

		/*
		 * Keep on reading from the socket to process messages from the
		 * server, and react accordingly.
		 */
		String responseLine;
		try {
			// gets information from the server:
			while ((responseLine = inputStream.readLine()) != null) {

				System.out.println("Message from server: " + responseLine);

				if (GomokuProtocol.isSetBlackColorMessage(responseLine)){
					System.out.println("You have been randomly assigned black.");
					isBlack = true;
				}
				else if (GomokuProtocol.isSetWhiteColorMessage(responseLine)){
					System.out.println("You have been randomly assigned white.");
					isBlack = false;
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
					System.out.println("Name: " + name);
					System.out.println("Opponent Name: " + opponent_name);
					// TODO: alert players of the name change
				}
				else if (GomokuProtocol.isPlayMessage(responseLine)){
					int[] detail = GomokuProtocol.getPlayDetail(responseLine);
					if (detail[0] == 1 && isBlack || detail[0] == 0 && !isBlack){
						// this player's move is coming back. Probably don't need to do anything?
					} else {
						int row = detail[1];
						int col = detail[2];
						// send message to gameboard that the opponent has played
					}
				}
				else if (GomokuProtocol.isChatMessage(responseLine)){
					String[] detail = GomokuProtocol.getChatDetail(responseLine);
					String sender = detail[0];
					String msg = detail[1];
					// TODO: send this to gui however that's going to happen
				}
				else if (GomokuProtocol.isGiveupMessage(responseLine)){
					System.out.println("A player has quit the game.");
					closeConnection();
				}
				else if (GomokuProtocol.isLoseMessage(responseLine)){
					System.out.println("Sorry, you lost :(");
					closeConnection();
				}
				else if (GomokuProtocol.isWinMessage(responseLine)){
					System.out.println("Congrats, you won!");
					closeConnection();
				}
				else if (GomokuProtocol.isResetMessage(responseLine)){
					// send to gui or AI
				}

			}

		}catch (IOException e) {
			System.err.println("IOException:  " + e);
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

	public static void setupConnection(String host, int portNumber) {
        //layout = new GuiLayout(this);

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
            new Thread(new AIclient()).start();
        }
    }



	//default constructor 
	public AIclient() {


	}

}
