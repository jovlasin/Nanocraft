package com.nanocraft.game.entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.core.ItemStacking;
import com.nanocraft.game.core.SaveHandler;
import com.nanocraft.game.input.KeyHandler;
import com.nanocraft.game.object.Arrow;
import com.nanocraft.game.object.Key;
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Sword;
import com.nanocraft.game.object.Torch;
import com.nanocraft.game.tile.Tile;

public class Player extends Entity {
    private static final String ARROW_ITEM_ID = "arrow";
    private static final int ARROW_COOLDOWN_SECONDS = 2;
    private static final int MINE_COOLDOWN_TICKS = 8;
    private static final int TOOL_SWING_TOTAL_TICKS = 8;
    private static final int TOOL_SWING_FRAME_SWITCH_TICK = 4;
    private static final int LAVA_MESSAGE_COOLDOWN_TICKS = 20;
    public final int screenX = gh.screenWidth / 2 - gh.tileSize / 2;
    public final int screenY = gh.screenHeight / 2 - gh.tileSize / 2;
    private int standCounter;
    private int mineCooldownTicks;
    private int toolSwingTicks;
    private int toolSwingSpriteNum = 1;
    private int lavaMessageCooldownTicks;
    private boolean interactRequested;
    private Entity toolSwingSource;
    private KeyHandler kh;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 30;
    public boolean cancelAttack;

    public Player(GameHandler gh, KeyHandler kh) {
        super(gh);
        this.kh = kh;

        setStats();
        setPos();
        setItems();
        getImage();
    }

    public int getAttack() {
        if (currentWeapon == null) {
            attackArea = new Rectangle(0, 0, 0, 0);
            return 0;
        }

        attackArea = currentWeapon.attackArea;
        return strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return dexterity + 1;
    }

