package com.nanocraft.game.tile;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image;
    public boolean collision;
    public int maxHealth;
    public int replacementTileId;
    public String dropItemType;
    public String requiredItemType;
    public String type;
    public String interactionType;
    public int contactDamage;
    public String targetMapPath;
    public int targetCol = -1;
    public int targetRow = -1;
    public String targetDirection;
}
