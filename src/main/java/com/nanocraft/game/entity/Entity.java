package com.nanocraft.game.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.tile.ResourceLoader;


public class Entity {
    public static final int DEFAULT_STACK_LIMIT = 99;
    public static final int TYPE_PLAYER = 0;
    public static final int TYPE_NPC = 1;
    public static final int TYPE_MONSTER = 2;
    public static final int TYPE_WEAPON = 3;
    public static final int TYPE_TOOL = 4;
    public static final int TYPE_CONSUMABLE = 6;
    public static final int VIEW_DISTANCE_TILES = 6;
    public static final int VIEW_WIDTH_TILES = 2;
    public static final int AGGRO_LOST_DISTANCE_TILES = 10;

    public GameHandler gh;
    public int worldX, worldY;
    public String direction;
    public int spriteCounter, hpBarCounter,  actionCounter, spriteNum, invincibleCounter, shotCounter, deathCounter;
    public int speed, maxLife, maxMana, mana, life, level, strength, dexterity, attack, defense, exp, nextLevelExp, coin;
    public Entity currentWeapon, currentTool;
    public Projectile projectile;
    public boolean collisionOn, collision, hpBarOn;
    public String name;
    public String itemId;
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
    public String dropItemType;
    public boolean stackable;
    public int stackCount;
    public int maxStackSize;
    public boolean knockBack;
    public String knockBackDirection;
    public int knockBackCounter;
    public int knockBackSpeed;

    public final int player = TYPE_PLAYER;
    public final int npc = TYPE_NPC;
    public final int monster = TYPE_MONSTER;
    public final int sword = TYPE_WEAPON;
    public final int tool = TYPE_TOOL;
    public final int consumable = TYPE_CONSUMABLE;
    public int type;
    public boolean aggroed;

    public Entity(GameHandler gh) {
        this.gh = gh;
        this.solidArea = new Rectangle();
        this.attackArea = new Rectangle(0, 0, 0, 0);
        this.direction = "down";
        this.spriteNum = 1;
        this.dialogues = new String[20];
        this.alive = true;
        this.stackCount = 1;
        this.maxStackSize = 1;
    }

    public BufferedImage scale(String imgPath, int width, int height ) {
        BufferedImage image = loadScaledImageIfPresent(imgPath, width, height);
        if (image == null) {
            throw new IllegalStateException("Failed to load image resource: " + imgPath + ".png");
        }
        return image;
    }

    protected BufferedImage scaleOrFallback(String primaryImgPath, String fallbackImgPath, int width, int height) {
        BufferedImage image = loadScaledImageIfPresent(primaryImgPath, width, height);
        if (image != null) {
            return image;
        }

        image = loadScaledImageIfPresent(fallbackImgPath, width, height);
        if (image != null) {
            return image;
        }

        throw new IllegalStateException(
            "Failed to load image resource: " + primaryImgPath + ".png and fallback " + fallbackImgPath + ".png"
        );
    }

