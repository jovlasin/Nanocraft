package com.nanocraft.game.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.nanocraft.game.core.GameHandler;

public class Entity {
    public GameHandler gh;
    public int worldX, worldY;
    public String direction;
    public int spriteCounter = 0;
    public int spriteNum;
    public int speed;
    public boolean collisionOn = false;
    
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public Rectangle solidArea;

    public Entity(GameHandler gh) {
        this.gh = gh;
        this.solidArea = new Rectangle();
        this.direction = "down";
        this.spriteNum = 1;
    }
}