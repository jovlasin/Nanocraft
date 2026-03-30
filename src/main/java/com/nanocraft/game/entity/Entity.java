package com.nanocraft.game.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.tile.ResourceLoader;


public class Entity {
    public GameHandler gh;
    public int worldX, worldY;
    public String direction;
    public int spriteCounter = 0;
    public int spriteNum;
    public int speed;
    public boolean collisionOn, collision;
    public String name;
    public String description;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public Rectangle solidArea;

    public Entity(GameHandler gh) {
        this.gh = gh;
        this.solidArea = new Rectangle();
        this.direction = "down";
        this.spriteNum = 1;
    }

    public BufferedImage scale(String imgPath, int width, int height ) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imgPath + ".png"));
            image = ResourceLoader.scaleImage(image, width, height);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int screenX = worldX - gh.player.worldX + gh.player.screenX;
        int screenY = worldY - gh.player.worldY + gh.player.screenY;

        if (worldX + gh.tileSize > gh.player.worldX - gh.player.screenX && 
            worldX - gh.tileSize < gh.player.worldX + gh.player.screenX && 
            worldY + gh.tileSize > gh.player.worldY - gh.player.screenY && 
            worldY - gh.tileSize < gh.player.worldY + gh.player.screenY) {
                
            switch (direction) {
                case "up":
                    if (spriteNum == 1) {image = up1;}
                    else if (spriteNum == 2) {image = up2;}
                break;

                case "down":
                    if (spriteNum == 1) {image = down1;}
                    else if (spriteNum == 2) {image = down2;}
                break;

                case "left":
                    if (spriteNum == 1) {image = left1;}
                    else if (spriteNum == 2) {image = left2;}
                break;

                case "right":
                    if (spriteNum == 1) {image = right1;}
                    else if (spriteNum == 2) {image = right2;}
                break;
            }

            g2.drawImage(image, screenX, screenY, null);
        }
    }
}