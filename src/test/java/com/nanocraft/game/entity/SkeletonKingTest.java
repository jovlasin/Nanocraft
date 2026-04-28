package com.nanocraft.game.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.monster.SkeletonKing;

public class SkeletonKingTest {
    @Test
    public void SkeletonKingSpawnsOnlyOnNetherMap() {
        GameHandler gh = new GameHandler();

        gh.th.loadMap("/map/nether.tmj");
        gh.refreshCurrentMapState();

        assertTrue(gh.monsters[0] instanceof SkeletonKing);

        gh.th.loadMap("/map/village.tmj");
        gh.refreshCurrentMapState();

         assertFalse(gh.monsters[0] instanceof SkeletonKing);
    }

    @Test
    public void SkeletonKingShootsArrowAtNearbyPlayer() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/nether.tmj");

        int[] spawnTile = gh.th.findNearestOpenTile(25, 25);
        assertNotNull(spawnTile);

        SkeletonKing boss = new SkeletonKing(gh);
        boss.worldX = spawnTile[0] * gh.tileSize;
        boss.worldY = spawnTile[1] * gh.tileSize;
        boss.shotCounter = 74;

        gh.player.worldX = boss.worldX + (gh.tileSize * 3);
        gh.player.worldY = boss.worldY;

        boss.update();

        assertEquals("right", boss.direction);
        assertEquals(1, gh.projectileList.size());
        Projectile projectile = (Projectile) gh.projectileList.get(0);
        assertSame(boss, projectile.user);
        assertEquals("right", projectile.direction);
    }

    @Test
    public void SkeletonKingDropsEyeOfEnderWhenDefeated() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/nether.tmj");
        gh.refreshCurrentMapState();
        gh.gameState = gh.play;

        assertTrue(gh.monsters[0] instanceof SkeletonKing);

        gh.monsters[0].life = 1;
        gh.player.damage(0, 99);
        gh.update();

        assertNull(gh.monsters[0]);
        assertTrue(hasWorldObject(gh, "eye_of_ender"));
    }

    @Test
    public void starterSwordDamagesSkeletonKing() {
        GameHandler gh = new GameHandler();
        SkeletonKing boss = new SkeletonKing(gh);
        boss.life = boss.maxLife;
        gh.monsters[0] = boss;

        gh.player.damage(0, gh.player.attack);

        assertEquals(boss.maxLife - 1, boss.life);
    }

    private boolean hasWorldObject(GameHandler gh, String itemId) {
        for (Entity object : gh.objs) {
            if (object != null && itemId.equals(object.itemId)) {
                return true;
            }
        }

        return false;
    }
}
