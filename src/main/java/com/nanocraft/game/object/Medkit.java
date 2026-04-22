package com.nanocraft.game.object;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;

public class Medkit extends Entity {
    private static final int HEAL_AMOUNT = 4;

    public Medkit(GameHandler gh) {
        super(gh);
        configureStacking(true, DEFAULT_STACK_LIMIT);
        itemId = "medkit";
        type = TYPE_CONSUMABLE;
        name = "Medkit";
        down1 = scale("/object/Medkit-1", gh.tileSize, gh.tileSize);
        description = "[" + name + "]\nPacked with bandages and supplies. Heals " + HEAL_AMOUNT + " life.";
    }

    @Override
    public boolean use(Player player) {
        int restoredLife = restoreLife(player, HEAL_AMOUNT);
        if (restoredLife <= 0) {
            gh.ui.addMessage("Health is full.");
            return false;
        }

        gh.ui.addMessage("Medkit restored " + restoredLife + " life.");
        return true;
    }
}