    public void update() {
        if (mineCooldownTicks > 0) {
            mineCooldownTicks--;
        }

        boolean moving = kh.up == true || kh.down == true || kh.left == true || kh.right == true;
        if (moving) {
            updateDirectionFromInput();
        }

        if (attacking == true) {
            attack();
        }

        else if (moving || kh.space == true || interactRequested == true) {
            collisionOn = false;
            gh.ch.checkTile(this);

            int objIndex = gh.ch.checkObject(this, true);
            acquireObject(objIndex);

            int npcIndex = gh.ch.checkEntity(this, gh.npcs);
            int monsterIndex = gh.ch.checkEntity(this, gh.monsters);
            interactMonster(monsterIndex);

            boolean interactionConsumed = handleInteraction(npcIndex);
            if (!interactionConsumed) {
                if (collisionOn == false && kh.space == false) {
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

                if (kh.space == true && cancelAttack == false && hasEquippedWeapon()) {
                    clearToolSwingAnimation();
                    attacking = true;
                    spriteCounter = 0;
                    if (playsAttackSwingSound(currentWeapon)) {
                        gh.playSound(GameHandler.SFX_SWORD_ATTACK);
                    }
                }

                cancelAttack = false;
                gh.kh.space = false;
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
                cancelAttack = false;
                gh.kh.space = false;
            }
        }

        else {
            standCounter++;

            if (standCounter == 20) {
                spriteNum = 1;
                standCounter = 0;
            }
        }

        updateToolSwingAnimation();
        handleShootRequest();
        applyContactTileDamage();

        if (lavaMessageCooldownTicks > 0) {
            lavaMessageCooldownTicks--;
        }

        if (invincible == true) {
            invincibleCounter++;

            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        if (shotCounter < getArrowCooldownTicks()) {
            shotCounter++;
        }
    }

    private boolean playsAttackSwingSound(Entity weapon) {
        return weapon instanceof Sword || weapon instanceof Pickaxe;
    }

    public void requestInteract() {
        interactRequested = true;
    }

    public void requestMine() {
        interactRequested = true;
    }

    public boolean addToInventory(Entity item) {
        return ItemStacking.addItem(inventory, inventorySize, item);
    }

    public Entity removeFromInventory(int index) {
        if (index < 0 || index >= inventory.size()) {
            return null;
        }

        return inventory.remove(index);
    }

    public boolean hasEquippedWeapon() {
        return currentWeapon != null;
    }

    public void handleRemovedInventoryItem(Entity removedItem) {
        if (removedItem == toolSwingSource) {
            clearToolSwingAnimation();
        }

        if (removedItem == currentTool) {
            currentTool = null;
        }

        if (removedItem == null || removedItem != currentWeapon) {
            return;
        }

        currentWeapon = findFirstWeaponInInventory();
        attack = getAttack();

        if (currentWeapon == null) {
            attacking = false;
            spriteCounter = 0;
        }
    }

    public boolean isInventoryFull() {
        return inventory.size() >= inventorySize;
    }

    public boolean canAcceptInventoryItem(Entity item) {
        return ItemStacking.canStore(inventory, inventorySize, item);
    }

    public void attemptMine() {
        attemptMineIfPossible();
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int tempX = screenX;
        int tempY = screenY;
        boolean showingActionAnimation = isShowingActionAnimation();

        switch (direction) {
            case "up":
                if (!showingActionAnimation) {
                    if (spriteNum == 1) {image = up1;}
                    else if (spriteNum == 2) {image = up2;}
                }

                else {
                    tempY = screenY - gh.tileSize;
                    image = resolveCurrentActionSprite();
                }
            break;

            case "down":
                if (!showingActionAnimation) {
                    if (spriteNum == 1) {image = down1;}
                    else if (spriteNum == 2) {image = down2;}
                }

                else {
                    image = resolveCurrentActionSprite();
                }
            break;

            case "left":
                if (!showingActionAnimation) {
                    if (spriteNum == 1) {image = left1;}
                    else if (spriteNum == 2) {image = left2;}
                }

                else {
                    tempX = screenX - gh.tileSize;
                    image = resolveCurrentActionSprite();
                }
            break;

            case "right":
                if (!showingActionAnimation) {
                    if (spriteNum == 1) {image = right1;}
                    else if (spriteNum == 2) {image = right2;}
                }

                else {
                    image = resolveCurrentActionSprite();
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
        up1 = scale("/player/playerWalkU1", gh.tileSize, gh.tileSize);
        up2 = scale("/player/playerWalkU2", gh.tileSize, gh.tileSize);
        down1 = scale("/player/playerWalkD1", gh.tileSize, gh.tileSize);
        down2 = scale("/player/playerWalkD2", gh.tileSize, gh.tileSize);
        left1 = scale("/player/playerWalkL1", gh.tileSize, gh.tileSize);
        left2 = scale("/player/playerWalkL2", gh.tileSize, gh.tileSize);
        right1 = scale("/player/playerWalkR1", gh.tileSize, gh.tileSize);
        right2 = scale("/player/playerWalkR2", gh.tileSize, gh.tileSize);

        attackUp1 = scale("/player/playerSwordU1", gh.tileSize, gh.tileSize * 2);
        attackUp2 = scale("/player/playerSwordU2", gh.tileSize, gh.tileSize * 2);
        attackDown1 = scale("/player/playerSwordD1", gh.tileSize, gh.tileSize * 2);
        attackDown2 = scale("/player/playerSwordD2", gh.tileSize, gh.tileSize * 2);
        attackLeft1 = scale("/player/playerSwordL1", gh.tileSize * 2, gh.tileSize);
        attackLeft2 = scale("/player/playerSwordL2", gh.tileSize * 2, gh.tileSize);
        attackRight1 = scale("/player/playerSwordR1", gh.tileSize * 2, gh.tileSize);
        attackRight2 = scale("/player/playerSwordR2", gh.tileSize * 2, gh.tileSize);
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
        projectile = new Arrow(gh);
        shotCounter = getArrowCooldownTicks();
        attack = getAttack();
        defense = getDefense();
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
                gh.ui.addMessage("Got a " + gh.objs[i].name + "!");
                gh.objs[i] = null;
            }
        }
    }

    private boolean interactNPC(int i) {
        if (gh.kh.space == true && i != 999) {
            cancelAttack = true;
            gh.gameState = gh.dialogue;
            gh.npcs[i].speak();
            return true;
        }

        return false;
    }

    private void interactMonster(int i) {
        if (i != 999) {
            if (gh.monsters[i].dying == false) {
                receiveDamage(gh.monsters[i].attack, 2);
            }
        }
    }

    private void attack() {
        spriteCounter++;

        if (spriteCounter <= 5) {
            spriteNum = 1;
        }

        else if (spriteCounter > 5 && spriteCounter <= 30) {
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

            int monsterIndex = gh.ch.checkEntity(this, gh.monsters);
            damage(monsterIndex, attack);

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

    private void updateToolSwingAnimation() {
        if (!isToolSwinging()) {
            return;
        }

        toolSwingTicks++;
        toolSwingSpriteNum = toolSwingTicks <= TOOL_SWING_FRAME_SWITCH_TICK ? 1 : 2;

        if (toolSwingTicks >= TOOL_SWING_TOTAL_TICKS) {
            clearToolSwingAnimation();
        }
    }

    private void startToolSwingAnimation(Entity tool) {
        if (tool == null) {
            return;
        }

        toolSwingSource = tool;
        toolSwingTicks = 0;
        toolSwingSpriteNum = 1;
    }

    private void clearToolSwingAnimation() {
        toolSwingSource = null;
        toolSwingTicks = 0;
        toolSwingSpriteNum = 1;
    }

    public void damage(int i, int attack) {
        if (i != 999) {
            if (gh.monsters[i].dying == true || gh.monsters[i].alive == false) {
                return;
            }

            if (gh.monsters[i].invincible == false) {
                Entity monster = gh.monsters[i];
                int monsterLifeBeforeHit = monster.life;
                int damage = attack - monster.defense;

                if (attack > 0 && damage < 1) {
                    damage = 1;
                }

                else if (damage < 0) {
                    damage = 0;
                }

                if (monsterLifeBeforeHit == monster.maxLife && monsterLifeBeforeHit > 1 && damage >= monsterLifeBeforeHit) {
                    damage = monsterLifeBeforeHit - 1;
                }

                monster.life -= damage;
                gh.playSound(GameHandler.SFX_MONSTER_HIT);
                gh.ui.addMessage(damage + " damage!");
                monster.invincible = true;
                monster.knockBack = true;
                monster.knockBackDirection = direction;
                monster.knockBackSpeed = Math.max(monster.speed + 2, gh.tileSize / 8);
                monster.knockBackCounter = 8;

                if (monster.life <= 0) {
                    monster.dying = true;
                    gh.ui.addMessage("You killed a " + monster.name + "!");
                    gh.ui.addMessage("Exp + " + monster.exp);
                    exp += monster.exp;
                    monster.onDefeat();
                    if (monster.alive == false) {
                        gh.markMonsterKilled(i);
                        gh.monsters[i] = null;
                    }
                    checkLevelUp();
                }
            }
        }
    }

    public void receiveDamage(int incomingAttack) {
        receiveDamage(incomingAttack, 2);
    }

    public void receiveDamage(int incomingAttack, int minimumDamage) {
        if (invincible == true) {
            return;
        }

        int damage = incomingAttack - defense;
        
        if (incomingAttack > 0 && damage < minimumDamage) {
            damage = minimumDamage;
        }

        else if (damage < 0) {
            damage = 0;
        }

        int previousLife = life;
        life = Math.max(0, life - damage);
        if (life < previousLife) {
            gh.playSound(GameHandler.SFX_PLAYER_DAMAGE);
        }
        invincible = true;

        if (life == 0) {
            gh.openGameOverMenu();
        }
    }

    private void applyContactTileDamage() {
        int contactDamage = gh.th.getContactDamageForArea(worldX, worldY, solidArea);
        if (contactDamage <= 0) {
            return;
        }

        if (receiveEnvironmentalDamage(contactDamage) && lavaMessageCooldownTicks == 0) {
            gh.ui.addMessage("Lava burns!");
            lavaMessageCooldownTicks = LAVA_MESSAGE_COOLDOWN_TICKS;
        }
    }

    private boolean receiveEnvironmentalDamage(int damage) {
        if (invincible == true || damage <= 0) {
            return false;
        }

        int previousLife = life;
        life = Math.max(0, life - damage);

        if (life < previousLife) {
            gh.playSound(GameHandler.SFX_PLAYER_DAMAGE);
        }
        
        invincible = true;
        if (life == 0) {
            gh.openGameOverMenu();
        }
        return life < previousLife;
    }

    private void checkLevelUp() {
        if (exp >= nextLevelExp) {
            level++;
            nextLevelExp = nextLevelExp * 2;
            maxLife += 2;
            life = maxLife;
            strength++;
            dexterity++;
            attack = getAttack();
            defense = getDefense();
            gh.playSound(GameHandler.SFX_LEVEL_UP);
            gh.gameState = gh.dialogue;
            gh.ui.currentDialogue = "You are level " + level + " now!\nYou feel stronger!";
        }
    }

    public void selectItem() {
        int itemIndex = gh.ui.getItemIndexOnSlot();

        if (itemIndex >= inventory.size()) {
            return;
        }

        Entity item = inventory.get(itemIndex);

        if (item.type == TYPE_WEAPON) {
            currentWeapon = item;
            attack = getAttack();
            return;
        }

        if ("torch".equalsIgnoreCase(item.itemId)) {
            if (currentTool == item) {
                currentTool = null;
                gh.ui.addMessage(item.name + " unequipped.");
            }
            else {
                currentTool = item;
                gh.ui.addMessage(item.name + " equipped.");
            }
            return;
        }

        if (item.type == consumable) {
            boolean itemWasUsed = item.use(this);
            closeInventoryAfterItemUse();

            if (!itemWasUsed) {
                return;
            }

            item.stackCount--;
            if (item.stackCount <= 0) {
                Entity removedItem = inventory.remove(itemIndex);
                handleRemovedInventoryItem(removedItem);
            }
            return;
        }

        gh.ui.addMessage(item.name + " can't be used.");
    }

    private void closeInventoryAfterItemUse() {
        if (gh.gameState == gh.inventory) {
            gh.gameState = gh.play;
        }
    }

    private Entity findFirstWeaponInInventory() {
        for (Entity item : inventory) {
            if (item != null && item.type == TYPE_WEAPON) {
                return item;
            }
        }

        return null;
    }

    BufferedImage resolveCurrentAttackSprite() {
        BufferedImage weaponSprite = currentWeapon == null ? null : currentWeapon.getAttackSprite(direction, spriteNum);
        if (weaponSprite != null) {
            return weaponSprite;
        }

        return getAttackSprite(direction, spriteNum);
    }

    BufferedImage resolveCurrentActionSprite() {
        if (attacking) {
            return resolveCurrentAttackSprite();
        }

        if (!isToolSwinging()) {
            return null;
        }

        BufferedImage toolSprite = toolSwingSource.getAttackSprite(direction, toolSwingSpriteNum);
        if (toolSprite != null) {
            return toolSprite;
        }

        return getAttackSprite(direction, toolSwingSpriteNum);
    }

    private void setItems() {
        inventory.add(currentWeapon);
        inventory.add(new Key(gh));
        inventory.add(new Torch(gh));
    }

    public SaveHandler.PlayerData createSaveData() {
        SaveHandler.PlayerData playerData = new SaveHandler.PlayerData();
        playerData.worldX = worldX;
        playerData.worldY = worldY;
        playerData.direction = direction;
        playerData.speed = speed;
        playerData.maxLife = maxLife;
        playerData.life = life;
        playerData.level = level;
        playerData.strength = strength;
        playerData.dexterity = dexterity;
        playerData.exp = exp;
        playerData.nextLevelExp = nextLevelExp;
        playerData.coin = coin;
        playerData.currentWeaponIndex = inventory.indexOf(currentWeapon);
        playerData.currentToolIndex = inventory.indexOf(currentTool);

        for (Entity item : inventory) {
            String itemId = gh.getItemId(item);
            if (itemId == null) {
                continue;
            }

            SaveHandler.ItemData itemData = new SaveHandler.ItemData();
            itemData.itemId = itemId;
            itemData.stackCount = Math.max(1, item.stackCount);
            playerData.inventory.add(itemData);
        }

        return playerData;
    }

    public void applySaveData(SaveHandler.PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        worldX = playerData.worldX;
        worldY = playerData.worldY;
        direction = playerData.direction == null || playerData.direction.isBlank() ? "down" : playerData.direction;
        speed = playerData.speed;
        maxLife = playerData.maxLife;
        life = Math.max(0, Math.min(playerData.life, maxLife));
        level = playerData.level;
        strength = playerData.strength;
        dexterity = playerData.dexterity;
        exp = playerData.exp;
        nextLevelExp = playerData.nextLevelExp;
        coin = playerData.coin;
        attacking = false;
        cancelAttack = false;
        collisionOn = false;
        invincible = false;
        invincibleCounter = 0;
        shotCounter = getArrowCooldownTicks();
        spriteCounter = 0;
        spriteNum = 1;
        currentTool = null;
        projectile = new Arrow(gh);

        inventory.clear();
        if (playerData.inventory != null) {
            for (SaveHandler.ItemData itemData : playerData.inventory) {
                if (itemData == null) {
                    continue;
                }

                Entity item = gh.createItemEntity(itemData.itemId);
                if (item == null) {
                    continue;
                }

                item.stackCount = Math.max(1, itemData.stackCount);
                inventory.add(item);
            }
        }

        if (playerData.currentWeaponIndex >= 0 && playerData.currentWeaponIndex < inventory.size()) {
            currentWeapon = inventory.get(playerData.currentWeaponIndex);
        }
        else {
            currentWeapon = findFirstWeaponInInventory();
        }

        if (playerData.currentToolIndex >= 0 && playerData.currentToolIndex < inventory.size()) {
            currentTool = inventory.get(playerData.currentToolIndex);
        }

        attack = getAttack();
        defense = getDefense();
    }
  
    private boolean attemptMineIfPossible() {
        int[] targetTileCoordinates = gh.th.findBreakableTileNear(worldX, worldY, solidArea, gh.tileSize);
        if (targetTileCoordinates == null) {
            return false;
        }

        Tile targetTile = gh.th.getTopBreakableTileAt(targetTileCoordinates[0], targetTileCoordinates[1]);
        if (targetTile == null) {
            return false;
        }

        if (mineCooldownTicks > 0) {
            return true;
        }

        mineCooldownTicks = MINE_COOLDOWN_TICKS;

        MiningRules.Result miningResult = MiningRules.evaluate(
            targetTile,
            hasEquippedItem(targetTile.requiredItemType),
            canStoreMinedDrop(targetTile.dropItemType)
        );

        if (miningResult == MiningRules.Result.MISSING_REQUIRED_ITEM) {
            gh.ui.addMessage("Need " + MiningRules.toDisplayName(targetTile.requiredItemType) + "!");
            return true;
        }

        if (miningResult == MiningRules.Result.INVENTORY_FULL) {
            gh.ui.addMessage("Inventory full!");
            return true;
        }

        startToolSwingAnimation(currentWeapon);
        if (playsAttackSwingSound(currentWeapon)) {
            gh.playSound(GameHandler.SFX_SWORD_ATTACK);
        }
        String dropItemType = gh.th.damageBreakableTile(targetTileCoordinates[0], targetTileCoordinates[1], 1);
        if (dropItemType == null || dropItemType.isBlank()) {
            gh.playSound(GameHandler.SFX_BLOCK_HIT);
            return true;
        }

        gh.playSound(GameHandler.SFX_BLOCK_BREAK);
        Entity minedItem = gh.createItemEntity(dropItemType);
        if (minedItem != null && addToInventory(minedItem)) {
            gh.ui.addMessage("Got a " + minedItem.name + "!");
        }

        return true;
    }

    public boolean hasItem(String itemId) {
        return findInventoryItem(itemId) != null;
    }

    public boolean hasEquippedItem(String itemId) {
        return currentWeapon != null
            && itemId != null
            && !itemId.isBlank()
            && itemId.equalsIgnoreCase(currentWeapon.itemId);
    }

    private Entity findInventoryItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }

        for (Entity item : inventory) {
            if (item != null && itemId.equalsIgnoreCase(item.itemId)) {
                return item;
            }
        }

        return null;
    }

    private void handleShootRequest() {
        if (!gh.kh.shoot) {
            return;
        }

        gh.kh.shoot = false;

        if (!hasItem(ARROW_ITEM_ID)) {
            gh.ui.addMessage("Need an Arrow!");
            return;
        }

        if (projectile.alive || shotCounter < getArrowCooldownTicks()) {
            return;
        }

        if (!consumeInventoryItem(ARROW_ITEM_ID)) {
            gh.ui.addMessage("Need an Arrow!");
            return;
        }

        projectile.set(worldX, worldY, direction, true, this);
        gh.projectileList.add(projectile);
        shotCounter = 0;
        gh.playSound(GameHandler.SFX_ARROW);
    }

    private boolean consumeInventoryItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return false;
        }

        for (int i = 0; i < inventory.size(); i++) {
            Entity item = inventory.get(i);
            if (item == null || !itemId.equalsIgnoreCase(item.itemId)) {
                continue;
            }

            item.stackCount--;
            if (item.stackCount <= 0) {
                Entity removedItem = inventory.remove(i);
                handleRemovedInventoryItem(removedItem);
            }

            return true;
        }

        return false;
    }

