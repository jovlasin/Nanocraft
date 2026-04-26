package com.nanocraft.game.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.object.Arrow;

public class SkeletonBoss extends Entity {
    private static final int DRAW_SIZE_MULTIPLIER = 3;
    private static final int CHASE_SPEED = 3;
    private static final int SHOOT_RANGE_TILES = 14;
    private static final int SHOOT_COOLDOWN_TICKS = 55;

    private final int drawSize;
    private final int horizontalDrawOffset;
    private final int verticalDrawOffset;
    private boolean defeatHandled;

    public SkeletonBoss(GameHandler gh) {
        super(gh);

        type = TYPE_MONSTER;
        name = "Skeleton Boss";
        speed = CHASE_SPEED;
        maxLife = 14;
        life = maxLife;
        attack = 5;
        defense = 0;
        exp = 10;
        dropItemType = "eye_of_ender";
        projectile = new Arrow(gh);
        projectile.attack = 5;
        projectile.speed = 10;

        drawSize = gh.tileSize * DRAW_SIZE_MULTIPLIER;
        horizontalDrawOffset = (drawSize - gh.tileSize - 74);
        verticalDrawOffset = drawSize - gh.tileSize * 3;

        getImage();

        solidArea.x = 10;
        solidArea.y = 10;
        solidArea.width = 80;
        solidArea.height = 120;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    public void update() {
        if (alive == false) {
            return;
        }

        super.update();
    }

    @Override
    public void setAction() {
        shotCounter++;

        int deltaX = (gh.player.worldX + (gh.tileSize / 2)) - (worldX + (gh.tileSize / 2));
        int deltaY = (gh.player.worldY + (gh.tileSize / 2)) - (worldY + (gh.tileSize / 2));
        int tileDistanceX = Math.abs(deltaX) / gh.tileSize;
        int tileDistanceY = Math.abs(deltaY) / gh.tileSize;
        speed = CHASE_SPEED;
        direction = resolveDirectionTowardPlayer(deltaX, deltaY);
        tryShootAtPlayer(tileDistanceX, tileDistanceY);
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        int screenX = worldX - gh.player.worldX + gh.player.screenX - horizontalDrawOffset;
        int screenY = worldY - gh.player.worldY + gh.player.screenY - verticalDrawOffset;

        if (worldX + drawSize > gh.player.worldX - gh.player.screenX &&
            worldX - drawSize < gh.player.worldX + gh.player.screenX &&
            worldY + drawSize > gh.player.worldY - gh.player.screenY &&
            worldY - drawSize < gh.player.worldY + gh.player.screenY) {

            if ("up".equals(direction)) {
                image = spriteNum == 1 ? up1 : up2;
            } else if ("down".equals(direction)) {
                image = spriteNum == 1 ? down1 : down2;
            } else if ("left".equals(direction)) {
                image = spriteNum == 1 ? left1 : left2;
            } else if ("right".equals(direction)) {
                image = spriteNum == 1 ? right1 : right2;
            }

            g2.drawImage(image, screenX, screenY, null);
        }
    }

    private void getImage() {
        up1 = scale("/monster/SkeletonWalkU1", drawSize, drawSize);
        up2 = scale("/monster/SkeletonWalkU2", drawSize, drawSize);
        down1 = scale("/monster/SkeletonWalkD1", drawSize, drawSize);
        down2 = scale("/monster/SkeletonWalkD2", drawSize, drawSize);
        left1 = scale("/monster/SkeletonWalkL1", drawSize, drawSize);
        left2 = scale("/monster/SkeletonWalkL2", drawSize, drawSize);
        right1 = scale("/monster/SkeletonWalkR1", drawSize, drawSize);
        right2 = scale("/monster/SkeletonWalkR2", drawSize, drawSize);
    }

    private void tryShootAtPlayer(int tileDistanceX, int tileDistanceY) {
        if (projectile == null || projectile.alive || shotCounter < SHOOT_COOLDOWN_TICKS) {
            return;
        }

        if (tileDistanceX > SHOOT_RANGE_TILES && tileDistanceY > SHOOT_RANGE_TILES) {
            return;
        }

        int projectileX = worldX + (gh.tileSize / 2);
        int projectileY = worldY + gh.tileSize;
        projectile.set(projectileX, projectileY, direction, true, this);
        gh.projectileList.add(projectile);
        shotCounter = 0;
    }

    @Override
    public void onDefeat() {
        gh.setSkeletonBossDefeated(true);

        if (defeatHandled == false && dropItemType != null && dropItemType.isBlank() == false) {
            gh.spawnDroppedItem(worldX, worldY, dropItemType);
            defeatHandled = true;
        }

        alive = false;
    }

    private String resolveDirectionTowardPlayer(int deltaX, int deltaY) {
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            return deltaX < 0 ? "left" : "right";
        }

        return deltaY < 0 ? "up" : "down";
    }
}
