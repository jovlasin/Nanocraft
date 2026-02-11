package entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.GameHandler;
import main.KeyHandler;
import object.Key;
import object.OldShield;
import object.Sword;

public class Player extends Entity {
    public final int screenX = gm.screenWidth / 2 - gm.tileSize / 2;
    public final int screenY = gm.screenHeight / 2 - gm.tileSize / 2;
    private int standCounter;
    private KeyHandler kh;

    public Player(GameManager gm, KeyHandler kh) {
        super(gm);
        this.kh = kh;

        setStats();
        setPos();
        getImage();
    }

    public void update() {
        if (kh.upPressed == true || kh.downPressed == true || kh.leftPressed == true || kh.rightPressed == true || kh.spacePressed == true) {
            
            if (kh.upPressed == true) {
                direction = "up";
            }

            else if (kh.downPressed == true) {
                direction = "down";
            }

            else if (kh.leftPressed == true) {
                direction = "left";
            }

            else if (kh.rightPressed == true) {
                direction = "right";
            }

            collisionOn = false;

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

        else {
            standCounter++;

            if (standCounter == 20) {
                spriteNum = 1;
                standCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        switch (direction) {
            case "up":
                if (spriteNum == 1) {
                    image = up1;
                }

                else if (spriteNum == 2) {
                    image = up2;
                }
            break;

            case "down":
                if (spriteNum == 1) {
                    image = down1;
                }
                
                else if (spriteNum == 2) {
                    image = down2;
                }
            break;

            case "left":
                if (spriteNum == 1) {
                    image = left1;
                }
                
                else if (spriteNum == 2) {
                    image = left2;
                }
            break;

            case "right":
                if (spriteNum == 1) {
                    image = right1;
                }

                else if (spriteNum == 2) {
                    image = right2;
                }
            break;
        }
        g2.drawImage(image, screenX, screenY, null);
    }

    private void getImage() {
        up1 = scale("/player/playerWalkU1", gm.tileSize, gm.tileSize);
        up2 = scale("/player/playerWalkU2", gm.tileSize, gm.tileSize);
        down1 = scale("/player/playerWalkD1", gm.tileSize, gm.tileSize);
        down2 = scale("/player/playerWalkD2", gm.tileSize, gm.tileSize);
        left1 = scale("/player/playerWalkL1", gm.tileSize, gm.tileSize);
        left2 = scale("/player/playerWalkL2", gm.tileSize, gm.tileSize);
        right1 = scale("/player/playerWalkR1", gm.tileSize, gm.tileSize);
        right2 = scale("/player/playerWalkR2", gm.tileSize, gm.tileSize);
    }

    private void setStats() {
        speed = 4;
        
    }

    private void setPos() {
        worldX = gm.tileSize * 23;
        worldY = gm.tileSize * 21;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}

