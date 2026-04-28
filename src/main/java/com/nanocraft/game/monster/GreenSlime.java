package com.nanocraft.game.monster;

import java.util.Random;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;

public class GreenSlime extends Entity {
    public GreenSlime(GameHandler gh) {
        super(gh);
        type = monster;
        name = "Green Slime";
        speed = 1;
        maxLife = 5;
        life = maxLife;
        attack = 4;
        defense = 0;
        exp = 2;

        solidArea.x = 3;
        solidArea.y = 28;
        solidArea.width = 42;
        solidArea.height = 14;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        getImage();
    }

    public void getImage() {
        up1 = scale("/monster/greenslime_down_1", gh.tileSize, gh.tileSize);
        up2 = scale("/monster/greenslime_down_2", gh.tileSize, gh.tileSize);
        down1 = scale("/monster/greenslime_down_1", gh.tileSize, gh.tileSize);
        down2 = scale("/monster/greenslime_down_2", gh.tileSize, gh.tileSize);
        left1 = scale("/monster/greenslime_down_1", gh.tileSize, gh.tileSize);
        left2 = scale("/monster/greenslime_down_2", gh.tileSize, gh.tileSize);
        right1 = scale("/monster/greenslime_down_1", gh.tileSize, gh.tileSize);
        right2 = scale("/monster/greenslime_down_2", gh.tileSize, gh.tileSize);
    }

    public void setAction() {
        actionCounter++;

        if (actionCounter == 120) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;
        
            if (i <= 25) {
                direction = "up";
            }

            if (i > 25 && i <= 50) {
                direction = "down";
            }

            if (i > 50 && i <= 75) {
                direction = "left";
            }

            if (i > 75 && i <= 100) {
                direction = "right";
            }
            actionCounter = 0; 
        }
    }

    public void aggro() {
        actionCounter = 0;
        direction = gh.player.direction;
    }
}
