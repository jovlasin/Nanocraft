package com.nanocraft.game.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Rectangle;

import org.junit.Test;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;

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
