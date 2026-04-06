package com.nanocraft.game.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.InputStream;

public class Ui {
    private GameHandler gh;
    private Font text;
    public Graphics2D g2d;
    public int commandNum, titleScreen, slotCol, slotRow;
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
    }

    public void draw(Graphics2D g2d) {
        this.g2d = g2d;
        g2d.setFont(text);
        g2d.setColor(Color.white);

        if (gh.gameState == gh.title) {
            drawTitleScreen();
        }

        else if (gh.gameState == gh.pause) {
            drawPauseScreen();
        }
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
}
