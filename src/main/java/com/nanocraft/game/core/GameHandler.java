package com.nanocraft.game.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JPanel;

import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.entity.Player;
import com.nanocraft.game.input.KeyHandler;
import com.nanocraft.game.tile.TileHandler;

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

    private Sound music = new Sound();
    private Sound se = new Sound();
    public CollisionHandler ch = new CollisionHandler(this);
    public Entity objs[] = new Entity[10];
    public ArrayList<Entity> entityList = new ArrayList<>();
    public AssetHandler ah = new AssetHandler(this);

    public GameHandler() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(kh);
        this.setFocusable(true);

        // TODO: Add sound files for game
        // music.load(0, "");
        // se.load(1, "");
        // se.load(2, "");
        // se.load(3, "");
        // se.load(4, "");
        // se.load(5, "");
        // se.load(6, "");
        // se.load(7, "");
        // se.load(8, "");
        // se.load(9, "");
        // se.load(10, "");
  
        // playMusic();
        ah.setObjects();
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
        entityList.add(player);

        for (int i = 0; i < th.getBelowPlayerLayerCount(); i++) {
            th.drawLayer(g2d, i);
        }

        for (int i = th.getBelowPlayerLayerCount(); i < th.getLayerCount(); i++) {
            th.drawLayer(g2d, i);
        }

        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                entityList.add(objs[i]);
            }
        }

        Collections.sort(entityList, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                int result = Integer.compare(e1.worldY, e2.worldY);
                return result;
            }
        });

        for (Entity e: entityList) {
            e.draw(g2d);
        }

        entityList.clear();
        g2d.dispose();
    }

    public void playMusic() {
        music.loop(0);
    }

    public void playSound(int i) {
        se.play(i);
    }

    public void stopSound(int i) {
        se.stop(i);
    }

    public void stopMusic() {
        music.stop(0);
    }
}
