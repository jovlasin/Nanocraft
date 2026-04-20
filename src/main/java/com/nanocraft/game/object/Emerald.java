package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Emerald extends Entity {
    public Emerald(GameHandler gh) {
        super(gh);
        name = "Emerald";
        down1 = scale("/object/Emerald", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA polished green gem.";
    }
}