    private BufferedImage loadScaledImageIfPresent(String imgPath, int width, int height) {
        if (imgPath == null || imgPath.isBlank()) {
            return null;
        }

        try (InputStream stream = getClass().getResourceAsStream(imgPath + ".png")) {
            if (stream == null) {
                return null;
            }

            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                return null;
            }

            return ResourceLoader.scaleImage(image, width, height);
        } catch (IOException e) {
            return null;
        }
    }

    public void draw(Graphics2D g2d) {
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

            if (type == monster && hpBarOn == true) {
                double oneScale = (double) gh.tileSize / maxLife;
                double hpValue = oneScale * life;

                g2d.setColor(new Color(35, 35, 35));
                g2d.fillRect(screenX - 1, screenY - 16, gh.tileSize + 2, 12);

                g2d.setColor(new Color(255, 0, 30));
                g2d.fillRect(screenX, screenY - 15, (int) hpValue, 10);
                hpBarCounter++;

                if (hpBarCounter > 600) {
                    hpBarCounter = 0;
                    hpBarOn = false;
                }
            }

            if (invincible == true) {
                hpBarOn = true;
                hpBarCounter = 0;
                changeAlpha(g2d, 0.4f);
            }

            if (dying == true) {
                deathEffect(g2d);
            }

            g2d.drawImage(image, screenX, screenY, null);
            changeAlpha(g2d, 1f);
        }
    }

    public void aggro() {}
    public void setAction() {}

    public void onDefeat() {
        alive = false;
    }

    public void update() {
        if (knockBack == true) {
            collisionOn = false;
            String movementDirection = knockBackDirection == null || knockBackDirection.isBlank() ? direction : knockBackDirection;
            String originalDirection = direction;
            int originalSpeed = speed;
            direction = movementDirection;
            speed = knockBackSpeed;

            gh.ch.checkTile(this);
            gh.ch.checkObject(this, false);
            gh.ch.checkEntity(this, gh.npcs);
            gh.ch.checkEntity(this, gh.monsters);

            if (collisionOn == false) {
                switch (movementDirection) {
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

            direction = originalDirection;
            speed = originalSpeed;
            knockBackCounter--;

            if (collisionOn == true || knockBackCounter <= 0) {
                knockBack = false;
                knockBackCounter = 0;
            }
        } else {
            setAction();

            collisionOn = false;
            gh.ch.checkTile(this);
            gh.ch.checkObject(this, false);
            gh.ch.checkEntity(this, gh.npcs);
            gh.ch.checkEntity(this, gh.monsters);
            boolean contact = gh.ch.checkPlayer(this);

            if (type == monster && contact == true) {
                damage(attack);
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
        }

        if (invincible == true) {
            invincibleCounter++;

            if (invincibleCounter > 50) {
                invincible = false;
                invincibleCounter = 0;
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
        
        if (shotCounter < 30) {
            shotCounter++;
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

    public void damage(int attack) {
        if (gh.player.invincible == false) {
                gh.playSound(6);
                gh.player.receiveDamage(attack, this instanceof Projectile ? 1 : 2);
        }
    }

    public void deathEffect(Graphics2D g2d) {
        deathCounter++;
        int i = 5;

        if (deathCounter <= i) {
            changeAlpha(g2d, 0f);
        }
        else if (deathCounter > i && deathCounter <= i * 2) {
            changeAlpha(g2d, 1f);
        }
        else if (deathCounter > i * 2 && deathCounter <= i * 3) {
            changeAlpha(g2d, 0f);
        }
        else if (deathCounter > i * 3 && deathCounter <= i * 4) {
            changeAlpha(g2d, 1f);
        }
        else if (deathCounter > i * 4 && deathCounter <= i * 5) {
            changeAlpha(g2d, 0f);
        }
        else if (deathCounter > i * 5 && deathCounter <= i * 6) {
            changeAlpha(g2d, 1f);
        }
        else if (deathCounter > i * 6 && deathCounter <= i * 7) {
            changeAlpha(g2d, 0f);
        }
        else if (deathCounter > i * 7 && deathCounter <= i * 8) {
            changeAlpha(g2d, 1f);
        }
        else if (deathCounter > i * 8) {
            alive = false;
        }
    }

    public BufferedImage getAttackSprite(String facingDirection, int attackFrame) {
        boolean useFirstFrame = attackFrame == 1;

        switch (facingDirection) {
            case "up":
                return useFirstFrame ? attackUp1 : attackUp2;

            case "down":
                return useFirstFrame ? attackDown1 : attackDown2;

            case "left":
                return useFirstFrame ? attackLeft1 : attackLeft2;

            case "right":
                return useFirstFrame ? attackRight1 : attackRight2;

            default:
                return null;
        }
    }

    public boolean use(Player player) {
        return false;
    }

    protected int restoreLife(Player player, int amount) {
        if (player == null || amount <= 0) {
            return 0;
        }

        int missingLife = player.maxLife - player.life;
        if (missingLife <= 0) {
            return 0;
        }

        int restoredLife = Math.min(amount, missingLife);
        player.life += restoredLife;
        return restoredLife;
    }

    protected String healthStatus(Player player) {
        if (player == null) {
            return "Health: 0/0";
        }

        return "Health: " + player.life + "/" + player.maxLife;
    }

    public String getStackKey() {
        return getClass().getName();
    }

    public boolean canStackWith(Entity other) {
        return other != null
            && stackable
            && other.stackable
            && maxStackSize > 1
            && other.maxStackSize > 1
            && getStackKey().equals(other.getStackKey());
    }

    public int getAvailableStackSpace() {
        return Math.max(0, maxStackSize - stackCount);
    }

    public Entity copyForStack(int count) {
        try {
            Entity copy = getClass().getDeclaredConstructor(GameHandler.class).newInstance(gh);
            copy.stackCount = count;
            return copy;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to copy stackable item: " + getClass().getName(), e);
        }
    }

    protected void configureStacking(boolean stackable, int maxStackSize) {
        this.stackable = stackable;
        this.maxStackSize = stackable ? Math.max(1, maxStackSize) : 1;
        this.stackCount = 1;
    }

    public void changeAlpha(Graphics2D g2d, float alpha) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
}
