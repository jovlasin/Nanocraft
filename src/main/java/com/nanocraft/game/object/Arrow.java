package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Projectile;

public class Arrow extends Projectile {
    public Arrow(GameHandler gh) {
        super(gh);

        name = "Arrow";
        speed = 8;
        maxLife = 30;
        life = maxLife;
        attack = 4;
        alive = false;
        
        solidArea.x = 5;
        solidArea.y = 20;
        solidArea.width = 35;
        solidArea.height = 25;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        getImage();
    }

    private void getImage() {
        up1 = scale("/projectile/arrowU1", gh.tileSize,gh.tileSize);
        up2 = scale("/projectile/arrowU1", gh.tileSize,gh.tileSize);
        down1 = scale("/projectile/arrowD1", gh.tileSize,gh.tileSize);
        down2 = scale("/projectile/arrowD1", gh.tileSize,gh.tileSize);
        left1 = scale("/projectile/arrowL1", gh.tileSize,gh.tileSize);
        left2 = scale("/projectile/arrowL1", gh.tileSize,gh.tileSize);
        right1 = scale("/projectile/arrowR1", gh.tileSize,gh.tileSize);
        right2 = scale("/projectile/arrowR1", gh.tileSize,gh.tileSize);
    }
}
