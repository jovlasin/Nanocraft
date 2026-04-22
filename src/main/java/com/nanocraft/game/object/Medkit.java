package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Medkit extends Entity {
    public Medkit(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Medkit";
        down1 = scale("/object/Medkit-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nPacked with bandages and supplies.";
    }
}
