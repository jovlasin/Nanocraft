package entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import main.GameManager;
import main.KeyHandler;
import object.Key;
import object.OldShield;
import object.Sword;

public class Player extends Entity {

    public final int screenX = gm.screenWidth / 2 - gm.tileSize / 2;
    public final int screenY = gm.screenHeight / 2 - gm.tileSize / 2;

    private int standCounter;
    private KeyHandler kh;
    public boolean cancelAttack;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 20;

    public Player(GameManager gm, KeyHandler kh) {
        super(gm);
        this.kh = kh;

        setStats();
        setItems();
        setPos();
        getImage();
    }

    public int getAttack() {
        attackArea = currentWeapon.attackArea;
        return strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return dexterity * currentShield.defenseValue;
    }

    public void update() {
        if (attacking == true) {
            attack();
        }

        else if (kh.upPressed == true || kh.downPressed == true ||
            kh.leftPressed == true || kh.rightPressed == true || kh.spacePressed == true) {
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

            // CHECK TILE COLLISION
            collisionOn = false;
            gm.ch.checkTile(this);

            // CHECK OBJECT COLLISION
            int objIndex = gm.ch.checkObject(this, true);
            acquireObject(objIndex);

            // CHECK NPC COLLISION
            int npcIndex = gm.ch.checkEntity(this, gm.npcs);
            interactNPC(npcIndex);

            int monsterIndex = gm.ch.checkEntity(this, gm.monsters);
            interactMonster(monsterIndex);

            gm.eh.checkEvent();

            if (collisionOn == false && kh.spacePressed == false) {
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

            if (kh.spacePressed == true && cancelAttack == false) {
                gm.playSound(7);
                attacking = true;
                spriteCounter = 0;
            }

            cancelAttack = false;
            gm.kh.spacePressed = false;
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

        if (invincible == true) {
            invincibleCounter++;

            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int tempX = screenX;
        int tempY = screenY;


        switch (direction) {
            case "up":
                if (attacking == false) {
                    if (spriteNum == 1) {image = up1;}
                    else if (spriteNum == 2) {image = up2;}
                }

                else if (attacking == true) {
                    tempY = screenY - gm.tileSize;
                    if (spriteNum == 1) {image = attackUp1;}
                    else if (spriteNum == 2) {image = attackUp2;}
                }
            break;

            case "down":
                if (attacking == false) {
                    if (spriteNum == 1) {image = down1;}
                    else if (spriteNum == 2) {image = down2;}
                }

                else if (attacking == true) {
                    if (spriteNum == 1) {image = attackDown1;}
                    else if (spriteNum == 2) {image = attackDown2;}
                }
            break;

            case "left":

                if (attacking == false) {
                    if (spriteNum == 1) {image = left1;}
                    else if (spriteNum == 2) {image = left2;}
                }

                else if (attacking == true) {
                    tempX = screenX - gm.tileSize;
                    if (spriteNum == 1) {image = attackLeft1;}
                    else if (spriteNum == 2) {image = attackLeft2;}
                }
            break;

            case "right":
                if (attacking == false) {
                    if (spriteNum == 1) {image = right1;}
                    else if (spriteNum == 2) {image = right2;}
                }

                else if (attacking == true) {
                    if (spriteNum == 1) {image = attackRight1;}
                    else if (spriteNum == 2) {image = attackRight2;}
                }
            break;
        }

        if (invincible == true) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        }

        g2.drawImage(image, tempX, tempY, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
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

        if (currentWeapon.type == sword) {
            attackUp1 = scale("/player/playerSwordU1", gm.tileSize, gm.tileSize * 2);
            attackUp2 = scale("/player/playerSwordU2", gm.tileSize, gm.tileSize * 2);
            attackDown1 = scale("/player/playerSwordD1", gm.tileSize, gm.tileSize * 2);
            attackDown2 = scale("/player/playerSwordD2", gm.tileSize, gm.tileSize * 2);
            attackLeft1 = scale("/player/playerSwordL1", gm.tileSize * 2, gm.tileSize);
            attackLeft2 = scale("/player/playerSwordL2", gm.tileSize * 2, gm.tileSize);
            attackRight1 = scale("/player/playerSwordR1", gm.tileSize * 2, gm.tileSize);
            attackRight2 = scale("/player/playerSwordR2", gm.tileSize * 2, gm.tileSize);
        }

        else if (currentWeapon.type == axe) {
            attackUp1 = scale("/player/playerAxeU1", gm.tileSize, gm.tileSize * 2);
            attackUp2 = scale("/player/playerAxeU2", gm.tileSize, gm.tileSize * 2);
            attackDown1 = scale("/player/playerAxeD1", gm.tileSize, gm.tileSize * 2);
            attackDown2 = scale("/player/playerAxeD2", gm.tileSize, gm.tileSize * 2);
            attackLeft1 = scale("/player/playerAxeL1", gm.tileSize * 2, gm.tileSize);
            attackLeft2 = scale("/player/playerAxeL2", gm.tileSize * 2, gm.tileSize);
            attackRight1 = scale("/player/playerAxeR1", gm.tileSize * 2, gm.tileSize);
            attackRight2 = scale("/player/playerAxeR2", gm.tileSize * 2, gm.tileSize);
        }
    }

    private void attack() {
        spriteCounter++;

        if (spriteCounter <= 5) {
            spriteNum = 1;
        }

        else if (spriteCounter > 5 && spriteCounter <= 25) {
            spriteNum = 2;

            int x = worldX;
            int y = worldY;
            int width = solidArea.width;
            int height = solidArea.height;

            switch (direction) {
                case "up":
                    worldY -= attackArea.height;
                break;

                case "down":
                    worldY += attackArea.height;
                break;

                case "left":
                    worldX -= attackArea.width;
                break;

                case "right":
                    worldX += attackArea.width;
                break;
            }

            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            int monsterIndex = gm.ch.checkEntity(this, gm.monsters);
            damage(monsterIndex);

            worldX = x;
            worldY = y;
            solidArea.width = width;
            solidArea.height = height;
        }

        else if (spriteCounter > 25) {
            spriteNum = 1;
            spriteCounter = 0;
            attacking = false;
        }
    }

    private void damage(int i) {
        if (i != 999) {
            if (gm.monsters[i].invincible == false) {
                gm.playSound(5);
                int damage = attack - gm.monsters[i].defense;

                if (damage < 0) {
                    damage = 0;
                }

                gm.monsters[i].life -= damage;
                gm.ui.addMessage(damage + " damage!");
                gm.monsters[i].invincible = true;
                gm.monsters[i].aggro();

                if (gm.monsters[i].life <= 0) {
                    gm.monsters[i].dying = true;
                    gm.ui.addMessage("You killed a " + gm.monsters[i].name + "!");
                    gm.ui.addMessage("Exp + " + gm.monsters[i].exp);
                    exp += gm.monsters[i].exp;
                    checkLevelUp();
                }
            }
        }
    }

    private void interactMonster(int i) {
        if (i != 999) {
            if (invincible == false && gm.monsters[i].dying == false) {
                gm.playSound(6);
                int damage = gm.monsters[i].attack - defense;

                if (damage < 0) {
                    damage = 0;
                }
                life -= damage;
                invincible = true;
            }
        }
    }

    private void checkLevelUp() {
        if (exp >= nextLevelExp) {
            level++;
            nextLevelExp = nextLevelExp * 2;
            maxLife += 2;
            // life = maxLife;
            strength++;
            dexterity++;
            attack = getAttack();
            defense = getDefense();
            gm.playSound(8);
            gm.gameState = gm.dialogue;
            gm.ui.currentDialogue = "You are level " + level + " now!\nYou feel stronger!";
        }
    }

    public void selectItem() {
        int itemIndex = gm.ui.getItemIndexOnSlot();

        if (itemIndex < inventory.size()) {
            Entity item = inventory.get(itemIndex);

            if (item.type == sword || item.type == axe) {
                currentWeapon = item;
                attack = getAttack();
                getImage();
            }

            else if (item.type == shield) {
                currentShield = item;
                defense = getDefense();
            }

            else if (item.type == consumable) {
                item.use(this);
                inventory.remove(itemIndex);
            }
        }
    }

    private void interactNPC(int i) {
        if (gm.kh.spacePressed == true) {
            if (i != 999) {
                cancelAttack = true;
                gm.gameState = gm.dialogue;
                gm.npcs[i].speak();
            }
        }
    }

    private void acquireObject(int i) {
        if (i != 999) {
            String text;

            if (inventory.size() < inventorySize) {
                inventory.add(gm.objs[i]);
                gm.playSound(1);
                text = "Got a " + gm.objs[i].name + "!";
                gm.objs[i] = null;
            }

            else {
                text = "Inventory full!";
            }
            gm.ui.addMessage(text);


        }
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
        currentWeapon = new Sword(gm);
        currentShield = new OldShield(gm);
        attack = getAttack();
        defense = getDefense();
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

    private void setItems() {
        inventory.add(currentWeapon);
        inventory.add(currentShield);
        inventory.add(new Key(gm));
    }
}

