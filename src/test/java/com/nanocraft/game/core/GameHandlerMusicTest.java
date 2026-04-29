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

        int loopCount() {
            return loopedTracks.size();
        }
    }

    @Test
    public void titleScreenUsesMainMenuMusic() {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        int titleMusic = gh.getTrackId("/sound/MainMenu.wav");
        assertEquals(titleMusic, gh.lastLoopedTrack());
    }

    @Test
    public void villageMapUsesVillageMusicAfterLeavingTitleScreen() {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        int titleMusic = gh.getTrackId("/sound/MainMenu.wav");
        int villageMusic = gh.getTrackId("/sound/village.wav");
        assertEquals(titleMusic, gh.lastLoopedTrack());

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(titleMusic));
    }

    @Test
    public void villageMapUsesNightMusicAtNightAndVillageMusicAtDay() {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();
        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int nightMusic = gh.getTrackId("/sound/night.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(villageMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(nightMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        gh.cycleTimeOfDay();

        assertEquals(nightMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(nightMusic));
    }

    @Test
    public void caveMapLoopsDungeonMusicAndLeavingCaveStopsIt() {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int caveMusic = gh.getTrackId("/sound/Moody Dungeon.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        gh.th.loadMap(AssetHandler.CAVE_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(caveMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        gh.th.loadMap(GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(caveMusic));
    }

    @Test
    public void netherMapLoopsChamberMusicAndLeavingNetherStopsIt() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int netherMusic = gh.getTrackId("/sound/Alone in the Chamber.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        setCurrentMapPath(gh, AssetHandler.NETHER_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(netherMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        setCurrentMapPath(gh, GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(netherMusic));
    }

    @Test
    public void desertMapLoopsDesertMusicAndLeavingDesertStopsIt() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int desertMusic = gh.getTrackId("/sound/Desert.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        setCurrentMapPath(gh, AssetHandler.DESERT_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(desertMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        setCurrentMapPath(gh, GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(desertMusic));
    }

    @Test
    public void desertMapUsesNightMusicAtNightAndDesertMusicAtDay() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();
        gh.gameState = gh.play;
        setCurrentMapPath(gh, AssetHandler.DESERT_MAP_PATH);
        gh.refreshCurrentMapState();

        int desertMusic = gh.getTrackId("/sound/Desert.wav");
        int nightMusic = gh.getTrackId("/sound/night.wav");
        assertEquals(desertMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(desertMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(nightMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(desertMusic));

        gh.cycleTimeOfDay();

        assertEquals(nightMusic, gh.lastLoopedTrack());

        gh.cycleTimeOfDay();

        assertEquals(desertMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(nightMusic));
    }

    @Test
    public void endMapLoopsEndMusicAndLeavingEndStopsIt() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int endMusic = gh.getTrackId("/sound/EndMusic.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        setCurrentMapPath(gh, AssetHandler.END_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(endMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        setCurrentMapPath(gh, GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(endMusic));
    }

    @Test
    public void defeatingBronzeDragonStopsEndMusicForFanfare() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        setCurrentMapPath(gh, AssetHandler.END_MAP_PATH);
        gh.refreshCurrentMapState();

        int endMusic = gh.getTrackId("/sound/EndMusic.wav");
        int loopCountBeforeDefeat = gh.loopCount();
        assertEquals(endMusic, gh.lastLoopedTrack());

        gh.handleBronzeDragonDefeat();

        assertTrue(gh.stoppedTracks.contains(endMusic));
        assertEquals(loopCountBeforeDefeat, gh.loopCount());
    }

    @Test
    public void enteringEndAfterBronzeDragonDefeatStaysSilent() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int loopCountBeforeEnteringEnd = gh.loopCount();
        gh.setBronzeDragonDefeated(true);

        setCurrentMapPath(gh, AssetHandler.END_MAP_PATH);
        gh.refreshCurrentMapState();

        assertTrue(gh.stoppedTracks.contains(villageMusic));
        assertEquals(loopCountBeforeEnteringEnd, gh.loopCount());
    }

    @Test
    public void innMapLoopsInnMusicAndLeavingInnStopsIt() throws Exception {
        RecordingMusicGameHandler gh = new RecordingMusicGameHandler();

        gh.playMusic();

        gh.gameState = gh.play;
        gh.refreshCurrentMapState();

        int villageMusic = gh.getTrackId("/sound/village.wav");
        int innMusic = gh.getTrackId("/sound/inn.wav");
        assertEquals(villageMusic, gh.lastLoopedTrack());

        setCurrentMapPath(gh, AssetHandler.INN_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(innMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(villageMusic));

        setCurrentMapPath(gh, GameHandler.STARTING_MAP_PATH);
        gh.refreshCurrentMapState();

        assertEquals(villageMusic, gh.lastLoopedTrack());
        assertTrue(gh.stoppedTracks.contains(innMusic));
    }

    private void setCurrentMapPath(GameHandler gh, String mapPath) throws Exception {
        Field currentMapPath = gh.th.getClass().getDeclaredField("currentMapPath");
        currentMapPath.setAccessible(true);
        currentMapPath.set(gh.th, mapPath);
    }
}
