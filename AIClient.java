package gomoku;
import sun.util.resources.ar.CurrencyNames_ar_MA;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

class Tile {
    public int row = -1;
    public int col = -1;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isValid() {
        return (row != -1 && col != -1);
    }
}

class Row {
    public Tile startPos;
    public int length = 0;

    public Row(Tile startPos, int length) {
        this.startPos = startPos;
        this.length = length;
    }
}

public class AIClient extends GomokuClient {
    @Override
    protected void handlePlayMessage(String responseLine) {
        int[] detail = GomokuProtocol.getPlayDetail(responseLine);
        int color = detail[0];
        int row = detail[1];
        int col = detail[2];
        gameboard[row][col] = color + 1; // probably off by 1
        // send message to gameboard that the opponent has played
        layout.placeGamePiece(row, col, color);

        if (layout.isMyTurn) {
            // TODO: get rid of sleep
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
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
        defend();
        attack();
    }

    private void defend() {
        // up here, call findFour(!isBlack) i.e. look for opponent
    }

    private void attack() {
        System.out.println("ready to attack");
        Tile targetTile = findTarget(isBlack);
        if (targetTile.isValid()) {
            System.out.println("yooooo");
            placeGamePiece(targetTile.row, targetTile.col);
        }
        else {
            System.out.println("didn't find any tiles of my color, so we goin random");
            // we have not placed any pieces
            int randomRow = ThreadLocalRandom.current().nextInt(0, 15);
            int randomCol = ThreadLocalRandom.current().nextInt(0, 15);

            while (gameboard[randomRow][randomCol] != 0) {
                randomRow = ThreadLocalRandom.current().nextInt(0, 15);
                randomCol = ThreadLocalRandom.current().nextInt(0, 15);
            }
            placeGamePiece(randomRow, randomCol);
        }
    }

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