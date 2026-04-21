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
import com.nanocraft.game.object.Key;
import com.nanocraft.game.object.OreChunk;
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
    public KeyHandler kh = new KeyHandler(this);
    public Player player = new Player(this, kh);
    public TileHandler th = new TileHandler(this);
    public CollisionHandler ch = new CollisionHandler(this);
    public Entity objs[] = new Entity[10];
    public Entity npcs[] = new Entity[10];
    public Entity monsters[] = new Entity[20];
    public ArrayList<Entity> entityList = new ArrayList<>();
    public AssetHandler ah = new AssetHandler(this);
    public Ui ui = new Ui(this);
    public ArrayList<Entity> projectileList = new ArrayList<>();
    public Utility u = new Utility(this);
    private boolean bronzeDragonDefeated;

    public final int title = 0;
    public final int pause = 1;
    public final int play = 2;
    public final int dialogue = 2;
    public final int stats = 4;
    public int gameState = 999;

    public GameHandler() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.addKeyListener(kh);
        ah.setObjects();
        ah.setNPCS();
        ah.setMonsters();

        gameState = title;
        // gameState = play;
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
        if (gameState == play) {
            player.update();

            for (int i = 0; i < npcs.length; i++) {
                if (npcs[i] != null) {
                    npcs[i].update();
                }
            }

            for (int i = 0; i < monsters.length; i++) {
                if (monsters[i] != null) {
                    monsters[i].update();

                    if (monsters[i].alive == false) {
                        monsters[i] = null;
                    }
                }
            }

            for (int i = projectileList.size() - 1; i >= 0; i--) {
                Entity projectile = projectileList.get(i);

                if (projectile == null || projectile.alive == false) {
                    projectileList.remove(i);
                    continue;
                }

                projectile.update();

                if (projectile.alive == false) {
                    projectileList.remove(i);
                }
            }
        }

        if (gameState == pause) {
            // nothing
        }
    }

    public void spawnDroppedItem(int worldX, int worldY, String itemType) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] != null) {
                continue;
            }

            Entity droppedItem = createDropEntity(itemType);
            if (droppedItem == null) {
                return;
            }

            droppedItem.worldX = worldX;
            droppedItem.worldY = worldY;
            objs[i] = droppedItem;
            return;
        }

        System.out.println("No free world object slot for dropped item: " + itemType);
    }

    private Entity createDropEntity(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return null;
        }

        String normalized = itemType.trim().toLowerCase();
        switch (normalized) {
            case "ore_chunk":
            case "orechunk":
            case "ore":
                return new OreChunk(this);

            case "key":
                return new Key(this);

            default:
                return new OreChunk(this);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameState == title) {
            ui.draw(g2d);
        }

        else {
            entityList.add(player);

            for (int i = 0; i < th.getBelowPlayerLayerCount(); i++) {
                th.drawLayer(g2d, i);
            }

            for (int i = th.getBelowPlayerLayerCount(); i < th.getLayerCount(); i++) {
                th.drawLayer(g2d, i);
            }

            for (int i = 0; i < npcs.length; i++) {
                if (npcs[i] != null) {
                    entityList.add(npcs[i]);
                }
            }

            for (int i = 0; i < monsters.length; i++) {
                if (monsters[i] != null) {
                    entityList.add(monsters[i]);
                }
            }

            for (int i = 0; i < objs.length; i++) {
                if (objs[i] != null) {
                    entityList.add(objs[i]);
                }
            }

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null) {
                    entityList.add(projectileList.get(i));
                }
            }

            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    int e1Depth = e1.worldY + e1.solidArea.y + e1.solidArea.height;
                    int e2Depth = e2.worldY + e2.solidArea.y + e2.solidArea.height;
                    int result = Integer.compare(e1Depth, e2Depth);
                    return result;
                }
            });

            for (Entity e: entityList) {
                e.draw(g2d);
            }

            entityList.clear();
            ui.draw(g2d);
        }
        
        g2d.dispose();
    }

    public void playMusic() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playMusic'");
    }

    public void playSound(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'playSound'");
    }

    public void clearProjectiles() {
        projectileList.clear();
    }

    public boolean isBronzeDragonDefeated() {
        return bronzeDragonDefeated;
    }

    public void setBronzeDragonDefeated(boolean bronzeDragonDefeated) {
        this.bronzeDragonDefeated = bronzeDragonDefeated;
    }
}
