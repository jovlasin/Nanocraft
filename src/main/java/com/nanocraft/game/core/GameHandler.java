package com.nanocraft.game.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Innkeeper;
import com.nanocraft.game.entity.Player;
import com.nanocraft.game.input.KeyHandler;
import com.nanocraft.game.object.Apple;
import com.nanocraft.game.object.ArrowItem;
import com.nanocraft.game.object.Diamond;
import com.nanocraft.game.object.Emerald;
import com.nanocraft.game.object.EyeOfEnder;
import com.nanocraft.game.object.Key;
import com.nanocraft.game.object.Meat;
import com.nanocraft.game.object.Medkit;
import com.nanocraft.game.object.OreChunk;
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Redstone;
import com.nanocraft.game.object.Sword;
import com.nanocraft.game.object.Torch;
import com.nanocraft.game.tile.TileHandler;

public class GameHandler extends JPanel implements Runnable {
    private static final String NETHER_MAP_PATH = "/map/nether.tmj";
    private static final String END_MAP_PATH = "/map/end.tmj";
    private static final int MUSIC_TITLE = 0;
    private static final int MUSIC_VILLAGE = 1;
    private static final int MUSIC_NIGHT = 2;
    private static final int MUSIC_CAVE = 3;
    private static final int MUSIC_NETHER = 4;
    private static final int MUSIC_DESERT = 5;
    private static final int MUSIC_END = 6;
    private static final int MUSIC_INN = 7;
    private static final int MUSIC_COUNT = MUSIC_INN + 1;
    private static final int NO_MUSIC = -1;
    public static final String STARTING_MAP_PATH = "/map/village.tmj";
    public static final int SFX_ARROW = 0;
    public static final int SFX_SWORD_ATTACK = 1;
    public static final int SFX_PLAYER_DAMAGE = 2;
    public static final int SFX_MONSTER_HIT = 3;
    public static final int SFX_CURSOR = 4;
    public static final int SFX_POWERUP = 5;
    public static final int SFX_BLOCK_HIT = 6;
    public static final int SFX_BLOCK_BREAK = 7;
    public static final int SFX_LEVEL_UP = 8;
    public static final int SFX_MAIN_MENU = 9;
    public static final int SFX_CHEST_OPEN = 10;
    public static final int SFX_ENTERING_END = 11;
    public static final int SFX_FANFARE = 12;
    private static final int SFX_COUNT = SFX_FANFARE + 1;
    private final int defaultTileSize = 16; // tiles are 16x16 pngs
    private final int scale = 3;
    private final int maxScreenCol = 16; // 16 tiles wide
    private final int maxScreenRow = 12; // 12 tiles tall
    public final int tileSize = defaultTileSize * scale; // scale tile to 48x48
    public final int screenWidth = tileSize * maxScreenCol; // scale screen width to 768px
    public final int screenHeight = tileSize * maxScreenRow; // scale screen height to 576 tall
    public double fps = 60; // update the game 60 times per sec
    public Thread gameThread;
    public KeyHandler kh = new KeyHandler(this);
    public Player player = new Player(this, kh);
    public TileHandler th = new TileHandler(this);

    private Sound music = new Sound();
    private Sound se = new Sound();
    public CollisionHandler ch = new CollisionHandler(this);
    public Entity objs[] = new Entity[10];
    public Entity npcs[] = new Entity[10];
    public Entity monsters[] = new Entity[50];
    public ArrayList<Entity> entityList = new ArrayList<>();
    public AssetHandler ah = new AssetHandler(this);
    public Ui ui = new Ui(this);
    public ChestState activeChest;
    public ArrayList<Entity> projectileList = new ArrayList<>();
    public Utility u = new Utility(this);
    public boolean bronzeDragonDefeated;
    public boolean skeletonKingDefeated;
    public final SaveHandler sm = new SaveHandler(this);
    public final Map<String, List<SaveHandler.WorldObjectData>> persistentObjectStates = new HashMap<>();
    public final Map<String, Set<Integer>> persistentMonsterStates = new HashMap<>();
    public DayNightCycle dayNightCycle = new DayNightCycle();
    public Innkeeper ik = new Innkeeper(this);
    private boolean monsterSpawnWindowOpen;
    private boolean wasNightPhase;
    private boolean wasNightMusicPhase;

