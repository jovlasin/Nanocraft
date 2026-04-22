package com.nanocraft.game.core;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;
import com.nanocraft.game.input.KeyHandler;
import com.nanocraft.game.object.Apple;
import com.nanocraft.game.object.Diamond;
import com.nanocraft.game.object.Emerald;
import com.nanocraft.game.object.Key;
import com.nanocraft.game.object.Meat;
import com.nanocraft.game.object.Medkit;
import com.nanocraft.game.object.OreChunk;
import com.nanocraft.game.object.Pickaxe;
import com.nanocraft.game.object.Redstone;
import com.nanocraft.game.object.Sword;
import com.nanocraft.game.tile.TileHandler;

public class GameHandler extends JPanel implements Runnable {
    private static final String NETHER_MAP_PATH = "/map/nether.tmj";
    private static final String END_MAP_PATH = "/map/end.tmj";
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
    public CollisionHandler ch = new CollisionHandler(this);
    public Entity objs[] = new Entity[10];
    public Entity npcs[] = new Entity[10];
    public Entity monsters[] = new Entity[20];
    public ArrayList<Entity> entityList = new ArrayList<>();
    public AssetHandler ah = new AssetHandler(this);
    public Ui ui = new Ui(this);
    public ChestState activeChest;
    public ArrayList<Entity> projectileList = new ArrayList<>();
    public Utility u = new Utility(this);
    private final SaveManager saveManager = new SaveManager();
    private final Map<String, List<SaveManager.WorldObjectData>> persistentObjectStates = new HashMap<>();
    public DayNightCycle dayNightCycle = new DayNightCycle();

    public final int title = 0;
    public final int pause = 1;
    public final int play = 2;
    public final int dialogue = 3;
    public final int stats = 4;
    public final int chest = 5;
    public int gameState = 999;
    private BufferedImage lightingFilter;

    public GameHandler() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(kh);
        refreshCurrentMapState();

