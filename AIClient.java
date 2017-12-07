package gomoku;
import sun.util.resources.ar.CurrencyNames_ar_MA;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

public class AIClient extends GomokuClient {

    StringBuilder rows = new StringBuilder();
    StringBuilder cols = new StringBuilder();
    StringBuilder posDiag = new StringBuilder();
    StringBuilder negDiag = new StringBuilder();

    public AIClient() {
        initializeStrings();
    }

    void initializeStrings(){
        // initialize row and col strings
        for (int i = 0; i < 240; i++){
            if (i % 16 == 15) {
                rows.append('\n');
                cols.append('\n');
            }
            else {
                rows.append('0');
                cols.append('0');
            }
        }
        // initialize pos/neg diagonal string
        int counter = 3;
        int nextBreak = 1;
        boolean counterGrowing = true;
        // each time, counter changes by counter*2 + 1
        for (int i = 0; i < 254; i++){
            if (i == nextBreak) {
                posDiag.append('\n');
                negDiag.append('\n');
                nextBreak += counter;
                if (counter == 16){
                    counterGrowing = false;
                }
                if (counterGrowing) {
                    counter++;
                } else {
                    counter--;
                }
            } else {
                posDiag.append('0');
                negDiag.append('0');
            }
        }
    }

    @Override
    protected void handlePlayMessage(String responseLine) {
        int[] detail = GomokuProtocol.getPlayDetail(responseLine);
        int color = detail[0];
        int row = detail[1];
        int col = detail[2];
        gameboard[row][col] = color + 1; // probably off by 1
        // TODO: update StringBuilders
        // send message to gameboard that the opponent has played
        layout.placeGamePiece(row, col, color);

        if (layout.isMyTurn) {
            play();
        }
    }

    @Override
    protected void startGame() {
        if (layout.isMyTurn) {
            play();
        }
    }

