package com.nanocraft.game.monster;

import java.util.Random;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.object.Arrow;

public class Skeleton extends Entity {
    private static final int VIEW_DISTANCE_TILES = 8;
    private static final int VIEW_WIDTH_TILES = 3;
    private static final int AGGRO_LOST_DISTANCE_TILES = 12;
    private boolean aggroed;

    public Skeleton(GameHandler gh) {
        super(gh);
        type = monster;
        name = "Skeleton";
        speed = 1;
        maxLife = 10;
        life = maxLife;
        attack = 10;
        defense = 0;
        exp = 10;
        projectile = new Arrow(gh);
        shotCounter = 30;

        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        getImage();
    }

    public void getImage() {
        up1 = scale("/monster/SkeletonWalkU1", gh.tileSize, gh.tileSize);
        up2 = scale("/monster/SkeletonWalkU2", gh.tileSize, gh.tileSize);
        down1 = scale("/monster/SkeletonWalkD1", gh.tileSize, gh.tileSize);
        down2 = scale("/monster/SkeletonWalkD2", gh.tileSize, gh.tileSize);
        left1 = scale("/monster/SkeletonWalkL1", gh.tileSize, gh.tileSize);
        left2 = scale("/monster/SkeletonWalkL2", gh.tileSize, gh.tileSize);
        right1 = scale("/monster/SkeletonWalkR1", gh.tileSize, gh.tileSize);
        right2 = scale("/monster/SkeletonWalkR2", gh.tileSize, gh.tileSize);
    }

    public void setAction() {
        if (aggroed) {
            if (isPlayerTooFarToChase()) {
                aggroed = false;
                actionCounter = 0;
            } else {
                chasePlayer();
                tryShootProjectile();
                return;
            }
        }

        if (isPlayerInView()) {
            aggro();
            return;
        }

        actionCounter++;
        if (actionCounter >= 120) {
            randomWander();
            actionCounter = 0;
        }
    }

    public void aggro() {
        aggroed = true;
        actionCounter = 0;
        chasePlayer();
    }

    private boolean isPlayerInView() {
        int tileSize = gh.tileSize;
        int zombieCenterX = worldX + tileSize / 2;
        int zombieCenterY = worldY + tileSize / 2;
        int playerCenterX = gh.player.worldX + tileSize / 2;
        int playerCenterY = gh.player.worldY + tileSize / 2;

        int dx = playerCenterX - zombieCenterX;
        int dy = playerCenterY - zombieCenterY;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int maxForwardDistance = tileSize * VIEW_DISTANCE_TILES;
        int maxSideDistance = tileSize * VIEW_WIDTH_TILES;

        switch (direction) {
            case "up":
                return dy < 0 && absDy <= maxForwardDistance && absDx <= maxSideDistance;
            case "down":
                return dy > 0 && absDy <= maxForwardDistance && absDx <= maxSideDistance;
            case "left":
                return dx < 0 && absDx <= maxForwardDistance && absDy <= maxSideDistance;
            case "right":
                return dx > 0 && absDx <= maxForwardDistance && absDy <= maxSideDistance;
            default:
                return false;
        }
    }

    private boolean isPlayerTooFarToChase() {
        int tileSize = gh.tileSize;
        int dx = (gh.player.worldX - worldX) / tileSize;
        int dy = (gh.player.worldY - worldY) / tileSize;
        return (dx * dx) + (dy * dy) > AGGRO_LOST_DISTANCE_TILES * AGGRO_LOST_DISTANCE_TILES;
    }

    private void chasePlayer() {
        int dx = gh.player.worldX - worldX;
        int dy = gh.player.worldY - worldY;

        if (Math.abs(dx) > Math.abs(dy)) {
            direction = dx < 0 ? "left" : "right";
        } else {
            direction = dy < 0 ? "up" : "down";
        }
    }

    private void randomWander() {
        Random random = new Random();
        int i = random.nextInt(100) + 1;

        if (i <= 25) {
            direction = "up";
        }

        if (i > 25 && i <= 50) {
            direction = "down";
        }

        if (i > 50 && i <= 75) {
            direction = "left";
        }

        if (i > 75 && i <= 100) {
            direction = "right";
        }
    }

    private void tryShootProjectile() {
        int i = new Random().nextInt(100) + 1;
        if (i > 99 && projectile.alive == false && shotCounter == 30) {
            projectile.set(worldX, worldY, direction, true, this);
            gh.projectileList.add(projectile);
            shotCounter = 0;
        }
    }
}
