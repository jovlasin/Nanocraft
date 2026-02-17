package tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;

import core.GameHandler;

public class TileHandler {
    private static final String DEFAULT_MAP_PATH = "/map/Mapv0.csv";

    private final GameHandler gh;
    private final Map<Integer, Tile> tileRegistry;
    private final List<int[][]> layers;
    private final List<String> layerNames;
    private final Gson gson;

    private int mapWidth;
    private int mapHeight;
    private int belowPlayerLayerCount;
    private boolean zeroMeansEmpty;

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        this.tileRegistry = new HashMap<>();
        this.layers = new ArrayList<>();
        this.layerNames = new ArrayList<>();
        this.gson = new Gson();
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

    private Tile getTile(int globalId) {
        if (zeroMeansEmpty && globalId == 0) {
            return null;
        }
        return tileRegistry.get(globalId);
    }

    private void loadMap(String filePath) {
        if (filePath.endsWith(".tmj") || filePath.endsWith(".json")) {
            loadTmjMap(filePath);
            return;
        }
        loadCsvMap(filePath);
    }

    private void loadTmjMap(String filePath) {
        clearMapData();
        zeroMeansEmpty = true;

        try (InputStream is = getRequiredResourceStream(filePath);
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
            updateBelowPlayerLayerCount();
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
                String imagePath = resolveResourcePath(mapFilePath, tileData.image);
                tile.image = loadScaledImage(imagePath);
                tile.collision = hasCollisionProperty(tileData.properties);

                int globalId = tileset.firstgid + tileData.id;
                tileRegistry.put(globalId, tile);
            }
        }
    }

    private void registerEmbeddedImageTileset(String mapFilePath, TiledTilesetData tileset) throws IOException {
        String imagePath = resolveResourcePath(mapFilePath, tileset.image);
        if (!resourceExists(imagePath)) {
            String tileFolderPath = "/tile/" + fileName(tileset.image);
            if (resourceExists(tileFolderPath)) {
                imagePath = tileFolderPath;
            }
        }

        if (!resourceExists(imagePath)) {
            return;
        }

        BufferedImage tilesetImage = ImageIO.read(getRequiredResourceStream(imagePath));
        if (tilesetImage == null) {
            return;
        }

        Map<Integer, Boolean> collisionByTileId = new HashMap<>();
        if (tileset.tiles != null) {
            for (TiledTileData tileData : tileset.tiles) {
                if (tileData == null) {
                    continue;
                }
                collisionByTileId.put(tileData.id, hasCollisionProperty(tileData.properties));
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
            tile.image = scaleImage(tilesetImage, gh.tileSize, gh.tileSize);
            tile.collision = collisionByTileId.getOrDefault(0, false);
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
            tile.image = scaleImage(tileImage, gh.tileSize, gh.tileSize);
            tile.collision = collisionByTileId.getOrDefault(localId, false);
            tileRegistry.put(tileset.firstgid + localId, tile);
        }
    }

    private void registerSourceTiles(String mapFilePath, TiledTilesetData tileset, int nextFirstgid) {
        String resolvedSourcePath = resolveResourcePath(mapFilePath, tileset.source);
        String imagePath = resolvedSourcePath;

        if (imagePath.toLowerCase().endsWith(".tsx")) {
            imagePath = imagePath.substring(0, imagePath.length() - 4) + ".png";
        }

        if (!resourceExists(imagePath)) {
            String fileName = fileName(imagePath);
            String tileFolderPath = "/tile/" + fileName;
            if (resourceExists(tileFolderPath)) {
                imagePath = tileFolderPath;
            }
        }

        if (!resourceExists(imagePath)) {
            return;
        }

        Tile tile = new Tile();
        tile.image = loadScaledImage(imagePath);
        tile.collision = false;

        int rangeEndExclusive = Math.max(tileset.firstgid + 1, nextFirstgid);
        for (int gid = tileset.firstgid; gid < rangeEndExclusive; gid++) {
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
        clearMapData();
        zeroMeansEmpty = false;
        registerLegacyTiles();

        try (InputStream is = getRequiredResourceStream(filePath);
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
            updateBelowPlayerLayerCount();
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
        belowPlayerLayerCount = 0;
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
        tile.image = loadScaledImage("/tile/" + name + ".png");
        tile.collision = collision;
        tileRegistry.put(id, tile);
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

    private int stripFlipFlags(int gid) {
        // Tiled encodes flip/rotation flags in the highest 3 bits.
        return gid & 0x1FFFFFFF;
    }

    private String resolveResourcePath(String mapFilePath, String relativePath) {
        String normalizedRelative = relativePath.replace("\\", "/");
        if (normalizedRelative.startsWith("/")) {
            return normalizedRelative;
        }

        int lastSlash = mapFilePath.lastIndexOf('/');
        String mapDir = lastSlash >= 0 ? mapFilePath.substring(0, lastSlash + 1) : "/";
        String combined = mapDir + normalizedRelative;
        String normalized = Paths.get(combined).normalize().toString().replace('\\', '/');
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private BufferedImage loadScaledImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(getRequiredResourceStream(resourcePath));
            return scaleImage(image, gh.tileSize, gh.tileSize);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load image resource: " + resourcePath, e);
        }
    }

    private InputStream getRequiredResourceStream(String resourcePath) throws IOException {
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return stream;
    }

    private boolean resourceExists(String resourcePath) {
        try (InputStream ignored = getClass().getResourceAsStream(resourcePath)) {
            return ignored != null;
        } catch (IOException e) {
            return false;
        }
    }

    private String fileName(String resourcePath) {
        int lastSlash = resourcePath.lastIndexOf('/');
        return lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return scaledImage;
    }

    private static class TiledMapData {
        int width;
        int height;
        List<TiledLayerData> layers;
        List<TiledTilesetData> tilesets;
    }

    private static class TiledLayerData {
        String name;
        String type;
        int[] data;
    }

    private static class TiledTilesetData {
        int firstgid;
        String source;
        String image;
        int imagewidth;
        int imageheight;
        int tilewidth;
        int tileheight;
        int tilecount;
        int columns;
        List<TiledTileData> tiles;
    }

    private static class TiledTileData {
        int id;
        String image;
        List<TiledPropertyData> properties;
    }

    private static class TiledPropertyData {
        String name;
        Object value;
    }
}