    private int getArrowCooldownTicks() {
        return Math.max(1, (int) Math.round(gh.fps * ARROW_COOLDOWN_SECONDS));
    }

    private boolean canStoreMinedDrop(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return true;
        }

        Entity minedItem = gh.createItemEntity(itemType);
        return minedItem == null || canAcceptInventoryItem(minedItem);
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

    public int[][] getAdjacentTiles() {
        return resolveAdjacentTiles(worldX, worldY, solidArea, gh.tileSize);
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

        ArrayList<int[]> targetTiles = new ArrayList<>();
        appendTilesForDirection(targetTiles, worldX, worldY, solidArea, tileSize, direction);
        return targetTiles.toArray(new int[targetTiles.size()][]);
    }

    public static int[][] resolveAdjacentTiles(int worldX, int worldY, Rectangle solidArea, int tileSize) {
        if (solidArea == null || tileSize <= 0) {
            return new int[0][];
        }

        ArrayList<int[]> targetTiles = new ArrayList<>();
        appendTilesForDirection(targetTiles, worldX, worldY, solidArea, tileSize, "up");
        appendTilesForDirection(targetTiles, worldX, worldY, solidArea, tileSize, "right");
        appendTilesForDirection(targetTiles, worldX, worldY, solidArea, tileSize, "down");
        appendTilesForDirection(targetTiles, worldX, worldY, solidArea, tileSize, "left");
        return targetTiles.toArray(new int[targetTiles.size()][]);
    }

