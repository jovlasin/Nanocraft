package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;

public class GameHandlerDragonSoundTest {
    private static class RecordingGameHandler extends GameHandler {
        int lastSoundId = -1;
        int[] soundCounts = new int[30];

        @Override
        public void playSound(int i) {
            lastSoundId = i;
            soundCounts[i]++;
        }

        int getSoundCount(int i) {
            return soundCounts[i];
        }
    }

    @Test
    public void bronzeDragonDefeatInEndPlaysFanfareSoundOnce() throws Exception {
        RecordingGameHandler gh = new RecordingGameHandler();
        setCurrentMapPath(gh, AssetHandler.END_MAP_PATH);

        gh.handleBronzeDragonDefeat();
        gh.handleBronzeDragonDefeat();

        assertEquals(1, gh.getSoundCount(GameHandler.SFX_FANFARE));
        assertEquals(GameHandler.SFX_FANFARE, gh.lastSoundId);
    }

    private void setCurrentMapPath(GameHandler gh, String mapPath) throws Exception {
        Field currentMapPath = gh.th.getClass().getDeclaredField("currentMapPath");
        currentMapPath.setAccessible(true);
        currentMapPath.set(gh.th, mapPath);
    }
}
