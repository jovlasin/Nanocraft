package com.nanocraft.game.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.nanocraft.game.core.GameHandler;

public class KeyHandler implements KeyListener {
    public boolean up, down, left, right, space, shoot;
    private boolean shootHeld;
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

        else if (gh.gameState == gh.play) {
            playState(code);
        }

        else if (gh.gameState == gh.pause) {
            pauseState(code);
        }

        else if (gh.gameState == gh.dialogue) {
            dialogueState(code);
        }

        else if (gh.gameState == gh.inventory) {
            inventoryState(code);
        }

        else if (gh.gameState == gh.stats) { 
            statsState(code);
        }

        else if (gh.gameState == gh.chest) {
            chestState(code);
        }

        else if (gh.gameState == gh.gameOver) {
            gameOverState(code);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            up = false;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            down = false;
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            left = false;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            right = false;
        }

        if (code == KeyEvent.VK_SPACE) {
            space = false;
        }

        if (code == KeyEvent.VK_F) {
            shootHeld = false;
            shoot = false;
        }
    }

    private void titleState(int code) {
        if (gh.ui.isControlMenuVisible()) {
            if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                gh.ui.closeControlMenu();
            }
            return;
        }

        if (gh.ui.isInSettings()) {
            titleSettingsState(code);
            return;
        }

        if (gh.ui.titleScreen == 0) {
            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                gh.ui.commandNum--;

                if (gh.ui.commandNum < 0) {
                    gh.ui.commandNum = 3;
                }
            }

            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                gh.ui.commandNum++;

                if (gh.ui.commandNum > 3) {
                    gh.ui.commandNum = 0;
                }
            }

            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                if (gh.ui.commandNum == 0) {
                    gh.sm.startNewGame();
                }

                else if (gh.ui.commandNum == 1) {
                    gh.loadGame();
                }

                else if (gh.ui.commandNum == 2) {
                    gh.ui.openSettings();
                }

                else if (gh.ui.commandNum == 3) {
                    System.exit(0);
                }
            }   
        }
    }

    private void titleSettingsState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.ui.closeSettings();
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            gh.ui.moveSettingsIndex(-1);
            return;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            gh.ui.moveSettingsIndex(1);
            return;
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            gh.selectSetting(-1);
            return;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            gh.selectSetting(1);
            return;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            gh.enterSettings();
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
            gh.openPauseMenu();
        }

        if (code == KeyEvent.VK_ESCAPE) {
            gh.openPauseMenu();
            return;
        }

        if (code == KeyEvent.VK_SPACE) {
            space = true;
            gh.player.requestInteract();
        }

        if (code == KeyEvent.VK_TAB) {
            gh.gameState = gh.inventory;
        }

        if (code == KeyEvent.VK_F) {
            if (!shootHeld) {
                shoot = true;
            }
            shootHeld = true;
        }
    }

    private void pauseState(int code) {
        if (gh.ui.isPauseExitConfirmationVisible()) {
            pauseExitConfirmationState(code);
            return;
        }

        if (gh.ui.isControlMenuVisible()) {
            if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                gh.ui.closeControlMenu();
            }
            return;
        }

        if (gh.ui.isInSettings()) {
            pauseSettingsState(code);
            return;
        }

        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) {
            gh.closePauseMenu();
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            gh.ui.movePauseMenuSelection(-1);
            return;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            gh.ui.movePauseMenuSelection(1);
            return;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            gh.activatePauseMenuSelection();
        }
    }

    private void pauseSettingsState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.ui.closeSettings();
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            gh.ui.moveSettingsIndex(-1);
            return;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            gh.ui.moveSettingsIndex(1);
            return;
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            gh.selectSetting(-1);
            return;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            gh.selectSetting(1);
            return;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            gh.enterSettings();
        }
    }

    private void pauseExitConfirmationState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.ui.closePauseExitConfirmation();
            return;
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            gh.ui.movePauseExitConfirmationSelection(-1);
            return;
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            gh.ui.movePauseExitConfirmationSelection(1);
            return;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            gh.confirmPauseExitSelection();
        }
    }

    private void dialogueState(int code) {
        if (gh.ik.isSleepPromptVisible()) {
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A || code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
                gh.ik.moveSleepPromptSelection(-1);
                return;
            }

            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
                gh.ik.moveSleepPromptSelection(1);
                return;
            }

            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                gh.ik.confirmSleepPromptSelection();
                return;
            }

            if (code == KeyEvent.VK_ESCAPE) {
                gh.ik.closeDialogue();
            }
            return;
        }

        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            gh.ik.closeDialogue();
        }
    }

    private void inventoryState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.gameState = gh.play;
            return;
        }

        if (code == KeyEvent.VK_TAB) {
            gh.gameState = gh.stats;
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            playCursorSoundIfMoved(gh.ui.moveInventoryCursor(0, -1));
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            playCursorSoundIfMoved(gh.ui.moveInventoryCursor(0, 1));
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            playCursorSoundIfMoved(gh.ui.moveInventoryCursor(-1, 0));
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            playCursorSoundIfMoved(gh.ui.moveInventoryCursor(1, 0));
        }

        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
            gh.player.selectItem();
        }
    }

    private void statsState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.gameState = gh.play;
            return;
        }

        if (code == KeyEvent.VK_TAB) {
            gh.gameState = gh.inventory;
        }
    }

    private void chestState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gh.closeChest();
            return;
        }

        if (code == KeyEvent.VK_TAB) {
            gh.ui.toggleChestPanel();
            return;
        }

        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
            gh.transferActiveChestSelection();
            return;
        }

        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            playCursorSoundIfMoved(gh.ui.moveChestCursor(0, -1));
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            playCursorSoundIfMoved(gh.ui.moveChestCursor(0, 1));
        }

        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            playCursorSoundIfMoved(gh.ui.moveChestCursor(-1, 0));
        }

        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            playCursorSoundIfMoved(gh.ui.moveChestCursor(1, 0));
        }
    }

    private void playCursorSoundIfMoved(boolean moved) {
        if (moved) {
            gh.playSound(GameHandler.SFX_CURSOR);
        }
    }

    private void gameOverState(int code) {
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            gh.ui.moveGameOverSelection(-1);
            return;
        }

        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            gh.ui.moveGameOverSelection(1);
            return;
        }

        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            gh.activateGameOverSelection();
        }
    }
}