        gameState = title;
        // gameState = play;
    }

    public void startGame() {
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
            player.update();

            for (int i = 0; i < npcs.length; i++) {
                if (npcs[i] != null) {
                    npcs[i].update();
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
        gameState = chest;
    }

    public void closeChest() {
        activeChest = null;
        ui.resetChestUi();
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

        persistentObjectStates.remove(currentMapPath);
        th.resetStoredMapState(currentMapPath);
        th.loadMap(currentMapPath);
        player.worldX = playerWorldX;
        player.worldY = playerWorldY;
        player.direction = playerDirection;
        refreshCurrentMapState();
        System.out.println("You slept. The world has reset.");
    }

    public void refreshCurrentMapState() {
        restoreCurrentMapObjects();
        ah.setNPCS();
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

    public List<SaveManager.ObjectMapData> createObjectSaveData() {
        rememberCurrentMapObjects();

        List<SaveManager.ObjectMapData> objectMaps = new ArrayList<>();
        for (Map.Entry<String, List<SaveManager.WorldObjectData>> entry : persistentObjectStates.entrySet()) {
            SaveManager.ObjectMapData objectMapData = new SaveManager.ObjectMapData();
            objectMapData.mapPath = entry.getKey();
            objectMapData.objects = copyWorldObjectData(entry.getValue());
            objectMaps.add(objectMapData);
        }

        return objectMaps;
    }

    public void applySaveData(SaveManager.SaveData saveData) {
        activeChest = null;
        projectileList.clear();
        ui.resetChestUi();
        ui.message.clear();
        ui.counter.clear();
        ui.currentDialogue = "";

        persistentObjectStates.clear();
        if (saveData.objectStates != null) {
            for (SaveManager.ObjectMapData objectMapData : saveData.objectStates) {
                if (objectMapData == null || objectMapData.mapPath == null || objectMapData.mapPath.isBlank()) {
                    continue;
                }

                persistentObjectStates.put(objectMapData.mapPath, copyWorldObjectData(objectMapData.objects));
            }
        }

        th.restoreChestSaveData(saveData.chests);
        th.restoreMapStateSaveData(saveData.mapStates);
        th.loadMap(saveData.currentMapPath);
        refreshCurrentMapState();
        player.applySaveData(saveData.player);
        gameState = play;
        ui.titleScreen = 1;
    }

    public boolean saveGame() {
        try {
            saveManager.save(this);
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
            if (!saveManager.load(this)) {
                ui.addMessage("No save file found.");
                closePauseMenu();
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

    public void closePauseMenu() {
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

            default:
                break;
        }
    }

    public Entity createItemEntity(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return null;
        }

        String normalized = itemType.trim().toLowerCase();
        switch (normalized) {
            case "apple":
                return new Apple(this);

            case "diamond":
                return new Diamond(this);

            case "emerald":
                return new Emerald(this);

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

        if (item instanceof Diamond) {
            return "diamond";
        }

        if (item instanceof Emerald) {
            return "emerald";
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

        if (item instanceof Sword) {
            return "sword";
        }

        return null;
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
                    int result = Integer.compare(e1.worldY, e2.worldY);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playMusic'");
    }

    public void playSound(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    public void cycleTimeOfDay() {
        if (isInDayMap()) {
            ui.addMessage("Time: Day");
            return;
        }

        dayNightCycle.advanceToNextPhase();
        ui.addMessage("Time: " + dayNightCycle.getPhaseName());
    }

    public boolean isInCave() {
        return AssetHandler.CAVE_MAP_PATH.equals(th.getCurrentMapPath());
    }

    public boolean isInDayMap() {
        String currentMapPath = th.getCurrentMapPath();
        return NETHER_MAP_PATH.equals(currentMapPath) || END_MAP_PATH.equals(currentMapPath);
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
        lightG.setColor(new Color(0f, 0.04f, 0.10f, darknessAlpha));
        lightG.fillRect(0, 0, screenWidth, screenHeight);

        int lightRadius = tileSize * 3;
        Point2D center = new Point2D.Float(player.screenX + (tileSize / 2f), player.screenY + (tileSize / 2f));
        float[] dist = {0f, 0.45f, 1f};
        Color[] colors = {
            new Color(0f, 0f, 0f, darknessAlpha),
            new Color(0f, 0f, 0f, darknessAlpha * 0.45f),
            new Color(0f, 0f, 0f, 0f)
        };

        lightG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        lightG.setComposite(AlphaComposite.DstOut);
        lightG.setPaint(new RadialGradientPaint(center, lightRadius, dist, colors));
        lightG.fillOval(Math.round((float) center.getX()) - lightRadius, Math.round((float) center.getY()) - lightRadius, lightRadius * 2, lightRadius * 2);
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
        List<SaveManager.WorldObjectData> savedObjects = persistentObjectStates.get(currentMapPath);
        if (savedObjects == null) {
            ah.setObjects();
            rememberCurrentMapObjects();
            return;
        }

        applyObjects(savedObjects);
    }

    private List<SaveManager.WorldObjectData> snapshotObjects(Entity[] sourceObjects) {
        List<SaveManager.WorldObjectData> savedObjects = new ArrayList<>();

        for (Entity object : sourceObjects) {
            if (object == null) {
                continue;
            }

            String itemId = getItemId(object);
            if (itemId == null) {
                continue;
            }

            SaveManager.WorldObjectData worldObjectData = new SaveManager.WorldObjectData();
            worldObjectData.itemId = itemId;
            worldObjectData.worldX = object.worldX;
            worldObjectData.worldY = object.worldY;
            worldObjectData.stackCount = Math.max(1, object.stackCount);
            savedObjects.add(worldObjectData);
        }

        return savedObjects;
    }

    private List<SaveManager.WorldObjectData> copyWorldObjectData(List<SaveManager.WorldObjectData> sourceObjects) {
        List<SaveManager.WorldObjectData> copy = new ArrayList<>();
        if (sourceObjects == null) {
            return copy;
        }

        for (SaveManager.WorldObjectData sourceObject : sourceObjects) {
            if (sourceObject == null) {
                continue;
            }

            SaveManager.WorldObjectData targetObject = new SaveManager.WorldObjectData();
            targetObject.itemId = sourceObject.itemId;
            targetObject.worldX = sourceObject.worldX;
            targetObject.worldY = sourceObject.worldY;
            targetObject.stackCount = sourceObject.stackCount;
            copy.add(targetObject);
        }

        return copy;
    }

    private void applyObjects(List<SaveManager.WorldObjectData> savedObjects) {
        int objectIndex = 0;

        for (SaveManager.WorldObjectData savedObject : savedObjects) {
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
}
