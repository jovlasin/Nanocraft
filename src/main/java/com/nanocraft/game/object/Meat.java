package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;

public class Meat extends Entity {
    private static final int HEAL_AMOUNT = 2;

    public Meat(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        itemId = "meat";
        type = TYPE_CONSUMABLE;
        name = "Meat";
        down1 = scale("/object/Meat-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA hearty meal. Heals " + HEAL_AMOUNT + " life.";
    }

    @Override
    public boolean use(Player player) {
        int restoredLife = restoreLife(player, HEAL_AMOUNT);
        if (restoredLife <= 0) {
            gh.ui.addMessage("Health is full. " + healthStatus(player));
            return false;
        }

        gh.ui.addMessage("You ate Meat. " + healthStatus(player));
        return true;
    }
}
