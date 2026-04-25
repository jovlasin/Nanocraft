package com.nanocraft.game.core;

import com.nanocraft.game.entity.BronzeDragon;
import com.nanocraft.game.entity.Elder;
import com.nanocraft.game.entity.SkeletonBoss;
import com.nanocraft.game.object.Key;
import com.nanocraft.game.tile.MapMarker;

public class AssetHandler {
    public static final String SPAWN_MAP_PATH = "/map/spawn.tmj";
    public static final String CAVE_MAP_PATH = "/map/cave.tmj";
    public static final String NETHER_MAP_PATH = "/map/nether.tmj";
    public static final String END_MAP_PATH = "/map/end.tmj";
    private static final String END_RETURN_PORTAL_MARKER = "end_return_portal_spawn";
    private static final String END_RETURN_PORTAL_LAYER = "Portal";
    private static final String END_RETURN_PORTAL_TILE_TYPE = "end_return_portal";
    private GameHandler gh;

    public AssetHandler(GameHandler gh) {
        this.gh = gh;
    }

    public void setObjects() {
        clearObjects();

        if (!shouldPlaceVillageAssets()) {
            return;
        }

        drawKey(0, 49, 12);
    }

    public void setNPCS() {
        clearNPCs();

        if (!shouldPlaceVillageAssets()) {
            return;
        }

        drawElder(0, 51, 18);
    }

    public void setMonsters() {
        clearMonsters();

        String currentMapPath = gh.th.getCurrentMapPath();
        if (NETHER_MAP_PATH.equals(currentMapPath)) {
            int[] spawnTile = gh.th.findNearestOpenTile(25, 25);
            if (spawnTile == null) {
                return;
            }

            drawSkeletonBoss(0, spawnTile[0], spawnTile[1]);
            return;
        }

        if (!END_MAP_PATH.equals(currentMapPath) || gh.isBronzeDragonDefeated()) {
            return;
        }

        MapMarker marker = gh.th.getMarker("bronze_dragon_spawn");
        int spawnCol = marker == null ? 8 : marker.col;
        int spawnRow = marker == null ? 9 : marker.row;
        drawBronzeDragon(0, spawnCol, spawnRow);
        gh.ui.addMessage("A bronze dragon guards the End.");
    }

    public boolean applyMapProgression() {
        if (!END_MAP_PATH.equals(gh.th.getCurrentMapPath()) || !gh.isBronzeDragonDefeated()) {
            return false;
        }

        return gh.th.placeTileAtMarker(END_RETURN_PORTAL_LAYER, END_RETURN_PORTAL_MARKER, END_RETURN_PORTAL_TILE_TYPE);
    }

    private void clearObjects() {
        for (int i = 0; i < gh.objs.length; i++) {
            gh.objs[i] = null;
        }
    }

    private void clearNPCs() {
        for (int i = 0; i < gh.npcs.length; i++) {
            gh.npcs[i] = null;
        }
    }

    private void clearMonsters() {
        for (int i = 0; i < gh.monsters.length; i++) {
            gh.monsters[i] = null;
        }
    }

    private void drawKey(int i, int x, int y) {
        gh.objs[i] = new Key(gh);
        gh.objs[i].worldX = gh.tileSize * x;
        gh.objs[i].worldY = gh.tileSize * y;
    }

    private void drawElder(int i, int x, int y) {
        gh.npcs[i] = new Elder(gh);
        gh.npcs[i].worldX = gh.tileSize * x;
        gh.npcs[i].worldY = gh.tileSize * y;
    }

    private void drawSkeletonBoss(int i, int x, int y) {
        gh.monsters[i] = new SkeletonBoss(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }

    private void drawBronzeDragon(int i, int x, int y) {
        gh.monsters[i] = new BronzeDragon(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }

    private boolean shouldPlaceVillageAssets() {
        String currentMapPath = gh.th.getCurrentMapPath();
        return !CAVE_MAP_PATH.equals(currentMapPath)
            && !NETHER_MAP_PATH.equals(currentMapPath)
            && !END_MAP_PATH.equals(currentMapPath);
    }
}
