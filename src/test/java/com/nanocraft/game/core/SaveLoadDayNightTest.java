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

    @Test
    public void restPhaseTurnsNightIntoDay() {
        DayNightCycle cycle = new DayNightCycle();
        cycle.setCurrentTick(0);

        cycle.advanceToRestPhase();

        assertFalse(cycle.isNight());
        assertTrue("Day".equals(cycle.getPhaseName()));
    }

    @Test
    public void restPhaseTurnsDayIntoNight() {
        DayNightCycle cycle = new DayNightCycle();
        cycle.setCurrentTick(12000);

        cycle.advanceToRestPhase();

        assertTrue(cycle.isNight());
        assertTrue("Night".equals(cycle.getPhaseName()));
    }
}
