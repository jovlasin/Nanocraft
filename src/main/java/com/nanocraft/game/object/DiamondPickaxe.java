package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class DiamondPickaxe extends Entity {
    public DiamondPickaxe(GameHandler gh) {
        super(gh);

        itemId = "diamond_pickaxe";
        type = tool;
        name = "Diamond Pickaxe";
        description = "[" + name + "]\nRequired for mining ore.";
        down1 = scaleOrFallback("/object/Diamond_Pickaxe", "/object/old_sword", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
