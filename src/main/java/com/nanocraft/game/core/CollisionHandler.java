package com.nanocraft.game.core;

import com.nanocraft.game.entity.Entity;

public class CollisionHandler {
    private final GameHandler gh;

    public CollisionHandler(GameHandler gh) {
        this.gh = gh;
    }

    public void checkTile(Entity entity) {
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;

        int entityTopRow = entityTopWorldY / gh.tileSize;
        int entityBottomRow = entityBottomWorldY / gh.tileSize;
        int entityLeftCol = entityLeftWorldX / gh.tileSize;
        int entityRightCol = entityRightWorldX / gh.tileSize;

        switch (entity.direction) {
            case "up":
                entityTopRow = (entityTopWorldY - entity.speed) / gh.tileSize;
                if (gh.th.isCollisionAt(entityLeftCol, entityTopRow) ||
                    gh.th.isCollisionAt(entityRightCol, entityTopRow)) {
                    entity.collisionOn = true;
                }
            break;

            case "down":
                entityBottomRow = (entityBottomWorldY + entity.speed) / gh.tileSize;
                if (gh.th.isCollisionAt(entityLeftCol, entityBottomRow) ||
                    gh.th.isCollisionAt(entityRightCol, entityBottomRow)) {
                    entity.collisionOn = true;
                }
            break;

            case "left":
                entityLeftCol = (entityLeftWorldX - entity.speed) / gh.tileSize;
                if (gh.th.isCollisionAt(entityLeftCol, entityTopRow) ||
                    gh.th.isCollisionAt(entityLeftCol, entityBottomRow)) {
                    entity.collisionOn = true;
                }
            break;

            case "right":
                entityRightCol = (entityRightWorldX + entity.speed) / gh.tileSize;
                if (gh.th.isCollisionAt(entityRightCol, entityTopRow) ||
                    gh.th.isCollisionAt(entityRightCol, entityBottomRow)) {
                    entity.collisionOn = true;
                }
            break;
        }
    }

    public int checkObject(Entity entity, boolean player) {
        int index = 999;
        int i = 0;

        for (Entity object: gh.objs) {
            if (object != null) {
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                object.solidArea.x = object.worldX + object.solidArea.x;
                object.solidArea.y = object.worldY + object.solidArea.y;

                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                    break;

                    case "down":
                        entity.solidArea.y += entity.speed;
                    break;

                    case "left":
                        entity.solidArea.x -= entity.speed;
                    break;

                    case "right":
                        entity.solidArea.x += entity.speed;
                    break;
                }

                if (entity.solidArea.intersects(object.solidArea)) {
                    if (object.collision == true) {
                        entity.collisionOn = true;
                    }

                    if (player == true) {
                        index = i;
                    }
                }

                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                object.solidArea.x = object.solidAreaDefaultX;
                object.solidArea.y = object.solidAreaDefaultY;
            }
            i++;
        }
        return index;
    }

    public int checkEntity(Entity entity, Entity[] targets) {
        int index = 999;
        int i = 0;

        for (Entity target: targets) {
            if (target != null) {
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                target.solidArea.x = target.worldX + target.solidArea.x;
                target.solidArea.y = target.worldY + target.solidArea.y;

                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                    break;

                    case "down":
                        entity.solidArea.y += entity.speed;
                    break;

                    case "left":
                        entity.solidArea.x -= entity.speed;
                    break;

                    case "right":
                        entity.solidArea.x += entity.speed;
                    break;
                }

                if (entity.solidArea.intersects(target.solidArea)) {
                    if (target != entity) {
                        entity.collisionOn = true;
                        index = i;
                    }
                }

                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                target.solidArea.x = target.solidAreaDefaultX;
                target.solidArea.y = target.solidAreaDefaultY;
            }
            i++;
        }
        return index;
    }

    public boolean checkPlayer(Entity entity) {
        boolean contactPlayer = false;
        entity.solidArea.x = entity.worldX + entity.solidArea.x;
        entity.solidArea.y = entity.worldY + entity.solidArea.y;

        gh.player.solidArea.x = gh.player.worldX + gh.player.solidArea.x;
        gh.player.solidArea.y = gh.player.worldY + gh.player.solidArea.y;

        switch (entity.direction) {
            case "up":
                entity.solidArea.y -= entity.speed;
            break;

            case "down":
                entity.solidArea.y += entity.speed;
            break;

            case "left":
                entity.solidArea.x -= entity.speed;
            break;

            case "right":
                entity.solidArea.x += entity.speed;
            break;
        }

        if (entity.solidArea.intersects(gh.player.solidArea)) {
            entity.collisionOn = true;
            contactPlayer = true;
        }

        entity.solidArea.x = entity.solidAreaDefaultX;
        entity.solidArea.y = entity.solidAreaDefaultY;
        gh.player.solidArea.x = gh.player.solidAreaDefaultX;
        gh.player.solidArea.y = gh.player.solidAreaDefaultY;

        return contactPlayer;
    }
}
