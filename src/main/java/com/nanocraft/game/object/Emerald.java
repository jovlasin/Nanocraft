package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Emerald extends Entity {
    public Emerald(GameHandler gh) {
        super(gh);

        itemId = "emerald";
        name = "Emerald";
        description = "[" + name + "]\nA polished green gem.";
        down1 = scaleOrFallback("/object/Emerald", "/tile/Green_Ore", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
