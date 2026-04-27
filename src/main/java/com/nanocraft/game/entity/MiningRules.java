package com.nanocraft.game.entity;

import com.nanocraft.game.tile.Tile;

public final class MiningRules {
    public enum Result {
        ALLOW,
        MISSING_REQUIRED_ITEM,
        INVENTORY_FULL
    }

    private MiningRules() {
    }

    public static Result evaluate(Tile tile, boolean hasRequiredItem, boolean hasInventorySpace) {
        if (tile == null) {
            return Result.ALLOW;
        }

        if (requiresItem(tile) && !hasRequiredItem) {
            return Result.MISSING_REQUIRED_ITEM;
        }

        if (dropsItem(tile) && !hasInventorySpace) {
            return Result.INVENTORY_FULL;
        }

        return Result.ALLOW;
    }

    public static String toDisplayName(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "";
        }

        String[] words = itemId.trim().split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1).toLowerCase());
            }
        }

        return builder.toString();
    }

    private static boolean requiresItem(Tile tile) {
        return tile.requiredItemType != null && !tile.requiredItemType.isBlank();
    }

    private static boolean dropsItem(Tile tile) {
        return tile.dropItemType != null && !tile.dropItemType.isBlank();
    }
}
