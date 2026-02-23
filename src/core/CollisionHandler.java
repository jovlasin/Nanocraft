package core;

import entity.Entity;

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
        return 999;
    }

    public int checkEntity(Entity entity, Entity[] targets) {
        return 999;
    }

    public boolean checkPlayer(Entity entity) {
        return false;
    }
}
