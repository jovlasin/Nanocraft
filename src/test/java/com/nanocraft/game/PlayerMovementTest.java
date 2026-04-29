package com.nanocraft.game;

import com.nanocraft.game.core.CollisionHandler;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.input.KeyHandler;
import com.nanocraft.game.tile.TileHandler;
import com.nanocraft.game.entity.Player;
import com.nanocraft.game.entity.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerMovementTest {

    private GameHandler gh;
    private Player player;
    private KeyHandler kh;
    private StubCollisionHandler stubCollision;

    @Before
    public void setUp() {
        gh = new GameHandler();

        kh = new KeyHandler(gh);
        stubCollision = new StubCollisionHandler(gh);

        gh.kh = kh;
        gh.ch = stubCollision;
        gh.th = new NoTransitionTileHandler(gh);

        player = new Player(gh, kh, false);
        gh.player = player;

        player.worldX = gh.tileSize * 10;
        player.worldY = gh.tileSize * 10;
        player.direction = "down";

        clearKeys();
    }

    @Test
    public void testMoveUp() {
        int startY = player.worldY;

        kh.up = true;
        player.update();

        assertEquals("up", player.direction);
        assertEquals(startY - player.speed, player.worldY);
    }

    @Test
    public void testMoveDown() {
        int startY = player.worldY;

        kh.down = true;
        player.update();

        assertEquals("down", player.direction);
        assertEquals(startY + player.speed, player.worldY);
    }

    @Test
    public void testMoveLeft() {
        int startX = player.worldX;

        kh.left = true;
        player.update();

        assertEquals("left", player.direction);
        assertEquals(startX - player.speed, player.worldX);
    }

    @Test
    public void testMoveRight() {
        int startX = player.worldX;

        kh.right = true;
        player.update();

        assertEquals("right", player.direction);
        assertEquals(startX + player.speed, player.worldX);
    }

    @Test
    public void testHoldingRightMovesContinuously() {
        int startX = player.worldX;

        kh.right = true;

        for (int i = 0; i < 5; i++) {
            player.update();
        }

        assertEquals(startX + (player.speed * 5), player.worldX);
    }

    @Test
    public void testNoMovementWhenNoKeyPressed() {
        int startX = player.worldX;
        int startY = player.worldY;

        player.update();

        assertEquals(startX, player.worldX);
        assertEquals(startY, player.worldY);
    }

    @Test
    public void testCollisionBlocksMovement() {
        int startY = player.worldY;

        stubCollision.forceCollision = true;
        kh.up = true;
        player.update();

        assertEquals("up", player.direction);
        assertEquals(startY, player.worldY);
    }

    @Test
    public void testDiagonalInputPrioritizesUp() {
        int startX = player.worldX;
        int startY = player.worldY;

        kh.up = true;
        kh.right = true;
        player.update();

        assertEquals("up", player.direction);
        assertEquals(startX, player.worldX);
        assertEquals(startY - player.speed, player.worldY);
    }

    @Test
    public void testSpaceDoesNotMovePlayer() {
        int startX = player.worldX;
        int startY = player.worldY;

        kh.up = true;
        kh.space = true;
        player.update();

        assertEquals("up", player.direction);
        assertEquals(startX, player.worldX);
        assertEquals(startY, player.worldY);
    }

    private void clearKeys() {
        kh.up = false;
        kh.down = false;
        kh.left = false;
        kh.right = false;
        kh.space = false;
        kh.shoot = false;
    }

    private static class StubCollisionHandler extends CollisionHandler {
        boolean forceCollision = false;

        StubCollisionHandler(GameHandler gh) {
            super(gh);
        }

        @Override
        public void checkTile(Entity entity) {
            entity.collisionOn = forceCollision;
        }

        @Override
        public int checkObject(Entity entity, boolean player) {
            return 999;
        }

        @Override
        public int checkEntity(Entity entity, Entity[] targets) {
            return 999;
        }

        @Override
        public boolean checkPlayer(Entity entity) {
            return false;
        }
    }

    private static class NoTransitionTileHandler extends TileHandler {
        NoTransitionTileHandler(GameHandler gh) {
            super(gh);
        }

        @Override
        public void checkMapTransition() {
            // no-op for unit tests
        }

        @Override
        public int getContactDamageForArea(int worldX, int worldY, java.awt.Rectangle solidArea) {
            return 0;
        }
    }
}