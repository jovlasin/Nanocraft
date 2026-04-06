package com.nanocraft.game.tile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

public final class MapLoader {
    public static final class MapData {
        final Map<Integer, Tile> tileRegistry;
        final List<int[][]> layers;
        final List<String> layerNames;
        final int mapWidth;
        final int mapHeight;
        final boolean zeroMeansEmpty;

        MapData(
            Map<Integer, Tile> tileRegistry,
            List<int[][]> layers,
            List<String> layerNames,
            int mapWidth,
            int mapHeight,
            boolean zeroMeansEmpty
        ) {
            this.tileRegistry = tileRegistry;
            this.layers = layers;
            this.layerNames = layerNames;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.zeroMeansEmpty = zeroMeansEmpty;
        }
    }

    private final int tileSize;
    private final Gson gson;

    private final Map<Integer, Tile> tileRegistry;
    private final List<int[][]> layers;
    private final List<String> layerNames;

    private int mapWidth;
    private int mapHeight;
    private boolean zeroMeansEmpty;

    public MapLoader(int tileSize) {
        this.tileSize = tileSize;
        this.gson = new Gson();
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerNames = new ArrayList<>();
    }

    public MapData loadMap(String filePath) {
        clearMapData();
        if (filePath.endsWith(".tmj") || filePath.endsWith(".json")) {
            loadTmjMap(filePath);
        } else {
            loadCsvMap(filePath);
        }

        return new MapData(
            tileRegistry,
            layers,
            layerNames,
            mapWidth,
            mapHeight,
            zeroMeansEmpty
        );
    }

    private void loadTmjMap(String filePath) {
        zeroMeansEmpty = true;

        try (InputStream is = ResourceLoader.getRequiredResourceStream(filePath);
            InputStreamReader reader = new InputStreamReader(is)) {
            TiledMapData mapData = gson.fromJson(reader, TiledMapData.class);
            if (mapData == null) {
                throw new IOException("Map file is empty or invalid JSON: " + filePath);
            }

            mapWidth = mapData.width;
            mapHeight = mapData.height;
            if (mapWidth <= 0 || mapHeight <= 0) {
                throw new IOException("Invalid map dimensions in " + filePath + ": " + mapWidth + "x" + mapHeight);
            }

            registerTmjTiles(filePath, mapData.tilesets);
            loadTmjLayers(mapData.layers);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load TMJ map: " + filePath, e);
        }
    }

