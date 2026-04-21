package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.entity.BronzeDragon;
import com.nanocraft.game.tile.MapMarker;

public class DragonFightMergeTest {
    @Test
    public void swapMapToEndSpawnsBronzeDragonAtMarker() {
        GameHandler gh = new GameHandler();

        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");

        MapMarker marker = gh.th.getMarker("bronze_dragon_spawn");
        assertNotNull(marker);
        assertEquals(8, marker.col);
        assertEquals(9, marker.row);
        assertTrue(gh.monsters[0] instanceof BronzeDragon);
        assertEquals(marker.col * gh.tileSize, gh.monsters[0].worldX);
        assertEquals(marker.row * gh.tileSize, gh.monsters[0].worldY);
    }

    @Test
    public void defeatedBronzeDragonDoesNotRespawnWhenReturningToEnd() {
        GameHandler gh = new GameHandler();
        gh.setBronzeDragonDefeated(true);

        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");

        assertNotNull(gh.th.getMarker("bronze_dragon_spawn"));
        assertNull(gh.monsters[0]);
    }
}
