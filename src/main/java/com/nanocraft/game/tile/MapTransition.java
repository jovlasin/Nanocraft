package com.nanocraft.game.tile;

public class MapTransition {
    public final int sourceCol;
    public final int sourceRow;
    public final int width;
    public final int height;
    public final String targetMapPath;
    public final int targetCol;
    public final int targetRow;
    public final String targetDirection;

    public MapTransition(int sourceCol, int sourceRow, int width, int height, String targetMapPath, int targetCol, int targetRow, String targetDirection) {
        this.sourceCol = sourceCol;
        this.sourceRow = sourceRow;
        this.width = width;
        this.height = height;
        this.targetMapPath = targetMapPath;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
        this.targetDirection = targetDirection;
    }

    public boolean contains(int col, int row) {
        return col >= sourceCol &&
            col < sourceCol + width &&
            row >= sourceRow &&
            row < sourceRow + height;
    }
}
