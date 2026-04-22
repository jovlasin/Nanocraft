package com.nanocraft.game.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.object.Heart;

public class Ui {
    private GameHandler gh;
    private Font text;
    public Graphics2D g2d;
    public int commandNum, titleScreen, slotCol, slotRow;
    public BufferedImage fullHeart, halfHeart, blankHeart;
    public ArrayList<String> message = new ArrayList<>();
    public ArrayList<Integer> counter = new ArrayList<>();
    public String currentDialogue = "";
    private Utility u;

    public Ui(GameHandler gh) {
        this.gh = gh;
        this.u = new Utility(gh);
        
        try {
            InputStream is = getClass().getResourceAsStream("/font/PixelOperator.ttf");
            text = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Entity heart = new Heart(gh);
        fullHeart = heart.image;
        halfHeart = heart.image2;
        blankHeart = heart.image3;
    }

    public void draw(Graphics2D g2d) {
        this.g2d = g2d;
        g2d.setFont(text);
        g2d.setColor(Color.white);

        if (gh.gameState == gh.title) {
            drawTitleScreen();
        }

        else if (gh.gameState == gh.play) {
            drawPlayerHealth();
            drawTimeOfDay();
            drawMessage();
        }

        else if (gh.gameState == gh.pause) {
            drawPauseScreen();
        }

        else if (gh.gameState == gh.dialogue) {
            drawPlayerHealth();
            drawTimeOfDay();
            drawDialogue();
        }

        else if (gh.gameState == gh.stats) {
            drawStats();
            drawInventory();
        }
    }
    
    public void addMessage(String text) {
        message.add(text);
        counter.add(0);
    }

    private void drawTitleScreen() {
        if (titleScreen == 0) {
            g2d.setColor(new Color(0, 0, 0));
            g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);
            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 96F));
            String text = "NanoCraft";
            int x = u.getXforCenteredText(text, g2d);
            int y = gh.tileSize * 3;

            g2d.setColor(Color.darkGray);
            g2d.drawString(text, x + 5, y + 5);

            g2d.setColor(Color.white);
            g2d.drawString(text, x, y);

