package main;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

public class GameHandler extends JPanel {
    private final int defaultTileSize = 16; // tiles are 16x16 pngs
    private final int scale = 3;
    private final int maxScreenCol = 16; // 16 tiles wide
    private final int maxScreenRow = 12; // 12 tiles tall
    private final int tileSize = defaultTileSize * scale; // scale tile to 48x48
    private final int screenWidth = tileSize * maxScreenCol; // scale screen width to 768px
    private final int screenHeight = tileSize * maxScreenRow; // scale screen height to 576 tall

    public GameHandler() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
    }

    
}
