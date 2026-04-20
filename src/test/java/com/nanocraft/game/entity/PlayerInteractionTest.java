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
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Sword;

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

    private void assertAttackSprite(Player player, String direction, int spriteNum, BufferedImage expectedSprite) {
        player.direction = direction;
        player.spriteNum = spriteNum;
        assertSame(expectedSprite, player.resolveCurrentAttackSprite());
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
}
