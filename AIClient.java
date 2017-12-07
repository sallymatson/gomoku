package gomoku;
import sun.util.resources.ar.CurrencyNames_ar_MA;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
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

    @Override
    public void setupConnection(String host, int portNumber, boolean isAI) {
        super.setupConnection(host, portNumber, isAI);
        layout.buttonGiveUp.setEnabled(false);
        layout.buttonReset.setEnabled(false);
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
        Tile placedTile = new Tile(row, col);
        // update StringBuilders
        rows.setCharAt(placedTile.getIndex(Direction.Row), Character.forDigit(color + 1, 10));
        cols.setCharAt(placedTile.getIndex(Direction.Column), Character.forDigit(color + 1, 10));
        posDiag.setCharAt(placedTile.getIndex(Direction.PosDiag), Character.forDigit(color + 1, 10));
        negDiag.setCharAt(placedTile.getIndex(Direction.NegDiag), Character.forDigit(color + 1, 10));
        // send message to gameboard that the opponent has played
        layout.placeGamePiece(row, col, color);

        if (layout.isMyTurn) {
            try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            play();
        }
    }

    @Override
    public void resetGameBoard() {
        super.resetGameBoard();
        initializeStrings();
    }

    @Override
    protected void startGame() {
        if (layout.isMyTurn) {
            play();
        }
    }

    private void play() {
        Tile targetTile = new Tile(-1, -1);
        GameStates gameStatesOffense = new GameStates(isBlack);
        GameStates gameStatesDefense = new GameStates(!isBlack);

        // if we can win (find a 4) -> win
        // look for 4 in 5 of color isBlack
        targetTile = checkCases(gameStatesOffense.fourCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can block them from winning (find a 4) -> block
        targetTile = checkCases(gameStatesDefense.fourCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 4 trap (find a 3) -> place
        targetTile = checkCases(gameStatesOffense.threeCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can block a 4 trap (find a 3) -> block
        targetTile = checkCases(gameStatesDefense.threeCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 3 trap (find a 2) -> place
        targetTile = checkCases(gameStatesOffense.twoCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 2 trap (find a 1) -> place
        targetTile = checkCases(gameStatesOffense.oneCases);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a 1 trap (find an empty 6) -> place
        targetTile = lookForEmpty(6);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // if we can place a regular 1 (find an empty 5) -> place
        targetTile = lookForEmpty(5);
        if (targetTile.isValid()) {
            placeGamePiece(targetTile.row, targetTile.col);
            return;
        }
        // petty defense
        // TODO: TDB
        // ...
        // if none of the above cases
        placeRandom();
    }

    private Tile checkCases(ArrayList<NextMove> cases) {
        int index = -1;
        for (NextMove move : cases) {
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

    private Tile lookForEmpty(int length) {
        int index = -1;
        String empty = new String(new char[length]).replace('\0', '0');
        int randomIndex = ThreadLocalRandom.current().nextInt(0, length);
        // pos diag
        index = posDiag.indexOf(empty);
        if (index != -1) {
            return new Tile(index + randomIndex, Direction.PosDiag);
        }
        // neg diag
        index = negDiag.indexOf(empty);
        if (index != -1) {
            return new Tile(index + randomIndex, Direction.NegDiag);
        }
        // cols
        index = cols.indexOf(empty);
        if (index != -1) {
            return new Tile(index + randomIndex, Direction.Column);
        }
        // rows
        index = rows.indexOf(empty);
        if (index != -1) {
            return new Tile(index + randomIndex, Direction.Row);
        }
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
}