    private void play() {
        Tile targetTile = new Tile(-1, -1);

        // if we can win (find a 4) -> win
        targetTile = lookFor4(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can block them from winning (find a 4) -> block
        targetTile = lookFor4(!isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 4 trap (find a 3) -> place
        targetTile = lookFor3(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can block a 4 trap (find a 3) -> block
        targetTile = lookFor3(!isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 3 trap (find a 2) -> place
        targetTile = lookFor2(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 2 trap (find a 1) -> place
        targetTile = lookFor1(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 1 trap (find an empty 6) -> place
        targetTile = lookForEmpty6(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a regular 1 (find an empty 5) -> place
        targetTile = lookForEmpty5(isBlack);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // defend
        // TODO: TDB
        // ...
        // if none of the above cases
        placeRandom();
    }

    private Tile lookFor4(boolean isBlack) {
        // look for 4 in 5 of color isBlack
        GameStates gameStates = new GameStates(isBlack);
        int index = -1;
        for (NextMove move : gameStates.fourCases) {
            // pos diag
            index = posDiag.indexOf(move.state.toString());
            if (index != -1) {
                return new Tile(index + move.nextMove, Direction.PosDiag);
            }
            // neg diag
            index = negDiag.indexOf(move.state.toString());
            if (index != -1) {
                return new Tile(index + move.nextMove, Direction.NegDiag);
            }
            // cols
            index = cols.indexOf(move.state.toString());
            if (index != -1) {
                return new Tile(index + move.nextMove, Direction.Column);
            }
            // rows
            index = rows.indexOf(move.state.toString());
            if (index != -1) {
                return new Tile(index + move.nextMove, Direction.Row);
            }
        }
        // if not found, return invalid Tile
        return new Tile(-1, -1);
    }

    private Tile lookFor3(boolean isBlack) {
        // TODO: look for _____
        // TODO: if found, return _____
        return new Tile(-1, -1);
    }

    private Tile lookFor2(boolean isBlack) {
        // TODO: look for _____
        // TODO: if found, return _____
        return new Tile(-1, -1);
    }

    private Tile lookFor1(boolean isBlack) {
        // TODO: look for _____
        // TODO: if found, return _____
        return new Tile(-1, -1);
    }

    private Tile lookForEmpty6(boolean isBlack) {
        // TODO: look for _____
        // TODO: if found, return _____
        return new Tile(-1, -1);
    }

    private Tile lookForEmpty5(boolean isBlack) {
        // TODO: look for _____
        // TODO: if found, return _____
        return new Tile(-1, -1);
    }

    private void placeRandom() {
        System.out.println("didn't find any tiles of my color, so we goin random");
        // TODO: loop through all indices and find empty ones -> put them in a list
        // TODO: pick random index of that list -> place at that index

        // we have not placed any pieces
        int randomRow = ThreadLocalRandom.current().nextInt(0, 15);
        int randomCol = ThreadLocalRandom.current().nextInt(0, 15);

        while (gameboard[randomRow][randomCol] != 0) {
            randomRow = ThreadLocalRandom.current().nextInt(0, 15);
            randomCol = ThreadLocalRandom.current().nextInt(0, 15);
        }
        placeGamePiece(randomRow, randomCol);
    }

    // ****************************************************************************

    private Tile findTarget(boolean isBlack) {
        Row longestRow = findLongestRow(isBlack);
        if (longestRow.length == 0) {
            return new Tile(-1, -1);
        }
        // at this point, we're guaranteed to have a spot to put it
        if (gameboard[longestRow.startPos.row][longestRow.startPos.col - 1] == 0) {
            return new Tile(longestRow.startPos.row, longestRow.startPos.col - 1);
        }
        else if (gameboard[longestRow.startPos.row][longestRow.startPos.col + longestRow.length] == 0) {
            return new Tile(longestRow.startPos.row, longestRow.startPos.col + longestRow.length);
        }
        else {
            System.out.println("Something got fucked up somehow...");
            return new Tile(-1, -1);
        }
        // findLongestColumn
        // (find longest sub-column of color isBlack)
        // findLongestDiagonal
        // (find longest sub-diagonal of color isBlack)
    }

    private Row findLongestRow(boolean isBlack) {
        // TODO: set this as a const in the class?
        final int maxPossible = 4; // use this to break out early if you find a 4
        int myColor = (isBlack ? 2 : 1);
        // find longest sub-row of color isBlack
        int longestRow = 0;
        Tile longestRowStartPos = new Tile(-1, -1);
        for (int row = 0; row < 15; row++) { // TODO: boardWidth
            int rowMax = 0;
            int rowMaxStartCol = -1;
            int currMax = 0;
            int currStartCol = -1;
            int col;
            for (col = 0; col < 15; col++) {
                if (gameboard[row][col] == myColor) {
                    if (currMax == 0) {
                        currStartCol = col;
                    }
                    currMax++;
                    if (currMax > rowMax) {
                        // TODO: somehow account for whether the row has empty space on either side
                        // while still finding max row in THIS row
                        // (problem is that e.g. we have a 3-row with space but a 4-row with no space,
                        // we'll skip the 3 row for the 4 row but only find out it doesn't have space
                        // outside the loop at the bottom
                        rowMax = currMax;
                        rowMaxStartCol = currStartCol;
                        if (currMax == maxPossible) {
                            // don't need to keep looking, we found a 4
                            break;
                        }
                    }
                } else {
                    currStartCol = -1;
                    currMax = 0;
                }
            }
            if (rowMax > longestRow) {
                if (rowMax > 0) {
                    // if up against left side of board, check if you can add to the right end of the row
                    if (rowMaxStartCol == 0) {
                        if (gameboard[row][rowMaxStartCol + rowMax] != 0)
                            continue;
                    }
                    // if up against right side of board, check if you can add to the left end of the row
                    else if (rowMaxStartCol + rowMax > 14) {
                        if (gameboard[row][rowMaxStartCol - 1] != 0)
                            continue;
                    }
                    // if in the middle, check both
                    else {
                        if (gameboard[row][rowMaxStartCol - 1] != 0 &&
                            gameboard[row][rowMaxStartCol + rowMax] != 0) {
                            continue;
                        }
                    }

                    // the row has space to be lengthened on at least one side
                    longestRow = rowMax;
                    longestRowStartPos = new Tile(row, rowMaxStartCol);
                    if (rowMax == maxPossible) {
                        // don't need to keep looking, we found a 4
                        break;
                    }
                }
            }
        }

        return new Row(longestRowStartPos, longestRow);
    }
}
