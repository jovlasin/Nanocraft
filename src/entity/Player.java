package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import core.GameHandler;
import input.KeyHandler;

public class Player extends Entity {
    public final int screenX = gh.screenWidth / 2 - gh.tileSize / 2;
    public final int screenY = gh.screenHeight / 2 - gh.tileSize / 2;
    private int standCounter;
    private KeyHandler kh;

    public Player(GameHandler gh, KeyHandler kh) {
        super(gh);
        this.kh = kh;

        setStats();
        setPos();
        getImage();
    }

    public void update() {
        if (kh.up == true || kh.down == true || kh.left == true || kh.right == true) {
            
            if (kh.up == true) {
                direction = "up";
            }

            else if (kh.down == true) {
                direction = "down";
            }

            else if (kh.left == true) {
                direction = "left";
            }

            else if (kh.right == true) {
                direction = "right";
            }

            collisionOn = false;
            checkTileCollision();

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

    private void checkTileCollision() {
        int nextWorldX = worldX;
        int nextWorldY = worldY;

        switch (direction) {
            case "up":
                nextWorldY -= speed;
            break;

            case "down":
                nextWorldY += speed;
            break;

            case "left":
                nextWorldX -= speed;
            break;

            case "right":
                nextWorldX += speed;
            break;
        }

        int leftCol = (nextWorldX + solidArea.x) / gh.tileSize;
        int rightCol = (nextWorldX + solidArea.x + solidArea.width - 1) / gh.tileSize;
        int topRow = (nextWorldY + solidArea.y) / gh.tileSize;
        int bottomRow = (nextWorldY + solidArea.y + solidArea.height - 1) / gh.tileSize;

        if (gh.th.isCollisionAt(leftCol, topRow) ||
            gh.th.isCollisionAt(rightCol, topRow) ||
            gh.th.isCollisionAt(leftCol, bottomRow) ||
            gh.th.isCollisionAt(rightCol, bottomRow)) {
            collisionOn = true;
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
        up1 = scale("/player/playerWalkU1", gh.tileSize, gh.tileSize);
        up2 = scale("/player/playerWalkU2", gh.tileSize, gh.tileSize);
        down1 = scale("/player/playerWalkD1", gh.tileSize, gh.tileSize);
        down2 = scale("/player/playerWalkD2", gh.tileSize, gh.tileSize);
        left1 = scale("/player/playerWalkL1", gh.tileSize, gh.tileSize);
        left2 = scale("/player/playerWalkL2", gh.tileSize, gh.tileSize);
        right1 = scale("/player/playerWalkR1", gh.tileSize, gh.tileSize);
        right2 = scale("/player/playerWalkR2", gh.tileSize, gh.tileSize);
    }

    private void setStats() {
        speed = 4;
    }

    private void setPos() {
        worldX = gh.tileSize * 50;
        worldY = gh.tileSize * 16;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public BufferedImage scale(String imgPath, int width, int height ) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imgPath + ".png"));
            image = scaleImage(image, width, height);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        
        return scaledImage;
    }
}

