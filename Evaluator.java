package gomoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator implements Runnable {

	
	private GameState gameState;
    private int currentDepth;
    private boolean stopSearch = false; //set to true when we run out of time.
    private String lastBestMove;
    private static int maxDepth = 4;
    
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public void run() {
	
		 GameState currentState = gameState;
	     lastBestMove = null;
	     String theMove = miniMax(currentState.getBoard(), currentState.getPlayer());
	     lastBestMove = theMove;
	     
		
	}
	
	 /**
     * The MiniMax algorithm for adversarial serach.
     * @param board The current game board.
     * @param player The player that you would like to generate a move for.
     * @return The most promising move.
     */
    public String miniMax(int[][] board, char player){
        currentDepth = 0;

        String bestMove = null;
        double bestMoveUtility = Double.NEGATIVE_INFINITY, moveUtility;
        String[] possibleMoves = generateMoves(board);
        //String[] possibleMoves = generateAdjacentMoves(board);
        for(int depth = 1; depth <= maxDepth; depth++){//Iteratively deepen the search.
            for(int i=0; i<possibleMoves.length; i++){
                int[][] theBoard = applyMove(board, player, possibleMoves[i]);
                moveUtility = minMove(theBoard, GameState.getEnemy(player), depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);//start the search

                if(moveUtility > bestMoveUtility){
                    this.lastBestMove = possibleMoves[i];
                    bestMove = possibleMoves[i];
                    bestMoveUtility = moveUtility;
                }
            }
            currentDepth = depth;
        }

        return bestMove;
    }

    /**
     * The MAX part of MiniMax algorithm. Maximizes expected outcomes.
     *
     * @param board The game board to be analyzed
     * @param player YOUR player.
     * @param depthLeft Keeps track of the depth of the search.
     * @param alpha The ALPHA part of A-B pruning
     * @param beta The BETA part of A-B pruning
     * @return A utility value for the specified move and player.
     */
    public double maxMove(int[][] board, char player, int depthLeft, double alpha, double beta){


        double currentUtility = GameState.getStateUtility(board, player);// - GameState.getStateUtility(board, GameState.getEnemy(player));

        if(currentUtility == GameState.FIVE_IN_A_ROW  || depthLeft == 0 || stopSearch){

            return currentUtility;
        }else{
            double bestMoveUtility = Double.NEGATIVE_INFINITY, moveUtility;
//            String[] moves = generateMoves(board);
            String[] moves = generateAdjacentMoves(board);
            List<int[][]> boards = generateBoards(board, player, moves);

            for(int[][] b : boards){
                moveUtility = minMove(b, GameState.getEnemy(player), depthLeft-1, alpha, beta);
                if(moveUtility > bestMoveUtility) bestMoveUtility = moveUtility;

                if(moveUtility >= beta) return moveUtility;//Alpha-Beta pruning

                if(moveUtility > alpha) alpha = moveUtility;
            }

            return bestMoveUtility;
        }
    }

    /**
     * The MIN part of MiniMax algoithm. Minimizes expected outcomes.
     *
     * @param board The game board to be analyzed.
     * @param player The ENEMY player.
     * @param depthLeft Keeps track of the depth of the search.
     * @param alpha The ALPHA part of A-B pruning
     * @param beta The BETA part of A-B pruning
     * @return A utility value for the specified move and player.
     */
    public double minMove(int[][] board, char player, int depthLeft, double alpha, double beta){
//        System.out.println("minMove called. depthLeft: "+depthLeft+". Player: "+player);
        double currentUtility = GameState.getStateUtility(board, player);

        if(currentUtility == GameState.FIVE_IN_A_ROW || depthLeft == 0 || stopSearch){
            return currentUtility;
        }else{
            double bestMoveUtility = Double.POSITIVE_INFINITY, moveUtility;
            String[] moves = generateAdjacentMoves(board);

            List<int[][]> boards = generateBoards(board, GameState.getEnemy(player), moves);

            for(int[][] b: boards){
                moveUtility = maxMove(b, GameState.getEnemy(player), depthLeft-1, alpha, beta);
                if(moveUtility < bestMoveUtility) bestMoveUtility = moveUtility;

                if(moveUtility <= alpha) return moveUtility;
                if(moveUtility < beta) beta = moveUtility;
            }

            return bestMoveUtility;
        }
    }

    /**
     * Makes a 'move' on a game board.
     *
     * @param board The current game board.
     * @param player The player that shall take the move.
     * @param move The move that shall be taken.
     * @return A new game board with the specified move executed.
     */
    public int[][] applyMove(int[][] board, char player, String move){
//        System.out.println("Possible move: "+game.getPlayer()+ " to "+move);

        int[][] newBoard = new int[board.length][board.length];
        for(int i=0; i<board.length; i++){
            newBoard[i] = board[i].clone();
        }

        String[] moves = move.split(" ");
        int x = Integer.parseInt(moves[0]);
        int y = Integer.parseInt(moves[1]);

        newBoard[x][y] = player;
        return newBoard;
    }


    /**
     * This move is set in the MiniMax function.
     * It can be used to get the (last generated) best move when the time runs out.
     *
     * @return The instance variable lastBestMove.
     */
    public String getMove() {
//        System.out.println("GetMove called. Best move: "+lastBestMove);
        return lastBestMove;
    }


    /**
     * Generate possible game boards.
     * Used in conjunction w/ generateMoves() to get the moves array.
     *
     * @param board The current game board
     * @param player The player that will make the move.
     * @param moves An Array of possible moves.
     * @return A List of character matricies (game boards) that are possible.
     */
    public List<int[][]> generateBoards(int[][] board, char player, String[] moves){
        List<int[][]> possibleBoards = new ArrayList<>();

        for(int i=0; i<moves.length; i++){
            possibleBoards.add(applyMove(board, player, moves[i]));
        }

        return possibleBoards;
    }


    /**
     * Generates possible adjacent moves based on a game board.
     * The moves are just empty spaces in the game.
     *
     * @param board The current game board.
     * @return A String array of possible adjacent moves.
     */
    public String[] generateAdjacentMoves(int[][] board) {
        List<String> moves = new ArrayList<>();
        Set<String> movesSet = new HashSet<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 'o' || board[i][j] == 'x') { //success!
                    for(int x=1; x <=6; x++){
                        if(i+x < board.length && board[i+x][j] == ' '){//down
                            movesSet.add( (i+x) + " " + j);
                        }
                        if(i-x >= 0 && board[i-x][j] == ' '){//up
                            movesSet.add( (i-x) + " " + j);
                        }
                        if(j+x < board.length && board[i][j+x] == ' '){//right
                            movesSet.add( i + " " + (j+x));
                        }
                        if(j-x >= 0 && board[i][j-x] == ' '){//left
                            movesSet.add( i + " " + (j-x));
                        }
                        if(i-x >= 0 && j-x >= 0 && board[i-x][j-x] == ' '){//top-left diag
                            movesSet.add( (i-x) + " " + (j-x));
                        }
                        if(i+x < board.length && j-x >=0 && board[i+x][j-x] == ' '){//bottom-left diag
                            movesSet.add( (i+x) + " " + (j-x));
                        }
                        if(i-x >= 0 && j+x < board.length && board[i-x][j+x] == ' '){//top-right diag
                            movesSet.add( (i-x) + " " + (j+x));
                        }
                        if(i+x < board.length && j+x < board.length && board[i+x][j+x] == ' '){//bottom-right diag
                            movesSet.add( (i+x) + " " + (j+x));
                        }
                    }
                }
            }
        }
        moves.addAll(movesSet);//avoid duplicates
        return moves.toArray(new String[moves.size()]);
    }

    /**
     * Generates possible moves based on a game board.
     * The moves are just empty spaces in the game.
     *
     * @param board The current game board.
     * @return A String array of possible moves.
     */
    public String[] generateMoves(int[][] board) {
        List<String> moves = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == ' ') { //success!
                    moves.add( i + " " + j);
                }
            }
        }
        return moves.toArray(new String[moves.size()]);
    }

    /**
     * To be called when you would like to stop searches.
     * Used in the Timer class to stop searching after a specified time.
     *
     * @param stopSearch Set to true to stop search
     */
    public void setStopSearch(boolean stopSearch) {
        this.stopSearch = stopSearch;
    }

    public Evaluator(GameState gameState) {
        this.gameState = gameState;
    }

}
