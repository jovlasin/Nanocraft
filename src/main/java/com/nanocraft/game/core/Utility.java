package com.nanocraft.game.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Utility {
    private GameHandler gh;

    public Utility(GameHandler gh) {
        this.gh = gh;
    }

    public int getXforCenteredText(String text, Graphics2D g2d) {
        int length = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int x = gh.screenWidth / 2 - length / 2;
        return x;
    }

    public int getXforRightText(String text, int tailX, Graphics2D g2d) {
        int length = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        int x = tailX - length;
        return x;
    }

    public void drawSubWindow(int x, int y, int width, int height, Graphics2D g2d) {
        Color c = new Color(0, 0, 0, 215);
        g2d.setColor(c);
        g2d.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255, 255, 255);
        g2d.setColor(c);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }
}
