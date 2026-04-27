package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;

public class Apple extends Entity {
    private static final int HEAL_AMOUNT = 1;

    public Apple(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        itemId = "apple";
        type = TYPE_CONSUMABLE;
        name = "Apple";
        down1 = scale("/object/Apple-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nA fresh apple. Heals " + HEAL_AMOUNT + " life.";
    }

    @Override
    public boolean use(Player player) {
        int restoredLife = restoreLife(player, HEAL_AMOUNT);
        if (restoredLife <= 0) {
            gh.ui.addMessage("Health is full. " + healthStatus(player));
            return false;
        }

        gh.ui.addMessage("You ate an Apple. " + healthStatus(player));
        return true;
    }
}
