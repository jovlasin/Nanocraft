package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class OreChunk extends Entity {
    public OreChunk(GameHandler gh) {
        super(gh);
        itemId = "ore_chunk";
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Ore Chunk";
        description = "[" + name + "]\nA chunk of mined ore.";
        down1 = scale("/object/key", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
