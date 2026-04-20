package com.nanocraft.game.object;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.core.GameHandler;

public class Key extends Entity {
    public Key(GameHandler gm) {
        super(gm);
        itemId = "key";
        name = "Key";
        down1 = scale("/object/key", gm.tileSize, gm.tileSize);
        description = "[" + name + "]\nIt opens something...";

        solidArea.x = 5;
        solidArea.y = 20;
        solidArea.width = 35;
        solidArea.height = 25;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
