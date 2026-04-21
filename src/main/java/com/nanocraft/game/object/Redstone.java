package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Redstone extends Entity {
    public Redstone(GameHandler gh) {
        super(gh);

        itemId = "redstone";
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Redstone";
        description = "[" + name + "]\nA bright red mineral.";
        down1 = scaleOrFallback("/object/Redstone", "/tile/Red_Orea", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
