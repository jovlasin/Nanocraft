package com.nanocraft.game.entity;

import com.nanocraft.game.core.GameHandler;

public class Projectile extends Entity {
    public Entity user;

    public Projectile(GameHandler gh) {
        super(gh);
    }

    public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.direction = direction;
        this.alive = alive;
        this.user = user;
        this.life = this.maxLife;
    }

    public void update() {
        int index = gh.ch.checkEntity(this, gh.monsters);

        if (index != 999) {
            gh.player.damage(index, attack);
            alive = false;
        }

        switch (direction) {
            case "up":
                worldY -= speed;
            break;

            case "down":
                worldY += speed;
            break;

            case "left":
                worldX -= speed;
            break;

            case "right":
                worldX += speed;
            break;
        }
        life--;
        
        if (life <= 0) {
            alive = false;
        }
        
        spriteCounter++;

        if (spriteCounter > 12) {
            if (spriteNum == 1) {
                spriteNum = 2;
            }

            else if (spriteNum == 2) {
                spriteNum = 1;
            }
            spriteCounter = 0;
        } 
    }
}
