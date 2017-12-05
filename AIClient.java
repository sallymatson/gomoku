package gomoku;
import java.util.concurrent.ThreadLocalRandom;

public class AIClient extends GomokuClient {
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
