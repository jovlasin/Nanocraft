package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Diamond extends Entity {
    public Diamond(GameHandler gh) {
        super(gh);

        itemId = "diamond";
        name = "Diamond";
        description = "[" + name + "]\nA rare ore crystal.";
        down1 = scaleOrFallback("/object/Diamond", "/tile/Diamond_Ore", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
