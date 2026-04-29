package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameHandlerFullscreenTest {
    @Test
    public void defaultsToWindowedViewport() {
        GameHandler gh = new GameHandler();

        assertFalse(gh.isFullScreen());
        assertEquals(768, gh.screenWidth);
        assertEquals(576, gh.screenHeight);
        assertEquals(360, gh.player.screenX);
        assertEquals(264, gh.player.screenY);
    }

    @Test
    public void fullscreenUsesSixteenByNineLogicalViewport() {
        GameHandler gh = new GameHandler();

        gh.setFullScreen(true);

        assertTrue(gh.isFullScreen());
        assertEquals(1024, gh.screenWidth);
        assertEquals(576, gh.screenHeight);
        assertEquals(488, gh.player.screenX);
        assertEquals(264, gh.player.screenY);
    }

    @Test
    public void leavingFullscreenRestoresWindowedViewport() {
        GameHandler gh = new GameHandler();
        gh.setFullScreen(true);

        gh.setFullScreen(false);

        assertFalse(gh.isFullScreen());
        assertEquals(768, gh.screenWidth);
        assertEquals(360, gh.player.screenX);
        assertEquals(264, gh.player.screenY);
    }
}