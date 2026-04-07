package com.nanocraft.game.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.input.KeyHandler;

public class Player extends Entity {
    private static final int MINE_COOLDOWN_TICKS = 8;
    public final int screenX = gh.screenWidth / 2 - gh.tileSize / 2;
    public final int screenY = gh.screenHeight / 2 - gh.tileSize / 2;
    private int standCounter;
    private int mineCooldownTicks;
    private boolean mineRequested;
    private KeyHandler kh;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 20;

    public Player(GameHandler gh, KeyHandler kh) {
        super(gh);
        this.kh = kh;

        setStats();
        setPos();
        getImage();
    }

    public int getAttack() {
        // attackArea = currentWeapon.attackArea;
        // return strength * currentWeapon.attackValue;
        return -999;
    }

    public int getDefense() {
        // return dexterity * currentShield.defenseValue;
        return -999;
    }

    public void update() {
        if (mineCooldownTicks > 0) {
            mineCooldownTicks--;
        }

        if (mineRequested && mineCooldownTicks == 0) {
            attemptMine();
            mineCooldownTicks = MINE_COOLDOWN_TICKS;
        }
        mineRequested = false;

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
            gh.ch.checkTile(this);

            int objIndex = gh.ch.checkObject(this, true);
            acquireObject(objIndex);

            gh.ch.checkEntity(this, gh.npcs);

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

    public void requestMine() {
        mineRequested = true;
    }

    public void attemptMine() {
        int centerX = worldX + solidArea.x + (solidArea.width / 2);
        int centerY = worldY + solidArea.y + (solidArea.height / 2);

        switch (direction) {
            case "up":
                centerY -= gh.tileSize;
            break;

            case "down":
                centerY += gh.tileSize;
            break;

            case "left":
                centerX -= gh.tileSize;
            break;

            case "right":
                centerX += gh.tileSize;
            break;

            default:
            break;
        }

        int targetCol = centerX / gh.tileSize;
        int targetRow = centerY / gh.tileSize;
        gh.th.damageBreakableTile(targetCol, targetRow, 1);
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
        maxLife = 6;
        life = maxLife;
        level = 1;
        strength = 1;
        dexterity = 1;
        exp = 0;
        nextLevelExp = 5;
        coin = 0;
        currentWeapon = new Sword(gh);
        currentShield = new OldShield(gm);
        projectile = new Arrow(gm);
        attack = getAttack();
        defense = getDefense();

    }

    private void setPos() {
        worldX = gh.tileSize * 20;
        worldY = gh.tileSize * 16;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    private void acquireObject(int i) {
        String text;
        
        if (i != 999) {
            if (inventory.size() < inventorySize) {
                inventory.add(gh.objs[i]);
                // gh.playSound(1);
                text = "Got a " + gh.objs[i].name + "!";
                gh.objs[i] = null;
            }

            else {
                text = "Inventory full!";
            }
            gh.ui.addMessage(text);
        }
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

