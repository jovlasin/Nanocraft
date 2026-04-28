package com.nanocraft.game.core;

import com.nanocraft.game.entity.Elder;
import com.nanocraft.game.entity.Innkeeper;
import com.nanocraft.game.monster.Dragon;
import com.nanocraft.game.monster.GreenSlime;
import com.nanocraft.game.monster.Skeleton;
import com.nanocraft.game.monster.SkeletonKing;
import com.nanocraft.game.monster.Zombie;
import com.nanocraft.game.object.Key;
import com.nanocraft.game.tile.MapMarker;

public class AssetHandler {
    public static final String SPAWN_MAP_PATH = "/map/village.tmj";
    public static final String CAVE_MAP_PATH = "/map/cave.tmj";
    public static final String NETHER_MAP_PATH = "/map/nether.tmj";
    public static final String END_MAP_PATH = "/map/end.tmj";
    public static final String DESERT_MAP_PATH = "/map/desert.tmj";
    private static final String END_RETURN_PORTAL_MARKER = "end_return_portal_spawn";
    private static final String END_RETURN_PORTAL_LAYER = "Portal";
    private static final String END_RETURN_PORTAL_TILE_TYPE = "end_return_portal";
    public static final String INN_MAP_PATH = "/map/inn.tmj";
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

        if (INN_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            int[] innkeeperTile = gh.th.findNearestOpenTile(3, 5);
            if (innkeeperTile != null) {
                drawInnkeeper(0, 5, 2);
            }
            return;
        }

        if (!shouldPlaceVillageAssets()) {
            return;
        }

