package tile;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.GameHandler;

public class TileHandler {
    private static final String DEFAULT_MAP_PATH = "/res/map/mapv0.csv";
    private final GameHandler gh;
    private final Map<Integer, Tile> tileRegistry;
    private final List<int[][]> layers;
    private final List<String> layerNames;

    private int mapWidth;
    private int mapHeight;
    private int belowPlayerLayerCount;
    private boolean zeroMeansEmpty;

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerNames = new ArrayList<>();

        MapLoader mapLoader = new MapLoader(gh.tileSize);
        MapLoader.MapData mapData = mapLoader.loadMap(DEFAULT_MAP_PATH);
        tileRegistry.putAll(mapData.tileRegistry);
        layers.addAll(mapData.layers);
        layerNames.addAll(mapData.layerNames);
        mapWidth = mapData.mapWidth;
        mapHeight = mapData.mapHeight;
        zeroMeansEmpty = mapData.zeroMeansEmpty;
        updateBelowPlayerLayerCount();
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

    private Tile getTile(int globalId) {
        if (zeroMeansEmpty && globalId == 0) {
            return null;
        }
        return tileRegistry.get(globalId);
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
