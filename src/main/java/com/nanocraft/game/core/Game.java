package com.nanocraft.game.core;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

public class Game {
    private final JFrame window;
    private final GameHandler gh;
    private final GraphicsDevice graphicsDevice;

    public Game() {
        window = new JFrame("NanoCraft");
        gh = new GameHandler();
        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(gh);
        gh.attachGame(this);
        if (gh.isFullScreen()) {
            applyFullScreen(true);
        } else {
            configureWindowedWindow();
        }
    }

    public void start() {
        window.setVisible(true);
        gh.requestFocusInWindow();
        gh.startGame();
    }

    public void applyFullScreen(boolean fullScreen) {
        if (fullScreen) {
            enterFullScreen();
        } else {
            exitFullScreen();
        }

        gh.requestFocusInWindow();
    }

    private void configureWindowedWindow() {
        window.pack();
        window.setLocationRelativeTo(null);
    }

    private void enterFullScreen() {
        window.dispose();
        window.setUndecorated(true);
        window.setExtendedState(JFrame.NORMAL);
        window.setVisible(true);

        if (graphicsDevice.isFullScreenSupported()) {
            graphicsDevice.setFullScreenWindow(window);
        } else {
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void exitFullScreen() {
        if (graphicsDevice.getFullScreenWindow() == window) {
            graphicsDevice.setFullScreenWindow(null);
        }

        window.dispose();
        window.setUndecorated(false);
        window.setExtendedState(JFrame.NORMAL);
        configureWindowedWindow();
        window.setVisible(true);
    }
}
