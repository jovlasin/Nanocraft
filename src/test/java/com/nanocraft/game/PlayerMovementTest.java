package com.nanocraft.game;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;
import com.nanocraft.game.core.CollisionHandler;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.input.KeyHandler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerMovementTest {

    private TestGameHandler gh;
    private Player player;
    private KeyHandler kh;

    @Before
    public void setUp() {
        gh = new TestGameHandler();
        kh = gh.kh;
        player = gh.player;

        kh.up = false;
        kh.down = false;
        kh.left = false;
        kh.right = false;
        kh.space = false;

        player.collisionOn = false;
        player.direction = "down";
    }

    @Test
    public void testMoveUp() {
        TestGameHandler gh = new TestGameHandler();
        Player player = gh.player;

        int startY = player.worldY;

        gh.kh.up = true;
        player.handleMovementOnly();

        assertEquals("up", player.direction);
        assertEquals(startY - player.speed, player.worldY);
    }

    @Test
    public void testMoveDown() {
        int startY = player.worldY;

        kh.down = true;
        player.handleMovementOnly();

        assertEquals("down", player.direction);
        assertEquals(startY + player.speed, player.worldY);
    }

    @Test
    public void testMoveLeft() {
        int startX = player.worldX;

        kh.left = true;
        player.handleMovementOnly();

        assertEquals("left", player.direction);
        assertEquals(startX - player.speed, player.worldX);
    }

    @Test
    public void testMoveRight() {
        int startX = player.worldX;

        kh.right = true;
        player.handleMovementOnly();

        assertEquals("right", player.direction);
        assertEquals(startX + player.speed, player.worldX);
    }

    @Test
    public void testHoldMovementKeyContinuously() {
        int startX = player.worldX;
        kh.right = true;

        for (int i = 0; i < 5; i++) {
            player.handleMovementOnly();
        }

        assertEquals(startX + (player.speed * 5), player.worldX);
    }

   @Test
    public void testCollisionStopsMovement() {
        TestGameHandler gh = new TestGameHandler();
        Player player = gh.player;

        int startY = player.worldY;

        gh.forceCollision = true;
        gh.kh.up = true;

        player.handleMovementOnly();

        assertEquals(startY, player.worldY);
    }

    @Test
    public void testDiagonalInputPrioritizesUp() {
        int startX = player.worldX;
        int startY = player.worldY;

        kh.up = true;
        kh.right = true;

        player.handleMovementOnly();

        assertEquals("up", player.direction);
        assertEquals(startX, player.worldX);
        assertEquals(startY - player.speed, player.worldY);
    }

    @Test
    public void testSpacePreventsMovement() {
        int startX = player.worldX;
        int startY = player.worldY;

        kh.up = true;
        kh.space = true;

        player.handleMovementOnly();

        assertEquals("up", player.direction);
        assertEquals(startX, player.worldX);
        assertEquals(startY, player.worldY);
    }

    @Test
    public void testMovementAfterTransitionLikePositionReset() {
        player.worldX = gh.tileSize * 5;
        player.worldY = gh.tileSize * 8;

        int startX = player.worldX;
        kh.right = true;

        player.handleMovementOnly();

        assertEquals(startX + player.speed, player.worldX);
    }

    static class TestGameHandler extends GameHandler {

        public KeyHandler kh;
        public Player player;
        public CollisionHandler ch;

        public boolean forceCollision = false;

        public TestGameHandler() {
            super();

            this.kh = new KeyHandler(this);
            this.player = new Player(this, kh, false);

            final TestGameHandler ghRef = this;

            this.ch = new CollisionHandler(this) {
                @Override
                public void checkTile(Entity entity) {
                    entity.collisionOn = ghRef.forceCollision;
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
            };
        }
    }

    static class FakeCollisionHandler extends CollisionHandler {
        private final TestGameHandler testGh;

        public FakeCollisionHandler(TestGameHandler gh) {
            super(gh);
            this.testGh = gh;
        }

        @Override
        public void checkTile(Entity entity) {
            entity.collisionOn = testGh.forceCollision;
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

    static class FakeTileHandler {

        public boolean forceCollision = false;

        public boolean isCollisionAt(int col, int row) {
            return forceCollision;
        }

        public void checkMapTransition() {
            // do nothing
        }
    }

    
}