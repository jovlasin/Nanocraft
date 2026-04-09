package com.nanocraft.game.entity;

import java.util.Random;
import com.nanocraft.game.core.GameHandler;

public class Elder extends Entity {
    public Elder(GameHandler gh) {
        super(gh);
        speed = 1;
        getImage();
        setDialogue();

        solidArea.x = 0;
        solidArea.y = 16;
        solidArea.width = 48;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public void getImage() {
        up1 = scale("/npc/elder_up_1", gh.tileSize, gh.tileSize);
        up2 = scale("/npc/elder_up_2", gh.tileSize, gh.tileSize);
        down1 = scale("/npc/elder_down_1", gh.tileSize, gh.tileSize);
        down2 = scale("/npc/elder_down_2", gh.tileSize, gh.tileSize);
        left1 = scale("/npc/elder_left_1", gh.tileSize, gh.tileSize);
        left2 = scale("/npc/elder_left_2", gh.tileSize, gh.tileSize);
        right1 = scale("/npc/elder_right_1", gh.tileSize, gh.tileSize);
        right2 = scale("/npc/elder_right_2", gh.tileSize, gh.tileSize);
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

    private void setDialogue() {
        dialogues[0] = "Hello, lad.";
        dialogues[1] = "So you've come to this island to find \nthe end?";
        dialogues[2] = "I used to be a great wizard but now... \nI'm just an old man too old to adventure.";
        dialogues[3] = "Well, good luck to you!";
    }

    public void speak() {
        super.speak();
    }
}
