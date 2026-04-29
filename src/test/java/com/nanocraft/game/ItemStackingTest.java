package com.nanocraft.game;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.core.ItemStacking;
import com.nanocraft.game.core.GameHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ItemStackingTest {

    @Test
    public void testCannotStoreNullItem() {
        List<Entity> items = new ArrayList<Entity>();

        assertFalse(ItemStacking.canStore(items, 10, null));
    }

    @Test
    public void testCanStoreItemWhenCapacityAvailable() {
        List<Entity> items = new ArrayList<Entity>();
        Entity item = new TestItem(null, "apple", true, 1, 10);

        assertTrue(ItemStacking.canStore(items, 10, item));
    }

    @Test
    public void testAddStackableItemCombinesWithExistingStack() {
        List<Entity> items = new ArrayList<Entity>();

        Entity existing = new TestItem(null, "apple", true, 5, 10);
        Entity incoming = new TestItem(null, "apple", true, 3, 10);

        items.add(existing);

        boolean added = ItemStacking.addItem(items, 10, incoming);

        assertTrue(added);
        assertEquals(1, items.size());
        assertEquals(8, items.get(0).stackCount);
    }

    @Test
    public void testCannotStoreWhenNoCapacity() {
        List<Entity> items = new ArrayList<Entity>();
        items.add(new TestItem(null, "sword", false, 1, 1));

        Entity incoming = new TestItem(null, "shield", false, 1, 1);

        assertFalse(ItemStacking.canStore(items, 1, incoming));
    }

    public static class TestItem extends Entity {
        public TestItem(GameHandler gh, String id, boolean stackable, int stackCount, int maxStackSize) {
            super(gh);
            this.itemId = id;
            this.name = id;
            this.stackable = stackable;
            this.stackCount = stackCount;
            this.maxStackSize = maxStackSize;
        }

        @Override
        public String getStackKey() {
            return itemId;
        }

        @Override
        public Entity copyForStack(int count) {
            return new TestItem(gh, itemId, stackable, count, maxStackSize);
        }
    }
}