package core;

import javax.swing.JFrame;

public class Game {
    public void run() {
        JFrame window = new JFrame();
        GameHandler gh = new GameHandler();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("NanoCraft");
        window.add(gh);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        gh.startGame();
    }
}
