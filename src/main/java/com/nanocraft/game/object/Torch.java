package com.nanocraft.game.object;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.tile.ResourceLoader;

public class Torch extends Entity {
    private static final int TILE_SOURCE_SIZE = 16;
    private static final int TORCH_TILE_COL = 2;
    private static final int TORCH_TILE_ROW = 2;

    public Torch(GameHandler gh) {
        super(gh);
        configureStacking(false, 1);

        itemId = "torch";
        type = TYPE_TOOL;
        name = "Torch";
        down1 = loadTorchIcon();
        description = "[" + name + "]\nEquip it to see better at night and in caves.";
    }

    private BufferedImage loadTorchIcon() {
        try {
            BufferedImage sheet = ImageIO.read(getClass().getResourceAsStream("/tile/objects_demo.png"));
            if (sheet == null) {
                throw new IOException("Missing objects_demo tilesheet");
            }

            BufferedImage tile = sheet.getSubimage(
                TORCH_TILE_COL * TILE_SOURCE_SIZE,
                TORCH_TILE_ROW * TILE_SOURCE_SIZE,
                TILE_SOURCE_SIZE,
                TILE_SOURCE_SIZE
            );
            return ResourceLoader.scaleImage(tile, gh.tileSize, gh.tileSize);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load torch icon", e);
        }
    }
}
