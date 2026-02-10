package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GameManager;
import main.Utility;

public class Entity {
    public GameManager gm;
    public int worldX, worldY;
    public String direction;
    public int speed, maxLife, life, spriteNum, level, strength, dexterity, attack, defense, exp, nextLevelExp, coin;
    public boolean collision, collisionOn, invincible, attacking, alive, dying, hpBarOn;
    public int actionCounter, spriteCounter, invincibleCounter, deathCounter, hpBarCounter;
    public int solidAreaDefaultX, solidAreaDefaultY;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public BufferedImage attackUp1, attackUp2, attackDown1, attackDown2, attackLeft1, attackLeft2, attackRight1, attackRight2;
    public BufferedImage image, image2, image3;
    public Rectangle solidArea, attackArea;
    public int dialogueIndex;
    public String[] dialogues;
    public String name;
    public Entity currentWeapon;
    public Entity currentShield;
    public int attackValue, defenseValue;
    public String description;
    
    public final int player = 0;
    public final int npc = 1;
    public final int monster = 2;
    public final int sword = 3;
    public final int axe = 4;
    public final int shield = 5;
    public final int consumable = 6;
    public int type;

    public Entity(GameManager gm) {
        this.gm = gm;
        this.solidArea = new Rectangle();
        this.attackArea = new Rectangle(0, 0, 0, 0);
        this.direction = "down";
        this.spriteNum = 1;
        this.dialogues = new String[20];
        this.alive = true;
    }

    public BufferedImage scale(String imgPath, int width, int height ) {
        Utility u = new Utility();
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imgPath + ".png"));
            image = u.scaleImage(image, width, height);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void use(Entity entity) {}

    public void speak() {
        if (dialogues[dialogueIndex] == null) {
            dialogueIndex = 0;
        }

        gm.ui.currentDialogue = dialogues[dialogueIndex];
        dialogueIndex++;

        switch (gm.player.direction) {
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

    public void deathEffect(Graphics2D g2) {
        deathCounter++;
        int i = 5;

        if (deathCounter <= i) {
            changeAlpha(g2, 0f);
        }
        else if (deathCounter > i && deathCounter <= i * 2) {
            changeAlpha(g2, 1f);
        }
        else if (deathCounter > i * 2 && deathCounter <= i * 3) {
            changeAlpha(g2, 0f);
        }
        else if (deathCounter > i * 3 && deathCounter <= i * 4) {
            changeAlpha(g2, 1f);
        }
        else if (deathCounter > i * 4 && deathCounter <= i * 5) {
            changeAlpha(g2, 0f);
        }
        else if (deathCounter > i * 5 && deathCounter <= i * 6) {
            changeAlpha(g2, 1f);
        }
        else if (deathCounter > i * 6 && deathCounter <= i * 7) {
            changeAlpha(g2, 0f);
        }
        else if (deathCounter > i * 7 && deathCounter <= i * 8) {
            changeAlpha(g2, 1f);
        }
        else if (deathCounter > i * 8) {
            alive = false;
        }
    }

    public void aggro(){}
    public void setAction() {}

    public void update() {
        setAction();

        collisionOn = false;
        gm.ch.checkTile(this);
        gm.ch.checkObject(this, false);
        gm.ch.checkEntity(this, gm.npcs);
        gm.ch.checkEntity(this, gm.monsters);
        boolean contact = gm.ch.checkPlayer(this);

        if (type == monster && contact == true) {
            if (gm.player.invincible == false) {
                gm.playSound(6);
                int damage = attack - gm.player.defense;

                if (damage < 0) {
                    damage = 0;
                }

                gm.player.life -= damage;
                gm.player.invincible = true;
            }
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

        if (invincible == true) {
            invincibleCounter++;

            if (invincibleCounter > 40) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int screenX = worldX - gm.player.worldX + gm.player.screenX;
        int screenY = worldY - gm.player.worldY + gm.player.screenY;

        if (worldX + gm.tileSize > gm.player.worldX - gm.player.screenX && 
            worldX - gm.tileSize < gm.player.worldX + gm.player.screenX && 
            worldY + gm.tileSize > gm.player.worldY - gm.player.screenY && 
            worldY - gm.tileSize < gm.player.worldY + gm.player.screenY) {
                
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
                double oneScale = (double) gm.tileSize / maxLife;
                double hpValue = oneScale * life;

                g2.setColor(new Color(35, 35, 35));
                g2.fillRect(screenX - 1, screenY - 16, gm.tileSize + 2, 12);

                g2.setColor(new Color(255, 0, 30));
                g2.fillRect(screenX, screenY - 15, (int) hpValue, 10);
                hpBarCounter++;

                if (hpBarCounter > 600) {
                    hpBarCounter = 0;
                    hpBarOn = false;
                }
            }

            if (invincible == true) {
                hpBarOn = true;
                hpBarCounter = 0;
                changeAlpha(g2, 0.4f);
            }

            if (dying == true) {
                deathEffect(g2);
            }

            g2.drawImage(image, screenX, screenY, null);
            changeAlpha(g2, 1f);
        }
    }

    private void changeAlpha(Graphics2D g2, float alpha) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
}
