package com.nanocraft.game;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Player;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapIntegrationTest {

    private GameHandler gh;
    private Player player;

    @Before
    public void setUp() {
        gh = new GameHandler(); // uses real TileHandler + map
        player = gh.player;
    }

    // Test 1. Map loads correctly
    @Test
    public void testMapLoads() {
        assertNotNull(gh.th);
        assertNotNull(gh.th.getCurrentMapPath());
        assertTrue(gh.th.getLayerCount() > 0);
    }

    // Test 2. Map dimensions valid
    @Test
    public void testMapHasValidDimensions() {
        assertTrue(gh.th.getLayerCount() > 0);
        assertTrue(gh.tileSize > 0);
    }

    // Test 3. Collision system works (edge of map should block)
    @Test
    public void testOutOfBoundsIsCollision() {
        boolean collision = gh.th.isCollisionAt(-1, -1);
        assertTrue(collision);
    }

    // Test 4. Player movement works on real map (no collision)
    @Test
    public void testPlayerMovesOnWalkableTile() {
        int startX = player.worldX;

        gh.kh.right = true;
        player.update();

        assertTrue(player.worldX != startX);
    }

    // Test 5. Player blocked by collision tile (edge test)
    @Test
    public void testPlayerBlockedAtMapEdge() {
        // force player near edge
        player.worldX = 0;
        player.worldY = 0;

        int startX = player.worldX;

        gh.kh.left = true;
        player.update();

        assertEquals(startX, player.worldX);
    }

    // Test 6. Map transition system does not crash
    @Test
    public void testMapTransitionCheckRuns() {
        player.worldX = gh.tileSize * 5;
        player.worldY = gh.tileSize * 5;

        gh.th.checkMapTransition();

        assertNotNull(gh.th.getCurrentMapPath());
    }
}
