package com.nanocraft.game.core;

import com.nanocraft.game.entity.Elder;
import com.nanocraft.game.object.Key;

public class AssetHandler {
    private GameHandler gh;

    public AssetHandler(GameHandler gh) {
        this.gh = gh;
    }

    public void setObjects() {
        drawKey(0, 49, 12);
    }

    public void setNPCS() {
        drawElder(0, 51, 18);
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
