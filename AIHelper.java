package gomoku;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

enum Direction {
    Row, Column, PosDiag, NegDiag
}

class Converter {

    public static Hashtable<Integer, Tile> indexToTilePosDiag;
    public static Hashtable<Tile, Integer> tileToIndexPosDiag;
    public static Hashtable<Integer, Tile> indexToTileNegDiag;
    public static Hashtable<Tile, Integer> tileToIndexNegDiag;

    static {
        System.out.println("I LIKE JAVA AGAINNNNNNNNNNN");
        indexToTilePosDiag = new Hashtable<Integer, Tile>();
        indexToTileNegDiag = new Hashtable<Integer, Tile>();
        tileToIndexNegDiag = new Hashtable<Tile, Integer>();
        tileToIndexPosDiag = new Hashtable<Tile, Integer>();
        int rowNeg = 14, colNeg = 0, rowStartNeg = 13, colStartNeg = 1;
        int rowPos = 0, colPos = 0, rowStartPos = 1, colStartPos = 1;
        Integer i = 0;
        while (rowStartNeg >= 0){
            indexToTileNegDiag.put(i, new Tile(rowNeg, colNeg));
            tileToIndexNegDiag.put(new Tile(rowNeg, colNeg), i);
            indexToTilePosDiag.put(i, new Tile(rowPos, colPos));
            tileToIndexPosDiag.put(new Tile(rowPos, colPos), i);
            System.out.println("Index: " + i + " Row: " + rowPos + " Col: " + colPos);
            if (rowNeg == 14){
                i++;
                System.out.println("*************************");
                rowNeg = rowStartNeg;
                rowStartNeg--;
                colNeg = 0;
                rowPos = rowStartPos;
                rowStartPos++;
                colPos = 0;
            } else {
                rowNeg++;
                colNeg++;
                rowPos--;
                colPos++;
            }
            i++;
        }
        System.out.println("SKUFHSDJHFSDHKFSDJHF");
        while (colStartNeg <= 15){
            indexToTileNegDiag.put(i, new Tile(rowNeg, colNeg));
            tileToIndexNegDiag.put(new Tile(rowNeg, colNeg), i);
            indexToTilePosDiag.put(i, new Tile(rowPos, colPos));
            tileToIndexPosDiag.put(new Tile(rowPos, colPos), i);
            System.out.println("Index: " + i + " Row: " + rowPos + " Col: " + colPos);
            if (colNeg == 14){
                i++;
                System.out.println("*************************");
                rowNeg = 0;
                colNeg = colStartNeg;
                colStartNeg++;
                rowPos = 14;
                colPos = colStartPos;
                colStartPos++;
            } else {
                rowNeg++;
                colNeg++;
                rowPos--;
                colPos++;
            }
            i++;
        }
    }
}

class Tile {
    public int row = -1;
    public int col = -1;

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
            row = Converter.indexToTilePosDiag.get(index).row;
            col = Converter.indexToTilePosDiag.get(index).col;
        }
        else if (direction == Direction.NegDiag) {
            row = Converter.indexToTileNegDiag.get(index).row;
            col = Converter.indexToTileNegDiag.get(index).col;
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
            Hashtable<Tile, Integer> sdf = Converter.tileToIndexPosDiag;
            return Converter.tileToIndexPosDiag.get(this);
        }
        else if (direction == Direction.NegDiag) {
            return Converter.tileToIndexNegDiag.get(this);
        }
        else {
            return -1;
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

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
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
                move.state = new StringBuilder(move.state.toString().replace('1', '2'));
            }
            for (NextMove move : threeCases) {
                move.state = new StringBuilder(move.state.toString().replace('1', '2'));
            }
            for (NextMove move : twoCases) {
                move.state = new StringBuilder(move.state.toString().replace('1', '2'));
            }
            for (NextMove move : oneCases) {
                move.state = new StringBuilder(move.state.toString().replace('1', '2'));
            }
        }
    }

}