    public final int title = 0;
    public final int pause = 1;
    public final int play = 2;
    public final int dialogue = 3;
    public final int inventory = 4;
    public final int stats = 5;
    public final int chest = 6;
    public final int gameOver = 7;
    public int gameState = 999;
    private BufferedImage lightingFilter;
    private int musicVolume = 100;
    private int sfxVolume = 100;
    private boolean musicLoaded;
    private boolean musicStarted;
    private int activeMusicTrack = -1;
    private boolean soundEffectsLoaded;

    // /** For unit tests; fullscreen is a no-op without a window. */
    // public GameHandler() {
    //     this(null);
    // }

    public GameHandler() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(kh);
        wasNightPhase = dayNightCycle.isNight();
        wasNightMusicPhase = shouldUseNightMusic();
        monsterSpawnWindowOpen = isNightForMonsterSpawns();
        refreshCurrentMapState();

        gameState = title;
        // gameState = play;
    }

    public void startGame() {
        playMusic();
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / fps;
        double delta = 0;
        long time = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - time) / drawInterval;
            time = currentTime;

            while (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (gameState == play) {
            dayNightCycle.update();
            reconcileMonsterSpawnWindow();
            player.update();

            for (int i = 0; i < npcs.length; i++) {
                if (npcs[i] != null) {
                    npcs[i].update();
                }
            }

            for (int i = 0; i < monsters.length; i++) {
                if (monsters[i] != null) {
                    if (monsters[i].alive == true && monsters[i].dying == false) {
                        monsters[i].update();
                    }
                }
            }

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    if (projectileList.get(i).alive == true) {
                        projectileList.get(i).update();
                    }
                  
                    if (projectileList.get(i).alive == false) {
                        projectileList.remove(i);
                    }
                }
            }

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    if (projectileList.get(i).alive == true) {
                        projectileList.get(i).update();
                    }

                    if (projectileList.get(i).alive == false) {
                        projectileList.remove(i);
                    }
                }
            }
        }

        if (gameState == pause) {
            // nothing
        }
    }

    public void openChest(ChestState chestState) {
        if (chestState == null) {
            return;
        }

        activeChest = chestState;
        activeChest.opened = true;
        ui.resetChestUi();
        ui.addMessage("Opened chest.");
        playSound(SFX_CHEST_OPEN);
        gameState = chest;
    }

    public void closeChest() {
        activeChest = null;
        ui.resetChestUi();
        playSound(SFX_CHEST_OPEN);
        gameState = play;
    }

    public void transferActiveChestSelection() {
        if (activeChest == null) {
            return;
        }

        if (ui.isChestPanelActive()) {
            transferChestItemToPlayer();
            return;
        }

        transferPlayerItemToChest();
    }

    public void spawnDroppedItem(int worldX, int worldY, String itemType) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                continue;
            }

            Entity droppedItem = createItemEntity(itemType);
            if (droppedItem == null) {
                System.out.println("Unknown dropped item type: " + itemType);
                return;
            }

            droppedItem.worldX = worldX;
            droppedItem.worldY = worldY;
            objs[i] = droppedItem;
            rememberCurrentMapObjects();
            return;
        }

        System.out.println("No free world object slot for dropped item: " + itemType);
    }

    public void onPlayerSleep() {
        String currentMapPath = th.getCurrentMapPath();
        int playerWorldX = player.worldX;
        int playerWorldY = player.worldY;
        String playerDirection = player.direction;

        player.life = player.maxLife;
        persistentObjectStates.clear();
        persistentMonsterStates.clear();
        th.resetAllChests();
        th.resetAllStoredMapStates();
        dayNightCycle.advanceToRestPhase();
        syncDayNightState();
        th.loadFreshMap(currentMapPath);
        player.worldX = playerWorldX;
        player.worldY = playerWorldY;
        player.direction = playerDirection;
        activeChest = null;
        ui.resetChestUi();
        refreshCurrentMapState();
        ik.closeDialogue();
        ui.addMessage("You feel rested.");
        ui.addMessage("Time: " + dayNightCycle.getPhaseName());
    }

    public void refreshCurrentMapState() {
        restoreCurrentMapObjects();
        ah.setNPCS();
        ah.setMonsters();
        ah.applyMapProgression();
        wasNightMusicPhase = shouldUseNightMusic();
        updateMusicForCurrentMap();
    }

    public void beforeMapChange() {
        rememberCurrentMapObjects();
    }

    public void rememberCurrentMapObjects() {
        String currentMapPath = th.getCurrentMapPath();
        if (currentMapPath == null || currentMapPath.isBlank()) {
            return;
        }

        persistentObjectStates.put(currentMapPath, snapshotObjects(objs));
    }

    public List<SaveHandler.ObjectMapData> createObjectSaveData() {
        rememberCurrentMapObjects();

        List<SaveHandler.ObjectMapData> objectMaps = new ArrayList<>();
        for (Map.Entry<String, List<SaveHandler.WorldObjectData>> entry : persistentObjectStates.entrySet()) {
            SaveHandler.ObjectMapData objectMapData = new SaveHandler.ObjectMapData();
            objectMapData.mapPath = entry.getKey();
            objectMapData.objects = copyWorldObjectData(entry.getValue());
            objectMaps.add(objectMapData);
        }

        return objectMaps;
    }

    public List<SaveHandler.MonsterMapData> createMonsterSaveData() {
        List<SaveHandler.MonsterMapData> monsterMaps = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : persistentMonsterStates.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }

            SaveHandler.MonsterMapData monsterMapData = new SaveHandler.MonsterMapData();
            monsterMapData.mapPath = entry.getKey();
            monsterMapData.killedSlots = new ArrayList<>(entry.getValue());
            monsterMaps.add(monsterMapData);
        }

        return monsterMaps;
    }

    public void applySaveData(SaveHandler.SaveData saveData) {
        activeChest = null;
        projectileList.clear();
        ui.resetChestUi();
        ui.message.clear();
        ui.counter.clear();
        ui.currentDialogue = "";

        persistentObjectStates.clear();
        persistentMonsterStates.clear();
        if (saveData.objectStates != null) {
            for (SaveHandler.ObjectMapData objectMapData : saveData.objectStates) {
                if (objectMapData == null || objectMapData.mapPath == null || objectMapData.mapPath.isBlank()) {
                    continue;
                }

                persistentObjectStates.put(objectMapData.mapPath, copyWorldObjectData(objectMapData.objects));
            }
        }

        if (saveData.monsterStates != null) {
            for (SaveHandler.MonsterMapData monsterMapData : saveData.monsterStates) {
                if (monsterMapData == null || monsterMapData.mapPath == null || monsterMapData.mapPath.isBlank()) {
                    continue;
                }

                Set<Integer> killedSlots = new HashSet<>();
                if (monsterMapData.killedSlots != null) {
                    for (Integer slotIndex : monsterMapData.killedSlots) {
                        if (slotIndex != null && slotIndex >= 0) {
                            killedSlots.add(slotIndex);
                        }
                    }
                }
                persistentMonsterStates.put(monsterMapData.mapPath, killedSlots);
            }
        }

        skeletonKingDefeated = saveData.skeletonKingDefeated;
        bronzeDragonDefeated = saveData.bronzeDragonDefeated;
          
        if (saveData.dayNightTick >= 0) {
            dayNightCycle.setCurrentTick(saveData.dayNightTick);
        }

        th.restoreChestSaveData(saveData.chests);
        th.restoreMapStateSaveData(saveData.mapStates);
        th.loadMap(saveData.currentMapPath);
        syncDayNightState();
        gameState = play;
        refreshCurrentMapState();
        player.applySaveData(saveData.player);
        ui.titleScreen = 1;
    }

    public boolean saveGame() {
        try {
            sm.save(this);
            ui.addMessage("Game saved.");
            return true;
        } catch (Exception e) {
            ui.addMessage("Save failed.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadGame() {
        try {
            if (!sm.load(this)) {
                ui.addMessage("No save file found.");
                if (gameState == pause) {
                    closePauseMenu();
                }
                return false;
            }

            ui.addMessage("Game loaded.");
            return true;
        } catch (Exception e) {
            System.out.println("Failed to load save.");
            e.printStackTrace();
            return false;
        }
    }

    public void openPauseMenu() {
        ui.resetPauseMenu();
        gameState = pause;
    }

    public void openGameOverMenu() {
        if (gameState == gameOver) {
            return;
        }

        kh.up = false;
        kh.down = false;
        kh.left = false;
        kh.right = false;
        kh.space = false;
        kh.shoot = false;
        ui.resetGameOverMenu();
        gameState = gameOver;
    }

    public void closePauseMenu() {
        ui.closePauseExitConfirmation();
        ui.closeSettings();
        gameState = play;
    }

    public void activatePauseMenuSelection() {
        switch (ui.getPauseMenuSelection()) {
            case 0:
                closePauseMenu();
                break;

            case 1:
                if (saveGame()) {
                    closePauseMenu();
                }
                break;

            case 2:
                if (loadGame()) {
                    closePauseMenu();
                }
                break;

            case 3:
                ui.openSettings();
                break;

            case 4:
                ui.openPauseExitConfirmation();
                break;

            default:
                break;
        }
    }

    public void enterSettings() {
        switch (ui.getSettingsIndex()) {
            case 0:
                setFullScreen(!isFullScreen());
                break;

            case 3:
                ui.openControlMenu();
                break;

            case 4:
                ui.closeSettings();
                break;

            default:
                break;
        }
    }

    public void selectSetting(int direction) {
        if (direction == 0) {
            return;
        }
        int index = ui.getSettingsIndex();
        if (index == 0) {
            setFullScreen(direction > 0);
        } else if (index == 1) {
            setMusicVolume(musicVolume + direction * 5);
        } else if (index == 2) {
            setSfxVolume(sfxVolume + direction * 5);
        }
    }

    public void confirmPauseExitSelection() {
        if (ui.shouldExitFromPauseConfirmation()) {
            System.exit(0);
            return;
        }

        ui.closePauseExitConfirmation();
    }

    public void activateGameOverSelection() {
        switch (ui.getGameOverSelection()) {
            case 0:
                if (hasSaveGame()) {
                    loadGame();
                } else {
                    sm.startNewGame();
                }
                break;

            case 1:
                System.exit(0);
                break;

            default:
                break;
        }
    }

    public boolean hasSaveGame() {
        return sm.hasSaveFile();
    }

    public Entity createItemEntity(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return null;
        }

        String normalized = itemType.trim().toLowerCase();
        switch (normalized) {
            case "arrow":
            case "arrows":
                return new ArrowItem(this);

            case "apple":
                return new Apple(this);

            case "diamond":
                return new Diamond(this);
            case "emerald":
                return new Emerald(this);
            case "eye_of_ender":
            case "eyeofender":
                return new EyeOfEnder(this);

            case "ore_chunk":
            case "orechunk":
            case "ore":
                return new OreChunk(this);

            case "key":
                return new Key(this);

            case "meat":
                return new Meat(this);

            case "medkit":
                return new Medkit(this);

            case "pickaxe":
            case "diamond_pickaxe":
            case "diamondpickaxe":
                return new Pickaxe(this);
            case "redstone":
                return new Redstone(this);
            case "torch":
                return new Torch(this);

            case "sword":
            case "normal_sword":
            case "normalsword":
                return new Sword(this);

            default:
                return null;
        }
    }

    public String getItemId(Entity item) {
        if (item == null) {
            return null;
        }

        if (item instanceof Apple) {
            return "apple";
        }

        if (item instanceof ArrowItem) {
            return "arrow";
        }

        if (item instanceof Diamond) {
            return "diamond";
        }

        if (item instanceof Emerald) {
            return "emerald";
        }

        if (item instanceof EyeOfEnder) {
            return "eye_of_ender";
        }

        if (item instanceof OreChunk) {
            return "ore_chunk";
        }

        if (item instanceof Key) {
            return "key";
        }

        if (item instanceof Meat) {
            return "meat";
        }

        if (item instanceof Medkit) {
            return "medkit";
        }

        if (item instanceof Pickaxe) {
            return "pickaxe";
        }

        if (item instanceof Redstone) {
            return "redstone";
        }

        if (item instanceof Torch) {
            return "torch";
        }

        if (item instanceof Sword) {
            return "sword";
        }

        return null;
    }
  
    public Entity createDropEntity(String itemType) {
        return createItemEntity(itemType);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameState == title) {
            ui.draw(g2d);
        }

        else {
            entityList.add(player);

            for (int i = 0; i < th.getBelowPlayerLayerCount(); i++) {
                th.drawLayer(g2d, i);
            }

            for (int i = th.getBelowPlayerLayerCount(); i < th.getLayerCount(); i++) {
                th.drawLayer(g2d, i);
            }

            for (int i = 0; i < npcs.length; i++) {
                if (npcs[i] != null) {
                    entityList.add(npcs[i]);
                }
            }

            for (int i = 0; i < monsters.length; i++) {
                if (monsters[i] != null) {
                    entityList.add(monsters[i]);
                }
            }

            for (int i = 0; i < objs.length; i++) {
                if (objs[i] != null) {
                    entityList.add(objs[i]);
                }
            }

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    entityList.add(projectileList.get(i));
                }
            }

            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    int e1Depth = e1.worldY + e1.solidArea.y + e1.solidArea.height;
                    int e2Depth = e2.worldY + e2.solidArea.y + e2.solidArea.height;
                    int result = Integer.compare(e1Depth, e2Depth);
                    return result;
                }
            });

            for (Entity e: entityList) {
                e.draw(g2d);
            }

            entityList.clear();
            drawLighting(g2d);
            ui.draw(g2d);
        }
        
        g2d.dispose();
    }

    public void playMusic() {
        musicStarted = true;
        updateMusicForCurrentMap();
    }

    private void updateMusicForCurrentMap() {
        if (!musicStarted || !isAudioPlaybackAvailable()) {
            return;
        }

        if (musicVolume == 0) {
            stopAllMusicTracks();
            return;
        }

        loadMusicIfNeeded();
        int nextMusicTrack = getCurrentMapMusicTrack();
        if (activeMusicTrack != nextMusicTrack) {
            stopActiveMusicTrack();
            activeMusicTrack = nextMusicTrack;
        }

        if (activeMusicTrack < 0) {
            return;
        }

        loopMusicTrack(activeMusicTrack);
    }

    private int getCurrentMapMusicTrack() {
        if (gameState == title) {
            return MUSIC_TITLE;
        }

        String currentMapPath = th.getCurrentMapPath();
        if (AssetHandler.CAVE_MAP_PATH.equals(currentMapPath)) {
            return MUSIC_CAVE;
        }

        if (AssetHandler.NETHER_MAP_PATH.equals(currentMapPath)) {
            return MUSIC_NETHER;
        }

        if (AssetHandler.DESERT_MAP_PATH.equals(currentMapPath)) {
            if (dayNightCycle.isNight()) {
                return MUSIC_NIGHT;
            }
            return MUSIC_DESERT;
        }

        if (STARTING_MAP_PATH.equals(currentMapPath)) {
            if (dayNightCycle.isNight()) {
                return MUSIC_NIGHT;
            }
            return MUSIC_VILLAGE;
        }

        if (AssetHandler.END_MAP_PATH.equals(currentMapPath)) {
            if (bronzeDragonDefeated) {
                return NO_MUSIC;
            }
            return MUSIC_END;
        }

        if (AssetHandler.INN_MAP_PATH.equals(currentMapPath)) {
            return MUSIC_INN;
        }

        return MUSIC_VILLAGE;
    }

    private void loadMusicIfNeeded() {
        if (musicLoaded) {
            return;
        }

        loadMusicTrack(MUSIC_TITLE, "/sound/MainMenu.wav");
        loadMusicTrack(MUSIC_VILLAGE, "/sound/village.wav");
        loadMusicTrack(MUSIC_NIGHT, "/sound/night.wav");
        loadMusicTrack(MUSIC_CAVE, "/sound/Moody Dungeon.wav");
        loadMusicTrack(MUSIC_NETHER, "/sound/Alone in the Chamber.wav");
        loadMusicTrack(MUSIC_DESERT, "/sound/Desert.wav");
        loadMusicTrack(MUSIC_END, "/sound/EndMusic.wav");
        loadMusicTrack(MUSIC_INN, "/sound/inn.wav");
        musicLoaded = true;
    }

    protected boolean isAudioPlaybackAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }

    protected void loadMusicTrack(int id, String path) {
        music.load(id, path, 1);
    }

    protected void loopMusicTrack(int id) {
        music.setVolume(id, musicVolume);
        music.loop(id);
    }

    protected void stopMusicTrack(int id) {
        music.stop(id);
    }

    private void stopActiveMusicTrack() {
        if (activeMusicTrack >= 0) {
            stopMusicTrack(activeMusicTrack);
        }
    }

    private void stopAllMusicTracks() {
        for (int i = 0; i < MUSIC_COUNT; i++) {
            stopMusicTrack(i);
        }
        activeMusicTrack = -1;
    }

    public void playSound(int i) {
        if (sfxVolume == 0 || GraphicsEnvironment.isHeadless()) {
            return;
        }

        loadSoundEffectsIfNeeded();
        se.setVolume(i, sfxVolume);
        se.play(i);
    }

    private void loadSoundEffectsIfNeeded() {
        if (soundEffectsLoaded) {
            return;
        }

        se.load(SFX_ARROW, "/sound/arrow.wav");
        se.load(SFX_SWORD_ATTACK, "/sound/attack.wav");
        se.load(SFX_PLAYER_DAMAGE, "/sound/damage.wav");
        se.load(SFX_MONSTER_HIT, "/sound/hit.wav");
        se.load(SFX_CURSOR, "/sound/cursor.wav");
        se.load(SFX_POWERUP, "/sound/powerup.wav");
        se.load(SFX_BLOCK_HIT, "/sound/blockhit.wav");
        se.load(SFX_BLOCK_BREAK, "/sound/blockbreak.wav");
        se.load(SFX_LEVEL_UP, "/sound/levelup.wav");
        se.load(SFX_MAIN_MENU, "/sound/MainMenu.wav");
        se.load(SFX_CHEST_OPEN, "/sound/ChestOpen.wav");
        se.load(SFX_ENTERING_END, "/sound/EnteringEnd.wav");
        se.load(SFX_FANFARE, "/sound/fanfare.wav");
        soundEffectsLoaded = true;
        applySfxVolume();
    }

    public boolean isFullScreen() {
        // TODO
        return false;
    }

    public void setFullScreen(boolean bool) {
        // TODO
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int percent) {
        musicVolume = Math.max(0, Math.min(100, percent));
        if (musicVolume == 0) {
            stopAllMusicTracks();
            return;
        }

        if (musicStarted) {
            updateMusicForCurrentMap();
        }
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int percent) {
        sfxVolume = Math.max(0, Math.min(100, percent));
        applySfxVolume();
    }

    private void applySfxVolume() {
        if (!soundEffectsLoaded) {
            return;
        }

        for (int i = 0; i < SFX_COUNT; i++) {
            se.setVolume(i, sfxVolume);
        }
    }
  
    public void clearProjectiles() {
        projectileList.clear();
    }

    public void handleBronzeDragonDefeat() {
        if (bronzeDragonDefeated) {
            return;
        }

        bronzeDragonDefeated = true;
        clearProjectiles();
        if (END_MAP_PATH.equals(th.getCurrentMapPath())) {
            updateMusicForCurrentMap();
            playSound(SFX_FANFARE);
        }
        ui.addMessage("The bronze dragon collapses into ash.");

        if (ah.applyMapProgression()) {
            ui.addMessage("A portal back to the village appears.");
        }
    }

    public boolean isBronzeDragonDefeated() {
        return bronzeDragonDefeated;
    }

    public void setBronzeDragonDefeated(boolean bronzeDragonDefeated) {
        this.bronzeDragonDefeated = bronzeDragonDefeated;
    }

    public boolean isSkeletonKingDefeated() {
        return skeletonKingDefeated;
    }

    public void setSkeletonKingDefeated(boolean skeletonKingDefeated) {
        this.skeletonKingDefeated = skeletonKingDefeated;
    }

    public void cycleTimeOfDay() {
        if (isInDayMap()) {
            ui.addMessage("Time: Day");
            return;
        }

        dayNightCycle.advanceToNextPhase();
        reconcileMonsterSpawnWindow();
        ui.addMessage("Time: " + dayNightCycle.getPhaseName());
    }

    public boolean isInCave() {
        return AssetHandler.CAVE_MAP_PATH.equals(th.getCurrentMapPath());
    }

    public boolean isInDayMap() {
        String currentMapPath = th.getCurrentMapPath();
        return NETHER_MAP_PATH.equals(currentMapPath) || END_MAP_PATH.equals(currentMapPath);
    }

    private boolean shouldUseNightMusic() {
        String currentMapPath = th.getCurrentMapPath();
        if (currentMapPath == null) {
            return false;
        }

        String phaseName = dayNightCycle.getPhaseName();
        if (!"Night".equals(phaseName) && !"Dawn".equals(phaseName)) {
            return false;
        }

        return STARTING_MAP_PATH.equals(currentMapPath) || AssetHandler.DESERT_MAP_PATH.equals(currentMapPath);
    }

    public boolean isNightForMonsterSpawns() {
        return isInCave() || (!isInDayMap() && dayNightCycle.isNight());
    }

    public float getCurrentDarknessAlpha() {
        if (isInDayMap()) {
            return 0f;
        }

        return isInCave() ? dayNightCycle.getMaxDarknessAlpha() : dayNightCycle.getDarknessAlpha();
    }

    public String getTimeLabel() {
        if (isInCave()) {
            return "Cave  Night";
        }

        if (isInDayMap()) {
            return "12:00  Day";
        }

        return dayNightCycle.getClockText() + "  " + dayNightCycle.getPhaseName();
    }

    private void drawLighting(Graphics2D g2d) {
        float darknessAlpha = getCurrentDarknessAlpha();

        if (darknessAlpha <= 0f) {
            return;
        }

        if (lightingFilter == null || lightingFilter.getWidth() != screenWidth || lightingFilter.getHeight() != screenHeight) {
            lightingFilter = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D lightG = lightingFilter.createGraphics();
        lightG.setComposite(AlphaComposite.Clear);
        lightG.fillRect(0, 0, screenWidth, screenHeight);

        lightG.setComposite(AlphaComposite.SrcOver);
        lightG.setColor(new Color(0f, 0f, 0f, darknessAlpha));
        lightG.fillRect(0, 0, screenWidth, screenHeight);

        lightG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        lightG.setComposite(AlphaComposite.DstOut);

        boolean torchEquipped = player.currentTool != null && "torch".equalsIgnoreCase(player.currentTool.itemId);
        float[] dist = {0f, 0.45f, 1f};
        Point2D playerLightCenter = new Point2D.Float(player.screenX + (tileSize / 2f), player.screenY + (tileSize / 2f));

        if (torchEquipped) {
            Color[] torchColors = {
                new Color(0f, 0f, 0f, darknessAlpha),
                new Color(0f, 0f, 0f, darknessAlpha * 0.45f),
                new Color(0f, 0f, 0f, 0f)
            };
            int playerLightRadius = tileSize * 5;
            lightG.setPaint(new RadialGradientPaint(playerLightCenter, playerLightRadius, dist, torchColors));
            lightG.fillOval(
                Math.round((float) playerLightCenter.getX()) - playerLightRadius,
                Math.round((float) playerLightCenter.getY()) - playerLightRadius,
                playerLightRadius * 2,
                playerLightRadius * 2
            );
        }
        lightG.dispose();

        g2d.drawImage(lightingFilter, 0, 0, null);
    }

    private void transferChestItemToPlayer() {
        if (activeChest == null) {
            return;
        }

        int itemIndex = ui.getSelectedChestSlotIndex();
        if (itemIndex < 0 || itemIndex >= activeChest.items.size()) {
            return;
        }

        Entity item = activeChest.items.get(itemIndex);
        if (!player.canAcceptInventoryItem(item)) {
            ui.addMessage("Inventory full.");
            return;
        }

        item = activeChest.removeItem(itemIndex);
        if (item == null) {
            return;
        }

        if (!player.addToInventory(item)) {
            activeChest.addItem(item);
            ui.addMessage("Inventory full.");
            return;
        }

        ui.addMessage("Moved " + item.name + ".");
    }

    private void transferPlayerItemToChest() {
        if (activeChest == null) {
            return;
        }

        int itemIndex = ui.getSelectedPlayerChestSlotIndex();
        if (itemIndex < 0 || itemIndex >= player.inventory.size()) {
            return;
        }

        Entity item = player.inventory.get(itemIndex);
        if (!activeChest.canAcceptItem(item)) {
            ui.addMessage("Chest is full.");
            return;
        }

        item = player.removeFromInventory(itemIndex);
        if (item == null) {
            return;
        }

        if (!activeChest.addItem(item)) {
            player.addToInventory(item);
            ui.addMessage("Chest is full.");
            return;
        }

        player.handleRemovedInventoryItem(item);
        ui.addMessage("Stored " + item.name + ".");
    }

    private void restoreCurrentMapObjects() {
        clearObjects();

        String currentMapPath = th.getCurrentMapPath();
        List<SaveHandler.WorldObjectData> savedObjects = persistentObjectStates.get(currentMapPath);
        if (savedObjects == null) {
            ah.setObjects();
            rememberCurrentMapObjects();
            return;
        }

        applyObjects(savedObjects);
    }

    private List<SaveHandler.WorldObjectData> snapshotObjects(Entity[] sourceObjects) {
        List<SaveHandler.WorldObjectData> savedObjects = new ArrayList<>();

        for (Entity object : sourceObjects) {
            if (object == null) {
                continue;
            }

            String itemId = getItemId(object);
            if (itemId == null) {
                continue;
            }

            SaveHandler.WorldObjectData worldObjectData = new SaveHandler.WorldObjectData();
            worldObjectData.itemId = itemId;
            worldObjectData.worldX = object.worldX;
            worldObjectData.worldY = object.worldY;
            worldObjectData.stackCount = Math.max(1, object.stackCount);
            savedObjects.add(worldObjectData);
        }

        return savedObjects;
    }

    private List<SaveHandler.WorldObjectData> copyWorldObjectData(List<SaveHandler.WorldObjectData> sourceObjects) {
        List<SaveHandler.WorldObjectData> copy = new ArrayList<>();
        if (sourceObjects == null) {
            return copy;
        }

        for (SaveHandler.WorldObjectData sourceObject : sourceObjects) {
            if (sourceObject == null) {
                continue;
            }

            SaveHandler.WorldObjectData targetObject = new SaveHandler.WorldObjectData();
            targetObject.itemId = sourceObject.itemId;
            targetObject.worldX = sourceObject.worldX;
            targetObject.worldY = sourceObject.worldY;
            targetObject.stackCount = sourceObject.stackCount;
            copy.add(targetObject);
        }

        return copy;
    }

    private void applyObjects(List<SaveHandler.WorldObjectData> savedObjects) {
        int objectIndex = 0;

        for (SaveHandler.WorldObjectData savedObject : savedObjects) {
            if (savedObject == null || objectIndex >= objs.length) {
                continue;
            }

            Entity object = createItemEntity(savedObject.itemId);
            if (object == null) {
                continue;
            }

            object.worldX = savedObject.worldX;
            object.worldY = savedObject.worldY;
            object.stackCount = Math.max(1, savedObject.stackCount);
            objs[objectIndex] = object;
            objectIndex++;
        }
    }

    private void clearObjects() {
        for (int i = 0; i < objs.length; i++) {
            objs[i] = null;
        }
    }

    public boolean isMonsterKilledOnCurrentMap(int slotIndex) {
        if (slotIndex < 0) {
            return false;
        }

        String currentMapPath = th.getCurrentMapPath();
        if (currentMapPath == null || currentMapPath.isBlank()) {
            return false;
        }

        Set<Integer> killedSlots = persistentMonsterStates.get(currentMapPath);
        return killedSlots != null && killedSlots.contains(slotIndex);
    }

    public void markMonsterKilled(int slotIndex) {
        if (slotIndex < 0) {
            return;
        }

        String currentMapPath = th.getCurrentMapPath();
        if (currentMapPath == null || currentMapPath.isBlank()) {
            return;
        }

        persistentMonsterStates.computeIfAbsent(currentMapPath, key -> new HashSet<>()).add(slotIndex);
    }

    private void reconcileMonsterSpawnWindow() {
        boolean nightPhaseNow = dayNightCycle.isNight();
        if (nightPhaseNow && !wasNightPhase) {
            clearKilledMonstersForNightRespawn();
        }
        wasNightPhase = nightPhaseNow;

        boolean nightMusicPhaseNow = shouldUseNightMusic();
        if (nightMusicPhaseNow != wasNightMusicPhase) {
            wasNightMusicPhase = nightMusicPhaseNow;
            updateMusicForCurrentMap();
        }

        boolean spawnWindowOpenNow = isNightForMonsterSpawns();
        if (spawnWindowOpenNow == monsterSpawnWindowOpen) {
            return;
        }

        monsterSpawnWindowOpen = spawnWindowOpenNow;
        ah.setMonsters();
    }

    public void syncDayNightState() {
        wasNightPhase = dayNightCycle.isNight();
        wasNightMusicPhase = shouldUseNightMusic();
        monsterSpawnWindowOpen = isNightForMonsterSpawns();
        lightingFilter = null;
    }

    private void clearKilledMonstersForNightRespawn() {
        List<String> mapPaths = new ArrayList<>(persistentMonsterStates.keySet());
        for (String mapPath : mapPaths) {
            if (AssetHandler.CAVE_MAP_PATH.equals(mapPath)) {
                continue;
            }

            persistentMonsterStates.remove(mapPath);
        }
    }
}
