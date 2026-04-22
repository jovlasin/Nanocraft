package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class Heart extends Entity {
    public Heart(GameHandler gm) {
        super(gm);
        name = "Heart";
        image = scale("/object/heart", gm.tileSize, gm.tileSize);
        image2 = scale("/object/halfHeart", gm.tileSize, gm.tileSize);
        image3 = scale("/object/blankHeart", gm.tileSize, gm.tileSize);
    }
}