    private static void appendTilesForDirection(
        ArrayList<int[]> targetTiles,
        int worldX,
        int worldY,
        Rectangle solidArea,
        int tileSize,
        String direction
    ) {
        int leftWorldX = worldX + solidArea.x;
        int rightWorldX = worldX + solidArea.x + solidArea.width - 1;
        int topWorldY = worldY + solidArea.y;
        int bottomWorldY = worldY + solidArea.y + solidArea.height - 1;
        int centerWorldX = worldX + solidArea.x + (solidArea.width / 2);
        int centerWorldY = worldY + solidArea.y + (solidArea.height / 2);

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
    }

    private boolean handleInteraction(int npcIndex) {
        if (!interactRequested && gh.kh.space == false) {
            return false;
        }

        interactRequested = false;

        int[][] targetTiles = getInteractionTiles();
        ChestState chest = gh.th.findChestAt(targetTiles);
        if (chest == null) {
            chest = gh.th.findChestNear(worldX, worldY, solidArea, gh.tileSize);
        }
        if (chest != null) {
            gh.openChest(chest);
            return true;
        }

        if (interactNPC(npcIndex)) {
            return true;
        }

        return attemptMineIfPossible();
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

    boolean isToolSwinging() {
        return toolSwingSource != null;
    }

    int getToolSwingSpriteNum() {
        return toolSwingSpriteNum;
    }

    private boolean isShowingActionAnimation() {
        return attacking || isToolSwinging();
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
