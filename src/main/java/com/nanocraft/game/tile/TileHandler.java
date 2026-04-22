package com.nanocraft.game.tile;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.nanocraft.game.core.ChestState;
import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.core.SaveManager;
import com.nanocraft.game.entity.Entity;

public class TileHandler {
    private static final String DEFAULT_MAP_PATH = "/map/village.tmj";
    private static final String VILLAGE_MAP_PATH = "/map/village.tmj";
    private static final String CHEST_INTERACTION_TYPE = "chest";
    private static final Set<String> FALLBACK_CHEST_TILE_TYPES = Set.of("034", "035");
    private static final Set<String> MARKER_MAP_CHEST_TILE_TYPES = Set.of("035");
    private static final int MAX_RANDOM_CHEST_LOOT_ITEMS = 3;
    private static final List<String> RANDOM_CHEST_LOOT_POOL = List.of(
        "apple",
        "meat",
        "medkit",
        "emerald",
        "diamond",
        "redstone"
    );
    private final GameHandler gh;
    private final MapLoader mapLoader;
    private final Map<Integer, Tile> tileRegistry;
    private final List<int[][]> layers;
    private final List<int[][]> layerHealth;
    private final List<String> layerNames;
    private final List<MapMarker> markers;
    private final Map<String, ChestState> chestRegistry;
    private final Map<String, StoredMapState> storedMapStates;

