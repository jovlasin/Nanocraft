package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.entity.BronzeDragon;
import com.nanocraft.game.tile.MapMarker;
import com.nanocraft.game.tile.MapTransition;

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
        assertEquals(36, gh.monsters[0].maxLife);
        assertEquals(36, gh.monsters[0].life);
        assertEquals(marker.col * gh.tileSize, gh.monsters[0].worldX);
        assertEquals(marker.row * gh.tileSize, gh.monsters[0].worldY);
    }

    @Test
    public void endPortalAppearsOnlyAfterBronzeDragonDefeat() {
        GameHandler gh = new GameHandler();

        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");

        MapMarker portalMarker = gh.th.getMarker("end_return_portal_spawn");
        assertNotNull(portalMarker);
        assertNull(gh.th.getTransitionAt(portalMarker.col, portalMarker.row));

        gh.handleBronzeDragonDefeat();

        MapTransition transition = gh.th.getTransitionAt(portalMarker.col, portalMarker.row);
        assertNotNull(transition);
        assertEquals("/map/village.tmj", transition.targetMapPath);
        assertEquals(25, transition.targetCol);
        assertEquals(6, transition.targetRow);
        assertEquals("down", transition.targetDirection);
    }

    @Test
    public void enteringEndPortalReturnsPlayerToVillage() {
        GameHandler gh = new GameHandler();

        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");
        gh.handleBronzeDragonDefeat();

        MapMarker portalMarker = gh.th.getMarker("end_return_portal_spawn");
        assertNotNull(portalMarker);

        gh.player.worldX = portalMarker.col * gh.tileSize;
        gh.player.worldY = portalMarker.row * gh.tileSize;
        gh.player.direction = "down";

        gh.th.checkMapTransition();

        assertEquals("/map/village.tmj", gh.th.getCurrentMapPath());
        assertEquals(25 * gh.tileSize, gh.player.worldX);
        assertEquals(6 * gh.tileSize, gh.player.worldY);
        assertEquals("down", gh.player.direction);
    }

    @Test
    public void defeatedBronzeDragonDoesNotRespawnWhenReturningToEnd() {
        GameHandler gh = new GameHandler();

        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");
        gh.handleBronzeDragonDefeat();
        gh.th.swapMap("/map/village.tmj", 25, 6, "down");
        gh.th.swapMap(AssetHandler.END_MAP_PATH, 8, 9, "down");

        assertNull(gh.monsters[0]);
        assertNotNull(gh.th.getMarker("bronze_dragon_spawn"));
        assertNotNull(gh.th.getTransitionAt(gh.th.getMarker("end_return_portal_spawn").col, gh.th.getMarker("end_return_portal_spawn").row));
    }
}
