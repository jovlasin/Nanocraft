package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Sword extends Entity {
    public Sword(GameHandler gh) {
        super(gh);
        configureStacking(false, 1);

        type = TYPE_WEAPON;
        name = "Normal Sword";
        down1 = scale("/object/old_sword", gh.tileSize, gh.tileSize);
        attackValue = 1;
        attackArea.width = 36;
        attackArea.height = 36;
        description = "[" + name + "]\nAn old, rusty sword.";

        attackUp1 = scale("/player/playerSwordU1", gh.tileSize, gh.tileSize * 2);
        attackUp2 = scale("/player/playerSwordU2", gh.tileSize, gh.tileSize * 2);
        attackDown1 = scale("/player/playerSwordD1", gh.tileSize, gh.tileSize * 2);
        attackDown2 = scale("/player/playerSwordD2", gh.tileSize, gh.tileSize * 2);
        attackLeft1 = scale("/player/playerSwordL1", gh.tileSize * 2, gh.tileSize);
        attackLeft2 = scale("/player/playerSwordL2", gh.tileSize * 2, gh.tileSize);
        attackRight1 = scale("/player/playerSwordR1", gh.tileSize * 2, gh.tileSize);
        attackRight2 = scale("/player/playerSwordR2", gh.tileSize * 2, gh.tileSize);
    }
}
