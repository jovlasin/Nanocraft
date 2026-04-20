package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Diamond extends Entity {
    public Diamond(GameHandler gh) {
        super(gh);
        name = "Diamond";
        down1 = scale("/object/Diamond", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA bright, valuable crystal.";
    }
}
