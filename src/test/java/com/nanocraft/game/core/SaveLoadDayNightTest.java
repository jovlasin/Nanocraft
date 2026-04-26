package com.nanocraft.game.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SaveLoadDayNightTest {
    @Test
    public void applySaveDataRestoresNightAfterCurrentSessionIsDay() {
        GameHandler gh = new GameHandler();
        SaveManager.SaveData saveData = new SaveManager.SaveData();
        saveData.currentMapPath = gh.th.getCurrentMapPath();
        saveData.player = gh.player.createSaveData();
        saveData.dayNightTick = 0;

        gh.dayNightCycle.setCurrentTick(12000);
        assertFalse(gh.dayNightCycle.isNight());
        assertTrue(gh.getTimeLabel().contains("Day"));
        assertTrue(gh.getCurrentDarknessAlpha() == 0f);

        gh.applySaveData(saveData);

        assertTrue(gh.dayNightCycle.isNight());
        assertTrue(gh.getTimeLabel().contains("Night"));
        assertTrue(gh.getCurrentDarknessAlpha() > 0f);
    }
}
