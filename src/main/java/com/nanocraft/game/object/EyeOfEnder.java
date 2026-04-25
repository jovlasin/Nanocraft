package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class EyeOfEnder extends Entity {
    public EyeOfEnder(GameHandler gh) {
        super(gh);

        itemId = "eye_of_ender";
        type = TYPE_TOOL;
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Eye of Ender";
        description = "[" + name + "]\nA strange eye that opens the way to the End.";
        down1 = scaleOrFallback("/object/Eye_Of_Ender", "/object/Emerald", gh.tileSize, gh.tileSize);
        collision = false;

        solidArea.x = 8;
        solidArea.y = 8;
        solidArea.width = 24;
        solidArea.height = 24;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
}
