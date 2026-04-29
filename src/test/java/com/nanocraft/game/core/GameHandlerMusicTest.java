package com.nanocraft.game.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class GameHandlerMusicTest {
    private static class RecordingMusicGameHandler extends GameHandler {
        final Map<Integer, String> loadedTracks = new HashMap<>();
        final List<Integer> loopedTracks = new ArrayList<>();
        final List<Integer> stoppedTracks = new ArrayList<>();

        @Override
        protected boolean isAudioPlaybackAvailable() {
            return true;
        }

        @Override
        protected void loadMusicTrack(int id, String path) {
            loadedTracks.put(id, path);
        }

        @Override
        protected void loopMusicTrack(int id) {
            loopedTracks.add(id);
        }

        @Override
        protected void stopMusicTrack(int id) {
            stoppedTracks.add(id);
        }

        int getTrackId(String path) {
            for (Map.Entry<Integer, String> entry : loadedTracks.entrySet()) {
                if (path.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return -1;
        }

        int lastLoopedTrack() {
            return loopedTracks.get(loopedTracks.size() - 1);
        }
    }

    @Test
    public void caveMapLoopsDungeonMusicAndLeavingCaveStopsIt() {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        int mainMusic = gh.getTrackId("/sound/MainMenu.wav");
        int caveMusic = gh.getTrackId("/sound/Moody Dungeon.wav");
        assertEquals(mainMusic, gh.lastLoopedTrack());

        gh.th.loadMap(AssetHandler.CAVE_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(caveMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(mainMusic));

        gh.th.loadMap(GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(mainMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(caveMusic));
    }

    @Test
    public void netherMapLoopsChamberMusicAndLeavingNetherStopsIt() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        int mainMusic = gh.getTrackId("/sound/MainMenu.wav");
        int netherMusic = gh.getTrackId("/sound/Alone in the Chamber.wav");
        assertEquals(mainMusic, gh.lastLoopedTrack());

        setCurrentMapPath(gh, AssetHandler.NETHER_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(netherMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(mainMusic));

        setCurrentMapPath(gh, GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(mainMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(netherMusic));
    }

    private void setCurrentMapPath(GameHandler gh, String mapPath) throws Exception {
        Field currentMapPath = gh.th.getClass().getDeclaredField("currentMapPath");
        currentMapPath.setAccessible(true);
        currentMapPath.set(gh.th, mapPath);
    }
}
