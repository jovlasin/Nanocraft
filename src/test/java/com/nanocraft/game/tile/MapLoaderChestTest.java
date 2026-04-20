package com.nanocraft.game.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.core.GameHandler;

public class MapLoaderChestTest {
    @Test
    public void loadsChestMarkersFromCaveMap() {
        MapLoader loader = new MapLoader(48);
        MapLoader.MapData mapData = loader.loadMap("/map/cave.tmj");

        assertEquals(13, mapData.chestDefinitions.size());
        assertTrue(mapData.chestDefinitions.stream()
            .anyMatch(chest -> chest.col == 31 && chest.row == 9 && chest.lootItemIds.contains("key")));
        assertTrue(mapData.chestDefinitions.stream()
            .anyMatch(chest -> chest.col == 29 && chest.row == 20 && chest.lootItemIds.size() == 3));
    }

    @Test
    public void loadsRectangleAndTileObjectChestMarkers() {
        MapLoader loader = new MapLoader(48);
        MapLoader.MapData mapData = loader.loadMap("/map/chest-object-types.tmj");

        assertEquals(2, mapData.chestDefinitions.size());
        assertTrue(mapData.chestDefinitions.stream()
            .anyMatch(chest -> chest.col == 1 && chest.row == 1 && chest.lootItemIds.contains("key")));
        assertTrue(mapData.chestDefinitions.stream()
            .anyMatch(chest -> chest.col == 2 && chest.row == 2 && chest.lootItemIds.contains("ore_chunk")));
    }

    @Test
    public void registersVillageChestTilesWithoutObjectMarkers() {
        MapLoader loader = new MapLoader(48);
        MapLoader.MapData mapData = loader.loadMap("/map/village.tmj");
        int[] chestTile = findFirstChestTile(mapData, "035");

        assertNotNull(chestTile);
        assertTrue(mapData.chestDefinitions.isEmpty());

        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/village.tmj");

        assertNotNull(gh.th.getChestAt(chestTile[0], chestTile[1]));
    }

    @Test
    public void registersChestLikeFallbackTilesOnMapsWithoutMarkers() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/chest-like-fallback.tmj");

        assertNotNull(gh.th.getChestAt(1, 1));
        assertTrue(gh.th.isCollisionAt(1, 1));
    }

    @Test
    public void doesNotRegisterDecorativeChestLikeTilesOnMarkerMaps() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");

        assertNull(gh.th.getChestAt(10, 35));
        assertNotNull(gh.th.getChestAt(9, 35));
    }

    private int[] findFirstChestTile(MapLoader.MapData mapData, String tileType) {
        for (int col = 0; col < mapData.mapWidth; col++) {
            for (int row = 0; row < mapData.mapHeight; row++) {
                if (!hasTileTypeAt(mapData, col, row, tileType)) {
                    continue;
                }

                return new int[] { col, row };
            }
        }

        return null;
    }

    private boolean hasTileTypeAt(MapLoader.MapData mapData, int col, int row, String tileType) {
        for (int[][] layer : mapData.layers) {
            int tileId = layer[col][row];
            if (mapData.zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile tile = mapData.tileRegistry.get(tileId);
            if (tile == null || tile.type == null) {
                continue;
            }

            if (tileType.equalsIgnoreCase(tile.type)) {
                return true;
            }
        }

        return false;
    }
}
