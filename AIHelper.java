package gomoku;

import java.util.ArrayList;
import java.util.Hashtable;

enum Direction {
    Row, Column, PosDiag, NegDiag
}

class Tile {
    public int row = -1;
    public int col = -1;

    public Hashtable<Integer, Tile> indexToTilePosDiag;
    public Hashtable<Tile, Integer> tileToIndexPosDiag;
    public Hashtable<Integer, Tile> indexToTileNegDiag;
    public Hashtable<Tile, Integer> tileToIndexNegDiag;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Tile(int index, Direction direction) {
        // convert index to row, col
        if (direction == Direction.Row) {
            row = index / 16;
            col = index % 16;
        }
        else if (direction == Direction.Column) {
            col = index / 16;
            row = index % 16;
        }
        else if (direction == Direction.PosDiag) {
            row = indexToTilePosDiag.get(index).row;
            col = indexToTilePosDiag.get(index).col;
        }
        else if (direction == Direction.NegDiag) {
            row = indexToTileNegDiag.get(index).row;
            col = indexToTileNegDiag.get(index).col;
        }
    }

    public int getIndex(Direction direction) {
        // convert row, col to index
        if (direction == Direction.Row) {
            return col + row * 16;
        }
        else if (direction == Direction.Column) {
            return row + col * 16;
        }
        else if (direction == Direction.PosDiag) {
            return tileToIndexPosDiag.get(this);
        }
        else if (direction == Direction.NegDiag) {
            return tileToIndexNegDiag.get(this);
        }
    }

    public boolean isValid() {
        return (row != -1 && col != -1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tile)) {
            return false;
        }
        Tile t = (Tile)obj;
        return (t.row == row && t.col == col);
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

class NextMove {
    public StringBuilder state;
    public int nextMove;

    public NextMove(StringBuilder state, int nextMove) {
        this.state = state;
        this.nextMove = nextMove;
    }
}

class GameStates {
    public ArrayList<NextMove> fourCases;
    public ArrayList<NextMove> threeCases;
    public ArrayList<NextMove> twoCases;
    public ArrayList<NextMove> oneCases;

    public GameStates(boolean isBlack) {
        fourCases = new ArrayList<NextMove>() {{
            add(new NextMove(new StringBuilder("01111"), 0));
            add(new NextMove(new StringBuilder("10111"), 1));
            add(new NextMove(new StringBuilder("11011"), 2));
            add(new NextMove(new StringBuilder("11101"), 3));
            add(new NextMove(new StringBuilder("11110"), 4));
        }};
        threeCases = new ArrayList<NextMove>() {{
            add(new NextMove(new StringBuilder("011010"), 3));
            add(new NextMove(new StringBuilder("010110"), 2));
            add(new NextMove(new StringBuilder("001110"), 1));
            add(new NextMove(new StringBuilder("011100"), 4));
        }};
        twoCases = new ArrayList<NextMove>() {{
            add(new NextMove(new StringBuilder("011000"), 4));
            add(new NextMove(new StringBuilder("010100"), 4));
            add(new NextMove(new StringBuilder("010010"), 3));
            add(new NextMove(new StringBuilder("000110"), 1));
            add(new NextMove(new StringBuilder("001010"), 1));
        }};
        oneCases = new ArrayList<NextMove>() {{
            add(new NextMove(new StringBuilder("010000"), 2));
            add(new NextMove(new StringBuilder("001000"), 1));
            add(new NextMove(new StringBuilder("000100"), 4));
            add(new NextMove(new StringBuilder("000010"), 3));
        }};

        if (isBlack) {
            for (NextMove move : fourCases) {
                move.state.replace('1', '2', move.state.toString());
            }
            for (NextMove move : threeCases) {
                move.state.replace('1', '2', move.state.toString());
            }
            for (NextMove move : twoCases) {
                move.state.replace('1', '2', move.state.toString());
            }
            for (NextMove move : oneCases) {
                move.state.replace('1', '2', move.state.toString());
            }
        }
    }

}
