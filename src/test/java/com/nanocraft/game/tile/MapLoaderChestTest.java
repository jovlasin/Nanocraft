package com.nanocraft.game.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class MapLoaderChestTest {
    private static final Set<String> RANDOM_CHEST_ITEM_NAMES = Set.of(
        "Arrow",
        "Apple",
        "Meat",
        "Medkit",
        "Emerald",
        "Diamond",
        "Redstone"
    );

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
    public void villageFallbackChestsReceiveStableRandomLootWithSinglePickaxeChest() {
        MapLoader loader = new MapLoader(48);
        MapLoader.MapData mapData = loader.loadMap("/map/village.tmj");
        List<int[]> chestTiles = findChestTiles(mapData);
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/village.tmj");

        Map<String, List<String>> initialLootByChest = new HashMap<>();
        int pickaxeItemCount = 0;
        int pickaxeOnlyChestCount = 0;

        for (int[] chestTile : chestTiles) {
            ChestState chest = gh.th.getChestAt(chestTile[0], chestTile[1]);
            assertNotNull(chest);
            List<String> lootNames = getItemNames(chest);
            assertLootIsInitialized(chest);
            initialLootByChest.put(chest.getKey(), lootNames);

            if (lootNames.size() == 1 && "Diamond Pickaxe".equals(lootNames.get(0))) {
                pickaxeOnlyChestCount++;
                pickaxeItemCount++;
                continue;
            }

            for (String lootName : lootNames) {
                assertTrue(RANDOM_CHEST_ITEM_NAMES.contains(lootName));
            }
        }

        assertEquals(1, pickaxeItemCount);
        assertEquals(1, pickaxeOnlyChestCount);

        gh.th.loadMap("/map/village.tmj");

        for (int[] chestTile : chestTiles) {
            ChestState chest = gh.th.getChestAt(chestTile[0], chestTile[1]);
            assertNotNull(chest);
            assertEquals(initialLootByChest.get(chest.getKey()), getItemNames(chest));
        }
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

    @Test
    public void caveChestMarkersReceiveStableRandomObjectLootWithoutPickaxe() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/cave.tmj");

        Map<String, List<String>> initialLootByChest = new HashMap<>();

        for (int col = 0; col < 200; col++) {
            for (int row = 0; row < 200; row++) {
                ChestState chest = gh.th.getChestAt(col, row);
                if (chest == null) {
                    continue;
                }

                assertLootIsInitialized(chest);
                List<String> lootNames = getItemNames(chest);
                assertFalse(lootNames.contains("Diamond Pickaxe"));

                for (String lootName : lootNames) {
                    assertTrue(RANDOM_CHEST_ITEM_NAMES.contains(lootName));
                }

                initialLootByChest.put(chest.getKey(), lootNames);
            }
        }

        gh.th.loadMap("/map/cave.tmj");

        for (Map.Entry<String, List<String>> entry : initialLootByChest.entrySet()) {
            String[] keyParts = entry.getKey().split("#|:");
            ChestState chest = gh.th.getChestAt(Integer.parseInt(keyParts[1]), Integer.parseInt(keyParts[2]));
            assertNotNull(chest);
            assertEquals(entry.getValue(), getItemNames(chest));
        }
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

    private List<int[]> findChestTiles(MapLoader.MapData mapData) {
        List<int[]> chestTiles = new ArrayList<>();

        for (int col = 0; col < mapData.mapWidth; col++) {
            for (int row = 0; row < mapData.mapHeight; row++) {
                if (hasChestTileAt(mapData, col, row)) {
                    chestTiles.add(new int[] { col, row });
                }
            }
        }

        return chestTiles;
    }

    @Test
    public void createsArrowChestLootAsRandomStackBetweenFiveAndTen() {
        GameHandler gh = new GameHandler();

        for (int i = 0; i < 25; i++) {
            Entity arrows = gh.th.createChestLootItem("arrow");
            assertNotNull(arrows);
            assertEquals("arrow", arrows.itemId);
            assertTrue(arrows.stackCount >= 5);
            assertTrue(arrows.stackCount <= 10);
        }
    }

    @Test
    public void createsNonArrowChestLootWithDefaultSingleItemStack() {
        GameHandler gh = new GameHandler();

        Entity apple = gh.th.createChestLootItem("apple");
        assertNotNull(apple);
        assertEquals("apple", apple.itemId);
        assertEquals(1, apple.stackCount);
    }

    @Test
    public void duplicateArrowLootEntriesStillCreateOneArrowStackPerChest() throws Exception {
        GameHandler gh = new GameHandler();
        ChestState chest = new ChestState("/map/test.tmj", 0, 0);
        Method addLootItems = TileHandler.class.getDeclaredMethod("addLootItems", ChestState.class, List.class, String.class);
        addLootItems.setAccessible(true);

        addLootItems.invoke(gh.th, chest, List.of("arrow", "arrow", "apple"), chest.getKey());

        assertEquals(1, countItemStacks(chest, "arrow"));
        int arrowCount = getItemCount(chest.items, "arrow");
        assertTrue(arrowCount >= 5);
        assertTrue(arrowCount <= 10);
        assertEquals(1, getItemCount(chest.items, "apple"));
    }

    private boolean hasChestTileAt(MapLoader.MapData mapData, int col, int row) {
        return hasTileTypeAt(mapData, col, row, "034") || hasTileTypeAt(mapData, col, row, "035");
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

    private List<String> getItemNames(ChestState chest) {
        List<String> itemNames = new ArrayList<>();

        for (Entity item : chest.items) {
            itemNames.add(item.name);
        }

        return itemNames;
    }

    private int countItemStacks(ChestState chest, String itemId) {
        int count = 0;

        for (Entity item : chest.items) {
            if (item != null && itemId.equalsIgnoreCase(item.itemId)) {
                count++;
            }
        }

        return count;
    }

    private int getItemCount(Iterable<Entity> items, String itemId) {
        int total = 0;

        for (Entity item : items) {
            if (item != null && itemId.equalsIgnoreCase(item.itemId)) {
                total += item.stackCount;
            }
        }

        return total;
    }

    private void assertLootIsInitialized(ChestState chest) {
        assertFalse(chest.items.isEmpty());
        assertTrue(chest.items.size() <= 3);

        for (Entity item : chest.items) {
            assertNotNull(item);
            assertNotNull(item.name);
            assertNotNull(item.down1);
            if ("arrow".equalsIgnoreCase(item.itemId)) {
                assertTrue(item.stackCount >= 5);
                assertTrue(item.stackCount <= 10);
            }
        }
    }
}
