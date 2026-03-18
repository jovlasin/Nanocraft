package com.nanocraft.game.core;

import javax.swing.JFrame;

public class Game {
    private JFrame window;
    private GameHandler gh;

    public Game() {
        window = new JFrame("NanoCraft");
        gh = new GameHandler();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(gh);
        window.pack();
        window.setLocationRelativeTo(null);
    }

    public void start() {
        window.setVisible(true);
        gh.startGame();
    }
}
