package com.nanocraft.game.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
}
