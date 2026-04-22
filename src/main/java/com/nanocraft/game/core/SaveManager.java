package com.nanocraft.game.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SaveManager {
    private static final Path SAVE_FILE_PATH = Path.of(System.getProperty("user.home"), ".nanocraft.json");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public boolean hasSaveFile() {
        return Files.isRegularFile(SAVE_FILE_PATH);
    }

    public void save(GameHandler gh) throws IOException {
        SaveData saveData = new SaveData();
        saveData.currentMapPath = gh.th.getCurrentMapPath();
        saveData.player = gh.player.createSaveData();
        saveData.chests = gh.th.createChestSaveData();
        saveData.mapStates = gh.th.createMapStateSaveData();
        saveData.objectStates = gh.createObjectSaveData();
        saveData.bronzeDragonDefeated = gh.isBronzeDragonDefeated();
        saveData.dayNightTick = gh.dayNightCycle.getCurrentTick();

        try (Writer writer = Files.newBufferedWriter(SAVE_FILE_PATH)) {
            gson.toJson(saveData, writer);
        }
    }

    public boolean load(GameHandler gh) throws IOException {
        if (!hasSaveFile()) {
            return false;
        }

        SaveData saveData;
        try (Reader reader = Files.newBufferedReader(SAVE_FILE_PATH)) {
            saveData = gson.fromJson(reader, SaveData.class);
        }

        if (saveData == null || saveData.player == null || saveData.currentMapPath == null || saveData.currentMapPath.isBlank()) {
            return false;
        }

        gh.applySaveData(saveData);
        return true;
    }

    public static class SaveData {
        public int version = 1;
        public String currentMapPath;
        public PlayerData player;
        public List<ChestData> chests = new ArrayList<>();
        public List<MapStateData> mapStates = new ArrayList<>();
        public List<ObjectMapData> objectStates = new ArrayList<>();
        public boolean bronzeDragonDefeated;
        public int dayNightTick = -1;
    }

    public static class PlayerData {
        public int worldX;
        public int worldY;
        public String direction;
        public int speed;
        public int maxLife;
        public int life;
        public int level;
        public int strength;
        public int dexterity;
        public int exp;
        public int nextLevelExp;
        public int coin;
        public int currentWeaponIndex = -1;
        public List<ItemData> inventory = new ArrayList<>();
    }

    public static class ItemData {
        public String itemId;
        public int stackCount = 1;
    }

    public static class ChestData {
        public String mapPath;
        public int col;
        public int row;
        public boolean opened;
        public List<ItemData> items = new ArrayList<>();
    }

    public static class MapStateData {
        public String mapPath;
        public List<LayerData> layers = new ArrayList<>();
        public List<LayerData> healthLayers = new ArrayList<>();
    }

    public static class LayerData {
        public int width;
        public int height;
        public int[] values;
    }

    public static class ObjectMapData {
        public String mapPath;
        public List<WorldObjectData> objects = new ArrayList<>();
    }

    public static class WorldObjectData {
        public String itemId;
        public int worldX;
        public int worldY;
        public int stackCount = 1;
    }

    public static Map<String, ChestData> indexChestsByKey(List<ChestData> chests) {
        Map<String, ChestData> indexed = new HashMap<>();
        if (chests == null) {
            return indexed;
        }

        for (ChestData chest : chests) {
            if (chest == null || chest.mapPath == null) {
                continue;
            }

            indexed.put(ChestState.buildKey(chest.mapPath, chest.col, chest.row), chest);
        }

        return indexed;
    }
}
