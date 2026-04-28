package com.nanocraft.game.core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sound {
    private static final int voices = 10;

    private final Clip[][] clips = new Clip[30][];

    public void load(int id, String path) {
        try {
            clips[id] = new Clip[voices];

            for (int i = 0; i < voices; i++) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource(path));
                Clip c = AudioSystem.getClip();
                c.open(ais);
                clips[id][i] = c;
                ais.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(int id) {
        Clip[] pool = clips[id];
        if (pool == null) return;

        for (Clip c : pool) {
            if (c == null) continue;

            if (!c.isRunning()) {
                c.setFramePosition(0);
                c.start();
                return;
            }
        }
    }

    public void loop(int id) {
        Clip[] pool = clips[id];
        if (pool == null) return;

        Clip c = pool[0];
        if (c == null) return;

        if (!c.isRunning()) {
            c.setFramePosition(0);
            c.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop(int id) {
        Clip[] pool = clips[id];
        if (pool == null) return;

        for (Clip c : pool) {
            if (c == null) continue;

            if (c.isRunning()) c.stop();
        }
    }
}
