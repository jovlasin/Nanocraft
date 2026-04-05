package com.nanocraft.game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.nanocraft.game.core.GameHandler;

public class KeyHandler implements KeyListener {
    public boolean up, down, left, right, space;
    private GameHandler gh;

    public KeyHandler(GameHandler gh) {
        this.gh = gh;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (gh.gameState == gh.title) {
            titleState(code);
        }

        if (gh.gameState == gh.play) {
            playState(code);
        }

        // else if (gh.gameState == gh.pause) {
        //     if (code == KeyEvent.VK_P) {
        //         gh.gameState = gh.play;
        //     }
        // }

        // else if (gh.gameState == gh.dialogue) {
        //     if (code == KeyEvent.VK_SPACE) {
        //         gh.gameState = gh.play;
        //     }
        // }

        // else if (gh.gameState == gh.stats) { 
        //     statsState(code);
        // }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP) {
            up = false;
        }

        if (code == KeyEvent.VK_DOWN) {
            down = false;
        }

        if (code == KeyEvent.VK_LEFT) {
            left = false;
        }

        if (code == KeyEvent.VK_RIGHT) {
            right = false;
        }
    }

    private void titleState(int code) {
        if (gh.ui.titleScreen == 0) {
            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                gh.ui.commandNum--;

                if (gh.ui.commandNum < 0) {
                    gh.ui.commandNum = 2;
                }
            }

            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                gh.ui.commandNum++;

                if (gh.ui.commandNum > 2) {
                    gh.ui.commandNum = 0;
                }
            }

            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                if (gh.ui.commandNum == 0) {
                    gh.ui.titleScreen = 1;
                    gh.gameState = gh.play;
                    gh.playMusic();
                }

                else if (gh.ui.commandNum == 1) {
                    // TODO
                }

                else if (gh.ui.commandNum == 2) {
                    System.exit(0);
                }
            }   
        }
    }

    private void playState(int code) {
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            up = true;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            down = true;
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            left = true;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            right = true;
        }

        if (code == KeyEvent.VK_P) {
            gh.gameState = gh.pause;   
        }

        if (code == KeyEvent.VK_SPACE) {
            space = true;
        }

        if (code == KeyEvent.VK_TAB) {
            gh.gameState = gh.stats;
        }

        // if (code == KeyEvent.VK_F) {
        //     shootPressed = true;
        // }
    }

    // private void pauseState(int code) {

    // }

    // private void dialogueState(int code) {

    // }

    // private void statsState(int code) {
        
    // }
}
