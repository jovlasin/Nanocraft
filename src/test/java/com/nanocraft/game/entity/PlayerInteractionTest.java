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
import java.awt.image.BufferedImage;

import org.junit.Test;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.object.Emerald;
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Sword;
import com.nanocraft.game.tile.Tile;

public class PlayerInteractionTest {
    private static final Rectangle SOLID_AREA = new Rectangle(8, 16, 32, 32);
    private static final int TILE_SIZE = 48;

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
