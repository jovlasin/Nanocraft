package core;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import entity.Player;
import input.KeyHandler;
import tile.TileHandler;
public class GameHandler extends JPanel implements Runnable {
private final int defaultTileSize = 16; // tiles are 16x16 pngs
private final int scale = 3;
private final int maxScreenCol = 16; // 16 tiles wide
private final int maxScreenRow = 12; // 12 tiles tall
public final int tileSize = defaultTileSize * scale; // scale tile to 48x48
public final int screenWidth = tileSize * maxScreenCol; // scale screen width to 768px
public final int screenHeight = tileSize * maxScreenRow; // scale screen height to 576 tall
public double fps = 60; // update the game 60 times per sec
public Thread gameThread;
public KeyHandler kh = new KeyHandler();
public Player player = new Player(this, kh);
public TileHandler th = new TileHandler(this);
public CollisionHandler ch = new CollisionHandler(this);

public GameHandler() {
this.setPreferredSize(new Dimension(screenWidth, screenHeight));
this.setBackground(Color.BLACK);
this.setDoubleBuffered(true);
this.addKeyListener(kh);
this.setFocusable(true);
}

public void startGame() {
gameThread = new Thread(this);
gameThread.start();
}

@Override
public void run() {
double drawInterval = 1000000000.0 / fps;
double delta = 0;
long time = System.nanoTime();
long currentTime;

while (gameThread != null) {
currentTime = System.nanoTime();
delta += (currentTime - time) / drawInterval;
time = currentTime;
while (delta >= 1) {
update();
repaint();
delta--;
    }
}
}

public void update() {
player.update();
}

public void paintComponent(Graphics g) {
super.paintComponent(g);
Graphics2D g2d = (Graphics2D) g;
for (int i = 0; i < th.getBelowPlayerLayerCount(); i++) {
th.drawLayer(g2d, i);
}
player.draw(g2d);
for (int i = th.getBelowPlayerLayerCount(); i < th.getLayerCount(); i++) {
th.drawLayer(g2d, i);
}
g2d.dispose();
    }
}