package com.nanocraft.game.entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.tile.ResourceLoader;

public class BronzeDragon extends Entity {
    private static final int MAX_LIFE = 36;
    private static final int FRAME_COUNT = 4;
    private static final int SOURCE_FRAME_SIZE = 16;
    private static final int DRAW_WIDTH_TILES = 3;
    private static final int DRAW_HEIGHT_TILES = 3;
    private static final int ANIMATION_INTERVAL_TICKS = 10;
    private static final int SHOOT_INTERVAL_TICKS = 75;
    private static final int INVINCIBLE_DURATION_TICKS = 18;
    private static final int DYING_DURATION_TICKS = 45;

    private final BufferedImage[] frames;
    private final int drawWidth;
    private final int drawHeight;

    private int animationCounter;
    private int animationFrame;
    private int shootCounter;
    private int dyingCounter;
    private boolean defeatHandled;

    public BronzeDragon(GameHandler gh) {
        super(gh);

        this.frames = new BufferedImage[FRAME_COUNT];
        this.drawWidth = gh.tileSize * DRAW_WIDTH_TILES;
        this.drawHeight = gh.tileSize * DRAW_HEIGHT_TILES;

        type = monster;
        name = "Bronze Dragon";
        speed = 0;
        maxLife = MAX_LIFE;
        life = maxLife;
        attack = 5;
        defense = 0;
        exp = 20;
        direction = "left";

        solidArea.x = gh.tileSize / 2;
        solidArea.y = gh.tileSize;
        solidArea.width = gh.tileSize * 2;
        solidArea.height = gh.tileSize * 2;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        loadFrames();
    }

    @Override
    public void update() {
        if (alive == false) {
            return;
        }

        updateInvincibility();

        if (dying == true) {
            updateDyingState();
            return;
        }

        updateFacing();
        updateAnimation();
        updateAttack();
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gh.player.worldX + gh.player.screenX;
        int screenY = worldY - gh.player.worldY + gh.player.screenY;

        if (worldX + drawWidth <= gh.player.worldX - gh.player.screenX ||
            worldX - drawWidth >= gh.player.worldX + gh.player.screenX ||
            worldY + drawHeight <= gh.player.worldY - gh.player.screenY ||
            worldY - drawHeight >= gh.player.worldY + gh.player.screenY) {
            return;
        }

        BufferedImage currentFrame = frames[animationFrame];
        float alpha = 1f;

        if (dying == true) {
            alpha = (dyingCounter / 4) % 2 == 0 ? 0.25f : 0.75f;
        } else if (invincible == true) {
            alpha = 0.6f;
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if ("right".equals(direction)) {
            g2.drawImage(currentFrame, screenX + drawWidth, screenY, -drawWidth, drawHeight, null);
        } else {
            g2.drawImage(currentFrame, screenX, screenY, null);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void loadFrames() {
        try {
            BufferedImage spriteSheet = ImageIO.read(getClass().getResourceAsStream("/boss/MatureBronzeDragon.png"));
            if (spriteSheet == null) {
                throw new IOException("Missing bronze dragon sprite sheet");
            }

            for (int i = 0; i < FRAME_COUNT; i++) {
                BufferedImage frame = spriteSheet.getSubimage(i * SOURCE_FRAME_SIZE, 0, SOURCE_FRAME_SIZE, SOURCE_FRAME_SIZE);
                frames[i] = ResourceLoader.scaleImage(frame, drawWidth, drawHeight);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load bronze dragon sprite sheet", e);
        }
    }

    private void updateFacing() {
        int playerCenterX = gh.player.worldX + gh.player.solidArea.x + (gh.player.solidArea.width / 2);
        int dragonCenterX = worldX + (drawWidth / 2);
        direction = playerCenterX >= dragonCenterX ? "right" : "left";
    }

    private void updateAnimation() {
        animationCounter++;
        if (animationCounter < ANIMATION_INTERVAL_TICKS) {
            return;
        }

        animationFrame++;
        if (animationFrame >= FRAME_COUNT) {
            animationFrame = 0;
        }
        animationCounter = 0;
    }

    private void updateAttack() {
        shootCounter++;
        if (shootCounter < SHOOT_INTERVAL_TICKS) {
            return;
        }

        shootCounter = 0;
        DragonFireball fireball = new DragonFireball(gh);
        int startX = worldX + (drawWidth / 2) - (DragonFireball.SIZE / 2);
        int startY = worldY + gh.tileSize + (gh.tileSize / 4);
        int targetX = gh.player.worldX + gh.player.solidArea.x + (gh.player.solidArea.width / 2) - (DragonFireball.SIZE / 2);
        int targetY = gh.player.worldY + gh.player.solidArea.y + (gh.player.solidArea.height / 2) - (DragonFireball.SIZE / 2);

        fireball.setTrajectory(startX, startY, targetX, targetY, attack);
        gh.projectileList.add(fireball);
    }

    private void updateInvincibility() {
        if (invincible == false) {
            return;
        }

        invincibleCounter++;
        if (invincibleCounter > INVINCIBLE_DURATION_TICKS) {
            invincible = false;
            invincibleCounter = 0;
        }
    }

    private void updateDyingState() {
        if (defeatHandled == false) {
            defeatHandled = true;
            gh.handleBronzeDragonDefeat();
        }

        dyingCounter++;
        if (dyingCounter > DYING_DURATION_TICKS) {
            alive = false;
        }
    }
}
