package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class ArrowItem extends Entity {
    public ArrowItem(GameHandler gh) {
        super(gh);
        itemId = "arrow";
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Arrow";
        down1 = scale("/object/Arrow", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nAmmo for ranged attacks.";
        collision = false;

        solidArea.x = 5;
        solidArea.y = 20;
        solidArea.width = 35;
        solidArea.height = 25;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
