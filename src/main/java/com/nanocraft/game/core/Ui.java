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
    private static final int INVENTORY_COLUMNS = 10;
    private static final int INVENTORY_ROWS = 3;
    private GameHandler gh;
    private Font text;
    public Graphics2D g2d;
    public int commandNum, titleScreen, slotCol, slotRow;
    public BufferedImage fullHeart, halfHeart, blankHeart;
    public ArrayList<String> message = new ArrayList<>();
    public ArrayList<Integer> counter = new ArrayList<>();
    public String currentDialogue = "";
    private boolean dialogueChoiceVisible;
    private String[] dialogueChoices = new String[0];
    private int dialogueChoiceIndex;
    private int pauseMenuIndex;
    private boolean pauseExitConfirmationVisible;
    private int pauseExitConfirmationIndex;
    private boolean inSettings;
    private int settingsIndex;
    private int gameOverIndex;
    private boolean controlMenu;
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
            drawPauseMenu();
        }

        else if (gh.gameState == gh.dialogue) {
            drawPlayerHealth();
            drawDialogue();
        }

        else if (gh.gameState == gh.inventory) {
            drawInventoryScreen();
        }

        else if (gh.gameState == gh.stats) {
            drawStatsScreen();
        }

        else if (gh.gameState == gh.chest) {
            drawPlayerHealth();
            drawChestScreen();
            drawMessage();
        }

        else if (gh.gameState == gh.gameOver) {
            drawGameOverScreen();
        }
    }

    public void addMessage(String text) {
        message.add(text);
        counter.add(0);
    }

    public void openDialogueChoice(String dialogue, String... choices) {
        currentDialogue = dialogue == null ? "" : dialogue;
        dialogueChoices = choices == null ? new String[0] : choices.clone();
        dialogueChoiceVisible = dialogueChoices.length > 0;
        dialogueChoiceIndex = 0;
    }

    public void closeDialogueChoice() {
        dialogueChoiceVisible = false;
        dialogueChoices = new String[0];
        dialogueChoiceIndex = 0;
    }

    public boolean isDialogueChoiceVisible() {
        return dialogueChoiceVisible;
    }

    public void moveDialogueChoiceSelection(int delta) {
        if (!dialogueChoiceVisible || dialogueChoices.length == 0) {
            return;
        }

        dialogueChoiceIndex = clamp(dialogueChoiceIndex + delta, 0, dialogueChoices.length - 1);
    }

    public int getDialogueChoiceIndex() {
        return dialogueChoiceIndex;
    }

    public void resetPauseMenu() {
        pauseMenuIndex = 0;
        pauseExitConfirmationVisible = false;
        pauseExitConfirmationIndex = 1;
        inSettings = false;
        settingsIndex = 0;
        controlMenu = false;
    }

    public void resetGameOverMenu() {
        gameOverIndex = 0;
    }

    public void moveGameOverSelection(int delta) {
        gameOverIndex = clamp(gameOverIndex + delta, 0, 1);
    }

    public int getGameOverSelection() {
        return gameOverIndex;
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

    public void openSettings() {
        inSettings = true;
        settingsIndex = 0;
        controlMenu = false;
    }

    public void closeSettings() {
        inSettings = false;
        settingsIndex = 0;
        controlMenu = false;
    }

    public boolean isInSettings() {
        return inSettings;
    }

    public void moveSettingsIndex(int delta) {
        settingsIndex = clamp(settingsIndex + delta, 0, 4);
    }

    public int getSettingsIndex() {
        return settingsIndex;
    }

    public void openControlMenu() {
        controlMenu = true;
    }

    public void closeControlMenu() {
        controlMenu = false;
    }

    public boolean isControlMenuVisible() {
        return controlMenu;
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

    public boolean moveChestCursor(int deltaCol, int deltaRow) {
        if (chestPanelActive) {
            int previousCol = chestSlotCol;
            int previousRow = chestSlotRow;
            chestSlotCol = clamp(chestSlotCol + deltaCol, 0, INVENTORY_COLUMNS - 1);
            chestSlotRow = clamp(chestSlotRow + deltaRow, 0, INVENTORY_ROWS - 1);
            return chestSlotCol != previousCol || chestSlotRow != previousRow;
        }

        int previousCol = playerChestSlotCol;
        int previousRow = playerChestSlotRow;
        playerChestSlotCol = clamp(playerChestSlotCol + deltaCol, 0, INVENTORY_COLUMNS - 1);
        playerChestSlotRow = clamp(playerChestSlotRow + deltaRow, 0, INVENTORY_ROWS - 1);
        return playerChestSlotCol != previousCol || playerChestSlotRow != previousRow;
    }

    public boolean moveInventoryCursor(int deltaCol, int deltaRow) {
        int previousCol = slotCol;
        int previousRow = slotRow;
        slotCol = clamp(slotCol + deltaCol, 0, INVENTORY_COLUMNS - 1);
        slotRow = clamp(slotRow + deltaRow, 0, INVENTORY_ROWS - 1);
        return slotCol != previousCol || slotRow != previousRow;
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
            int h = gh.tileSize * 4;
            int y = gh.tileSize;

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 18F));
            g2d.setColor(new Color(240, 190, 90));
            String line = "SURVIVE. MINE. FIGHT.";
            g2d.drawString(line, u.getXforCenteredText(line, g2d), y + 25);

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 58F));
            g2d.setColor(Color.white);
            String title = "NanoCraft";
            g2d.drawString(title, u.getXforCenteredText(title, g2d), y + 94);

            g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 24F));
            g2d.setColor(new Color(190, 190, 190));
            String subtitle = "A compact adventure awaits.";
            g2d.drawString(subtitle, u.getXforCenteredText(subtitle, g2d), y + 145);

            int menuY = y + h + gh.tileSize;

            String[] options = {"NEW GAME", "LOAD GAME", "SETTINGS", "QUIT"};
            int optionStartY = menuY + 40;
            int optionHeight = 70;

            for (int i = 0; i < options.length; i++) {
                drawCenteredMenuOption(options[i], optionStartY + (i * optionHeight), commandNum == i);
            }
        }

        if (inSettings) {
            drawSettingsMenu();
        }

        if (controlMenu) {
            drawControlsMenu();
        }
    }

    private void drawPauseMenu() {
        if (inSettings) {
            drawSettingsMenu();
        } else {
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

            g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 48F));
            String title = "PAUSED";
            int titleY = gh.tileSize + 20;
            g2d.setColor(Color.white);
            g2d.drawString(title, u.getXforCenteredText(title, g2d), titleY);

            int lineH = 60;
            int startY = titleY + gh.tileSize + 65;

            String[] options = {"Continue", "Save", "Load", "Settings", "Quit"};

            for (int row = 0; row < options.length; row++) {
                String label = options[row];

                g2d.setFont(text.deriveFont(Font.BOLD, 26F));
                int y = startY + row * lineH;
                int labelX = u.getXforCenteredText(label, g2d);

                boolean selected = pauseMenuIndex == row;
                g2d.setColor(selected ? new Color(240, 190, 90) : Color.white);

                if (selected) {
                    g2d.drawString(">", labelX - gh.tileSize, y);
                }

                g2d.drawString(label, labelX, y);
            }
            
            g2d.setColor(Color.white);
            g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 20F));
            String help = "ESC: Resume   ENTER/SPACE: Select";
            g2d.drawString(help, u.getXforCenteredText(help, g2d), gh.screenHeight - gh.tileSize);
        }

        if (pauseExitConfirmationVisible && !inSettings) {
            drawPauseExitConfirmation();
        }

        if (controlMenu) {
            drawControlsMenu();
        }
    }

    private void drawSettingsMenu() {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 48F));
        String head = "SETTINGS";
        int titleY = gh.tileSize + 24;
        g2d.setColor(Color.white);
        g2d.drawString(head, u.getXforCenteredText(head, g2d), titleY);

        int lineH = 60;
        int startY = titleY + gh.tileSize + 80;
        int centerX = gh.screenWidth / 2;
        int label = centerX - gh.tileSize * 5;

        for (int row = 0; row < 5; row++) {
            g2d.setFont(text.deriveFont(Font.BOLD, 26F));
            int y = startY + row * lineH;
            boolean sel = settingsIndex == row;
            g2d.setColor(sel ? new Color(240, 190, 90) : Color.white);

            if (row == 0) {
                if (sel) {
                    g2d.drawString(">", label - gh.tileSize, y);
                }
                g2d.drawString("Full screen", label, y);
                String box = gh.isFullScreen() ? "[X]" : "[  ]";
                g2d.drawString(box, centerX + gh.tileSize, y);
            } else if (row == 1) {
                if (sel) {
                    g2d.drawString(">", label - gh.tileSize, y);
                }
                g2d.setColor(Color.white);
                g2d.drawString("Music volume", label, y);
                g2d.setColor(sel ? new Color(240, 190, 90) : Color.white);
                drawVolumeSlider(centerX - 40, y - 18, gh.getMusicVolume());
            } else if (row == 2) {
                if (sel) {
                    g2d.drawString(">", label - gh.tileSize, y);
                }
                g2d.setColor(Color.white);
                g2d.drawString("Sound effects", label, y);
                g2d.setColor(sel ? new Color(240, 190, 90) : Color.white);
                drawVolumeSlider(centerX - 40, y - 18, gh.getSfxVolume());
            } else if (row == 3) {
                int scX = u.getXforCenteredText("SHOW CONTROLS", g2d);
                if (sel) {
                    g2d.setColor(new Color(240, 190, 90));
                    g2d.drawString(">", scX - gh.tileSize, y);
                }
                g2d.setColor(sel ? new Color(240, 190, 90) : Color.white);
                g2d.drawString("SHOW CONTROLS", scX, y);
            } else {
                int backX = u.getXforCenteredText("BACK", g2d);
                if (sel) {
                    g2d.setColor(new Color(240, 190, 90));
                    g2d.drawString(">", backX - gh.tileSize, y);
                }
                g2d.setColor(sel ? new Color(240, 190, 90) : Color.white);
                g2d.drawString("BACK", backX, y);
            }
        }

        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 20F));
        String help = "UP/DOWN: Move   LEFT/RIGHT: Adjust sliders   ENTER: Choose   ESC: Back";
        g2d.drawString(help, u.getXforCenteredText(help, g2d), gh.screenHeight - gh.tileSize);
    }

    private void drawVolumeSlider(int x, int y, int percent) {
        Font before = g2d.getFont();
        int w = gh.tileSize * 5;
        int h = 16;
        g2d.setColor(new Color(50, 50, 55));
        g2d.fillRoundRect(x, y, w, h, 6, 6);
        int fill = (w - 4) * percent / 100;
        g2d.setColor(new Color(200, 160, 70));
        g2d.fillRoundRect(x + 2, y + 2, Math.max(0, fill), h - 4, 4, 4);
        g2d.setColor(Color.white);
        g2d.setFont(text.deriveFont(Font.PLAIN, 20F));
        g2d.drawString(percent + "%", x + w + 10, y + 14);
        g2d.setFont(before);
    }

    private void drawControlsMenu() {
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

        int frameW = Math.min(gh.screenWidth - gh.tileSize * 2, gh.tileSize * 14);
        int frameH = gh.tileSize * 10;
        int frameX = (gh.screenWidth - frameW) / 2;
        int frameY = (gh.screenHeight - frameH) / 2;
        u.drawSubWindow(frameX, frameY, frameW, frameH, g2d);

        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 36F));
        String t = "CONTROLS";
        g2d.drawString(t, u.getXforCenteredText(t, g2d), frameY + 44);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 22F));
        int tx = frameX + 32;
        int ty = frameY + 90;
        String[] lines = {
            "Move:  W A S D  or  Arrow keys",
            "Interact:  SPACE",
            "Shoot:  F",
            "Inventory / stats:  TAB",
            "Pause:  ESC",
            "Dialogue:  SPACE to continue",
        };

        for (String line : lines) {
            g2d.drawString(line, tx, ty);
            ty += 32;
        }

        g2d.setFont(g2d.getFont().deriveFont(Font.ITALIC, 20F));
        String closeHint = "ESC or ENTER: Close";
        g2d.drawString(closeHint, u.getXforCenteredText(closeHint, g2d), frameY + frameH - 28);
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

    private void drawGameOverScreen() {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 48F));
        g2d.setColor(Color.white);
        String title = "GAME OVER";
        int titleY = gh.tileSize + 24;
        g2d.drawString(title, u.getXforCenteredText(title, g2d), titleY);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 24F));
        String subtitle = gh.hasSaveGame() ? "Retry from your last save or quit the game." : "No save found. You can quit or start a new run.";
        g2d.drawString(subtitle, u.getXforCenteredText(subtitle, g2d), titleY + 70);

        int lineH = 60;
        int startY = titleY + gh.tileSize + 150;
        String[] options = {"Retry", "Quit"};

        for (int row = 0; row < options.length; row++) {
            String label = options[row];
            g2d.setFont(text.deriveFont(Font.BOLD, 26F));
            int y = startY + row * lineH;
            int labelX = u.getXforCenteredText(label, g2d);

            boolean selected = gameOverIndex == row;
            g2d.setColor(selected ? new Color(240, 190, 90) : Color.white);

            if (selected) {
                g2d.drawString(">", labelX - gh.tileSize, y);
            }

            g2d.drawString(label, labelX, y);
        }

        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 20F));
        String help = "UP/DOWN: Move   ENTER/SPACE: Select";
        g2d.drawString(help, u.getXforCenteredText(help, g2d), gh.screenHeight - gh.tileSize);
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

    private void drawInventoryScreen() {
        drawOverlayBackdrop();

        int outerWidth = getOverlayFrameWidth();
        int outerHeight = gh.screenHeight - gh.tileSize;
        int outerX = (gh.screenWidth - outerWidth) / 2;
        int outerY = gh.tileSize / 2;
        u.drawSubWindow(outerX, outerY, outerWidth, outerHeight, g2d);

        drawScreenHeader("INVENTORY", "TAB: Stats   ESC: Close   ENTER/SPACE: Equip or use", outerX, outerY, outerWidth);

        int panelX = outerX + 24;
        int panelY = outerY + 74;
        int panelWidth = outerWidth - 48;
        int slotGap = 10;
        int horizontalPadding = (panelWidth - (INVENTORY_COLUMNS * gh.tileSize) - ((INVENTORY_COLUMNS - 1) * slotGap)) / 2;
        int panelHeight = (INVENTORY_ROWS * gh.tileSize) + ((INVENTORY_ROWS - 1) * slotGap) + (horizontalPadding * 2);
        int descriptionX = panelX;
        int descriptionY = panelY + panelHeight + 20;

        u.drawSubWindow(panelX, panelY, panelWidth, panelHeight, g2d);

        int slotXstart = panelX + horizontalPadding;
        int slotYstart = panelY + horizontalPadding;
        int slotSize = gh.tileSize + slotGap;
        int slotX = slotXstart;
        int slotY = slotYstart;

        for (int i = 0; i < gh.player.inventory.size(); i++) {
            Entity item = gh.player.inventory.get(i);
            if (item == gh.player.currentWeapon || item == gh.player.currentTool) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.fillRoundRect(slotX - 3, slotY - 3, gh.tileSize + 6, gh.tileSize + 6, 12, 12);
            }
            drawItemSlot(item, slotX, slotY);

            slotX += slotSize;
            if ((i + 1) % INVENTORY_COLUMNS == 0) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int cursorX = slotXstart + (slotSize * slotCol);
        int cursorY = slotYstart + (slotSize * slotRow);
        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cursorX - 4, cursorY - 4, gh.tileSize + 8, gh.tileSize + 8, 12, 12);

        int itemIndex = getItemIndexOnSlot();
        Entity selectedItem = itemIndex < gh.player.inventory.size() ? gh.player.inventory.get(itemIndex) : null;

        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 28F));
        g2d.drawString("Description", descriptionX + 20, descriptionY + 34);

        int detailX = descriptionX + 20;
        int detailY = descriptionY + 72;
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 26F));

        if (selectedItem != null) {
            for (String line : selectedItem.description.split("\n")) {
                g2d.drawString(line, detailX, detailY);
                detailY += 30;
            }
        } else {
            g2d.drawString("Move the cursor over an item to inspect it.", detailX, detailY);
        }
    }

    private void drawStatsScreen() {
        drawOverlayBackdrop();

        int outerWidth = getOverlayFrameWidth();
        int outerHeight = gh.screenHeight - gh.tileSize;
        int outerX = (gh.screenWidth - outerWidth) / 2;
        int outerY = gh.tileSize / 2;
        u.drawSubWindow(outerX, outerY, outerWidth, outerHeight, g2d);

        drawScreenHeader("STATS", "TAB: Inventory   ESC: Close", outerX, outerY, outerWidth);

        int leftX = outerX + 24;
        int leftY = outerY + 74;
        int leftPanelWidth = gh.tileSize * 6 + 18;
        int rightPanelX = leftX + leftPanelWidth + 24;
        int rightPanelY = leftY;
        int rightPanelWidth = outerX + outerWidth - rightPanelX - 24;

        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 28F));
        g2d.drawString("Character", leftX + 20, leftY + 34);
        g2d.drawString("Combat", rightPanelX + 20, rightPanelY + 34);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 28F));
        int labelX = leftX + 20;
        int valueX = leftX + leftPanelWidth - 24;
        int textY = leftY + 78;
        int lineHeight = 38;

        drawLabeledValue("Level", String.valueOf(gh.player.level), labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Life", gh.player.life + " / " + gh.player.maxLife, labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Strength", String.valueOf(gh.player.strength), labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Dexterity", String.valueOf(gh.player.dexterity), labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Exp", String.valueOf(gh.player.exp), labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Next Level", String.valueOf(gh.player.nextLevelExp), labelX, valueX, textY);
        textY += lineHeight;
        drawLabeledValue("Coins", String.valueOf(gh.player.coin), labelX, valueX, textY);

        int combatLabelX = rightPanelX + 20;
        int combatValueX = rightPanelX + rightPanelWidth - 24;
        int combatY = rightPanelY + 78;
        drawLabeledValue("Attack", String.valueOf(gh.player.attack), combatLabelX, combatValueX, combatY);
        combatY += lineHeight;
        drawLabeledValue("Defense", String.valueOf(gh.player.defense), combatLabelX, combatValueX, combatY);
        combatY += lineHeight + 12;
        drawLabeledValue("Weapon", getEquippedWeaponName(), combatLabelX, combatValueX, combatY);

        if (gh.player.currentWeapon != null && gh.player.currentWeapon.down1 != null) {
            int weaponBoxY = combatY + 28;
            int weaponBoxSize = gh.tileSize + 24;
            g2d.setColor(new Color(255, 255, 255, 30));
            g2d.fillRoundRect(combatLabelX, weaponBoxY, weaponBoxSize, weaponBoxSize, 12, 12);
            g2d.drawImage(gh.player.currentWeapon.down1, combatLabelX + 12, weaponBoxY + 12, null);
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

        if (dialogueChoiceVisible) {
            drawDialogueChoices(x, y + 8);
        }
    }

    private void drawDialogueChoices(int startX, int y) {
        int x = startX;

        for (int i = 0; i < dialogueChoices.length; i++) {
            String choice = dialogueChoices[i];
            String label = (dialogueChoiceIndex == i ? "> " : "  ") + choice;
            g2d.setColor(dialogueChoiceIndex == i ? new Color(240, 190, 90) : Color.white);
            g2d.drawString(label, x, y);
            x += g2d.getFontMetrics().stringWidth(label) + 36;
        }

        g2d.setColor(Color.white);
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
        drawOverlayBackdrop();

        int outerWidth = getOverlayFrameWidth();
        int outerHeight = gh.screenHeight - gh.tileSize;
        int outerX = (gh.screenWidth - outerWidth) / 2;
        int outerY = gh.tileSize / 2;
        u.drawSubWindow(outerX, outerY, outerWidth, outerHeight, g2d);

        drawScreenHeader("Chest", "TAB: Switch panel   SPACE/ENTER: Transfer item   ESC: Close", outerX, outerY, outerWidth);

        int panelX = outerX + 24;
        int panelWidth = outerWidth - 48;
        int slotGap = 10;
        int horizontalPadding = (panelWidth - (INVENTORY_COLUMNS * gh.tileSize) - ((INVENTORY_COLUMNS - 1) * slotGap)) / 2;
        int panelHeight = (INVENTORY_ROWS * gh.tileSize) + ((INVENTORY_ROWS - 1) * slotGap) + (horizontalPadding * 2);
        int topPanelY = outerY + 74;
        int bottomPanelY = outerY + outerHeight - panelHeight - 20;

        drawGridPanel(panelX, topPanelY, panelWidth, panelHeight, "", gh.activeChest == null ? List.of() : gh.activeChest.items, chestSlotCol, chestSlotRow, chestPanelActive, false, horizontalPadding, slotGap);
        drawGridPanel(panelX, bottomPanelY, panelWidth, panelHeight, "", gh.player.inventory, playerChestSlotCol, playerChestSlotRow, !chestPanelActive, true, horizontalPadding, slotGap);
    }

    private void drawGridPanel(int frameX, int frameY, int frameWidth, int frameHeight, String title, List<Entity> items,
        int cursorCol, int cursorRow, boolean active, boolean highlightEquippedWeapon, int horizontalPadding, int slotGap) {
        u.drawSubWindow(frameX, frameY, frameWidth, frameHeight, g2d);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 28F));
        g2d.drawString(title, frameX + 20, frameY + 34);

        int slotXstart = frameX + horizontalPadding;
        int slotYstart = frameY + horizontalPadding;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gh.tileSize + slotGap;

        for (int i = 0; i < items.size(); i++) {
            Entity item = items.get(i);
            if (highlightEquippedWeapon && item == gh.player.currentWeapon) {
                g2d.setColor(new Color(240, 190, 90));
                g2d.fillRoundRect(slotX - 3, slotY - 3, gh.tileSize + 6, gh.tileSize + 6, 12, 12);
            }
            drawItemSlot(item, slotX, slotY);

            slotX += slotSize;
            if ((i + 1) % INVENTORY_COLUMNS == 0) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        int cursorX = slotXstart + (slotSize * cursorCol);
        int cursorY = slotYstart + (slotSize * cursorRow);
        g2d.setColor(active ? new Color(240, 190, 90) : Color.white);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cursorX - 4, cursorY - 4, gh.tileSize + 8, gh.tileSize + 8, 12, 12);
        g2d.setColor(Color.white);
    }

    public int getItemIndexOnSlot() {
        return slotCol + (slotRow * INVENTORY_COLUMNS);
    }

    private int getItemIndexOnSlot(int slotCol, int slotRow) {
        return slotCol + (slotRow * INVENTORY_COLUMNS);
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

    private void drawCenteredMenuOption(String label, int y, boolean selected) {
        g2d.setFont(text.deriveFont(Font.BOLD, 26F));
        int labelX = u.getXforCenteredText(label, g2d);
        g2d.setColor(selected ? new Color(240, 190, 90) : Color.white);

        if (selected) {
            g2d.drawString(">", labelX - gh.tileSize, y);
        }

        g2d.drawString(label, labelX, y);
    }

    private void drawOverlayBackdrop() {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, gh.screenWidth, gh.screenHeight);
    }

    private int getOverlayFrameWidth() {
        int maxWindowedWidth = gh.tileSize * 14;
        return Math.min(gh.screenWidth - (gh.tileSize * 2), maxWindowedWidth);
    }

    private void drawScreenHeader(String title, String hint, int frameX, int frameY, int frameWidth) {
        g2d.setColor(Color.white);
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 38F));
        g2d.drawString(title, frameX + 20, frameY + 42);

        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 22F));
        int hintX = u.getXforRightText(hint, frameX + frameWidth - 20, g2d);
        g2d.drawString(hint, hintX, frameY + 38);
    }

    private void drawLabeledValue(String label, String value, int labelX, int valueX, int y) {
        g2d.drawString(label, labelX, y);
        int alignedValueX = u.getXforRightText(value, valueX, g2d);
        g2d.drawString(value, alignedValueX, y);
    }

    private String getEquippedWeaponName() {
        if (gh.player.currentWeapon == null || gh.player.currentWeapon.name == null) {
            return "None";
        }

        return gh.player.currentWeapon.name;
    }
}
