package com.nanocraft.game.core;

import com.nanocraft.game.entity.Elder;
import com.nanocraft.game.object.Key;

public class AssetHandler {
    public static final String SPAWN_MAP_PATH = "/map/spawn.tmj";
    public static final String CAVE_MAP_PATH = "/map/cave.tmj";
    private GameHandler gh;

    public AssetHandler(GameHandler gh) {
        this.gh = gh;
    }

    public void setObjects() {
        clearObjects();

        if (CAVE_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            return;
        }

        drawKey(0, 49, 12);
    }

    public void setNPCS() {
        clearNPCs();

        if (CAVE_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            return;
        }

        drawElder(0, 51, 18);
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
}
