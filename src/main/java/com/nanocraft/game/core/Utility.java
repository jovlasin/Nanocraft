package com.nanocraft.game.core;

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
}