        drawElder(0, 51, 18);
    }

    public void setMonsters() {
        clearMonsters();

        if (NETHER_MAP_PATH.equals(gh.th.getCurrentMapPath()) && !gh.isSkeletonKingDefeated()) {
            drawSkeletonKing(0, 25, 25);
        }
      
        if (END_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            if (gh.isBronzeDragonDefeated()) {
              return;
            }
            
            MapMarker marker = gh.th.getMarker("bronze_dragon_spawn");
            int spawnCol = marker == null ? 8 : marker.col;
            int spawnRow = marker == null ? 9 : marker.row;
            drawBronzeDragon(0, spawnCol, spawnRow);
            gh.ui.addMessage("A bronze dragon guards the End.");
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
            drawZombies(16, 5, 9);
            drawZombies(17, 12, 46);
            drawZombies(18, 28, 32);
            drawZombies(19, 4, 49);

            drawSkeletons(20, 17, 25);
            drawSkeletons(21, 2, 42);
            drawSkeletons(22, 37, 6);
            drawSkeletons(23, 49, 24);
            drawSkeletons(24, 41, 38);
            drawSkeletons(25, 28, 24);
            drawSkeletons(26, 5, 36);
            drawSkeletons(27, 41, 12);
            drawSkeletons(28, 28, 41);
            drawSkeletons(29, 8, 17);
        }

        if (CAVE_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            drawGreenSlime(0, 12, 9);
            drawGreenSlime(1, 22, 5);
            drawGreenSlime(2, 19, 29);
            drawGreenSlime(3, 45, 4);
            drawGreenSlime(4, 57, 13);
            drawGreenSlime(5, 71, 12);
            drawGreenSlime(6, 62, 41);
            drawGreenSlime(7, 50, 61);
            drawGreenSlime(8, 62, 59);
            drawGreenSlime(9, 72, 57);

            drawZombies(10, 4, 17);
            drawZombies(11, 19, 20);
            drawZombies(12, 41, 27);
            drawZombies(13, 42, 39);
            drawZombies(14, 54, 34);
            drawZombies(15, 70, 23);
            drawZombies(16, 56, 50);
            drawZombies(17, 53, 74);
            drawZombies(18, 39, 95);
            drawZombies(19, 64, 86);
            drawZombies(20, 2, 91);
            drawZombies(21, 3, 61);
            drawZombies(22, 3, 44);
            drawZombies(23, 14, 51);
            drawZombies(24, 13, 87);
            drawZombies(25, 26, 45);
            drawZombies(26, 28, 61);
            drawZombies(27, 40, 51);
            drawZombies(28, 38, 76);
            drawZombies(29, 32, 90);

            
            
            drawSkeletons(30, 10, 29);
            drawSkeletons(31, 14, 17);
            drawSkeletons(32, 32, 26);
            drawSkeletons(33, 47, 18);
            drawSkeletons(34, 66, 5);
            drawSkeletons(35, 75, 30);
            drawSkeletons(36, 49, 52);
            drawSkeletons(37, 75, 51);
            drawSkeletons(38, 65, 73);
            drawSkeletons(39, 64, 95);
            drawSkeletons(40, 10, 56);
            drawSkeletons(41, 18, 46);
            drawSkeletons(42, 32, 56);
            drawSkeletons(43, 17, 65);
            drawSkeletons(44, 2, 85);
            drawSkeletons(45, 12, 76);
            drawSkeletons(46, 30, 71);
            drawSkeletons(47, 8, 97);
            drawSkeletons(48, 23, 90);
            drawSkeletons(49, 35, 84);
        }

        if (DESERT_MAP_PATH.equals(gh.th.getCurrentMapPath())) {
            drawGreenSlime(0, 59, 64);
            drawGreenSlime(1, 64, 53);
            drawGreenSlime(2, 16, 72);
            drawGreenSlime(3, 1, 63);
            drawGreenSlime(4, 7, 61);
            drawGreenSlime(5, 33, 55);
            drawGreenSlime(6, 34, 46);
            drawGreenSlime(7, 44, 35);
            drawGreenSlime(8, 28, 6);
            drawGreenSlime(9, 50, 14);

            drawZombies(10, 65, 73);
            drawZombies(11, 70, 61);
            drawZombies(12, 49, 73);
            drawZombies(13, 34, 70);
            drawZombies(14, 48, 61);
            drawZombies(15, 68, 43);
            drawZombies(16, 54, 52);
            drawZombies(17, 46, 54);
            drawZombies(18, 30, 50);
            drawZombies(19, 18, 54);
            drawZombies(20, 6, 44);
            drawZombies(21, 6, 9);
            drawZombies(22, 19, 5);
            drawZombies(23, 19, 15);
            drawZombies(24, 12, 22);
            drawZombies(25, 32, 34);
            drawZombies(26, 56, 23);
            drawZombies(27, 61, 16);
            drawZombies(28, 37, 18);
            drawZombies(29, 55, 7);

            drawSkeletons(30, 71, 54);
            drawSkeletons(31, 60, 57);
            drawSkeletons(32, 60, 45);
            drawSkeletons(33, 29, 66);
            drawSkeletons(34, 22, 51);
            drawSkeletons(35, 14, 42);
            drawSkeletons(36, 1, 40);
            drawSkeletons(37, 41, 43);
            drawSkeletons(38, 3, 25);
            drawSkeletons(39, 19, 24);
            drawSkeletons(40, 8, 16);
            drawSkeletons(41, 3, 2);
            drawSkeletons(42, 9, 3);
            drawSkeletons(43, 40, 2);
            drawSkeletons(44, 61, 1);
            drawSkeletons(45, 65, 7);
            drawSkeletons(46, 67, 22);
            drawSkeletons(47, 55, 13);
            drawSkeletons(47, 47, 13);
            drawSkeletons(49, 51, 24);
        }
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

    private void drawInnkeeper(int i, int x, int y) {
        gh.npcs[i] = new Innkeeper(gh);
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
  
    private void drawSkeletonKing(int i, int x, int y) {
        gh.monsters[i] = new SkeletonKing(gh);
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
  
    private void drawBronzeDragon(int i, int x, int y) {
        gh.monsters[i] = new Dragon(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }

    private void drawSkeletons(int i, int x, int y) {
        if (gh.isMonsterKilledOnCurrentMap(i)) {
            return;
        }

        gh.monsters[i] = new Skeleton(gh);
        gh.monsters[i].worldX = gh.tileSize * x;
        gh.monsters[i].worldY = gh.tileSize * y;
    }
  
    private boolean shouldPlaceVillageAssets() {
        String currentMapPath = gh.th.getCurrentMapPath();
        return !CAVE_MAP_PATH.equals(currentMapPath)
            && !NETHER_MAP_PATH.equals(currentMapPath)
            && !END_MAP_PATH.equals(currentMapPath)
            && !INN_MAP_PATH.equals(currentMapPath);
    }
}
