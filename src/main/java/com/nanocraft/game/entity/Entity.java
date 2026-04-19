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
    public int spriteCounter, actionCounter, spriteNum, invincibleCounter, shotCounter;
    public int speed, maxLife, maxMana, mana, life, level, strength, dexterity, attack, defense, exp, nextLevelExp, coin;
    public Entity currentWeapon, currentShield;
    public Projectile projectile;
    public boolean collisionOn, collision;
    public String name;
    public String description;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2, attackLeft1, attackLeft2, attackRight1, attackRight2;
    public BufferedImage image, image2, image3;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public Rectangle solidArea;
    public Rectangle attackArea;
    public int dialogueIndex;
    public String[] dialogues;
    public boolean invincible;
    public boolean dying;
    public boolean alive;
    public boolean attacking;
    public int attackValue;

    public final int player = 0;
    public final int npc = 1;
    public final int monster = 2;
    public final int sword = 3;
    public final int consumable = 6;
    public int type;

    public Entity(GameHandler gh) {
        this.gh = gh;
        this.solidArea = new Rectangle();
        this.attackArea = new Rectangle(0, 0, 0, 0);
        this.direction = "down";
        this.spriteNum = 1;
        this.dialogues = new String[20];
        this.alive = true;
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

    public void setAction() {}

    public void update() {
        setAction();

        collisionOn = false;
        gh.ch.checkTile(this);
        gh.ch.checkObject(this, false);
        gh.ch.checkEntity(this, gh.npcs);
        gh.ch.checkEntity(this, gh.monsters);
        boolean contact = gh.ch.checkPlayer(this);

        if (type == monster && contact == true) {
            // damage(attack);
        }

        if (collisionOn == false) {
            switch (direction) {
                case "up":
                    worldY -= speed;
                break;
            
                case "down":
                    worldY += speed;
                break;

                case "left":
                    worldX -= speed;
                break;

                case "right":
                    worldX += speed;
                break;
            }
        }
        
        spriteCounter++;

        if (spriteCounter > 14) {
            if (spriteNum == 1) {
                spriteNum = 2;
            }

            else if (spriteNum == 2) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        }
    }

    public void speak() {
        if (dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
        }

        gh.ui.currentDialogue = dialogues[dialogueIndex];
        dialogueIndex++;

        switch (gh.player.direction) {
            case "up":
                direction = "down";    
            break;

            case "down":
                direction = "up";    
            break;

            case "left":
                direction = "right";    
            break;

            case "right":
                direction = "left";    
            break;
        }
    }
}