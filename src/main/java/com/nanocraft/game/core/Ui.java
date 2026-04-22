package com.nanocraft.game.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
    private int pauseMenuIndex;
    private boolean pauseExitConfirmationVisible;
    private int pauseExitConfirmationIndex;
    private int chestSlotCol;
    private int chestSlotRow;
    private int playerChestSlotCol;
    private int playerChestSlotRow;
    private boolean chestPanelActive = true;
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
            drawMessage();
        }

        else if (gh.gameState == gh.pause) {
            drawPauseScreen();
        }

        else if (gh.gameState == gh.dialogue) {
            drawPlayerHealth();
            drawDialogue();
        }

        else if (gh.gameState == gh.stats) {
            drawStats();
            drawInventory();
        }

        else if (gh.gameState == gh.chest) {
            drawPlayerHealth();
            drawChestScreen();
            drawMessage();
        }
    }

    public void addMessage(String text) {
        message.add(text);
        counter.add(0);
    }

    public void resetPauseMenu() {
        pauseMenuIndex = 0;
        pauseExitConfirmationVisible = false;
        pauseExitConfirmationIndex = 1;
    }

    public void movePauseMenuSelection(int delta) {
        pauseMenuIndex = clamp(pauseMenuIndex + delta, 0, 4);
    }

    public int getPauseMenuSelection() {
        return pauseMenuIndex;
    }

    public void openPauseExitConfirmation() {
        pauseExitConfirmationVisible = true;
        pauseExitConfirmationIndex = 1;
    }

    public void closePauseExitConfirmation() {
        pauseExitConfirmationVisible = false;
        pauseExitConfirmationIndex = 1;
    }

    public boolean isPauseExitConfirmationVisible() {
        return pauseExitConfirmationVisible;
    }

    public void movePauseExitConfirmationSelection(int delta) {
        pauseExitConfirmationIndex = clamp(pauseExitConfirmationIndex + delta, 0, 1);
    }

    public boolean shouldExitFromPauseConfirmation() {
        return pauseExitConfirmationIndex == 1;
    }

    public void resetChestUi() {
        chestSlotCol = 0;
        chestSlotRow = 0;
        playerChestSlotCol = 0;
        playerChestSlotRow = 0;
        chestPanelActive = true;
    }

    public void toggleChestPanel() {
        chestPanelActive = !chestPanelActive;
    }

    public void moveChestCursor(int deltaCol, int deltaRow) {
        if (chestPanelActive) {
            chestSlotCol = clamp(chestSlotCol + deltaCol, 0, 4);
            chestSlotRow = clamp(chestSlotRow + deltaRow, 0, 3);
            return;
        }

        playerChestSlotCol = clamp(playerChestSlotCol + deltaCol, 0, 4);
        playerChestSlotRow = clamp(playerChestSlotRow + deltaRow, 0, 3);
    }

    public boolean isChestPanelActive() {
        return chestPanelActive;
    }

    public int getSelectedChestSlotIndex() {
        return getItemIndexOnSlot(chestSlotCol, chestSlotRow);
    }

    public int getSelectedPlayerChestSlotIndex() {
        return getItemIndexOnSlot(playerChestSlotCol, playerChestSlotRow);
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
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

        int frameWidth = gh.tileSize * 7;
        int frameHeight = gh.tileSize * 7;
        int frameX = (gh.screenWidth - frameWidth) / 2;
        int frameY = (gh.screenHeight - frameHeight) / 2;
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 48F));
        String title = "PAUSED";
        g2d.drawString(title, u.getXforCenteredText(title, g2d), frameY + gh.tileSize + 8);

        String[] options = { "CONTINUE", "SAVE", "LOAD", "EXIT" };
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 32F));

        int optionY = frameY + (gh.tileSize * 2) + 12;
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int optionX = u.getXforCenteredText(option, g2d);

            if (pauseMenuIndex == i) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.drawString(">", optionX - gh.tileSize, optionY);
            }

            g2d.setColor(Color.white);
            g2d.drawString(option, optionX, optionY);
            optionY += gh.tileSize - 2;
        }

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 20F));
        String help = "ESC: Resume   ENTER/SPACE: Select";
        g2d.drawString(help, u.getXforCenteredText(help, g2d), frameY + frameHeight - 18);

        if (pauseExitConfirmationVisible) {
            drawPauseExitConfirmation();
        }
    }

    private void drawPauseExitConfirmation() {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

        int frameWidth = gh.tileSize * 10;
        int frameHeight = gh.tileSize * 4;
        int frameX = (gh.screenWidth - frameWidth) / 2;
        int frameY = (gh.screenHeight - frameHeight) / 2;
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 28F));
        String line1 = "Exit Game?";
        g2d.drawString(line1, u.getXforCenteredText(line1, g2d), frameY + 42);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 22F));
        String line2 = "All unsaved progress will be lost if you exit now.";
        g2d.drawString(line2, u.getXforCenteredText(line2, g2d), frameY + 84);

        String[] options = { "CANCEL", "EXIT" };
        int optionY = frameY + frameHeight - 34;
        int cancelX = frameX + (gh.tileSize * 2);
        int exitX = frameX + frameWidth - (gh.tileSize * 3);

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 26F));
        for (int i = 0; i < options.length; i++) {
            int optionX = i == 0 ? cancelX : exitX;
            if (pauseExitConfirmationIndex == i) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.drawString(">", optionX - 28, optionY);
            }

            g2d.setColor(Color.white);
            g2d.drawString(options[i], optionX, optionY);
        }
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
            Entity item = gh.player.inventory.get(i);
            if (item == gh.player.currentWeapon) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.fillRoundRect(slotX, slotY, gh.tileSize, gh.tileSize, 10, 10);
            }
            drawItemSlot(item, slotX, slotY);

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

        if (gh.player.currentWeapon != null && gh.player.currentWeapon.down1 != null) {
            g2d.drawImage(gh.player.currentWeapon.down1, tailX - gh.tileSize, textY , null);
            textY += gh.tileSize;
        }
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

    private void drawChestScreen() {
        int panelY = gh.tileSize / 2;
        int panelWidth = gh.tileSize * 6;
        int panelHeight = gh.tileSize * 6;
        int leftPanelX = gh.tileSize / 2;
        int rightPanelX = gh.screenWidth - panelWidth - (gh.tileSize / 2);
        int infoY = panelY + panelHeight + (gh.tileSize / 4);

        drawGridPanel(leftPanelX, panelY, panelWidth, panelHeight, "Chest", gh.activeChest == null ? List.of() : gh.activeChest.items,
            chestSlotCol, chestSlotRow, chestPanelActive);
        drawGridPanel(rightPanelX, panelY, panelWidth, panelHeight, "Inventory", gh.player.inventory,
            playerChestSlotCol, playerChestSlotRow, !chestPanelActive);

        int infoWidth = gh.screenWidth - gh.tileSize;
        int infoHeight = gh.tileSize * 3;
        int infoX = gh.tileSize / 2;
        u.drawSubWindow(infoX, infoY, infoWidth, infoHeight, g2d);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 24F));
        int textX = infoX + 20;
        int textY = infoY + 36;
        g2d.drawString("TAB: Switch panel   SPACE/ENTER: Transfer item   ESC: Close chest", textX, textY);
        textY += 36;

        Entity selectedItem = getSelectedChestScreenItem();
        if (selectedItem != null) {
            for (String line : selectedItem.description.split("\n")) {
                g2d.drawString(line, textX, textY);
                textY += 28;
            }
            return;
        }

        g2d.drawString("Select an item to move it between the chest and your inventory.", textX, textY);
    }

    private void drawGridPanel(int frameX, int frameY, int frameWidth, int frameHeight, String title, List<Entity> items,
        int cursorCol, int cursorRow, boolean active) {
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 28F));
        g2d.drawString(title, frameX + 20, frameY + 34);

        int slotXstart = frameX + 20;
        int slotYstart = frameY + 52;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gh.tileSize + 3;

        for (int i = 0; i < items.size(); i++) {
            Entity item = items.get(i);
            drawItemSlot(item, slotX, slotY);

            slotX += slotSize;
            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int cursorX = slotXstart + (slotSize * cursorCol);
        int cursorY = slotYstart + (slotSize * cursorRow);
        g2d.setColor(active ? new Color(240, 190, 90) : Color.white);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cursorX, cursorY, gh.tileSize, gh.tileSize, 10, 10);
        g2d.setColor(Color.white);
    }

    private Entity getSelectedChestScreenItem() {
        if (chestPanelActive) {
            if (gh.activeChest == null) {
                return null;
            }

            int itemIndex = getSelectedChestSlotIndex();
            if (itemIndex < gh.activeChest.items.size()) {
                return gh.activeChest.items.get(itemIndex);
            }
            return null;
        }

        int itemIndex = getSelectedPlayerChestSlotIndex();
        if (itemIndex < gh.player.inventory.size()) {
            return gh.player.inventory.get(itemIndex);
        }

        return null;
    }

    public int getItemIndexOnSlot() {
        return getItemIndexOnSlot(slotCol, slotRow);
    }

    private int getItemIndexOnSlot(int slotCol, int slotRow) {
        return slotCol + (slotRow * 5);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void drawItemSlot(Entity item, int slotX, int slotY) {
        if (item == null) {
            return;
        }

        if (item.down1 != null) {
            g2d.drawImage(item.down1, slotX, slotY, null);
        }

        if (item.stackCount > 1) {
            drawStackCountBadge(item.stackCount, slotX, slotY);
        }
    }

    private void drawStackCountBadge(int count, int slotX, int slotY) {
        String countText = String.valueOf(count);
        Font originalFont = g2d.getFont();
        Font badgeFont = originalFont.deriveFont(Font.BOLD, 18F);
        g2d.setFont(badgeFont);

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(countText);
        int badgeWidth = textWidth + 10;
        int badgeHeight = metrics.getAscent() + 4;
        int badgeX = slotX + gh.tileSize - badgeWidth - 2;
        int badgeY = slotY + gh.tileSize - badgeHeight - 2;
        int textX = badgeX + 5;
        int textY = badgeY + metrics.getAscent() - 1;

        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);
        g2d.setColor(Color.white);
        g2d.drawString(countText, textX, textY);

        g2d.setFont(originalFont);
        g2d.setColor(Color.white);
    }
}

