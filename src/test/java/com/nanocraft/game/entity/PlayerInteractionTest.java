package com.nanocraft.game.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.junit.Test;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.monster.Dragon;
import com.nanocraft.game.monster.GreenSlime;
import com.nanocraft.game.monster.Zombie;
import com.nanocraft.game.object.Apple;
import com.nanocraft.game.object.Emerald;
import com.nanocraft.game.object.Meat;
import com.nanocraft.game.object.Medkit;
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Sword;
import com.nanocraft.game.tile.Tile;

public class PlayerInteractionTest {
    private static final Rectangle SOLID_AREA = new Rectangle(8, 16, 32, 32);
    private static final int TILE_SIZE = 48;

    private static class RecordingGameHandler extends GameHandler {
        int soundPlayCount;
        int lastSoundId = -1;

        @Override
        public void playSound(int i) {
            soundPlayCount++;
            lastSoundId = i;
        }
    }

    @Test
    public void resolvesInteractionTilesForAllDirections() {
        assertArrayEquals(
            new int[][] { { 10, 10 }, { 9, 10 } },
            Player.resolveInteractionTiles(456, 496, SOLID_AREA, TILE_SIZE, "up")
        );
        assertArrayEquals(
            new int[][] { { 10, 10 }, { 9, 10 } },
            Player.resolveInteractionTiles(456, 432, SOLID_AREA, TILE_SIZE, "down")
        );
        assertArrayEquals(
            new int[][] { { 10, 10 }, { 10, 9 } },
            Player.resolveInteractionTiles(504, 448, SOLID_AREA, TILE_SIZE, "left")
        );
        assertArrayEquals(
            new int[][] { { 10, 10 }, { 10, 9 } },
            Player.resolveInteractionTiles(441, 448, SOLID_AREA, TILE_SIZE, "right")
        );
    }

    @Test
    public void findsOneSidedChestFromValidFacingPosition() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");

        int[][] targetTiles = Player.resolveInteractionTiles(1272, 1808, gh.player.solidArea, gh.tileSize, "up");
        ChestState chest = gh.th.findChestAt(targetTiles);

