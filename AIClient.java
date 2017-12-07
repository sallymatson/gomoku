package gomoku;
import java.util.concurrent.ThreadLocalRandom;

public class AIClient extends GomokuClient {

    StringBuilder rows = new StringBuilder(233);
    StringBuilder cols = new StringBuilder(233);
    StringBuilder posDiag;
    StringBuilder negDiag;

    int posDiagonalIndex(int row, int col){
        // TODO
        int strIndex = 2;
        return strIndex;
    }

    int negDiagonalIndex(int row, int col){
        // TODO
        int strIndex = 0;
        return strIndex;
    }

    int rowIndex(int row, int col){
        return col + row*16;
    }

    int colIndex(int row, int col){
        return row + col*16;
    }

    void initializeStrings(){
        // initialize row and col strings
        for (int i = 15; i <= 232; i+=16){
            rows.setCharAt(i, 'X');
            cols.setCharAt(i, 'X');
        }
        // initialize pos/neg diagonal string
        int counter = 2;
        for (int i = 1; i<135; i+=counter){
            posDiag.setCharAt(i, 'X');
            negDiag.setCharAt(i, 'X');
            counter++;
        }

    }

    @Override
    protected void handlePlayMessage(String responseLine) {
        int[] detail = GomokuProtocol.getPlayDetail(responseLine);
        int color = detail[0];
        int row = detail[1];
        int col = detail[2];
        gameboard[row][col] = color; // probably off by 1
        // send message to gameboard that the opponent has played
        layout.placeGamePiece(row, col, color);

        if (layout.isMyTurn) {
            try {
                Thread.sleep(3000);
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
        // TODO: respond and place a piece!

        int playRow = ThreadLocalRandom.current().nextInt(0, 15);
        int playCol = ThreadLocalRandom.current().nextInt(0, 15);

        while (gameboard[playRow][playCol] != 0) {
            playRow = ThreadLocalRandom.current().nextInt(0, 15);
            playCol = ThreadLocalRandom.current().nextInt(0, 15);
        }
        placeGamePiece(playRow, playCol);
    }
}
