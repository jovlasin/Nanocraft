package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Sword extends Entity {
    public Sword(GameHandler gh) {
        super(gh);

        itemId = "sword";
        type = sword;
        name = "Normal Sword";
        down1 = scale("/object/old_sword", gh.tileSize, gh.tileSize);
        attackValue = 1;
        attackArea.width = 36;
        attackArea.height = 36;
        description = "[" + name + "]\nAn old, rusty sword.";
    }
}
