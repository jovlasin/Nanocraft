package com.nanocraft.game;

import com.nanocraft.game.core.GameHandler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameHandlerIntegrationTest {

    private GameHandler gh;

    @Before
    public void setUp() {
        gh = new GameHandler();
    }

    @Test
    public void testGameStartsInTitleState() {
        assertEquals(gh.title, gh.gameState);
    }

    @Test
    public void testUpdateRunsInPlayStateWithoutCrash() {
        gh.gameState = gh.play;

        gh.update();

        assertEquals(gh.play, gh.gameState);
    }

    @Test
    public void testPauseStateUpdateDoesNotCrash() {
        gh.gameState = gh.pause;

        gh.update();

        assertEquals(gh.pause, gh.gameState);
    }
}