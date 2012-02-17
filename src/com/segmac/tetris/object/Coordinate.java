package com.segmac.tetris.object;

public class Coordinate {
    public int column;
    public int row;

    public Coordinate(int coloum, int row) {
        this.column = coloum;
        this.row = row;
    }

    public boolean equals(Coordinate other) {
        if (column == other.column && row == other.row) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Coordinate: [" + column + "," + row + "]";
    }
}
