package com.nanocraft.game.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChestDefinition {
    public final String mapPath;
    public final int col;
    public final int row;
    public final List<String> lootItemIds;

    public ChestDefinition(String mapPath, int col, int row, List<String> lootItemIds) {
        this.mapPath = mapPath;
        this.col = col;
        this.row = row;
        this.lootItemIds = Collections.unmodifiableList(new ArrayList<>(lootItemIds == null ? List.of() : lootItemIds));
    }

    public String getKey() {
        return buildKey(mapPath, col, row);
    }

    public static String buildKey(String mapPath, int col, int row) {
        return mapPath + "#" + col + ":" + row;
    }
}
