package com.nanocraft.game.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.input.KeyHandler;

public class Player extends Entity {
    private static final int MINE_COOLDOWN_TICKS = 8;
    public final int screenX = gh.screenWidth / 2 - gh.tileSize / 2;
    public final int screenY = gh.screenHeight / 2 - gh.tileSize / 2;
    private int standCounter;
    private int mineCooldownTicks;
    private boolean interactRequested;
    private KeyHandler kh;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 20;
    public int maxLife;
    public int life;

    public Player(GameHandler gh, KeyHandler kh) {
        super(gh);
        this.kh = kh;

        setStats();
        setPos();
        getImage();
    }

    public void update() {
        if (mineCooldownTicks > 0) {
            mineCooldownTicks--;
        }

        boolean moving = kh.up == true || kh.down == true || kh.left == true || kh.right == true;
        if (moving) {
            updateDirectionFromInput();
        }

        if (handleInteraction()) {
            return;
        }

        if (moving) {
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

                gh.th.checkMapTransition();
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

    public void requestInteract() {
        interactRequested = true;
    }

    public boolean addToInventory(Entity item) {
        if (item == null || isInventoryFull()) {
            return false;
        }

        inventory.add(item);
        return true;
    }

    public Entity removeFromInventory(int index) {
        if (index < 0 || index >= inventory.size()) {
            return null;
        }

        return inventory.remove(index);
    }

    public boolean isInventoryFull() {
        return inventory.size() >= inventorySize;
    }

    public void attemptMine() {
        int[] targetTile = getPrimaryInteractionTile();
        int targetCol = targetTile[0];
        int targetRow = targetTile[1];
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
    }

    private void setPos() {
        worldX = gh.tileSize * 25;
        worldY = gh.tileSize * 6;

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    private void acquireObject(int i) {
        if (i != 999) {
            if (addToInventory(gh.objs[i])) {
                // gh.playSound(1);
                // text = "Got a " + gh.objs[i].name + "!";
                System.out.println("Got a " + gh.objs[i].name + "!");
                gh.objs[i] = null;
            }

            else {
                // text = "Inventory full!";
                System.out.println("Inventory full!");
            }
            // gh.ui.addMessage(text);
            
            
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

    public int[][] getInteractionTiles() {
        return resolveInteractionTiles(worldX, worldY, solidArea, gh.tileSize, direction);
    }

    public int[] getPrimaryInteractionTile() {
        int[][] targetTiles = getInteractionTiles();
        if (targetTiles.length == 0) {
            return new int[] { worldX / gh.tileSize, worldY / gh.tileSize };
        }

        return targetTiles[0];
    }

    public static int[][] resolveInteractionTiles(int worldX, int worldY, Rectangle solidArea, int tileSize, String direction) {
        if (solidArea == null || tileSize <= 0) {
            return new int[0][];
        }

        int leftWorldX = worldX + solidArea.x;
        int rightWorldX = worldX + solidArea.x + solidArea.width - 1;
        int topWorldY = worldY + solidArea.y;
        int bottomWorldY = worldY + solidArea.y + solidArea.height - 1;
        int centerWorldX = worldX + solidArea.x + (solidArea.width / 2);
        int centerWorldY = worldY + solidArea.y + (solidArea.height / 2);

        ArrayList<int[]> targetTiles = new ArrayList<>();
        switch (direction) {
            case "up":
                appendUniqueTile(targetTiles, toTileIndex(centerWorldX, tileSize), toTileIndex(topWorldY - 1, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(leftWorldX, tileSize), toTileIndex(topWorldY - 1, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(rightWorldX, tileSize), toTileIndex(topWorldY - 1, tileSize));
            break;

            case "down":
                appendUniqueTile(targetTiles, toTileIndex(centerWorldX, tileSize), toTileIndex(bottomWorldY + 1, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(leftWorldX, tileSize), toTileIndex(bottomWorldY + 1, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(rightWorldX, tileSize), toTileIndex(bottomWorldY + 1, tileSize));
            break;

            case "left":
                appendUniqueTile(targetTiles, toTileIndex(leftWorldX - 1, tileSize), toTileIndex(centerWorldY, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(leftWorldX - 1, tileSize), toTileIndex(topWorldY, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(leftWorldX - 1, tileSize), toTileIndex(bottomWorldY, tileSize));
            break;

            case "right":
                appendUniqueTile(targetTiles, toTileIndex(rightWorldX + 1, tileSize), toTileIndex(centerWorldY, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(rightWorldX + 1, tileSize), toTileIndex(topWorldY, tileSize));
                appendUniqueTile(targetTiles, toTileIndex(rightWorldX + 1, tileSize), toTileIndex(bottomWorldY, tileSize));
            break;

            default:
            break;
        }

        return targetTiles.toArray(new int[targetTiles.size()][]);
    }

    private boolean handleInteraction() {
        if (!interactRequested) {
            return false;
        }

        interactRequested = false;

        int[][] targetTiles = getInteractionTiles();
        ChestState chest = gh.th.findChestAt(targetTiles);
        if (chest != null) {
            gh.openChest(chest);
            return true;
        }

        for (int[] targetTile : targetTiles) {
            String interactionType = gh.th.getInteractionTypeAt(targetTile[0], targetTile[1]);
            if ("sleep".equals(interactionType)) {
                gh.onPlayerSleep();
                return true;
            }
        }

        if (mineCooldownTicks == 0) {
            attemptMine();
            mineCooldownTicks = MINE_COOLDOWN_TICKS;
        }

        return false;
    }

    private void updateDirectionFromInput() {
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
    }

    private static void appendUniqueTile(ArrayList<int[]> targetTiles, int col, int row) {
        for (int[] targetTile : targetTiles) {
            if (targetTile[0] == col && targetTile[1] == row) {
                return;
            }
        }

        targetTiles.add(new int[] { col, row });
    }

    private static int toTileIndex(int worldCoordinate, int tileSize) {
        return Math.floorDiv(worldCoordinate, tileSize);
    }
}
