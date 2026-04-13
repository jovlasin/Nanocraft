package com.nanocraft.game.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
}
