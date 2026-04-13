package com.nanocraft.game.core;

import java.util.ArrayList;
import java.util.List;

import com.nanocraft.game.entity.Entity;

public class ChestState {
    public static final int CAPACITY = 20;

    public final String mapPath;
    public final int col;
    public final int row;
    public final List<Entity> items;
    public boolean opened;

    public ChestState(String mapPath, int col, int row) {
        this.mapPath = mapPath;
        this.col = col;
        this.row = row;
        this.items = new ArrayList<>();
    }

    public boolean addItem(Entity item) {
        if (item == null || isFull()) {
            return false;
        }

        items.add(item);
        return true;
    }

    public Entity removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }

        return items.remove(index);
    }

    public boolean isFull() {
        return items.size() >= CAPACITY;
    }

    public String getKey() {
        return buildKey(mapPath, col, row);
    }

    public static String buildKey(String mapPath, int col, int row) {
        return mapPath + "#" + col + ":" + row;
    }
}
