package com.nanocraft.game.entity;

import com.nanocraft.game.core.GameHandler;

public class SleepNPC extends Entity {

    public SleepNPC(GameHandler gh) {
        super(gh);
        speed = 0;
        getImage();
        setDialogue();

        solidArea.x = 0;
        solidArea.y = 16;
        solidArea.width = 48;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    private void getImage() {
        up1 = scale("/npc/sleep_npc_up1", gh.tileSize, gh.tileSize);
        up2 = scale("/npc/sleep_npc_up2", gh.tileSize, gh.tileSize);
        down1 = scale("/npc/sleep_npc_down1", gh.tileSize, gh.tileSize);
        down2 = scale("/npc/sleep_npc_down2", gh.tileSize, gh.tileSize);
        left1 = scale("/npc/sleep_npc_left1", gh.tileSize, gh.tileSize);
        left2 = scale("/npc/sleep_npc_left2", gh.tileSize, gh.tileSize);
        right1 = scale("/npc/sleep_npc_right1", gh.tileSize, gh.tileSize);
        right2 = scale("/npc/sleep_npc_right2", gh.tileSize, gh.tileSize);
    }

    private void setDialogue() {
        dialogues[0] = "Welcome to the inn.\nRest well, traveler.";
    }

    @Override
    public void speak() {
        super.speak();
        gh.onPlayerSleep();
        gh.ui.addMessage("You rested at the inn.");
    }
}
