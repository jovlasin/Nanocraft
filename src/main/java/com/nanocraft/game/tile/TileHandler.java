package com.nanocraft.game.tile;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nanocraft.game.core.GameHandler;

public class TileHandler {
    private static final String DEFAULT_MAP_PATH = "/map/village.tmj";
    private final GameHandler gh;
    private final MapLoader mapLoader;
    private final Map<Integer, Tile> tileRegistry;
    private final List<int[][]> layers;
    private final List<int[][]> layerHealth;
    private final List<String> layerNames;

    private int mapWidth;
    private int mapHeight;
    private int belowPlayerLayerCount;
    private boolean zeroMeansEmpty;
    private String currentMapPath;
    private final List<MapTransition> transitions;

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        this.mapLoader = new MapLoader(gh.tileSize);
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerHealth = new ArrayList<>();
        this.layerNames = new ArrayList<>();
        this.transitions = new ArrayList<>();
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
        MapLoader.MapData mapData = mapLoader.loadMap(mapPath);
        tileRegistry.clear();
        layers.clear();
        layerNames.clear();

        tileRegistry.putAll(mapData.tileRegistry);
        layers.addAll(mapData.layers);
        layerNames.addAll(mapData.layerNames);
        transitions.clear();
        transitions.addAll(mapData.transitions);
        mapWidth = mapData.mapWidth;
        mapHeight = mapData.mapHeight;
        zeroMeansEmpty = mapData.zeroMeansEmpty;
        currentMapPath = mapPath;
        initializeLayerHealth();
        updateBelowPlayerLayerCount();
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
        loadMap(mapPath);
        gh.player.worldX = playerCol * gh.tileSize;
        gh.player.worldY = playerRow * gh.tileSize;
        gh.player.direction = direction;
        gh.ah.setObjects();
        gh.ah.setNPCS();
    }

    public void damageBreakableTile(int col, int row, int damage) {
        if (!isInsideMap(col, row) || damage <= 0) {
            return;
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
                return;
            }

            replaceTile(layerIndex, col, row, tile.replacementTileId);
            if (tile.dropItemType != null && !tile.dropItemType.isBlank()) {
                int dropWorldX = col * gh.tileSize;
                int dropWorldY = row * gh.tileSize;
                gh.spawnDroppedItem(dropWorldX, dropWorldY, tile.dropItemType);
            }
            return;
        }
    }

    public void replaceTile(int layerIndex, int col, int row, int newTileId) {
        if (!isInsideMap(col, row) || layerIndex < 0 || layerIndex >= layers.size()) {
            return;
        }

        int[][] layer = layers.get(layerIndex);
        layer[col][row] = newTileId;
        setLayerHealth(layerIndex, col, row, initialHealthForTile(newTileId));
    }

    private Tile getTile(int globalId) {
        if (zeroMeansEmpty && globalId == 0) {
            return null;
        }
        return tileRegistry.get(globalId);
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
}