    private void registerTmjTiles(String mapFilePath, List<TiledTilesetData> tilesets) throws IOException {
        if (tilesets == null) {
            return;
        }

        List<TiledTilesetData> sortedTilesets = new ArrayList<>(tilesets);
        sortedTilesets.sort(Comparator.comparingInt(t -> t.firstgid));

        for (int i = 0; i < sortedTilesets.size(); i++) {
            TiledTilesetData tileset = sortedTilesets.get(i);
            if (tileset == null) {
                continue;
            }

            if (tileset.source != null) {
                int nextFirstgid = i + 1 < sortedTilesets.size() ? sortedTilesets.get(i + 1).firstgid : tileset.firstgid + 1;
                registerSourceTiles(mapFilePath, tileset, nextFirstgid);
                continue;
            }

            if (tileset.image != null) {
                registerEmbeddedImageTileset(mapFilePath, tileset);
                continue;
            }

            if (tileset.tiles == null) {
                continue;
            }

            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null || tileData.image == null) {
                    continue;
                }

                Tile tile = new Tile();
                String imagePath = ResourceLoader.resolveResourcePath(mapFilePath, tileData.image);
                tile.image = ResourceLoader.loadScaledImage(imagePath, tileSize);
                tile.collision = hasCollisionProperty(tileData.properties);
                tile.maxHealth = getIntProperty(tileData.properties, "oreHealth", 0);
                tile.replacementTileId = getIntProperty(tileData.properties, "replacementTileId", 0);
                tile.dropItemType = getStringProperty(tileData.properties, "dropItemType", null);

                int globalId = tileset.firstgid + tileData.id;
                tileRegistry.put(globalId, tile);
            }
        }
    }

    private void registerEmbeddedImageTileset(String mapFilePath, TiledTilesetData tileset) throws IOException {
        String imagePath = ResourceLoader.resolveResourcePath(mapFilePath, tileset.image);
        if (!ResourceLoader.resourceExists(imagePath)) {
            String tileFolderPath = "/tile/" + ResourceLoader.fileName(tileset.image);
            if (ResourceLoader.resourceExists(tileFolderPath)) {
                imagePath = tileFolderPath;
            }
        }

        if (!ResourceLoader.resourceExists(imagePath)) {
            return;
        }

        BufferedImage tilesetImage = ImageIO.read(ResourceLoader.getRequiredResourceStream(imagePath));
        if (tilesetImage == null) {
            return;
        }

        Map<Integer, Boolean> collisionByTileId = new HashMap<>();
        Map<Integer, Integer> healthByTileId = new HashMap<>();
        Map<Integer, Integer> replacementByTileId = new HashMap<>();
        Map<Integer, String> dropByTileId = new HashMap<>();
        if (tileset.tiles != null) {
            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null) {
                    continue;
                }
                collisionByTileId.put(tileData.id, hasCollisionProperty(tileData.properties));
                healthByTileId.put(tileData.id, getIntProperty(tileData.properties, "oreHealth", 0));
                replacementByTileId.put(tileData.id, getIntProperty(tileData.properties, "replacementTileId", 0));
                dropByTileId.put(tileData.id, getStringProperty(tileData.properties, "dropItemType", null));
            }
        }

        int tileCount = tileset.tilecount > 0 ? tileset.tilecount : 1;
        int tileWidth = tileset.tilewidth > 0 ? tileset.tilewidth
            : (tileset.imagewidth > 0 ? tileset.imagewidth : tilesetImage.getWidth());
        int tileHeight = tileset.tileheight > 0 ? tileset.tileheight
            : (tileset.imageheight > 0 ? tileset.imageheight : tilesetImage.getHeight());
        int columns = tileset.columns > 0 ? tileset.columns : 1;

        if (tileCount == 1) {
            Tile tile = new Tile();
            tile.image = ResourceLoader.scaleImage(tilesetImage, tileSize, tileSize);
            tile.collision = collisionByTileId.getOrDefault(0, false);
            tile.maxHealth = healthByTileId.getOrDefault(0, 0);
            tile.replacementTileId = replacementByTileId.getOrDefault(0, 0);
            tile.dropItemType = dropByTileId.get(0);
            tileRegistry.put(tileset.firstgid, tile);
            return;
        }

        for (int localId = 0; localId < tileCount; localId++) {
            int col = localId % columns;
            int row = localId / columns;
            int sourceX = col * tileWidth;
            int sourceY = row * tileHeight;

            if (sourceX + tileWidth > tilesetImage.getWidth() || sourceY + tileHeight > tilesetImage.getHeight()) {
                continue;
            }

            BufferedImage tileImage = tilesetImage.getSubimage(sourceX, sourceY, tileWidth, tileHeight);
            Tile tile = new Tile();
            tile.image = ResourceLoader.scaleImage(tileImage, tileSize, tileSize);
            tile.collision = collisionByTileId.getOrDefault(localId, false);
            tile.maxHealth = healthByTileId.getOrDefault(localId, 0);
            tile.replacementTileId = replacementByTileId.getOrDefault(localId, 0);
            tile.dropItemType = dropByTileId.get(localId);
            tileRegistry.put(tileset.firstgid + localId, tile);
        }
    }

    private void registerSourceTiles(String mapFilePath, TiledTilesetData tileset, int nextFirstgid) {
        String resolvedSourcePath = ResourceLoader.resolveResourcePath(mapFilePath, tileset.source);
        String imagePath = resolvedSourcePath;

        if (imagePath.toLowerCase().endsWith(".tsx")) {
            imagePath = imagePath.substring(0, imagePath.length() - 4) + ".png";
        }

        if (!ResourceLoader.resourceExists(imagePath)) {
            String fileName = ResourceLoader.fileName(imagePath);
            String tileFolderPath = "/tile/" + fileName;
            if (ResourceLoader.resourceExists(tileFolderPath)) {
                imagePath = tileFolderPath;
            }
        }

        if (!ResourceLoader.resourceExists(imagePath)) {
            return;
        }

        BufferedImage sharedImage = ResourceLoader.loadScaledImage(imagePath, tileSize);
        Map<Integer, Boolean> collisionByTileId = new HashMap<>();
        Map<Integer, Integer> healthByTileId = new HashMap<>();
        Map<Integer, Integer> replacementByTileId = new HashMap<>();
        Map<Integer, String> dropByTileId = new HashMap<>();
        if (tileset.tiles != null) {
            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null) {
                    continue;
                }
                collisionByTileId.put(tileData.id, hasCollisionProperty(tileData.properties));
                healthByTileId.put(tileData.id, getIntProperty(tileData.properties, "oreHealth", 0));
                replacementByTileId.put(tileData.id, getIntProperty(tileData.properties, "replacementTileId", 0));
                dropByTileId.put(tileData.id, getStringProperty(tileData.properties, "dropItemType", null));
            }
        }

        int rangeEndExclusive = Math.max(tileset.firstgid + 1, nextFirstgid);
        for (int gid = tileset.firstgid; gid < rangeEndExclusive; gid++) {
            int localId = gid - tileset.firstgid;
            Tile tile = new Tile();
            tile.image = sharedImage;
            tile.collision = collisionByTileId.getOrDefault(localId, false);
            tile.maxHealth = healthByTileId.getOrDefault(localId, 0);
            tile.replacementTileId = replacementByTileId.getOrDefault(localId, 0);
            tile.dropItemType = dropByTileId.get(localId);
            tileRegistry.put(gid, tile);
        }
    }

    private void loadTmjLayers(List<TiledLayerData> mapLayers) {
        if (mapLayers == null) {
            return;
        }

        for (TiledLayerData layerData : mapLayers) {
            if (layerData == null || !"tilelayer".equals(layerData.type) || layerData.data == null) {
                continue;
            }

            int[][] grid = new int[mapWidth][mapHeight];
            int maxEntries = Math.min(layerData.data.length, mapWidth * mapHeight);
            for (int index = 0; index < maxEntries; index++) {
                int col = index % mapWidth;
                int row = index / mapWidth;
                grid[col][row] = stripFlipFlags(layerData.data[index]);
            }

            layers.add(grid);
            layerNames.add(layerData.name == null ? "Layer" + layers.size() : layerData.name);
        }
    }

    private void loadCsvMap(String filePath) {
        zeroMeansEmpty = false;
        registerLegacyTiles();

        try (InputStream is = ResourceLoader.getRequiredResourceStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }

            mapHeight = lines.size();
            if (mapHeight == 0) {
                throw new IOException("CSV map has no rows: " + filePath);
            }

            mapWidth = lines.get(0).split(",").length;
            if (mapWidth == 0) {
                throw new IOException("CSV map has no columns: " + filePath);
            }

            int[][] grid = new int[mapWidth][mapHeight];
            for (int row = 0; row < mapHeight; row++) {
                String[] cols = lines.get(row).split(",");
                if (cols.length != mapWidth) {
                    throw new IOException("Inconsistent CSV row width at row " + row + " in " + filePath);
                }

                for (int col = 0; col < mapWidth; col++) {
                    grid[col][row] = Integer.parseInt(cols[col].trim());
                }
            }

            layers.add(grid);
            layerNames.add("Ground");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load CSV map: " + filePath, e);
        }
    }

    private void clearMapData() {
        tileRegistry.clear();
        layers.clear();
        layerNames.clear();
        mapWidth = 0;
        mapHeight = 0;
    }

    private void registerLegacyTiles() {
        setupLegacyTile(0, "000", true);
        setupLegacyTile(1, "001", false);
        setupLegacyTile(2, "002", false);
        setupLegacyTile(3, "003", false);
        setupLegacyTile(4, "004", false);
        setupLegacyTile(5, "005", true);
        setupLegacyTile(6, "006", true);
    }

    private void setupLegacyTile(int id, String name, boolean collision) {
        Tile tile = new Tile();
        tile.image = ResourceLoader.loadScaledImage("/res/tile/" + name + ".png", tileSize);
        tile.collision = collision;
        tileRegistry.put(id, tile);
    }

    private boolean hasCollisionProperty(List<TiledPropertyData> properties) {
        if (properties == null) {
            return false;
        }

        for (TiledPropertyData property : properties) {
            if (property == null || property.name == null) {
                continue;
            }

            if ("collision".equalsIgnoreCase(property.name)) {
                return parseBoolean(property.value);
            }
        }
        return false;
    }

    private int getIntProperty(List<TiledPropertyData> properties, String propertyName, int defaultValue) {
        if (properties == null || propertyName == null) {
            return defaultValue;
        }

        for (TiledPropertyData property : properties) {
            if (property == null || property.name == null) {
                continue;
            }

            if (!propertyName.equalsIgnoreCase(property.name)) {
                continue;
            }

            return parseInt(property.value, defaultValue);
        }

        return defaultValue;
    }

    private String getStringProperty(List<TiledPropertyData> properties, String propertyName, String defaultValue) {
        if (properties == null || propertyName == null) {
            return defaultValue;
        }

        for (TiledPropertyData property : properties) {
            if (property == null || property.name == null) {
                continue;
            }

            if (!propertyName.equalsIgnoreCase(property.name)) {
                continue;
            }

            if (property.value == null) {
                return defaultValue;
            }

            String value = String.valueOf(property.value).trim();
            return value.isEmpty() ? defaultValue : value;
        }

        return defaultValue;
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private int parseInt(Object value, int defaultValue) {
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        if (value instanceof String stringValue) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    private int stripFlipFlags(int gid) {
        // Tiled encodes flip/rotation flags in the highest 3 bits.
        return gid & 0x1FFFFFFF;
    }
}
