package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Redstone extends Entity {
    public Redstone(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        name = "Redstone";
        down1 = scale("/object/Redstone", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA glowing red crystal shard.";
    }
}
