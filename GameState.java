package gomoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameState {
    
    public static final double FIVE_IN_A_ROW = Double.POSITIVE_INFINITY;
    public static final double STRAIGHT_FOUR_POINTS = 1000;
    public static final double FOURS_POINTS = 500;
    public static final double THREES_POINTS = 100;
    public static final double TWOS_POINTS = 5;
    public static final double ONES_POINTS = 1;
	private static GuiLayout layout;

	private char player;
    public int board[][] = new int[15][15];
    private boolean isGame;
    private static int moveCount;

    private static boolean isStraightFour(String in, char player) {
        String straightFour = " " + player + player + player + player + " ";
        return in.equals(straightFour);
    }

    public char getEnemy(){
        return (player == 'x')? 'o':'x';
    }
    public static char getEnemy(char p){
        return (p == 'x')? 'o':'x';
    }
    public int[][] getBoard() {
        return this.board;
    }
    public char getPlayer() {
        return this.player;
    }

    /**
    *
    * @param val the number of pieces in a row
    * @return the value to add
    */
    private static double getPointsToAdd(int val) {
       switch (val) {
           case 1:
               return ONES_POINTS;
           case 2:
               return TWOS_POINTS;
           case 3:
               return THREES_POINTS;
           case 5:
               return FIVE_IN_A_ROW;
           default:
               return 0;
       }
    }
   
   
   /**
    * evaluator function, give credit 
    * Gets the utility for a specified player in a specified state.
    *
    * @param board The game board to analyze
    * @param player The player's state to analyze
    * @return The utility of the gamestate.
    */
    public static double getStateUtility(int[][] board, char player) {
        double[][] maxUtility = new double[board.length][board.length];

        char enemy = getEnemy(player);
        double evaluation = 0.0;
        int boardLength = board.length;
        int count;
        int lastEnemyEncounteredCol, lastEnemyEncounteredRow;
        int encounteredEnemy, encounteredEnemyY;


        for (int row = 0; row < boardLength; row++) {
            lastEnemyEncounteredCol = -1;
            lastEnemyEncounteredRow = -1;
            for (int col = 0; col < boardLength; col++) {
                if (board[row][col] == enemy) {
                    lastEnemyEncounteredCol = col;//keep track of the last encountered enemy
                    lastEnemyEncounteredRow = row;
                }

                //If we find the string contains the player
                if (board[row][col] == player) {

                    encounteredEnemy = -1;
                    //====================CHECK TO THE RIGHT====================
                    if (col <= boardLength - 5) {//to be sure there can actually be a 5-in-a-row to this direction

                       count = 1; //Sum of how many of our players we encounter in the next 4 spaces
                       for (int x = col + 1; x < col + 5; x++) {
                           if (board[row][x] == player) {
                               count++;
                           } else if (board[row][x] == enemy) {
                               encounteredEnemy = x;
                               break;
                           }
                       }

                       if (count < 3 || count == 5) {
                           evaluation += getPointsToAdd(count);
                           // debug
                           System.out.println("[horiz]BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + getPointsToAdd(count));
                       } else if (count == 3) {
                           if (encounteredEnemy == -1) {
                               evaluation += THREES_POINTS;
                               // debug
                               System.out.println("[horiz(1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                           } else if (lastEnemyEncounteredCol > -1) {//we encountered an enemy before seeing our player
                               if (col - 1 >= 0 && encounteredEnemy == col + 4) {//we have enough room to make a 4, check to the left one to see if we can make a 5 (-O-X-XXO--)
                                   if (board[row][col - 1] != enemy) {
                                       evaluation += THREES_POINTS;
                                       // debug
                                       System.out.println("[horiz](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                                   }
                               } else if (col - 2 >= 0 && encounteredEnemy == col + 3) {//we are stuck on 3, check to the left 2 to see if we can make it a 5
                                   evaluation += THREES_POINTS;
                                   // debug
                                   System.out.println("[horiz](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                               }
                           }
                       } else if (count == 4 && col - 1 < 0 && encounteredEnemy == -1) {//havent encountered an enemy before seeing our player
                           // debug
                           System.out.println("[horiz](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           evaluation += FOURS_POINTS;
                       } else if (encounteredEnemy > -1 && (col + 5 >= boardLength || col - 1 < 0)) {
                           //enemy is blocking us at the edge of the board (OXXXX)
                           // debug
                           System.out.println("[horiz]BLOCKING ON EDGE!!!!!!");
                       } else { //check for the straight four
                           String rowString = new String(board[row], col - 1, 6); //Create string representation to check for straight 4
                           if (isStraightFour(rowString, player)) {//If it is a straight 4
                               // debug
                               System.out.println("[horiz](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + STRAIGHT_FOUR_POINTS);
                               evaluation += STRAIGHT_FOUR_POINTS;
                           } else if (encounteredEnemy == -1) {//If it is possible to have a straight 4, and we have not encountered an enemy while searching
                               evaluation += FOURS_POINTS;
                               // debug
                               System.out.println("[horiz](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           } else { //If it is possible to have a straight 4, but we have encountered an enemy while searching, check if there is room on left
                               if (board[row][col - 1] != enemy) {
                                   evaluation += FOURS_POINTS;
                                   // debug
                                   System.out.println("[horiz](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                               }
                           }
                       }

                    }//FINISH CHECKING TO THE RIGHT
                    maxUtility[row][col] = evaluation;
                    evaluation = 0;


                    encounteredEnemy = -1;
                    //====================CHECK BELOW====================
                    if (row <= boardLength - 5) {//to be sure there can actually be a 5-in-a-row to this direction

                       count = 1; //Sum of how many of our players we encounter in the next 4 spaces
                       for (int x = row + 1; x < row + 5; x++) {
                           if (board[x][col] == player) {
                               count++;
                           } else if (board[x][col] == enemy) {
                               encounteredEnemy = x;
                               break;
                           }
                       }

                       if (count < 3 || count == 5) {
                           evaluation += getPointsToAdd(count);
                           // debug
                           System.out.println("[down]BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + getPointsToAdd(count));
                       } else if (count == 3) {
                           if (encounteredEnemy == -1) {
                               evaluation += THREES_POINTS;
                               // debug
                               System.out.println("[down](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                           } else if (lastEnemyEncounteredRow > -1) {//we encountered an enemy before seeing our player
                               if (row - 1 >= 0 && encounteredEnemy == row + 4) {//we have enough room to make a 4, check above to see if we can make a 5 (-O-X-XXO--)
                                   if (board[row - 1][col] != enemy) {
                                       evaluation += THREES_POINTS;
                                       // debug
                                       System.out.println("[down](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                                   }
                               } else if (row - 2 >= 0 && encounteredEnemy == row + 3) {//we are stuck on 3, check to the left 2 to see if we can make it a 5
                                   evaluation += THREES_POINTS;
                                   // debug
                                   System.out.println("[down](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                               }
                           }
                       } else if (count == 4 && row - 1 < 0 && encounteredEnemy == -1) {//havent encountered an enemy before seeing our player
                           // debug
                           System.out.println("[down](0)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           evaluation += FOURS_POINTS;
                       } else if (encounteredEnemy > -1 && (row + 5 >= boardLength || row - 1 < 0)) {
                           //enemy is blocking us at the edge of the board (OXXXX)
                           // debug
                           System.out.println("[down]BLOCKING ON EDGE!!!!!!");
                       } else { //check for the straight four
                           int[] newChars = new int[6];
                           String rowString = new String();

                           for (int b = row - 1, i = 0; b < boardLength && b < row + 5; b++, i++) {
                               newChars[i] = board[b][col];
                               rowString += newChars[i];
                           }

                           if (isStraightFour(rowString, player)) {//If it is a straight 4
                               // debug
                               System.out.println("[down](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + STRAIGHT_FOUR_POINTS);
                               evaluation += STRAIGHT_FOUR_POINTS;
                           } else if (encounteredEnemy == -1) {//If it is possible to have a straight 4, and we have not encountered an enemy while searching
                               evaluation += FOURS_POINTS;
                               // debug
                               System.out.println("[down](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           } else { //If it is possible to have a straight 4, but we have encountered an enemy while searching, check if there is room on left
                               if (board[row - 1][col] != enemy) {
                                   evaluation += FOURS_POINTS;
                                   // debug
                                   System.out.println("[down](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                               }
                           }
                       }

                    }//FINISH CHECKING BELOW

                    if(evaluation > maxUtility[row][col]){
                       maxUtility[row][col] = evaluation;
                    }
                    evaluation = 0;

                    encounteredEnemy = -1;
                    //====================CHECK ABOVE====================
                    if (row >= 4) {//to be sure there can actually be a 5-in-a-row to this direction

                       count = 1; //Sum of how many of our players we encounter in the next 4 spaces
                       for (int x = row - 1; x > row - 5; x--) {
                           if (board[x][col] == player) {

                               count++;
                           } else if (board[x][col] == enemy) {
                               encounteredEnemy = x;
                               break;
                           }
                       }

                       if (count < 3 || count == 5) {
                           evaluation += getPointsToAdd(count);
                           // debug
                           System.out.println("[up]BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + getPointsToAdd(count));
                       } else if (count == 3) {
                           if (encounteredEnemy == -1) {
                               evaluation += THREES_POINTS;
                               // debug
                               System.out.println("[up](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                           } else if (lastEnemyEncounteredRow > -1) {//we encountered an enemy before seeing our player
                               if (row + 1 < boardLength && encounteredEnemy == row - 4) {//we have enough room to make a 4, check upwards to see if we can make a 5 (-O-X-XXO--)
                                   if (board[row + 1][col] != enemy) {
                                       evaluation += THREES_POINTS;
                                       // debug
                                       System.out.println("[up](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                                   }
                               } else if (row + 2 < boardLength && encounteredEnemy == row - 3) {//we are stuck on 3, check upwards 2 to see if we can make it a 5
                                   evaluation += THREES_POINTS;
                                   // debug
                                   System.out.println("[up](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                               }
                           }
                       } else if (count == 4 && row + 1 >= boardLength && encounteredEnemy == -1) {//havent encountered an enemy before seeing our player
                           // debug
                           System.out.println("[up](0)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           evaluation += FOURS_POINTS;
                       } else if (encounteredEnemy > -1 && (row - 5 < 0 || row + 1 >= boardLength)) {
                           //enemy is blocking us at the edge of the board (OXXXX)
                           // debug
                           System.out.println("[up]BLOCKING ON EDGE!!!!!!");
                       } else { //check for the straight four
                           int[] newChars = new int[6];
                           String rowString = new String();

                           for (int b = row + 1, i = 0; b >= 0 && b > row - 5; b--, i++) {
                               newChars[i] = board[b][col];
                               rowString += newChars[i];
                           }

                           if (isStraightFour(rowString, player)) {//If it is a straight 4
                               // debug
                               System.out.println("[up](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + STRAIGHT_FOUR_POINTS);
                               evaluation += STRAIGHT_FOUR_POINTS;
                           } else if (encounteredEnemy == -1) {//If it is possible to have a straight 4, and we have not encountered an enemy while searching
                               evaluation += FOURS_POINTS;
                               // debug
                               System.out.println("[up](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           } else { //If it is possible to have a straight 4, but we have encountered an enemy while searching, check if there is room on left
                               if (board[row + 1][col] != enemy) {
                                   evaluation += FOURS_POINTS;
                                   // debug
                                   System.out.println("[up](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                               }
                           }
                       }

                    }//FINISH CHECKING ABOVE

                    if (evaluation > maxUtility[row][col]) {
                       maxUtility[row][col] = evaluation;
                    }
                    evaluation = 0;

                    encounteredEnemy = -1;
                    encounteredEnemyY = -1;
                    //====================CHECK BOTTOM-RIGHT DIAGONALLY====================
                    if (col + 4 < boardLength && row + 4 < boardLength) {//to be sure there can actually be a 5-in-a-row to this direction

                       count = 1; //Sum of how many of our players we encounter in the next 4 spaces
                       for (int x = row + 1, y = col + 1; x < row + 5 && y < col + 5; x++, y++) {
                           if (board[x][y] == player) {
                               count++;
                           } else if (board[x][y] == enemy) {
                               encounteredEnemy = x;
                               encounteredEnemyY = y;
                               break;
                           }
                       }

                       if (count < 3 || count == 5) {
                           evaluation += getPointsToAdd(count);
                           // debug
                           System.out.println("[BR-D]BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + getPointsToAdd(count));
                       } else if (count == 3) {
                           if (encounteredEnemy == -1) {
                               evaluation += THREES_POINTS;
                               // debug
                               System.out.println("[BR-D](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                           } else if (lastEnemyEncounteredRow > -1) {//we encountered an enemy before seeing our player
                               if ((row + 1 < boardLength && col - 1 >= 0) && (encounteredEnemy == row + 4 && encounteredEnemyY == col + 4)) {//we have enough room to make a 4, check upwards to see if we can make a 5 (-O-X-XXO--)
                                   if (board[row + 1][col - 1] != enemy) {
                                       evaluation += THREES_POINTS;
                                       // debug
                                       System.out.println("[BR-D](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                                   }
                               } else if (row - 2 >= 0 && col - 2 >= 0 && (encounteredEnemy == row + 3 && encounteredEnemyY == col + 3)) {//we are stuck on 3, check upwards 2 to see if we can make it a 5
                                   evaluation += THREES_POINTS;
                                   // debug
                                   System.out.println("[BR-D](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                               }
                           }
                       } else if (count == 4 && (col - 1 < 0 && row - 1 < 0) && encounteredEnemy == -1) {//havent encountered an enemy before seeing our player
                           // debug
                           System.out.println("[BR-D](0)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           evaluation += FOURS_POINTS;
                       } else if (encounteredEnemy > -1 && (row + 5 >= boardLength || row - 1 < 0) && (col + 5 >= boardLength || col - 1 < 0)) {
                           //enemy is blocking us at the edge of the board (OXXXX)
                           // debug
                           System.out.println("[BR-D]BLOCKING ON EDGE!!!!!!");
                       } else { //check for the straight four
                           int[] newChars = new int[6];
                           String rowString = new String();

                           for (int b = row - 1, c = col - 1, i = 0; b < boardLength && b < row + 5 && b >= 0 && c >= 0; b++, c++, i++) {
                               // debug
                               System.out.println(b + " " + c);
                               newChars[i] = board[b][c];
                               rowString += newChars[i];
                           }


                           if (isStraightFour(rowString, player)) {//If it is a straight 4
                               // debug
                               System.out.println("[BR-D](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + STRAIGHT_FOUR_POINTS);
                               evaluation += STRAIGHT_FOUR_POINTS;
                           } else if (encounteredEnemy == -1) {//If it is possible to have a straight 4, and we have not encountered an enemy while searching
                               evaluation += FOURS_POINTS;
                               // debug
                               System.out.println("[BR-D](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           } else { //If it is possible to have a straight 4, but we have encountered an enemy while searching, check if there is room on left
                               if (row-1 >= 0 && col-1 >= 0 && board[row - 1][col - 1] != enemy) {
                                   evaluation += FOURS_POINTS;
                                   // debug
                                   System.out.println("[BR-D](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                               }
                           }
                       }

                    } // FINISH BOTTOM RIGHT DIAGONAL

                    if(evaluation > maxUtility[row][col]){
                       maxUtility[row][col] = evaluation;
                    }
                    evaluation = 0;

                    encounteredEnemy = -1;
                    encounteredEnemyY = -1;
                    //====================CHECK TOP-RIGHT DIAGONALLY====================
                    if (col + 4 < boardLength && row - 4 >= 0) {//to be sure there can actually be a 5-in-a-row to this direction

                       count = 1; //Sum of how many of our players we encounter in the next 4 spaces
                       for (int x = row - 1, y = col + 1; x > row - 5 && y < col + 5; x--, y++) {
                           if (board[x][y] == player) {
                               count++;
                           } else if (board[x][y] == enemy) {
                               encounteredEnemy = x;
                               encounteredEnemyY = y;
                               break;
                           }
                       }

                       if (count < 3 || count == 5) {
                           evaluation += getPointsToAdd(count);
                           // debug
                           System.out.println("[TR-D]BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + getPointsToAdd(count));
                       } else if (count == 3) {
                           if (encounteredEnemy == -1) {
                               evaluation += THREES_POINTS;
                               // debug
                               System.out.println("[TR-D](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                           } else if (lastEnemyEncounteredRow > -1) {//we encountered an enemy before seeing our player
                               if ((row - 1 >= 0 && col - 1 >= 0) && (encounteredEnemy == row - 4 && encounteredEnemyY == col + 4)) {//we have enough room to make a 4, check upwards to see if we can make a 5 (-O-X-XXO--)
                                   if (board[row - 1][col - 1] != enemy) {
                                       evaluation += THREES_POINTS;
                                       // debug
                                       System.out.println("[TR-D](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                                   }
                               } else if (row + 2 < boardLength && col - 2 >= 0 && (encounteredEnemy == row - 3 && encounteredEnemyY == col + 3)) {//we are stuck on 3, check upwards 2 to see if we can make it a 5
                                   evaluation += THREES_POINTS;
                                   // debug
                                   System.out.println("[TR-D](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + THREES_POINTS);
                               }
                           }
                       } else if (count == 4 && (col - 1 < 0 && row + 1 >= boardLength) && encounteredEnemy == -1) {//havent encountered an enemy before seeing our player
                           // debug
                           System.out.println("[TR-D](0)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           evaluation += FOURS_POINTS;
                       } else if (encounteredEnemy > -1 && (row - 5 < 0 || row + 1 >= boardLength) && (col + 5 >= boardLength || col - 1 < 0)) {
                           //enemy is blocking us at the edge of the board (OXXXX)
                           // debug
                           System.out.println("[TR-D]BLOCKING ON EDGE!!!!!!");
                       } else { //check for the straight four
                           int[] newChars = new int[6];
                           String rowString = new String();

                           for (int b = row + 1, c = col - 1, i = 0; b < boardLength && b > row - 5 && c >= 0; b--, c++, i++) {
                               newChars[i] = board[b][c];
                               rowString += newChars[i];
                           }

                           if (isStraightFour(rowString, player)) {//If it is a straight 4
                               // debug
                               System.out.println("[TR-D](1)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + STRAIGHT_FOUR_POINTS);
                               evaluation += STRAIGHT_FOUR_POINTS;
                           } else if (encounteredEnemy == -1) {//If it is possible to have a straight 4, and we have not encountered an enemy while searching
                               evaluation += FOURS_POINTS;
                               // debug
                               System.out.println("[TR-D](2)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                           } else { //If it is possible to have a straight 4, but we have encountered an enemy while searching, check if there is room on left

                               if (board[row + 1][col - 1] != enemy) {
                                   evaluation += FOURS_POINTS;
                                   // debug
                                   System.out.println("[TR-D](3)BOARD[" + row + "][" + col + "]: ADDED UTILITY VALUE OF: " + FOURS_POINTS);
                               }

                           }
                       }
                    }//FINISH TOP-RIGHT DIAGONAL SEARCH

                    if(evaluation > maxUtility[row][col]){
                        maxUtility[row][col] = evaluation;
                    }

                }
            } //inner (column) loop
        } //outer (row) loop

        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {
                evaluation += maxUtility[i][j];
            }
        }

        return evaluation;
   }

    public boolean myTurn(){
        switch(player){
            case 'o':
                return moveCount == 1 || moveCount % 2 == 1;

            case 'x':
                return moveCount == 0 || moveCount % 2 == 0;
        }

        return false;
    }
}