        assertNotNull(chest);
        assertEquals(27, chest.col);
        assertEquals(37, chest.row);
    }

    @Test
    public void chestTilesBlockMovement() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");

        assertTrue(gh.th.isCollisionAt(31, 9));
    }

    @Test
    public void opensCaveChestWhenAdjacentButFacingAway() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.gameState = gh.play;

        gh.player.worldX = 31 * gh.tileSize - 24;
        gh.player.worldY = (9 + 1) * gh.tileSize - 16;
        gh.player.direction = "down";
        gh.player.requestInteract();
        gh.kh.space = true;

        gh.player.update();

        assertEquals(gh.chest, gh.gameState);
        assertNotNull(gh.activeChest);
        assertEquals(31, gh.activeChest.col);
        assertEquals(9, gh.activeChest.row);
    }

    @Test
    public void opensVillageChestWithoutMarkerWhenAdjacentButFacingAway() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/village.tmj");
        gh.gameState = gh.play;

        ChestPlacement placement = findOpenableChestPlacement(gh);
        gh.player.worldX = placement.worldX;
        gh.player.worldY = placement.worldY;
        gh.player.direction = placement.facingAwayDirection;
        gh.player.requestInteract();
        gh.kh.space = true;

        gh.player.update();

        assertEquals(gh.chest, gh.gameState);
        assertNotNull(gh.activeChest);
        assertEquals(placement.chestCol, gh.activeChest.col);
        assertEquals(placement.chestRow, gh.activeChest.row);
    }

    @Test
    public void opensVillageChestWhenStandingDiagonally() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/village.tmj");
        gh.gameState = gh.play;

        gh.player.worldX = 12 * gh.tileSize + 24;
        gh.player.worldY = 11 * gh.tileSize + 24;
        gh.player.direction = "down";
        gh.player.requestInteract();
        gh.kh.space = true;

        gh.player.update();

        assertEquals(gh.chest, gh.gameState);
        assertNotNull(gh.activeChest);
        assertEquals(12, gh.activeChest.col);
        assertEquals(11, gh.activeChest.row);
    }

    @Test
    public void opensChestLikeFallbackTileWithoutMarker() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/chest-like-fallback.tmj");
        gh.gameState = gh.play;

        gh.player.worldX = gh.tileSize - 24;
        gh.player.worldY = (gh.tileSize * 2) - 16;
        gh.player.direction = "down";
        gh.player.requestInteract();
        gh.kh.space = true;

        gh.player.update();

        assertEquals(gh.chest, gh.gameState);
        assertNotNull(gh.activeChest);
        assertEquals(1, gh.activeChest.col);
        assertEquals(1, gh.activeChest.row);
    }

    @Test
    public void miningRequiresPickaxeAndDropsOreAfterExpectedNumberOfHits() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.gameState = gh.play;

        OrePlacement placement = findMineableOrePlacement(gh);
        Tile oreTile = gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow);

        assertNotNull(oreTile);
        assertEquals("diamond_pickaxe", oreTile.requiredItemType);

        int initialInventorySize = gh.player.inventory.size();
        performMineInteraction(gh, placement);
        assertEquals(initialInventorySize, gh.player.inventory.size());

        Pickaxe pickaxe = new Pickaxe(gh);
        assertEquals("diamond_pickaxe", pickaxe.itemId);
        assertTrue(gh.player.addToInventory(pickaxe));
        assertTrue(gh.player.hasItem("diamond_pickaxe"));
        assertFalse(gh.player.hasEquippedItem("diamond_pickaxe"));

        performMineInteraction(gh, placement);
        assertEquals(initialInventorySize + 1, gh.player.inventory.size());
        assertNotNull(gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow));

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();
        assertTrue(gh.player.hasEquippedItem("diamond_pickaxe"));

        int inventoryBeforeDrop = gh.player.inventory.size();
        for (int i = 0; i < oreTile.maxHealth - 1; i++) {
            performMineInteraction(gh, placement);
        }
        assertEquals(inventoryBeforeDrop, gh.player.inventory.size());

        performMineInteraction(gh, placement);

        assertEquals(inventoryBeforeDrop + 1, gh.player.inventory.size());
        Entity minedItem = gh.player.inventory.get(gh.player.inventory.size() - 1);
        assertEquals(oreTile.dropItemType, minedItem.itemId);
        assertNull(gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow));
    }

    @Test
    public void miningUsesCollisionProximityForReachableOre() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.gameState = gh.play;

        OrePlacement placement = findCollisionReachableOrePlacement(gh, "emerald");
        assertNotNull(placement);

        Pickaxe pickaxe = new Pickaxe(gh);
        assertTrue(gh.player.addToInventory(pickaxe));
        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();
        assertTrue(gh.player.hasEquippedItem("diamond_pickaxe"));

        Tile oreTile = gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow);
        assertNotNull(oreTile);
        assertEquals("emerald", oreTile.dropItemType);

        int inventoryBeforeDrop = gh.player.inventory.size();
        for (int i = 0; i < oreTile.maxHealth; i++) {
            performMineInteraction(gh, placement);
        }

        assertEquals(inventoryBeforeDrop + 1, gh.player.inventory.size());
        Entity minedItem = gh.player.inventory.get(gh.player.inventory.size() - 1);
        assertEquals("emerald", minedItem.itemId);
        assertNull(gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow));
    }

    @Test
    public void unequipsPlayerWhenEquippedSwordLeavesInventory() {
        GameHandler gh = new GameHandler();
        Entity equippedSword = gh.player.currentWeapon;

        Entity removedItem = gh.player.removeFromInventory(0);
        gh.player.handleRemovedInventoryItem(removedItem);

        assertSame(equippedSword, removedItem);
        assertNull(gh.player.currentWeapon);
        assertEquals(0, gh.player.attack);

        gh.gameState = gh.play;
        gh.kh.space = true;
        gh.player.update();

        assertFalse(gh.player.attacking);
    }

    @Test
    public void equipsReplacementSwordWhenCurrentSwordLeavesInventory() {
        GameHandler gh = new GameHandler();
        Sword replacementSword = new Sword(gh);
        gh.player.addToInventory(replacementSword);

        Entity removedItem = gh.player.removeFromInventory(0);
        gh.player.handleRemovedInventoryItem(removedItem);

        assertSame(replacementSword, gh.player.currentWeapon);
        assertEquals(gh.player.strength * replacementSword.attackValue, gh.player.attack);
    }

    @Test
    public void equipsPickaxeWhenSelectedFromInventory() {
        GameHandler gh = new GameHandler();
        Pickaxe pickaxe = new Pickaxe(gh);
        gh.player.addToInventory(pickaxe);

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertSame(pickaxe, gh.player.currentWeapon);
        assertEquals(gh.player.strength * pickaxe.attackValue, gh.player.attack);
    }

    @Test
    public void usesAppleAndRemovesItFromInventory() {
        GameHandler gh = new GameHandler();
        Apple apple = new Apple(gh);
        assertTrue(gh.player.addToInventory(apple));
        gh.player.life = gh.player.maxLife - 1;
        gh.gameState = gh.inventory;

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(gh.player.maxLife, gh.player.life);
        assertEquals(2, gh.player.inventory.size());
        assertEquals("You ate an Apple. Health: 6/6", latestMessage(gh));
        assertEquals(gh.play, gh.gameState);
    }

    @Test
    public void usingHealingItemAtFullHealthDoesNotConsumeIt() {
        GameHandler gh = new GameHandler();
        Meat meat = new Meat(gh);
        assertTrue(gh.player.addToInventory(meat));
        gh.player.life = gh.player.maxLife;
        gh.gameState = gh.inventory;

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(gh.player.maxLife, gh.player.life);
        assertEquals(3, gh.player.inventory.size());
        assertSame(meat, gh.player.inventory.get(2));
        assertEquals(1, meat.stackCount);
        assertEquals("Health is full. Health: 6/6", latestMessage(gh));
        assertEquals(gh.play, gh.gameState);
    }

    @Test
    public void usingStackedMeatConsumesOnlyOneItem() {
        GameHandler gh = new GameHandler();
        assertTrue(gh.player.addToInventory(new Meat(gh)));
        assertTrue(gh.player.addToInventory(new Meat(gh)));
        gh.player.life = gh.player.maxLife - 2;

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(gh.player.maxLife, gh.player.life);
        assertEquals(3, gh.player.inventory.size());
        assertEquals(1, gh.player.inventory.get(2).stackCount);
        assertEquals("You ate Meat. Health: 6/6", latestMessage(gh));
    }

    @Test
    public void usingStackedMedkitConsumesOnlyOneItemAndCapsAtMaxHealth() {
        GameHandler gh = new GameHandler();
        assertTrue(gh.player.addToInventory(new Medkit(gh)));
        assertTrue(gh.player.addToInventory(new Medkit(gh)));
        gh.player.life = gh.player.maxLife - 3;

        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(gh.player.maxLife, gh.player.life);
        assertEquals(3, gh.player.inventory.size());
        assertEquals(1, gh.player.inventory.get(2).stackCount);
        assertEquals("You used a Medkit. Health: 6/6", latestMessage(gh));
    }

    @Test
    public void nonUsableItemsAreNotConsumedWhenSelected() {
        GameHandler gh = new GameHandler();
        Entity key = gh.player.inventory.get(1);

        gh.ui.slotCol = 1;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(2, gh.player.inventory.size());
        assertSame(key, gh.player.inventory.get(1));
    }

    @Test
    public void miningDoesNotStartPickaxeSwingWhenPickaxeIsNotEquipped() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.gameState = gh.play;

        OrePlacement placement = findMineableOrePlacement(gh);
        Tile oreTile = gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow);
        Pickaxe pickaxe = new Pickaxe(gh);
        assertTrue(gh.player.addToInventory(pickaxe));

        Sword sword = new Sword(gh);
        gh.player.currentWeapon = sword;
        gh.player.attack = gh.player.getAttack();

        gh.player.worldX = placement.worldX;
        gh.player.worldY = placement.worldY;
        gh.player.direction = placement.direction;
        gh.player.requestInteract();
        gh.kh.space = true;

        gh.player.update();

        assertFalse(gh.player.attacking);
        assertFalse(gh.player.isToolSwinging());
        assertSame(sword, gh.player.currentWeapon);
        assertSame(sword.getAttackSprite(placement.direction, 1), gh.player.resolveCurrentAttackSprite());
        assertNotNull(oreTile);
        assertNotNull(gh.th.getTopBreakableTileAt(placement.oreCol, placement.oreRow));
        assertSame(sword, gh.player.currentWeapon);
    }

    @Test
    public void stacksMatchingEmeraldsIntoSingleInventorySlot() {
        GameHandler gh = new GameHandler();

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        assertTrue(gh.player.addToInventory(new Emerald(gh)));

        assertEquals(3, gh.player.inventory.size());
        assertEquals(2, gh.player.inventory.get(2).stackCount);
    }

    @Test
    public void createsSecondInventorySlotWhenStackLimitIsExceeded() {
        GameHandler gh = new GameHandler();

        for (int i = 0; i < 100; i++) {
            assertTrue(gh.player.addToInventory(new Emerald(gh)));
        }

        assertEquals(4, gh.player.inventory.size());
        assertEquals(99, gh.player.inventory.get(2).stackCount);
        assertEquals(1, gh.player.inventory.get(3).stackCount);
    }

    @Test
    public void matchingPartialStackAcceptsPickupWhenInventorySlotsAreFull() {
        GameHandler gh = new GameHandler();

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        gh.player.inventory.get(2).stackCount = 98;

        while (gh.player.inventory.size() < gh.player.inventorySize) {
            assertTrue(gh.player.addToInventory(new Pickaxe(gh)));
        }

        assertTrue(gh.player.isInventoryFull());
        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        assertEquals(gh.player.inventorySize, gh.player.inventory.size());
        assertEquals(99, gh.player.inventory.get(2).stackCount);
    }

    @Test
    public void keepsWeaponsAsSeparateInventoryEntries() {
        GameHandler gh = new GameHandler();

        assertTrue(gh.player.addToInventory(new Pickaxe(gh)));
        assertTrue(gh.player.addToInventory(new Pickaxe(gh)));

        assertEquals(4, gh.player.inventory.size());
        assertEquals(1, gh.player.inventory.get(2).stackCount);
        assertEquals(1, gh.player.inventory.get(3).stackCount);
    }

    @Test
    public void stackedInventoryKeepsSlotSelectionCompact() {
        GameHandler gh = new GameHandler();
        Pickaxe pickaxe = new Pickaxe(gh);

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        assertTrue(gh.player.addToInventory(pickaxe));

        gh.ui.slotCol = 3;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        assertEquals(4, gh.player.inventory.size());
        assertSame(pickaxe, gh.player.currentWeapon);
    }

    @Test
    public void transfersChestStacksIntoExistingPlayerStacks() {
        GameHandler gh = new GameHandler();
        ChestState chest = new ChestState("/map/test.tmj", 0, 0);
        Emerald chestEmeralds = new Emerald(gh);
        chestEmeralds.stackCount = 3;

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        assertTrue(chest.addItem(chestEmeralds));

        gh.openChest(chest);
        gh.transferActiveChestSelection();

        assertTrue(chest.items.isEmpty());
        assertEquals(4, gh.player.inventory.get(2).stackCount);
    }

    @Test
    public void transfersPlayerStacksIntoExistingChestStacks() {
        GameHandler gh = new GameHandler();
        ChestState chest = new ChestState("/map/test.tmj", 0, 0);
        Emerald chestEmeralds = new Emerald(gh);
        chestEmeralds.stackCount = 4;
        Emerald playerEmeralds = new Emerald(gh);
        playerEmeralds.stackCount = 3;

        assertTrue(chest.addItem(chestEmeralds));
        assertTrue(gh.player.addToInventory(playerEmeralds));

        gh.openChest(chest);
        gh.ui.toggleChestPanel();
        gh.ui.moveChestCursor(2, 0);
        gh.transferActiveChestSelection();

        assertEquals(2, gh.player.inventory.size());
        assertEquals(7, chest.items.get(0).stackCount);
    }

    @Test
    public void fullStackTransferToPlayerFailsAtomicallyWhenCapacityIsInsufficient() {
        GameHandler gh = new GameHandler();
        ChestState chest = new ChestState("/map/test.tmj", 0, 0);
        Emerald chestEmeralds = new Emerald(gh);
        chestEmeralds.stackCount = 50;

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        gh.player.inventory.get(2).stackCount = 90;
        while (gh.player.inventory.size() < gh.player.inventorySize) {
            assertTrue(gh.player.addToInventory(new Pickaxe(gh)));
        }
        assertTrue(chest.addItem(chestEmeralds));

        gh.openChest(chest);
        gh.transferActiveChestSelection();

        assertEquals(90, gh.player.inventory.get(2).stackCount);
        assertEquals(50, chest.items.get(0).stackCount);
        assertEquals(50, getTotalEmeraldCount(chest.items));
    }

    @Test
    public void overflowTransferToPlayerUsesPartialStackSpaceAndFreeSlot() {
        GameHandler gh = new GameHandler();
        ChestState chest = new ChestState("/map/test.tmj", 0, 0);
        Emerald chestEmeralds = new Emerald(gh);
        chestEmeralds.stackCount = 50;

        assertTrue(gh.player.addToInventory(new Emerald(gh)));
        gh.player.inventory.get(2).stackCount = 90;
        while (gh.player.inventory.size() < gh.player.inventorySize - 1) {
            assertTrue(gh.player.addToInventory(new Pickaxe(gh)));
        }
        assertTrue(chest.addItem(chestEmeralds));

        gh.openChest(chest);
        gh.transferActiveChestSelection();

        assertTrue(chest.items.isEmpty());
        assertEquals(gh.player.inventorySize, gh.player.inventory.size());
        assertEquals(140, getTotalEmeraldCount(gh.player.inventory));
        assertEquals(2, countEmeraldStacks(gh.player.inventory));
        assertEquals(99, gh.player.inventory.get(2).stackCount);
    }

    @Test
    public void resolvesDirectionalPickaxeAttackSpritesFromEquippedWeapon() {
        GameHandler gh = new GameHandler();
        Pickaxe pickaxe = new Pickaxe(gh);
        gh.player.currentWeapon = pickaxe;

        assertAttackSprite(gh.player, "up", 1, pickaxe.attackUp1);
        assertAttackSprite(gh.player, "up", 2, pickaxe.attackUp2);
        assertAttackSprite(gh.player, "down", 1, pickaxe.attackDown1);
        assertAttackSprite(gh.player, "down", 2, pickaxe.attackDown2);
        assertAttackSprite(gh.player, "left", 1, pickaxe.attackLeft1);
        assertAttackSprite(gh.player, "left", 2, pickaxe.attackLeft2);
        assertAttackSprite(gh.player, "right", 1, pickaxe.attackRight1);
        assertAttackSprite(gh.player, "right", 2, pickaxe.attackRight2);
    }

    @Test
    public void swordStillUsesSwordAttackSprites() {
        GameHandler gh = new GameHandler();
        Sword sword = new Sword(gh);
        gh.player.currentWeapon = sword;

        gh.player.direction = "up";
        gh.player.spriteNum = 1;

        assertSame(sword.attackUp1, gh.player.resolveCurrentAttackSprite());
        assertNotEquals(sword.attackUp1, gh.player.up1);
    }

    @Test
    public void swordSwingCanOnlyDamageMonsterOnce() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;
        gh.monsters = new Entity[50];

        GreenSlime slime = new GreenSlime(gh);
        slime.worldX = gh.player.worldX;
        slime.worldY = gh.player.worldY - gh.tileSize;
        gh.monsters[0] = slime;

        gh.player.direction = "up";
        gh.kh.space = true;

        for (int i = 0; i < 30; i++) {
            gh.update();
        }

        assertEquals(slime.maxLife - Math.min(gh.player.attack, slime.maxLife - 1), slime.life);
    }

    @Test
    public void starterSwordStillDamagesDefensiveMonsters() {
        GameHandler gh = new GameHandler();
        Zombie zombie = new Zombie(gh);
        gh.monsters[0] = zombie;

        gh.player.damage(0, gh.player.attack);

        assertTrue(zombie.life < zombie.maxLife);
    }

    @Test
    public void strongPlayerCannotOneShotFullHealthMonster() {
        GameHandler gh = new GameHandler();
        GreenSlime slime = new GreenSlime(gh);
        gh.monsters[0] = slime;
        gh.player.strength = 99;
        gh.player.attack = gh.player.getAttack();

        gh.player.damage(0, gh.player.attack);

        assertEquals(1, slime.life);
        assertFalse(slime.dying);
    }

    @Test
    public void swordSwingCanOnlyDamageDragonOnce() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;

        Dragon dragon = new Dragon(gh);
        dragon.worldX = gh.player.worldX;
        dragon.worldY = gh.player.worldY - gh.tileSize;
        gh.monsters[0] = dragon;

        gh.player.direction = "up";
        gh.kh.space = true;

        for (int i = 0; i < 30; i++) {
            gh.update();
        }

        assertEquals(dragon.maxLife - Math.min(gh.player.attack, dragon.maxLife - 1), dragon.life);
    }

    @Test
    public void hittingMonsterAppliesKnockbackInAttackDirection() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;
        GreenSlime slime = new GreenSlime(gh);
        slime.worldX = gh.player.worldX;
        slime.worldY = gh.player.worldY - gh.tileSize;
        gh.monsters[0] = slime;
        gh.player.direction = "up";
        int startY = slime.worldY;

        gh.player.damage(0, gh.player.attack);
        assertEquals(startY, slime.worldY);

        for (int i = 0; i < 4; i++) {
            gh.update();
        }

        assertTrue(slime.worldY <= startY - (gh.tileSize / 2));
    }

    @Test
    public void lethalDamageOpensGameOverMenu() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;
        gh.player.life = 1;

        gh.player.receiveDamage(gh.player.defense + 1);

        assertEquals(0, gh.player.life);
        assertEquals(gh.gameOver, gh.gameState);
        assertEquals(0, gh.ui.getGameOverSelection());
    }

    @Test
    public void legacyEntityDamageAlsoClampsToZeroAndOpensGameOver() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;
        gh.player.life = 1;

        Entity attacker = new Entity(gh);
        attacker.damage(gh.player.defense + 2);

        assertEquals(0, gh.player.life);
        assertEquals(gh.gameOver, gh.gameState);
    }

    @Test
    public void gameOverMenuRespondsToKeyboardNavigation() {
        GameHandler gh = new GameHandler();
        gh.openGameOverMenu();

        gh.kh.keyPressed(new KeyEvent(gh, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 'S'));
        assertEquals(1, gh.ui.getGameOverSelection());

        gh.kh.keyPressed(new KeyEvent(gh, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'W'));
        assertEquals(0, gh.ui.getGameOverSelection());
    }

    @Test
    public void startingNewGameResetsPlayerAndReturnsToPlay() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.gameOver;
        gh.player.life = 1;
        gh.player.coin = 99;
        gh.player.inventory.clear();

        gh.sm.startNewGame();

        assertEquals(gh.play, gh.gameState);
        assertEquals(gh.player.maxLife, gh.player.life);
        assertEquals(0, gh.player.coin);
        assertEquals(2, gh.player.inventory.size());
        assertEquals(gh.tileSize * 25, gh.player.worldX);
        assertEquals(gh.tileSize * 6, gh.player.worldY);
    }

    @Test
    public void leveledPlayerStillTakesMinimumTwoDamageFromWeakMonsterMelee() {
        GameHandler gh = new GameHandler();
        gh.player.dexterity = 4;
        gh.player.defense = gh.player.getDefense();
        gh.player.life = gh.player.maxLife;

        gh.player.receiveDamage(new GreenSlime(gh).attack);

        assertEquals(gh.player.maxLife - 2, gh.player.life);
    }

    @Test
    public void shootingWithoutArrowsDoesNotPlaySound() {
        RecordingGameHandler gh = new RecordingGameHandler();
        gh.gameState = gh.play;

        gh.kh.shoot = true;
        gh.player.update();

        assertTrue(gh.projectileList.isEmpty());
        assertEquals(0, gh.soundPlayCount);
    }

    @Test
    public void successfulArrowShotPlaysArrowSoundOnce() {
        RecordingGameHandler gh = new RecordingGameHandler();
        gh.gameState = gh.play;

        Entity arrows = gh.createItemEntity("arrow");
        assertNotNull(arrows);
        assertTrue(gh.player.addToInventory(arrows));

        gh.kh.shoot = true;
        gh.player.update();

        assertEquals(1, gh.projectileList.size());
        assertEquals(1, gh.soundPlayCount);
        assertEquals(GameHandler.SFX_ARROW, gh.lastSoundId);
    }

    @Test
    public void shootingDuringCooldownDoesNotPlaySound() {
        RecordingGameHandler gh = new RecordingGameHandler();
        gh.gameState = gh.play;

        Entity arrows = gh.createItemEntity("arrow");
        assertNotNull(arrows);
        arrows.stackCount = 2;
        assertTrue(gh.player.addToInventory(arrows));

        gh.kh.shoot = true;
        gh.player.update();
        gh.projectileList.clear();
        gh.player.projectile.alive = false;

        gh.kh.shoot = true;
        gh.player.update();

        assertEquals(1, gh.soundPlayCount);
        assertEquals(GameHandler.SFX_ARROW, gh.lastSoundId);
        assertEquals(1, getItemCount(gh.player.inventory, "arrow"));
    }

    @Test
    public void swordAttackStartPlaysAttackSoundOnce() {
        RecordingGameHandler gh = new RecordingGameHandler();
        gh.gameState = gh.play;

        gh.kh.space = true;
        gh.player.update();

        assertTrue(gh.player.attacking);
        assertEquals(1, gh.soundPlayCount);
        assertEquals(GameHandler.SFX_SWORD_ATTACK, gh.lastSoundId);
    }

    @Test
    public void nonSwordSpaceActionsDoNotPlayAttackSound() {
        RecordingGameHandler gh = new RecordingGameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.gameState = gh.play;

        int[][] targetTiles = Player.resolveInteractionTiles(1272, 1808, gh.player.solidArea, gh.tileSize, "up");
        ChestState chest = gh.th.findChestAt(targetTiles);
        assertNotNull(chest);

        gh.player.worldX = 1272;
        gh.player.worldY = 1808;
        gh.player.direction = "up";
        gh.player.requestInteract();
        gh.kh.space = true;
        gh.player.update();

        assertEquals(gh.chest, gh.gameState);
        assertEquals(0, gh.soundPlayCount);

        RecordingGameHandler toolGh = new RecordingGameHandler();
        toolGh.gameState = toolGh.play;
        Pickaxe pickaxe = new Pickaxe(toolGh);
        assertTrue(toolGh.player.addToInventory(pickaxe));
        toolGh.ui.slotCol = 2;
        toolGh.ui.slotRow = 0;
        toolGh.player.selectItem();
        toolGh.kh.space = true;
        toolGh.player.update();

        assertTrue(toolGh.player.attacking);
        assertEquals(0, toolGh.soundPlayCount);

        RecordingGameHandler unarmedGh = new RecordingGameHandler();
        Entity removedItem = unarmedGh.player.removeFromInventory(0);
        unarmedGh.player.handleRemovedInventoryItem(removedItem);
        unarmedGh.gameState = unarmedGh.play;
        unarmedGh.kh.space = true;
        unarmedGh.player.update();

        assertFalse(unarmedGh.player.attacking);
        assertEquals(0, unarmedGh.soundPlayCount);
    }

    @Test
    public void shootingRequiresArrowInInventoryAndConsumesOnePerShot() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;

        gh.kh.shoot = true;
        gh.player.update();

        assertTrue(gh.projectileList.isEmpty());
        assertFalse(gh.player.hasItem("arrow"));

        Entity arrows = gh.createItemEntity("arrow");
        assertNotNull(arrows);
        arrows.stackCount = 2;
        assertTrue(gh.player.addToInventory(arrows));

        gh.kh.shoot = true;
        gh.player.update();

        assertEquals(1, gh.projectileList.size());
        assertEquals(1, getItemCount(gh.player.inventory, "arrow"));
    }

    @Test
    public void shootingNeedsANewKeyPressAndRespectsTwoSecondCooldown() {
        GameHandler gh = new GameHandler();
        gh.gameState = gh.play;

        Entity arrows = gh.createItemEntity("arrow");
        assertNotNull(arrows);
        arrows.stackCount = 2;
        assertTrue(gh.player.addToInventory(arrows));

        pressShootKey(gh);
        gh.update();

        assertEquals(1, gh.projectileList.size());
        assertEquals(1, getItemCount(gh.player.inventory, "arrow"));

        releaseShootKey(gh);
        pressShootKey(gh);
        gh.update();

        assertEquals(1, gh.projectileList.size());
        assertEquals(1, getItemCount(gh.player.inventory, "arrow"));

        releaseShootKey(gh);
        for (int i = 0; i < 130; i++) {
            gh.update();
        }

        assertTrue(gh.projectileList.isEmpty());
        assertEquals(1, getItemCount(gh.player.inventory, "arrow"));

        pressShootKey(gh);
        gh.update();

        assertEquals(0, getItemCount(gh.player.inventory, "arrow"));
        assertEquals(1, gh.projectileList.size());
    }

    @Test
    public void talkingToInnkeeperOpensSleepPrompt() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/inn.tmj");
        gh.refreshCurrentMapState();
        gh.gameState = gh.play;

        Entity innkeeper = gh.npcs[0];
        assertNotNull(innkeeper);

        gh.player.worldX = innkeeper.worldX;
        gh.player.worldY = innkeeper.worldY + 35;
        gh.player.direction = "up";
        gh.kh.space = true;

        gh.player.update();

        assertEquals(gh.dialogue, gh.gameState);
        assertTrue(gh.ik.isSleepPromptVisible());
        assertEquals("Want to rest?\nSleeping restores the world.", gh.ui.currentDialogue);
    }

    @Test
    public void sleepRestoresHealthWorldStateAndCyclesNightToDay() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");
        gh.refreshCurrentMapState();

        OrePlacement orePlacement = findMineableOrePlacement(gh);
        Pickaxe pickaxe = new Pickaxe(gh);
        assertTrue(gh.player.addToInventory(pickaxe));
        gh.ui.slotCol = 2;
        gh.ui.slotRow = 0;
        gh.player.selectItem();

        Tile oreTile = gh.th.getTopBreakableTileAt(orePlacement.oreCol, orePlacement.oreRow);
        assertNotNull(oreTile);

        for (int i = 0; i < oreTile.maxHealth; i++) {
            performMineInteraction(gh, orePlacement);
        }
        assertNull(gh.th.getTopBreakableTileAt(orePlacement.oreCol, orePlacement.oreRow));

        ChestState caveChest = gh.th.getChestAt(31, 9);
        assertNotNull(caveChest);
        caveChest.items.clear();
        assertTrue(caveChest.items.isEmpty());

        gh.th.loadMap("/map/inn.tmj");
        gh.refreshCurrentMapState();
        gh.player.life = gh.player.maxLife - 3;
        gh.dayNightCycle.setCurrentTick(0);

        gh.onPlayerSleep();

        assertEquals(gh.player.maxLife, gh.player.life);
        assertFalse(gh.dayNightCycle.isNight());
        assertEquals(gh.play, gh.gameState);

        gh.th.loadMap("/map/cave.tmj");
        gh.refreshCurrentMapState();

        assertNotNull(gh.th.getTopBreakableTileAt(orePlacement.oreCol, orePlacement.oreRow));
        assertFalse(gh.th.getChestAt(31, 9).items.isEmpty());
    }

    private String latestMessage(GameHandler gh) {
        return gh.ui.message.get(gh.ui.message.size() - 1);
    }

    private ChestPlacement findOpenableChestPlacement(GameHandler gh) {
        for (int col = 0; col < 200; col++) {
            for (int row = 0; row < 200; row++) {
                ChestState chest = gh.th.getChestAt(col, row);
                if (chest == null) {
                    continue;
                }

                ChestPlacement placement = findPlacementAroundChest(gh, col, row);
                if (placement != null) {
                    return placement;
                }
            }
        }

        fail("Expected to find at least one openable chest in the map.");
        return null;
    }

    private void pressShootKey(GameHandler gh) {
        gh.kh.keyPressed(new KeyEvent(gh, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F, 'F'));
    }

    private void releaseShootKey(GameHandler gh) {
        gh.kh.keyReleased(new KeyEvent(gh, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_F, 'F'));
    }

    private OrePlacement findMineableOrePlacement(GameHandler gh) {
        for (int col = 0; col < 200; col++) {
            for (int row = 0; row < 200; row++) {
                Tile oreTile = gh.th.getTopBreakableTileAt(col, row);
                if (oreTile == null || oreTile.requiredItemType == null || oreTile.requiredItemType.isBlank()) {
                    continue;
                }

                OrePlacement placement = findPlacementAroundOre(gh, col, row);
                if (placement != null) {
                    return placement;
                }
            }
        }

        fail("Expected to find at least one mineable ore tile with an open adjacent space.");
        return null;
    }

    private ChestPlacement findPlacementAroundChest(GameHandler gh, int chestCol, int chestRow) {
        if (!gh.th.isCollisionAt(chestCol, chestRow + 1)) {
            return new ChestPlacement(
                chestCol,
                chestRow,
                chestCol * gh.tileSize - 24,
                (chestRow + 1) * gh.tileSize - 16,
                "down"
            );
        }

        if (!gh.th.isCollisionAt(chestCol + 1, chestRow)) {
            return new ChestPlacement(
                chestCol,
                chestRow,
                chestCol * gh.tileSize + 40,
                chestRow * gh.tileSize - 16,
                "right"
            );
        }

        if (!gh.th.isCollisionAt(chestCol, chestRow - 1)) {
            return new ChestPlacement(
                chestCol,
                chestRow,
                chestCol * gh.tileSize - 24,
                chestRow * gh.tileSize - gh.tileSize,
                "up"
            );
        }

        if (!gh.th.isCollisionAt(chestCol - 1, chestRow)) {
            return new ChestPlacement(
                chestCol,
                chestRow,
                chestCol * gh.tileSize - 40,
                chestRow * gh.tileSize - 16,
                "left"
            );
        }

        return null;
    }

    private OrePlacement findPlacementAroundOre(GameHandler gh, int oreCol, int oreRow) {
        OrePlacement placement = buildOrePlacement(gh, oreCol, oreRow, oreCol, oreRow + 1, "up");
        if (placement != null) {
            return placement;
        }

        placement = buildOrePlacement(gh, oreCol, oreRow, oreCol + 1, oreRow, "left");
        if (placement != null) {
            return placement;
        }

        placement = buildOrePlacement(gh, oreCol, oreRow, oreCol, oreRow - 1, "down");
        if (placement != null) {
            return placement;
        }

        return buildOrePlacement(gh, oreCol, oreRow, oreCol - 1, oreRow, "right");
    }

    private OrePlacement findCollisionReachableOrePlacement(GameHandler gh, String dropItemType) {
        for (int col = 0; col < 200; col++) {
            for (int row = 0; row < 200; row++) {
                Tile oreTile = gh.th.getTopBreakableTileAt(col, row);
                if (oreTile == null || !dropItemType.equalsIgnoreCase(oreTile.dropItemType)) {
                    continue;
                }

                OrePlacement placement = findCollisionReachablePlacementAroundOre(gh, col, row);
                if (placement != null) {
                    return placement;
                }
            }
        }

        return null;
    }

    private OrePlacement findCollisionReachablePlacementAroundOre(GameHandler gh, int oreCol, int oreRow) {
        OrePlacement placement = buildCollisionReachableOrePlacement(gh, oreCol, oreRow, oreCol, oreRow + 1, "up");
        if (placement != null) {
            return placement;
        }

        placement = buildCollisionReachableOrePlacement(gh, oreCol, oreRow, oreCol + 1, oreRow, "left");
        if (placement != null) {
            return placement;
        }

        placement = buildCollisionReachableOrePlacement(gh, oreCol, oreRow, oreCol, oreRow - 1, "down");
        if (placement != null) {
            return placement;
        }

        return buildCollisionReachableOrePlacement(gh, oreCol, oreRow, oreCol - 1, oreRow, "right");
    }

    private OrePlacement buildOrePlacement(
        GameHandler gh,
        int oreCol,
        int oreRow,
        int standCol,
        int standRow,
        String direction
    ) {
        if (gh.th.isCollisionAt(standCol, standRow)) {
            return null;
        }

        int worldX = standCol * gh.tileSize - gh.player.solidArea.x;
        int worldY = standRow * gh.tileSize - gh.player.solidArea.y;
        int[] targetTile = Player.resolveInteractionTiles(worldX, worldY, gh.player.solidArea, gh.tileSize, direction)[0];
        if (targetTile[0] != oreCol || targetTile[1] != oreRow) {
            return null;
        }

        return new OrePlacement(oreCol, oreRow, worldX, worldY, direction);
    }

    private OrePlacement buildCollisionReachableOrePlacement(
        GameHandler gh,
        int oreCol,
        int oreRow,
        int standCol,
        int standRow,
        String direction
    ) {
        if (gh.th.isCollisionAt(standCol, standRow)) {
            return null;
        }

        int worldX = standCol * gh.tileSize - gh.player.solidArea.x;
        int worldY = standRow * gh.tileSize - gh.player.solidArea.y;
        int[][] targetTiles = Player.resolveInteractionTiles(worldX, worldY, gh.player.solidArea, gh.tileSize, direction);
        for (int[] targetTile : targetTiles) {
            if (targetTile[0] == oreCol && targetTile[1] == oreRow) {
                return null;
            }
        }

        int[] targetTile = gh.th.findBreakableTileNear(worldX, worldY, gh.player.solidArea, gh.tileSize);
        if (targetTile != null && targetTile[0] == oreCol && targetTile[1] == oreRow) {
            return new OrePlacement(oreCol, oreRow, worldX, worldY, direction);
        }

        return null;
    }

    private void performMineInteraction(GameHandler gh, OrePlacement placement) {
        gh.player.worldX = placement.worldX;
        gh.player.worldY = placement.worldY;
        gh.player.direction = placement.direction;
        gh.player.requestInteract();
        gh.kh.space = true;
        gh.player.update();
        gh.kh.space = false;

        for (int i = 0; i < 8; i++) {
            gh.player.update();
        }
    }

    private void assertAttackSprite(Player player, String direction, int spriteNum, BufferedImage expectedSprite) {
        player.direction = direction;
        player.spriteNum = spriteNum;
        assertSame(expectedSprite, player.resolveCurrentAttackSprite());
    }

    private int getTotalEmeraldCount(Iterable<Entity> items) {
        int total = 0;
        for (Entity item : items) {
            if (item instanceof Emerald) {
                total += item.stackCount;
            }
        }
        return total;
    }

    private int countEmeraldStacks(Iterable<Entity> items) {
        int count = 0;
        for (Entity item : items) {
            if (item instanceof Emerald) {
                count++;
            }
        }
        return count;
    }

    private int getItemCount(Iterable<Entity> items, String itemId) {
        int total = 0;
        for (Entity item : items) {
            if (item != null && itemId.equalsIgnoreCase(item.itemId)) {
                total += item.stackCount;
            }
        }
        return total;
    }

    private static final class ChestPlacement {
        final int chestCol;
        final int chestRow;
        final int worldX;
        final int worldY;
        final String facingAwayDirection;

        ChestPlacement(int chestCol, int chestRow, int worldX, int worldY, String facingAwayDirection) {
            this.chestCol = chestCol;
            this.chestRow = chestRow;
            this.worldX = worldX;
            this.worldY = worldY;
            this.facingAwayDirection = facingAwayDirection;
        }
    }

    private static final class OrePlacement {
        final int oreCol;
        final int oreRow;
        final int worldX;
        final int worldY;
        final String direction;

        OrePlacement(int oreCol, int oreRow, int worldX, int worldY, String direction) {
            this.oreCol = oreCol;
            this.oreRow = oreRow;
            this.worldX = worldX;
            this.worldY = worldY;
            this.direction = direction;
        }
    }
}
