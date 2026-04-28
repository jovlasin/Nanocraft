package com.nanocraft.game.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.entity.Projectile;

public class Fireball extends Projectile {
    public static final int SIZE = 24;
    private static final double FIREBALL_SPEED = 4.5;

    private double preciseX;
    private double preciseY;
    private double velocityX;
    private double velocityY;

    public Fireball(GameHandler gh) {
        super(gh);

        name = "Dragon Fireball";
        maxLife = 120;
        life = maxLife;
        alive = false;

        solidArea = new Rectangle(0, 0, SIZE, SIZE);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public void setTrajectory(int startX, int startY, int targetX, int targetY, int damage) {
        double deltaX = targetX - startX;
        double deltaY = targetY - startY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance == 0) {
            distance = 1;
        }

        this.velocityX = (deltaX / distance) * FIREBALL_SPEED;
        this.velocityY = (deltaY / distance) * FIREBALL_SPEED;
        this.preciseX = startX;
        this.preciseY = startY;
        this.attack = damage;
        set(startX, startY, "down", true, null);
    }

    @Override
    public void update() {
        if (alive == false) {
            return;
        }

        preciseX += velocityX;
        preciseY += velocityY;
        worldX = (int) Math.round(preciseX);
        worldY = (int) Math.round(preciseY);

        if (hitsWall() || hitsPlayer()) {
            alive = false;
            return;
        }

        life--;
        if (life <= 0) {
            alive = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int screenX = worldX - gh.player.worldX + gh.player.screenX;
        int screenY = worldY - gh.player.worldY + gh.player.screenY;

        if (worldX + SIZE <= gh.player.worldX - gh.player.screenX ||
            worldX - SIZE >= gh.player.worldX + gh.player.screenX ||
            worldY + SIZE <= gh.player.worldY - gh.player.screenY ||
            worldY - SIZE >= gh.player.worldY + gh.player.screenY) {
            return;
        }

        g2.setColor(new Color(255, 110, 10, 220));
        g2.fillOval(screenX, screenY, SIZE, SIZE);

        g2.setColor(new Color(255, 225, 140, 180));
        g2.fillOval(screenX + 6, screenY + 6, SIZE - 12, SIZE - 12);

        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(120, 25, 0));
        g2.drawOval(screenX, screenY, SIZE, SIZE);
    }

    private boolean hitsWall() {
        int leftCol = worldX / gh.tileSize;
        int rightCol = (worldX + SIZE - 1) / gh.tileSize;
        int topRow = worldY / gh.tileSize;
        int bottomRow = (worldY + SIZE - 1) / gh.tileSize;

        return gh.th.isCollisionAt(leftCol, topRow) ||
            gh.th.isCollisionAt(rightCol, topRow) ||
            gh.th.isCollisionAt(leftCol, bottomRow) ||
            gh.th.isCollisionAt(rightCol, bottomRow);
    }

    private boolean hitsPlayer() {
        Rectangle fireballArea = new Rectangle(worldX, worldY, SIZE, SIZE);
        Rectangle playerArea = new Rectangle(
            gh.player.worldX + gh.player.solidArea.x,
            gh.player.worldY + gh.player.solidArea.y,
            gh.player.solidArea.width,
            gh.player.solidArea.height
        );

        if (fireballArea.intersects(playerArea) == false) {
            return false;
        }

        gh.player.receiveDamage(attack, 1);
        return true;
    }
}
