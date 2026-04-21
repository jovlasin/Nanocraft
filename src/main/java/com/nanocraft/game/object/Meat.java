package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Meat extends Entity {
    public Meat(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Meat";
        down1 = scale("/object/Meat-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA hearty meal.";
    }
}
