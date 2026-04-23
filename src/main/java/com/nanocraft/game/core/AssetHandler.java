package com.nanocraft.game.core;

import com.nanocraft.game.entity.Elder;
import com.nanocraft.game.object.Key;
import com.nanocraft.monster.GreenSlime;
import com.nanocraft.monster.Zombie;

public class AssetHandler {
    public static final String SPAWN_MAP_PATH = "/map/village.tmj";
    public static final String CAVE_MAP_PATH = "/map/cave.tmj";
    public static final String NETHER_MAP_PATH = "/map/nether.tmj";
    public static final String END_MAP_PATH = "/map/end.tmj";
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

    public void setMonsters() {
        clearMonsters();

        if (NETHER_MAP_PATH.equals(gh.th.getCurrentMapPath()) || END_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            return;
        }

        if (!CAVE_MAP_PATH.equals(gh.th.getCurrentMapPath()) && !gh.isNightForMonsterSpawns()) {
            return;
        }

        if (SPAWN_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            drawGreenSlime(0, 30, 46);
            drawGreenSlime(1, 33, 42);
            drawGreenSlime(2, 46, 40);
            drawGreenSlime(3, 34, 36);
            drawGreenSlime(4, 35, 28);
            drawGreenSlime(5, 38, 21);
            drawGreenSlime(6, 1, 46);
            drawGreenSlime(7, 10, 38);
            drawGreenSlime(8, 46, 6);
            drawGreenSlime(9, 12, 5);

            drawZombies(10, 25, 49);
            drawZombies(11, 2, 21);
            drawZombies(12, 14, 11);
            drawZombies(13, 47, 46);
            drawZombies(14, 7, 26);
            drawZombies(15, 7, 18);
            drawZombies(16, 1, 6);
            drawZombies(17, 12, 46);
            drawZombies(18, 28, 32);
            drawZombies(19, 4, 49);
        }

        

        
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

    private void drawGreenSlime(int i, int x, int y) {
        if (gh.isMonsterKilledOnCurrentMap(i)) {
            return;
        }

        gh.monsters[i] = new GreenSlime(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }

    private void drawZombies(int i, int x, int y) {
        if (gh.isMonsterKilledOnCurrentMap(i)) {
            return;
        }

        gh.monsters[i] = new Zombie(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }
}
