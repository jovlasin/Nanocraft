package com.nanocraft.game.tile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class MapLoader {
    public static final class MapData {
        final Map<Integer, Tile> tileRegistry;
        final List<int[][]> layers;
        final List<String> layerNames;
        final List<MapTransition> transitions;
        final List<ChestDefinition> chestDefinitions;
        final int mapWidth;
        final int mapHeight;
        final boolean zeroMeansEmpty;

        MapData(
            Map<Integer, Tile> tileRegistry,
            List<int[][]> layers,
            List<String> layerNames,
            List<MapTransition> transitions,
            List<ChestDefinition> chestDefinitions,
            int mapWidth,
            int mapHeight,
            boolean zeroMeansEmpty
        ) {
            this.tileRegistry = tileRegistry;
            this.layers = layers;
            this.layerNames = layerNames;
            this.transitions = transitions;
            this.chestDefinitions = chestDefinitions;
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
    private final List<MapTransition> transitions;
    private final List<ChestDefinition> chestDefinitions;

    private int mapWidth;
    private int mapHeight;
    private boolean zeroMeansEmpty;

    public MapLoader(int tileSize) {
        this.tileSize = tileSize;
        this.gson = new Gson();
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerNames = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.chestDefinitions = new ArrayList<>();
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
            transitions,
            chestDefinitions,
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
            loadTmjLayers(filePath, mapData);
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
                tile.type = resolveTileType(tileset.name, tileData.image);
                tile.interactionType = getInteractionType(tileData.properties);
                applyTransitionProperties(tile, mapFilePath, tileData.properties);

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
        Map<Integer, String> interactionByTileId = new HashMap<>();
        if (tileset.tiles != null) {
            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null) {
                    continue;
                }
                collisionByTileId.put(tileData.id, hasCollisionProperty(tileData.properties));
                healthByTileId.put(tileData.id, getIntProperty(tileData.properties, "oreHealth", 0));
                replacementByTileId.put(tileData.id, getIntProperty(tileData.properties, "replacementTileId", 0));
                dropByTileId.put(tileData.id, getStringProperty(tileData.properties, "dropItemType", null));
                interactionByTileId.put(tileData.id, getInteractionType(tileData.properties));
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
            tile.type = resolveTileType(tileset.name, tileset.image);
            tile.interactionType = interactionByTileId.get(0);
            applyTransitionProperties(tile, mapFilePath, tileset.tiles == null ? null : tileset.tiles.stream()
                .filter(tileData -> tileData != null && tileData.id == 0)
                .findFirst()
                .map(tileData -> tileData.properties)
                .orElse(null));
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
            tile.type = resolveTileType(tileset.name, tileset.image);
            tile.interactionType = interactionByTileId.get(localId);
            applyTransitionProperties(tile, mapFilePath, findTileProperties(tileset.tiles, localId));
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
        Map<Integer, String> interactionByTileId = new HashMap<>();
        if (tileset.tiles != null) {
            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null) {
                    continue;
                }
                collisionByTileId.put(tileData.id, hasCollisionProperty(tileData.properties));
                healthByTileId.put(tileData.id, getIntProperty(tileData.properties, "oreHealth", 0));
                replacementByTileId.put(tileData.id, getIntProperty(tileData.properties, "replacementTileId", 0));
                dropByTileId.put(tileData.id, getStringProperty(tileData.properties, "dropItemType", null));
                interactionByTileId.put(tileData.id, getInteractionType(tileData.properties));
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
            tile.type = resolveTileType(tileset.name, imagePath);
            tile.interactionType = interactionByTileId.get(localId);
            applyTransitionProperties(tile, mapFilePath, findTileProperties(tileset.tiles, localId));
            tileRegistry.put(gid, tile);
        }
    }

    private void loadTmjLayers(String mapFilePath, TiledMapData mapData) {
        if (mapData == null || mapData.layers == null) {
            return;
        }

        for (TiledLayerData layerData : mapData.layers) {
            if (layerData == null) {
                continue;
            }

            if ("tilelayer".equals(layerData.type) && layerData.data != null) {
                int[][] grid = new int[mapWidth][mapHeight];
                int[] decodedLayerData = decodeLayerData(layerData);
                int maxEntries = Math.min(decodedLayerData.length, mapWidth * mapHeight);
                for (int index = 0; index < maxEntries; index++) {
                    int col = index % mapWidth;
                    int row = index / mapWidth;
                    grid[col][row] = stripFlipFlags(decodedLayerData[index]);
                }

                layers.add(grid);
                layerNames.add(layerData.name == null ? "Layer" + layers.size() : layerData.name);
                continue;
            }

            if ("objectgroup".equals(layerData.type) && layerData.objects != null) {
                loadTransitions(mapFilePath, mapData, layerData.objects);
                loadChests(mapFilePath, mapData, layerData.objects);
            }
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
        transitions.clear();
        chestDefinitions.clear();
        mapWidth = 0;
        mapHeight = 0;
        zeroMeansEmpty = false;
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
        tile.type = name;
        tileRegistry.put(id, tile);
    }

    private String resolveTileType(String tilesetName, String imagePath) {
        if (tilesetName != null && !tilesetName.isBlank()) {
            return tilesetName;
        }

        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        String fileName = ResourceLoader.fileName(imagePath);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }

        return fileName;
    }

    private void loadTransitions(String mapFilePath, TiledMapData mapData, List<TiledObjectData> objects) {
        int sourceTileWidth = mapData.tilewidth > 0 ? mapData.tilewidth : 1;
        int sourceTileHeight = mapData.tileheight > 0 ? mapData.tileheight : 1;

        for (TiledObjectData objectData : objects) {
            if (objectData == null) {
                continue;
            }

            String targetMap = getStringProperty(objectData.properties, "targetMap", null);
            int targetCol = getIntProperty(objectData.properties, "targetCol", -1);
            int targetRow = getIntProperty(objectData.properties, "targetRow", -1);
            if (targetMap == null || targetMap.isBlank() || targetCol < 0 || targetRow < 0) {
                continue;
            }

            int sourceCol = (int) Math.floor(objectData.x / sourceTileWidth);
            int sourceRow = (int) Math.floor(objectData.y / sourceTileHeight);
            int widthInTiles = Math.max(1, (int) Math.ceil(objectData.width / sourceTileWidth));
            int heightInTiles = Math.max(1, (int) Math.ceil(objectData.height / sourceTileHeight));
            String targetDirection = getStringProperty(objectData.properties, "targetDirection", "down");

            transitions.add(new MapTransition(
                sourceCol,
                sourceRow,
                widthInTiles,
                heightInTiles,
                ResourceLoader.resolveResourcePath(mapFilePath, targetMap),
                targetCol,
                targetRow,
                targetDirection
            ));
        }
        
    }

    private void loadChests(String mapFilePath, TiledMapData mapData, List<TiledObjectData> objects) {
        int sourceTileWidth = mapData.tilewidth > 0 ? mapData.tilewidth : 1;
        int sourceTileHeight = mapData.tileheight > 0 ? mapData.tileheight : 1;

        for (TiledObjectData objectData : objects) {
            if (objectData == null || objectData.type == null) {
                continue;
            }

            if (!"chest".equalsIgnoreCase(objectData.type.trim())) {
                continue;
            }

            int sourceCol = getObjectTileCol(objectData, sourceTileWidth);
            int sourceRow = getObjectTileRow(objectData, sourceTileHeight);
            List<String> lootItemIds = parseLootItemIds(getStringProperty(objectData.properties, "loot", ""));

            chestDefinitions.add(new ChestDefinition(mapFilePath, sourceCol, sourceRow, lootItemIds));
        }
    }

    private int getObjectTileCol(TiledObjectData objectData, int tileWidth) {
        if (objectData == null) {
            return -1;
        }

        return (int) Math.floor(objectData.x / tileWidth);
    }

    private int getObjectTileRow(TiledObjectData objectData, int tileHeight) {
        if (objectData == null) {
            return -1;
        }

        double sourceY = objectData.gid == null ? objectData.y : objectData.y - tileHeight;
        return (int) Math.floor(sourceY / tileHeight);
    }

    private List<TiledPropertyData> findTileProperties(List<TiledTileData> tiles, int localId) {
        if (tiles == null) {
            return null;
        }

        for (TiledTileData tileData : tiles) {
            if (tileData != null && tileData.id == localId) {
                return tileData.properties;
            }
        }

        return null;
    }

    private void applyTransitionProperties(Tile tile, String mapFilePath, List<TiledPropertyData> properties) {
        if (tile == null || properties == null) {
            return;
        }

        String targetMap = getStringProperty(properties, "targetMap", null);
        int targetCol = getIntProperty(properties, "targetCol", -1);
        int targetRow = getIntProperty(properties, "targetRow", -1);
        if (targetMap == null || targetMap.isBlank() || targetCol < 0 || targetRow < 0) {
            return;
        }

        tile.targetMapPath = ResourceLoader.resolveResourcePath(mapFilePath, targetMap);
        tile.targetCol = targetCol;
        tile.targetRow = targetRow;
        tile.targetDirection = getStringProperty(properties, "targetDirection", "down");
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

    private String getInteractionType(List<TiledPropertyData> properties) {
        String interactionType = getStringProperty(properties, "interactionType", null);
        if (interactionType != null) {
            return interactionType.trim().toLowerCase();
        }

        interactionType = getStringProperty(properties, "interaction", null);
        if (interactionType != null) {
            return interactionType.trim().toLowerCase();
        }

        if (getBooleanProperty(properties, "sleep", false)) {
            return "sleep";
        }

        return null;
    }

    private boolean getBooleanProperty(List<TiledPropertyData> properties, String propertyName, boolean defaultValue) {
        if (properties == null || propertyName == null) {
            return defaultValue;
        }

        for (TiledPropertyData property : properties) {
            if (property == null || property.name == null) {
                continue;
            }

            if (propertyName.equalsIgnoreCase(property.name)) {
                return parseBoolean(property.value);
            }
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

    private List<String> parseLootItemIds(String lootValue) {
        List<String> lootItemIds = new ArrayList<>();
        if (lootValue == null || lootValue.isBlank()) {
            return lootItemIds;
        }

        String[] rawItems = lootValue.split(",");
        for (String rawItem : rawItems) {
            if (rawItem == null) {
                continue;
            }

            String itemId = rawItem.trim().toLowerCase();
            if (!itemId.isEmpty()) {
                lootItemIds.add(itemId);
            }
        }

        return lootItemIds;
    }

    private int stripFlipFlags(int gid) {
        // Tiled encodes flip/rotation flags in the highest 3 bits.
        return gid & 0x1FFFFFFF;
    }

    private int[] decodeLayerData(TiledLayerData layerData) {
        JsonElement dataElement = layerData.data;

        if (dataElement.isJsonArray()) {
            return decodeJsonArrayLayerData(dataElement.getAsJsonArray());
        }

        if (!dataElement.isJsonPrimitive()) {
            throw new IllegalStateException("Unsupported Tiled layer data format for layer: " + layerData.name);
        }

        JsonPrimitive primitive = dataElement.getAsJsonPrimitive();
        if (!primitive.isString()) {
            throw new IllegalStateException("Unsupported Tiled layer primitive data for layer: " + layerData.name);
        }

        String rawData = primitive.getAsString();
        String encoding = layerData.encoding == null ? "" : layerData.encoding.trim().toLowerCase();
        String compression = layerData.compression == null ? "" : layerData.compression.trim().toLowerCase();

        if ("csv".equals(encoding)) {
            return decodeCsvLayerData(rawData);
        }

        if ("base64".equals(encoding) || encoding.isEmpty()) {
            return decodeBase64LayerData(rawData, compression);
        }

        throw new IllegalStateException(
            "Unsupported Tiled layer encoding '" + encoding + "' for layer: " + layerData.name
        );
    }

    private int[] decodeJsonArrayLayerData(JsonArray arrayData) {
        int[] decoded = new int[arrayData.size()];
        for (int i = 0; i < arrayData.size(); i++) {
            JsonElement value = arrayData.get(i);
            decoded[i] = value == null || value.isJsonNull() ? 0 : value.getAsInt();
        }
        return decoded;
    }

    private int[] decodeCsvLayerData(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return new int[0];
        }

        String[] values = rawData.split(",");
        int[] decoded = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            String current = values[i].trim();
            decoded[i] = current.isEmpty() ? 0 : Integer.parseInt(current);
        }
        return decoded;
    }

    private int[] decodeBase64LayerData(String rawData, String compression) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(rawData.replaceAll("\\s+", ""));
            byte[] uncompressedBytes = decompressLayerBytes(decodedBytes, compression);
            return bytesToIntArray(uncompressedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid base64-encoded Tiled layer data", e);
        }
    }

    private byte[] decompressLayerBytes(byte[] bytes, String compression) {
        if (compression == null || compression.isBlank()) {
            return bytes;
        }

        try {
            if ("gzip".equals(compression)) {
                return readAllBytes(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            }

            if ("zlib".equals(compression)) {
                return readAllBytes(new InflaterInputStream(new ByteArrayInputStream(bytes)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decompress Tiled layer data", e);
        }

        throw new IllegalStateException("Unsupported Tiled layer compression: " + compression);
    }

    private byte[] readAllBytes(InputStream stream) throws IOException {
        try (InputStream input = stream; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private int[] bytesToIntArray(byte[] bytes) {
        int valueCount = bytes.length / 4;
        int[] decoded = new int[valueCount];

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < valueCount; i++) {
            decoded[i] = buffer.getInt();
        }

        return decoded;
    }
}
