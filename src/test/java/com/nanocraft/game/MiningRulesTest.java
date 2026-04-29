package com.nanocraft.game;

import com.nanocraft.game.tile.Tile;
import com.nanocraft.game.entity.MiningRules;
import org.junit.Test;

import static org.junit.Assert.*;

public class MiningRulesTest {

    @Test
    public void testNullTileIsAllowed() {
        assertEquals(MiningRules.Result.ALLOW, MiningRules.evaluate(null, false, false));
    }

    @Test
    public void testMissingRequiredItemBlocksMining() {
        Tile tile = new Tile();
        tile.requiredItemType = "pickaxe";

        assertEquals(
            MiningRules.Result.MISSING_REQUIRED_ITEM,
            MiningRules.evaluate(tile, false, true)
        );
    }

    @Test
    public void testFullInventoryBlocksMiningWhenTileDropsItem() {
        Tile tile = new Tile();
        tile.dropItemType = "ore_chunk";

        assertEquals(
            MiningRules.Result.INVENTORY_FULL,
            MiningRules.evaluate(tile, true, false)
        );
    }

    @Test
    public void testMiningAllowedWhenRequirementsMet() {
        Tile tile = new Tile();
        tile.requiredItemType = "pickaxe";
        tile.dropItemType = "ore_chunk";

        assertEquals(
            MiningRules.Result.ALLOW,
            MiningRules.evaluate(tile, true, true)
        );
    }

    @Test
    public void testDisplayNameFormatting() {
        assertEquals("Iron Pickaxe", MiningRules.toDisplayName("iron_pickaxe"));
    }
}