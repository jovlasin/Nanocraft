package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Pickaxe extends Entity {
    public Pickaxe(GameHandler gh) {
        super(gh);
        configureStacking(false, 1);

        itemId = "diamond_pickaxe";
        type = TYPE_WEAPON;
        name = "Diamond Pickaxe";
        down1 = scale("/object/Diamond_Pickaxe", gh.tileSize, gh.tileSize);
        attackValue = 0;
        attackArea.width = 36;
        attackArea.height = 36;
        description = "[" + name + "]\nA sturdy pickaxe for heavy swings.";

        attackUp1 = scale("/player/playerPickaxeU1", gh.tileSize, gh.tileSize * 2);
        attackUp2 = scale("/player/playerPickaxeU2", gh.tileSize, gh.tileSize * 2);
        attackDown1 = scale("/player/playerPickaxeD1", gh.tileSize, gh.tileSize * 2);
        attackDown2 = scale("/player/playerPickaxeD2", gh.tileSize, gh.tileSize * 2);
        attackLeft1 = scale("/player/playerPickaxeL1", gh.tileSize * 2, gh.tileSize);
        attackLeft2 = scale("/player/playerPickaxeL2", gh.tileSize * 2, gh.tileSize);
        attackRight1 = scale("/player/playerPickaxeR1", gh.tileSize * 2, gh.tileSize);
        attackRight2 = scale("/player/playerPickaxeR2", gh.tileSize * 2, gh.tileSize);
    }
}
