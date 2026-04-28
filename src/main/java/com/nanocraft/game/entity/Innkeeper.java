package com.nanocraft.game.entity;

import com.nanocraft.game.core.GameHandler;

public class Innkeeper extends Entity {
    public Innkeeper(GameHandler gh) {
        super(gh);
        name = "Innkeeper";
        speed = 0;
        type = npc;
        getImage();

        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    private void getImage() {
        down1 = scale("/npc/innkeeper", gh.tileSize, gh.tileSize);
    }

    @Override
    public void speak() {
        openSleepPrompt();
    }

    @Override
    public void update() {
        setAction();

        collisionOn = false;
        gh.ch.checkTile(this);
        gh.ch.checkObject(this, false);
        gh.ch.checkEntity(this, gh.npcs);
        gh.ch.checkEntity(this, gh.monsters);
        boolean contact = gh.ch.checkPlayer(this);

        if (type == monster && contact == true) {
            damage(attack);
        }

        if (collisionOn == false) {
            switch (direction) {
                case "up":
                    worldY -= speed;
                break;
            
                case "down":
                    worldY += speed;
                break;

                case "left":
                    worldX -= speed;
                break;

                case "right":
                    worldX += speed;
                break;
            }
        }
    }

    public void openSleepPrompt() {
        gh.gameState = gh.dialogue;
        gh.ui.openDialogueChoice("Want to rest?\nSleeping restores the world.", "Yes", "No");
    }

    public boolean isSleepPromptVisible() {
        return gh.ui.isDialogueChoiceVisible();
    }

    public void moveSleepPromptSelection(int delta) {
        gh.ui.moveDialogueChoiceSelection(delta);
    }

    public void confirmSleepPromptSelection() {
        if (!gh.ui.isDialogueChoiceVisible()) {
            closeDialogue();
            return;
        }

        if (gh.ui.getDialogueChoiceIndex() == 0) {
            gh.onPlayerSleep();
            return;
        }

        closeDialogue();
    }

    public void closeDialogue() {
        gh.ui.closeDialogueChoice();
        gh.ui.currentDialogue = "";
        gh.gameState = gh.play;
    }
}
