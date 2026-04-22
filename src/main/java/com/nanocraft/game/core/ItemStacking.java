package com.nanocraft.game.core;

import java.util.List;

import com.nanocraft.game.entity.Entity;

public final class ItemStacking {
    private ItemStacking() {}

    public static boolean canStore(List<Entity> items, int capacity, Entity item) {
        if (items == null || item == null || capacity <= 0) {
            return false;
        }

        int remaining = Math.max(1, item.stackCount);

        if (item.stackable) {
            for (Entity existingItem : items) {
                if (existingItem != null && existingItem.canStackWith(item)) {
                    remaining -= existingItem.getAvailableStackSpace();
                    if (remaining <= 0) {
                        return true;
                    }
                }
            }
        }

        int freeSlots = Math.max(0, capacity - items.size());
        int slotCapacity = item.stackable ? item.maxStackSize : 1;
        return remaining <= (freeSlots * slotCapacity);
    }

    public static boolean addItem(List<Entity> items, int capacity, Entity item) {
        if (!canStore(items, capacity, item)) {
            return false;
        }

        int remaining = Math.max(1, item.stackCount);

        if (item.stackable) {
            for (Entity existingItem : items) {
                if (existingItem == null || !existingItem.canStackWith(item)) {
                    continue;
                }

                int transferAmount = Math.min(existingItem.getAvailableStackSpace(), remaining);
                existingItem.stackCount += transferAmount;
                remaining -= transferAmount;

                if (remaining == 0) {
                    return true;
                }
            }
        }

        boolean usedOriginal = false;
        while (remaining > 0) {
            int entryCount = item.stackable ? Math.min(item.maxStackSize, remaining) : 1;
            Entity entry = usedOriginal ? item.copyForStack(entryCount) : item;
            entry.stackCount = entryCount;
            items.add(entry);
            remaining -= entryCount;
            usedOriginal = true;
        }

        return true;
    }
}
