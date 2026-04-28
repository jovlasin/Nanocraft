package com.nanocraft.game.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.core.GameHandler;

public class LavaDamageTest {
    private static final String CAVE_MAP_PATH = "/map/cave.tmj";
    private static final String NETHER_MAP_PATH = "/map/nether.tmj";

    @Test
    public void caveLavaUsesMapDamageAndDoesNotBlockMovement() {
        MapLoader.MapData mapData = new MapLoader(48).loadMap(CAVE_MAP_PATH);
        Tile lavaTile = findLavaTile(mapData);

        assertNotNull(lavaTile);
        assertEquals(5, lavaTile.contactDamage);
        assertTrue(lavaTile.collision == false);
    }

    @Test
    public void netherLavaUsesDefaultDamageWhenMapHasNoDamageProperty() {
        MapLoader.MapData mapData = new MapLoader(48).loadMap(NETHER_MAP_PATH);
        Tile lavaTile = findLavaTile(mapData);

        assertNotNull(lavaTile);
        assertTrue(lavaTile.contactDamage > 0);
        assertTrue(lavaTile.collision == false);
    }

    @Test
    public void playerTakesLavaDamageAndShowsBurnMessage() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap(CAVE_MAP_PATH);
        int[] lavaTile = findPlacedLavaTile(gh);
        int initialLife = gh.player.life;

        assertNotNull(lavaTile);
        gh.player.worldX = lavaTile[0] * gh.tileSize;
        gh.player.worldY = lavaTile[1] * gh.tileSize;

        gh.player.update();

        assertEquals(initialLife - gh.th.getContactDamageAt(lavaTile[0], lavaTile[1]), gh.player.life);
        assertTrue(gh.ui.message.contains("Lava burns!"));
    }

    private Tile findLavaTile(MapLoader.MapData mapData) {
        for (Tile tile : mapData.tileRegistry.values()) {
            if (tile != null && "Lava".equals(tile.type)) {
                return tile;
            }
        }

        return null;
    }

    private int[] findPlacedLavaTile(GameHandler gh) {
        for (int col = 0; col < 200; col++) {
            for (int row = 0; row < 200; row++) {
                if (gh.th.getContactDamageAt(col, row) > 0) {
                    return new int[] { col, row };
                }
            }
        }

        return null;
    }
}
