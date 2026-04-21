package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Apple extends Entity {
    public Apple(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Apple";
        down1 = scale("/object/Apple-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA fresh apple.";
    }
}
