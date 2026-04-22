package com.nanocraft.game.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.nanocraft.game.tile.Tile;

public class MiningRulesTest {
    @Test
    public void blocksMiningWithoutRequiredPickaxe() {
        Tile tile = oreTile("diamond_pickaxe", "diamond");

        MiningRules.Result result = MiningRules.evaluate(tile, false, true);

        assertEquals(MiningRules.Result.MISSING_REQUIRED_ITEM, result);
    }

    @Test
    public void allowsMiningWithPickaxeAndInventorySpace() {
        Tile tile = oreTile("diamond_pickaxe", "diamond");

        MiningRules.Result result = MiningRules.evaluate(tile, true, true);

        assertEquals(MiningRules.Result.ALLOW, result);
    }

    @Test
    public void blocksMiningWhenInventoryIsFull() {
        Tile tile = oreTile("diamond_pickaxe", "emerald");

        MiningRules.Result result = MiningRules.evaluate(tile, true, false);

        assertEquals(MiningRules.Result.INVENTORY_FULL, result);
    }

    @Test
    public void keepsOreDropIdsDistinct() {
        Tile diamondOre = oreTile("diamond_pickaxe", "diamond");
        Tile redstoneOre = oreTile("diamond_pickaxe", "redstone");
        Tile emeraldOre = oreTile("diamond_pickaxe", "emerald");

        assertEquals("diamond", diamondOre.dropItemType);
        assertEquals("redstone", redstoneOre.dropItemType);
        assertEquals("emerald", emeraldOre.dropItemType);
    }

    private Tile oreTile(String requiredItemType, String dropItemType) {
        Tile tile = new Tile();
        tile.requiredItemType = requiredItemType;
        tile.dropItemType = dropItemType;
        return tile;
    }
}
