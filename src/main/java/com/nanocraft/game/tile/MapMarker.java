package com.nanocraft.game.tile;

public class MapMarker {
    public final String name;
    public final String type;
    public final int col;
    public final int row;

    public MapMarker(String name, String type, int col, int row) {
        this.name = name;
        this.type = type;
        this.col = col;
        this.row = row;
    }
}
