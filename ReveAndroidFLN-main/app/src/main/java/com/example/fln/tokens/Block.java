package com.example.fln.tokens;

public class Block {
    public final int row;
    public final int col;
    public final BaseValue value;

    public Block(int row, int col, BaseValue value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public boolean equals(Block b) {
        return this.row == b.row && this.col == b.col && this.value.equals(b.value);
    }
}
