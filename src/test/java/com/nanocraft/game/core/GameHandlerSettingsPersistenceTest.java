package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class GameHandlerSettingsPersistenceTest {
    @Test
    public void persistsFullscreenAndVolumeSettingsAcrossInstances() throws Exception {
        Path tempSettingsFile = Files.createTempFile("nanocraft-settings", ".json");
        Files.deleteIfExists(tempSettingsFile);
        String previousSettingsPath = System.getProperty("nanocraft.settings.path");

        try {
            System.setProperty("nanocraft.settings.path", tempSettingsFile.toString());

            GameHandler first = new GameHandler();
            first.setFullScreen(true);
            first.setMusicVolume(35);
            first.setSfxVolume(65);

            GameHandler second = new GameHandler();

            assertTrue(second.isFullScreen());
            assertEquals(1024, second.screenWidth);
            assertEquals(35, second.getMusicVolume());
            assertEquals(65, second.getSfxVolume());
        } finally {
            restoreProperty("nanocraft.settings.path", previousSettingsPath);
            Files.deleteIfExists(tempSettingsFile);
        }
    }

    @Test
    public void defaultsStayWhenSettingsFileIsMissing() throws Exception {
        Path tempSettingsFile = Files.createTempFile("nanocraft-settings-missing", ".json");
        Files.deleteIfExists(tempSettingsFile);
        String previousSettingsPath = System.getProperty("nanocraft.settings.path");

        try {
            System.setProperty("nanocraft.settings.path", tempSettingsFile.toString());

            GameHandler gh = new GameHandler();

            assertFalse(gh.isFullScreen());
            assertEquals(100, gh.getMusicVolume());
            assertEquals(100, gh.getSfxVolume());
        } finally {
            restoreProperty("nanocraft.settings.path", previousSettingsPath);
            Files.deleteIfExists(tempSettingsFile);
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
            return;
        }

        System.setProperty(key, value);
    }
}