            x = gh.screenWidth / 2 - (gh.tileSize * 2) / 2;
            y += gh.tileSize * 2;
            g2d.drawImage(gh.player.down1, x, y, gh.tileSize * 2, gh.tileSize * 2, null);

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 40F));

            text = "NEW GAME";
            x = u.getXforCenteredText(text, g2d);
            y += gh.tileSize * 3.5;
            g2d.drawString(text, x, y);

            if (commandNum == 0) {
                g2d.drawString(">", x - gh.tileSize, y);
            }

            text = "LOAD GAME";
            x = u.getXforCenteredText(text, g2d);
            y += gh.tileSize;
            g2d.drawString(text, x, y);

            if (commandNum == 1) {
                g2d.drawString(">", x - gh.tileSize, y);
            }

            text = "QUIT";
            x = u.getXforCenteredText(text, g2d);
            y += gh.tileSize;
            g2d.drawString(text, x, y);

            if (commandNum == 2) {
                g2d.drawString(">", x - gh.tileSize, y);
            }
        }
    }

    private void drawPauseScreen() {
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 100f));
        String text = "PAUSED";
        
        int x = u.getXforCenteredText(text, g2d);
        int y = gh.screenHeight / 2;

        g2d.drawString(text, x, y);
    }

    private void drawPlayerHealth() {
        int x = gh.tileSize / 2;
        int y = gh.tileSize / 2;
        int max = gh.player.maxLife;
        int life = gh.player.life;

        for (int i = 0; i < max / 2; i++) {
            g2d.drawImage(blankHeart, x, y, null);

            int hearts = life - (i * 2);

            if (hearts >= 2) {
                g2d.drawImage(fullHeart, x, y, null);
            }

            else if (hearts == 1) {
                g2d.drawImage(halfHeart, x, y, null);
            }
            x += gh.tileSize;
        }
    }

    private void drawInventory() {
        final int frameX = gh.tileSize * 9;
        final int frameY = gh.tileSize;
        final int frameWidth = gh.tileSize * 6;
        final int frameHeight = gh.tileSize * 5;
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);

        final int slotXstart = frameX + 20;
        final int slotYstart = frameY + 20;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gh.tileSize + 3;

        for (int i = 0; i < gh.player.inventory.size(); i++) {
            if (gh.player.inventory.get(i) == gh.player.currentWeapon) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.fillRoundRect(slotX, slotY, gh.tileSize, gh.tileSize, 10, 10);
            }
            g2d.drawImage(gh.player.inventory.get(i).down1, slotX, slotY, null);
            slotX += slotSize;

            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int cursorX = slotXstart + (slotSize * slotCol);
        int cursorY = slotYstart + (slotSize * slotRow);
        int cursorWidth = gh.tileSize;
        int cursorHeight = gh.tileSize;

        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
        
        int dframeX = frameX;
        int dframeY = frameY + frameHeight;
        int dframeWidth = frameWidth;
        int dframeHeight = gh.tileSize * 3;

        int textX = dframeX + 20;
        int textY = dframeY + gh.tileSize;
        g2d.setFont(g2d.getFont().deriveFont(28F));

        int itemIndex = getItemIndexOnSlot();
        
        if (itemIndex < gh.player.inventory.size()) {
            u.drawSubWindow(dframeX, dframeY, dframeWidth, dframeHeight, g2d);
            
            for (String line: gh.player.inventory.get(itemIndex).description.split("\n")) {
                g2d.drawString(line, textX, textY);
                textY += 32;
            }
        }
    }

    private void drawStats() {
        final int frameX = gh.tileSize;
        final int frameY = gh.tileSize;
        final int frameWidth = gh.tileSize * 5;
        final int frameHeight = gh.tileSize * 9;
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);

        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(32F));

        int textX = frameX + 20;
        int textY = frameY + gh.tileSize;
        final int lineHeight = 35;

        g2d.drawString("Level", textX, textY);
        textY += lineHeight;
        g2d.drawString("Life", textX, textY);
        textY += lineHeight;
        g2d.drawString("Strength", textX, textY);
        textY += lineHeight;
        g2d.drawString("Dexterity", textX, textY);
        textY += lineHeight;
        g2d.drawString("Attack", textX, textY);
        textY += lineHeight;
        g2d.drawString("Defense", textX, textY);
        textY += lineHeight;
        g2d.drawString("Exp", textX, textY);
        textY += lineHeight;
        g2d.drawString("Next Level", textX, textY);
        textY += lineHeight;
        g2d.drawString("Coin", textX, textY);
        textY += lineHeight + 35;
        g2d.drawString("Weapon", textX, textY);

        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + gh.tileSize;
        String value;

        value = String.valueOf(gh.player.level);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.life + "/" + gh.player.maxLife);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.strength);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.dexterity);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.attack);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.defense);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.exp);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.nextLevelExp);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gh.player.coin);
        textX = u.getXforRightText(value, tailX, g2d);
        g2d.drawString(value, textX, textY);
        textY += lineHeight;

        g2d.drawImage(gh.player.currentWeapon.down1, tailX - gh.tileSize, textY , null);
        textY += gh.tileSize;
    }

    private void drawDialogue() {
        int x = gh.tileSize * 2;
        int y = gh.tileSize / 2;
        int width = gh.screenWidth - (gh.tileSize * 4);
        int height = gh.tileSize * 4;
        u.drawSubWindow(x, y, width, height, g2d);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 32f));
        x += gh.tileSize;
        y += gh.tileSize;

        for (String line: currentDialogue.split("\n")) {
            g2d.drawString(line, x, y);
            y += 40;
        }
    }

    private void drawMessage() {
        int messageX = gh.tileSize;
        int messageY = gh.tileSize * 4;
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 32F));

        for (int i = 0; i < message.size(); i++) {
            
            if (message.get(i) != null) {
                g2d.setColor(Color.black);
                g2d.drawString(message.get(i), messageX + 2, messageY + 2);
                g2d.setColor(Color.white);
                g2d.drawString(message.get(i), messageX, messageY);

                int count = counter.get(i) + 1;
                counter.set(i, count);
                messageY += 50;

                if (counter.get(i) > 180) {
                    message.remove(i);
                    counter.remove(i);
                }
            }    
        }
    }

    private void drawTimeOfDay() {
        String timeLabel = gh.getTimeLabel();
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 24F));

        int boxWidth = gh.tileSize * 4;
        int boxHeight = gh.tileSize;
        int boxX = gh.screenWidth - boxWidth - (gh.tileSize / 2);
        int boxY = gh.tileSize / 2;

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 14, 14);
        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 14, 14);

        int textX = boxX + Math.max(14, (boxWidth - g2d.getFontMetrics().stringWidth(timeLabel)) / 2);
        int textY = boxY + (boxHeight / 2) + 8;
        g2d.drawString(timeLabel, textX, textY);
    }

    public int getItemIndexOnSlot() {
        return slotCol + (slotRow * 5);
    }
}