    private int mapWidth;
    private int mapHeight;
    private int belowPlayerLayerCount;
    private boolean zeroMeansEmpty;
    private boolean currentMapHasChestDefinitions;
    private String currentMapPath;
    private final List<MapTransition> transitions;
    private final Random random;
    private String specialVillageChestKey;

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        this.mapLoader = new MapLoader(gh.tileSize);
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerHealth = new ArrayList<>();
        this.layerNames = new ArrayList<>();
        this.chestRegistry = new HashMap<>();
        this.storedMapStates = new HashMap<>();
        this.transitions = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.random = new Random();
        loadMap(DEFAULT_MAP_PATH);
    }

    public int getLayerCount() {
        return layers.size();
    }

    public int getBelowPlayerLayerCount() {
        return belowPlayerLayerCount;
    }

    public void drawLayer(Graphics2D g2, int layerIndex) {
        if (layerIndex < 0 || layerIndex >= layers.size()) {
            return;
        }

        int[][] layer = layers.get(layerIndex);
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < mapWidth && worldRow < mapHeight) {
            int num = layer[worldCol][worldRow];

            if (zeroMeansEmpty && num == 0) {
                worldCol++;
                if (worldCol == mapWidth) {
                    worldCol = 0;
                    worldRow++;
                }
                continue;
            }

            Tile currentTile = getTile(num);
            if (currentTile == null || currentTile.image == null) {
                worldCol++;
                if (worldCol == mapWidth) {
                    worldCol = 0;
                    worldRow++;
                }
                continue;
            }

            int worldX = worldCol * gh.tileSize;
            int worldY = worldRow * gh.tileSize;
            int screenX = worldX - gh.player.worldX + gh.player.screenX;
            int screenY = worldY - gh.player.worldY + gh.player.screenY;

            if (worldX + gh.tileSize > gh.player.worldX - gh.player.screenX &&
                worldX - gh.tileSize < gh.player.worldX + gh.player.screenX &&
                worldY + gh.tileSize > gh.player.worldY - gh.player.screenY &&
                worldY - gh.tileSize < gh.player.worldY + gh.player.screenY) {
                g2.drawImage(currentTile.image, screenX, screenY, null);
            }

            worldCol++;
            if (worldCol == mapWidth) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public boolean isCollisionAt(int col, int row) {
        if (col < 0 || row < 0 || col >= mapWidth || row >= mapHeight) {
            return true;
        }

        if (getChestAt(col, row) != null) {
            return true;
        }

        for (int[][] layer : layers) {
            int tileId = layer[col][row];
            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile currentTile = getTile(tileId);
            if (currentTile != null && currentTile.collision) {
                return true;
            }
        }
        return false;
    }

    public void loadMap(String mapPath) {
        rememberCurrentMapState();

        MapLoader.MapData mapData = mapLoader.loadMap(mapPath);
        tileRegistry.clear();
        layers.clear();
        layerNames.clear();
        markers.clear();

        tileRegistry.putAll(mapData.tileRegistry);
        layers.addAll(mapData.layers);
        layerNames.addAll(mapData.layerNames);
        transitions.clear();
        transitions.addAll(mapData.transitions);
        markers.addAll(mapData.markers);
        mapWidth = mapData.mapWidth;
        mapHeight = mapData.mapHeight;
        zeroMeansEmpty = mapData.zeroMeansEmpty;
        currentMapHasChestDefinitions = mapData.chestDefinitions != null && !mapData.chestDefinitions.isEmpty();
        currentMapPath = mapPath;
        registerChestDefinitions(mapData.chestDefinitions);
        registerChestTiles();
        initializeLayerHealth();
        restoreStoredMapState(mapPath);
        updateBelowPlayerLayerCount();
    }

    public ChestState getChestAt(int col, int row) {
        if (!isInsideMap(col, row) || currentMapPath == null || currentMapPath.isBlank()) {
            return null;
        }

        return chestRegistry.get(ChestState.buildKey(currentMapPath, col, row));
    }

    public ChestState findChestAt(int[][] targetTiles) {
        if (targetTiles == null) {
            return null;
        }

        for (int[] targetTile : targetTiles) {
            if (targetTile == null || targetTile.length < 2) {
                continue;
            }

            ChestState chest = getChestAt(targetTile[0], targetTile[1]);
            if (chest != null) {
                return chest;
            }
        }

        return null;
    }

    public ChestState findChestNear(int worldX, int worldY, java.awt.Rectangle solidArea, int padding) {
        if (solidArea == null) {
            return null;
        }

        int leftWorldX = worldX + solidArea.x - padding;
        int rightWorldX = worldX + solidArea.x + solidArea.width - 1 + padding;
        int topWorldY = worldY + solidArea.y - padding;
        int bottomWorldY = worldY + solidArea.y + solidArea.height - 1 + padding;

        int startCol = Math.floorDiv(leftWorldX, gh.tileSize);
        int endCol = Math.floorDiv(rightWorldX, gh.tileSize);
        int startRow = Math.floorDiv(topWorldY, gh.tileSize);
        int endRow = Math.floorDiv(bottomWorldY, gh.tileSize);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                ChestState chest = getChestAt(col, row);
                if (chest != null) {
                    return chest;
                }
            }
        }

        return null;
    }

    public boolean hasTileTypeAt(int col, int row, String tileType) {
        if (!isInsideMap(col, row) || tileType == null || tileType.isBlank()) {
            return false;
        }

        for (int[][] layer : layers) {
            int tileId = layer[col][row];
            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile currentTile = getTile(tileId);
            if (currentTile == null || currentTile.type == null) {
                continue;
            }

            if (tileType.equalsIgnoreCase(currentTile.type)) {
                return true;
            }
        }

        return false;
    }

    public String getInteractionTypeAt(int col, int row) {
        if (!isInsideMap(col, row)) {
            return null;
        }

        for (int layerIndex = layers.size() - 1; layerIndex >= 0; layerIndex--) {
            int tileId = layers.get(layerIndex)[col][row];
            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile currentTile = getTile(tileId);
            if (currentTile != null && currentTile.interactionType != null && !currentTile.interactionType.isBlank()) {
                return currentTile.interactionType;
            }
        }

        return null;
    }

    public String getCurrentMapPath() {
        return currentMapPath;
    }

    public MapMarker getMarker(String markerName) {
        if (markerName == null || markerName.isBlank()) {
            return null;
        }

        for (MapMarker marker : markers) {
            if (marker != null && marker.name != null && marker.name.equalsIgnoreCase(markerName)) {
                return marker;
            }
        }

        return null;
    }

    public boolean placeTileAtMarker(String layerName, String markerName, String tileType) {
        MapMarker marker = getMarker(markerName);
        if (marker == null) {
            return false;
        }

        int tileId = findTileIdByType(tileType);
        if (tileId < 0) {
            return false;
        }

        return setTileAt(layerName, marker.col, marker.row, tileId);
    }

    public boolean setTileAt(String layerName, int col, int row, int tileId) {
        int layerIndex = findLayerIndex(layerName);
        if (layerIndex < 0 || !isInsideMap(col, row)) {
            return false;
        }

        if (tileId < 0 || !tileRegistry.containsKey(tileId)) {
            return false;
        }

        int[][] layer = layers.get(layerIndex);
        if (layer[col][row] == tileId) {
            return false;
        }

        replaceTile(layerIndex, col, row, tileId);
        return true;
    }

    public MapTransition getTransitionAt(int col, int row) {
        for (MapTransition transition : transitions) {
            if (transition != null && transition.contains(col, row)) {
                return transition;
            }
        }

        if (!isInsideMap(col, row)) {
            return null;
        }

        for (int[][] layer : layers) {
            int tileId = layer[col][row];
            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile tile = getTile(tileId);
            if (tile == null || tile.targetMapPath == null || tile.targetMapPath.isBlank()) {
                continue;
            }

            if (tile.targetCol < 0 || tile.targetRow < 0) {
                continue;
            }

            return new MapTransition(
                col,
                row,
                1,
                1,
                tile.targetMapPath,
                tile.targetCol,
                tile.targetRow,
                tile.targetDirection == null ? "down" : tile.targetDirection
            );
        }

        return null;
    }

    public void checkMapTransition() {
        int playerCol = (gh.player.worldX + gh.player.solidArea.x + (gh.player.solidArea.width / 2)) / gh.tileSize;
        int playerRow = (gh.player.worldY + gh.player.solidArea.y + (gh.player.solidArea.height / 2)) / gh.tileSize;
        MapTransition transition = getTransitionAt(playerCol, playerRow);
        if (transition != null) {
            swapMap(transition.targetMapPath, transition.targetCol, transition.targetRow, transition.targetDirection);
        }
    }

    public void swapMap(String mapPath, int playerCol, int playerRow, String direction) {
        gh.beforeMapChange();
        loadMap(mapPath);
        gh.player.worldX = playerCol * gh.tileSize;
        gh.player.worldY = playerRow * gh.tileSize;
        gh.player.direction = direction;
        gh.clearProjectiles();
        gh.refreshCurrentMapState();
    }

    public Tile getTopBreakableTileAt(int col, int row) {
        if (!isInsideMap(col, row)) {
            return null;
        }

        for (int layerIndex = layers.size() - 1; layerIndex >= 0; layerIndex--) {
            int[][] layer = layers.get(layerIndex);
            int tileId = layer[col][row];

            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile tile = getTile(tileId);
            if (tile != null && tile.maxHealth > 0) {
                return tile;
            }
        }

        return null;
    }

    public String damageBreakableTile(int col, int row, int damage) {
        if (!isInsideMap(col, row) || damage <= 0) {
            return null;
        }

        for (int layerIndex = layers.size() - 1; layerIndex >= 0; layerIndex--) {
            int[][] layer = layers.get(layerIndex);
            int tileId = layer[col][row];

            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile tile = getTile(tileId);
            if (tile == null || tile.maxHealth <= 0) {
                continue;
            }

            int currentHealth = getLayerHealth(layerIndex, col, row);
            if (currentHealth <= 0) {
                currentHealth = tile.maxHealth;
            }

            int nextHealth = currentHealth - damage;
            if (nextHealth > 0) {
                setLayerHealth(layerIndex, col, row, nextHealth);
                return null;
            }

            replaceTile(layerIndex, col, row, tile.replacementTileId);
            return tile.dropItemType;
        }

        return null;
    }

    public void replaceTile(int layerIndex, int col, int row, int newTileId) {
        if (!isInsideMap(col, row) || layerIndex < 0 || layerIndex >= layers.size()) {
            return;
        }

        int[][] layer = layers.get(layerIndex);
        layer[col][row] = newTileId;
        setLayerHealth(layerIndex, col, row, initialHealthForTile(newTileId));
        rememberCurrentMapState();
    }

    public List<SaveManager.ChestData> createChestSaveData() {
        List<SaveManager.ChestData> chestDataList = new ArrayList<>();

        for (ChestState chestState : chestRegistry.values()) {
            if (chestState == null) {
                continue;
            }

            SaveManager.ChestData chestData = new SaveManager.ChestData();
            chestData.mapPath = chestState.mapPath;
            chestData.col = chestState.col;
            chestData.row = chestState.row;
            chestData.opened = chestState.opened;

            for (Entity item : chestState.items) {
                String itemId = gh.getItemId(item);
                if (itemId == null) {
                    continue;
                }

                SaveManager.ItemData itemData = new SaveManager.ItemData();
                itemData.itemId = itemId;
                itemData.stackCount = Math.max(1, item.stackCount);
                chestData.items.add(itemData);
            }

            chestDataList.add(chestData);
        }

        return chestDataList;
    }

    public void restoreChestSaveData(List<SaveManager.ChestData> savedChests) {
        chestRegistry.clear();
        if (savedChests == null) {
            return;
        }

        for (SaveManager.ChestData savedChest : savedChests) {
            if (savedChest == null || savedChest.mapPath == null || savedChest.mapPath.isBlank()) {
                continue;
            }

            ChestState chestState = new ChestState(savedChest.mapPath, savedChest.col, savedChest.row);
            chestState.opened = savedChest.opened;

            if (savedChest.items != null) {
                for (SaveManager.ItemData itemData : savedChest.items) {
                    if (itemData == null) {
                        continue;
                    }

                    Entity item = gh.createItemEntity(itemData.itemId);
                    if (item == null) {
                        continue;
                    }

                    item.stackCount = Math.max(1, itemData.stackCount);
                    chestState.addItem(item);
                }
            }

            chestRegistry.put(chestState.getKey(), chestState);
        }
    }

    public List<SaveManager.MapStateData> createMapStateSaveData() {
        rememberCurrentMapState();

        List<SaveManager.MapStateData> mapStateDataList = new ArrayList<>();
        for (Map.Entry<String, StoredMapState> entry : storedMapStates.entrySet()) {
            SaveManager.MapStateData mapStateData = new SaveManager.MapStateData();
            mapStateData.mapPath = entry.getKey();
            mapStateData.layers = toLayerData(entry.getValue().layers);
            mapStateData.healthLayers = toLayerData(entry.getValue().healthLayers);
            mapStateDataList.add(mapStateData);
        }

        return mapStateDataList;
    }

    public void restoreMapStateSaveData(List<SaveManager.MapStateData> savedMapStates) {
        storedMapStates.clear();
        if (savedMapStates == null) {
            return;
        }

        for (SaveManager.MapStateData savedMapState : savedMapStates) {
            if (savedMapState == null || savedMapState.mapPath == null || savedMapState.mapPath.isBlank()) {
                continue;
            }

            storedMapStates.put(
                savedMapState.mapPath,
                new StoredMapState(fromLayerData(savedMapState.layers), fromLayerData(savedMapState.healthLayers))
            );
        }
    }

    public void resetStoredMapState(String mapPath) {
        if (mapPath == null || mapPath.isBlank()) {
            return;
        }

        storedMapStates.remove(mapPath);
    }

    private Tile getTile(int globalId) {
        if (zeroMeansEmpty && globalId == 0) {
            return null;
        }
        return tileRegistry.get(globalId);
    }

    private int findLayerIndex(String layerName) {
        if (layerName == null || layerName.isBlank()) {
            return -1;
        }

        for (int i = 0; i < layerNames.size(); i++) {
            String currentLayerName = layerNames.get(i);
            if (currentLayerName != null && currentLayerName.equalsIgnoreCase(layerName)) {
                return i;
            }
        }

        return -1;
    }

    private int findTileIdByType(String tileType) {
        if (tileType == null || tileType.isBlank()) {
            return -1;
        }

        int matchingTileId = -1;

        for (Map.Entry<Integer, Tile> entry : tileRegistry.entrySet()) {
            Tile tile = entry.getValue();
            if (tile == null || tile.type == null || !tile.type.equalsIgnoreCase(tileType)) {
                continue;
            }

            if (matchingTileId < 0 || entry.getKey() < matchingTileId) {
                matchingTileId = entry.getKey();
            }
        }

        return matchingTileId;
    }

    private void initializeLayerHealth() {
        layerHealth.clear();

        for (int[][] layer : layers) {
            int[][] healthGrid = new int[mapWidth][mapHeight];
            for (int col = 0; col < mapWidth; col++) {
                for (int row = 0; row < mapHeight; row++) {
                    healthGrid[col][row] = initialHealthForTile(layer[col][row]);
                }
            }
            layerHealth.add(healthGrid);
        }
    }

    private int initialHealthForTile(int tileId) {
        if (zeroMeansEmpty && tileId == 0) {
            return 0;
        }

        Tile tile = getTile(tileId);
        if (tile == null || tile.maxHealth <= 0) {
            return 0;
        }

        return tile.maxHealth;
    }

    private int getLayerHealth(int layerIndex, int col, int row) {
        if (!isInsideMap(col, row) || layerIndex < 0 || layerIndex >= layerHealth.size()) {
            return 0;
        }

        return layerHealth.get(layerIndex)[col][row];
    }

    private void setLayerHealth(int layerIndex, int col, int row, int health) {
        if (!isInsideMap(col, row) || layerIndex < 0 || layerIndex >= layerHealth.size()) {
            return;
        }

        layerHealth.get(layerIndex)[col][row] = Math.max(0, health);
        rememberCurrentMapState();
    }

    private boolean isInsideMap(int col, int row) {
        return col >= 0 && row >= 0 && col < mapWidth && row < mapHeight;
    }

    private void updateBelowPlayerLayerCount() {
        belowPlayerLayerCount = layers.size();
        for (int i = 0; i < layerNames.size(); i++) {
            if (isAbovePlayerLayer(layerNames.get(i))) {
                belowPlayerLayerCount = i;
                return;
            }
        }
    }

    private boolean isAbovePlayerLayer(String layerName) {
        String name = layerName == null ? "" : layerName.toLowerCase();
        return name.contains("above") || name.contains("over") || name.contains("roof") || name.contains("top");
    }

    private void registerChestDefinitions(List<ChestDefinition> definitions) {
        if (definitions == null) {
            return;
        }

        for (ChestDefinition definition : definitions) {
            if (definition == null) {
                continue;
            }

            if (!hasChestLikeTileAt(definition.col, definition.row)) {
                System.out.println("Chest marker at " + definition.getKey() + " is not placed on top of a chest tile.");
            }

            chestRegistry.computeIfAbsent(definition.getKey(), ignored -> createChestState(definition));
        }
    }

    private void registerChestTiles() {
        if (currentMapPath == null || currentMapPath.isBlank()) {
            return;
        }

        List<int[]> chestTiles = findChestTilesOnCurrentMap();
        if (isVillageFallbackMap() && specialVillageChestKey == null && !chestTiles.isEmpty()) {
            int[] specialChest = chestTiles.get(random.nextInt(chestTiles.size()));
            specialVillageChestKey = ChestState.buildKey(currentMapPath, specialChest[0], specialChest[1]);
        }

        for (int[] chestTile : chestTiles) {
            int chestCol = chestTile[0];
            int chestRow = chestTile[1];
            String chestKey = ChestState.buildKey(currentMapPath, chestCol, chestRow);
            chestRegistry.computeIfAbsent(chestKey, ignored -> createInitialChestState(chestCol, chestRow));
        }
    }

    private List<int[]> findChestTilesOnCurrentMap() {
        List<int[]> chestTiles = new ArrayList<>();

        for (int col = 0; col < mapWidth; col++) {
            for (int row = 0; row < mapHeight; row++) {
                if (hasChestLikeTileAt(col, row)) {
                    chestTiles.add(new int[] { col, row });
                }
            }
        }

        return chestTiles;
    }

    private boolean hasChestLikeTileAt(int col, int row) {
        if (!isInsideMap(col, row)) {
            return false;
        }

        Set<String> chestTileTypes = currentMapHasChestDefinitions
            ? MARKER_MAP_CHEST_TILE_TYPES
            : FALLBACK_CHEST_TILE_TYPES;

        for (int[][] layer : layers) {
            int tileId = layer[col][row];
            if (zeroMeansEmpty && tileId == 0) {
                continue;
            }

            Tile currentTile = getTile(tileId);
            if (currentTile == null) {
                continue;
            }

            if (currentTile.interactionType != null
                && CHEST_INTERACTION_TYPE.equalsIgnoreCase(currentTile.interactionType.trim())) {
                return true;
            }

            if (currentTile.type == null || currentTile.type.isBlank()) {
                continue;
            }

            if (chestTileTypes.contains(currentTile.type.trim())) {
                return true;
            }
        }

        return false;
    }

    private ChestState createChestState(ChestDefinition definition) {
        return createInitialChestState(definition.mapPath, definition.col, definition.row);
    }

    private ChestState createInitialChestState(int col, int row) {
        return createInitialChestState(currentMapPath, col, row);
    }

    private ChestState createInitialChestState(String mapPath, int col, int row) {
        ChestState chestState = new ChestState(mapPath, col, row);
        String chestKey = chestState.getKey();
        List<String> lootItems = chestKey.equals(specialVillageChestKey)
            ? List.of("pickaxe")
            : buildRandomChestLoot();

        addLootItems(chestState, lootItems, chestKey);
        return chestState;
    }

    private List<String> buildRandomChestLoot() {
        int itemCount = 1 + random.nextInt(MAX_RANDOM_CHEST_LOOT_ITEMS);
        List<String> lootItems = new ArrayList<>(itemCount);

        for (int i = 0; i < itemCount; i++) {
            lootItems.add(RANDOM_CHEST_LOOT_POOL.get(random.nextInt(RANDOM_CHEST_LOOT_POOL.size())));
        }

        return lootItems;
    }

    private void addLootItems(ChestState chestState, List<String> lootItemIds, String chestKey) {
        for (String itemId : lootItemIds) {
            if (chestState.isFull()) {
                System.out.println("Chest at " + chestKey + " exceeded capacity. Extra items were ignored.");
                break;
            }

            Entity item = gh.createItemEntity(itemId);
            if (item == null) {
                System.out.println("Unknown chest item type '" + itemId + "' at " + chestKey + ".");
                continue;
            }

            chestState.addItem(item);
        }
    }

    private boolean isVillageFallbackMap() {
        return VILLAGE_MAP_PATH.equals(currentMapPath) && !currentMapHasChestDefinitions;
    }

    private void rememberCurrentMapState() {
        if (currentMapPath == null || currentMapPath.isBlank() || layers.isEmpty()) {
            return;
        }

        storedMapStates.put(currentMapPath, new StoredMapState(copyLayerList(layers), copyLayerList(layerHealth)));
    }

    private void restoreStoredMapState(String mapPath) {
        StoredMapState storedMapState = storedMapStates.get(mapPath);
        if (storedMapState == null) {
            rememberCurrentMapState();
            return;
        }

        applyLayerCopies(layers, storedMapState.layers);
        applyLayerCopies(layerHealth, storedMapState.healthLayers);
    }

    private List<SaveManager.LayerData> toLayerData(List<int[][]> sourceLayers) {
        List<SaveManager.LayerData> layerDataList = new ArrayList<>();
        if (sourceLayers == null) {
            return layerDataList;
        }

        for (int[][] layer : sourceLayers) {
            if (layer == null || layer.length == 0 || layer[0].length == 0) {
                continue;
            }

            SaveManager.LayerData layerData = new SaveManager.LayerData();
            layerData.width = layer.length;
            layerData.height = layer[0].length;
            layerData.values = flattenLayer(layer);
            layerDataList.add(layerData);
        }

        return layerDataList;
    }

    private List<int[][]> fromLayerData(List<SaveManager.LayerData> layerDataList) {
        List<int[][]> restoredLayers = new ArrayList<>();
        if (layerDataList == null) {
            return restoredLayers;
        }

        for (SaveManager.LayerData layerData : layerDataList) {
            if (layerData == null || layerData.width <= 0 || layerData.height <= 0) {
                continue;
            }

            restoredLayers.add(expandLayer(layerData.width, layerData.height, layerData.values));
        }

        return restoredLayers;
    }

    private List<int[][]> copyLayerList(List<int[][]> sourceLayers) {
        List<int[][]> copies = new ArrayList<>();
        for (int[][] layer : sourceLayers) {
            if (layer == null) {
                continue;
            }

            int width = layer.length;
            int height = width == 0 ? 0 : layer[0].length;
            int[][] copy = new int[width][height];
            for (int col = 0; col < width; col++) {
                System.arraycopy(layer[col], 0, copy[col], 0, height);
            }
            copies.add(copy);
        }
        return copies;
    }

    private void applyLayerCopies(List<int[][]> targetLayers, List<int[][]> sourceLayers) {
        if (targetLayers.size() != sourceLayers.size()) {
            return;
        }

        for (int i = 0; i < targetLayers.size(); i++) {
            int[][] targetLayer = targetLayers.get(i);
            int[][] sourceLayer = sourceLayers.get(i);
            if (targetLayer.length != sourceLayer.length || (targetLayer.length > 0 && targetLayer[0].length != sourceLayer[0].length)) {
                return;
            }

            for (int col = 0; col < targetLayer.length; col++) {
                System.arraycopy(sourceLayer[col], 0, targetLayer[col], 0, targetLayer[col].length);
            }
        }
    }

    private int[] flattenLayer(int[][] layer) {
        int width = layer.length;
        int height = width == 0 ? 0 : layer[0].length;
        int[] values = new int[width * height];
        int index = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                values[index++] = layer[col][row];
            }
        }

        return values;
    }

    private int[][] expandLayer(int width, int height, int[] values) {
        int[][] layer = new int[width][height];
        int index = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                layer[col][row] = values != null && index < values.length ? values[index] : 0;
                index++;
            }
        }

        return layer;
    }

    private static final class StoredMapState {
        private final List<int[][]> layers;
        private final List<int[][]> healthLayers;

        private StoredMapState(List<int[][]> layers, List<int[][]> healthLayers) {
            this.layers = layers;
            this.healthLayers = healthLayers;
        }
    }
}
