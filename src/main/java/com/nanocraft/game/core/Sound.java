package com.nanocraft.game.core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

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

    public void setVolume(int id, int percent) {
        Clip[] pool = clips[id];
        if (pool == null) return;

        int clampedPercent = Math.max(0, Math.min(100, percent));

        for (Clip c : pool) {
            if (c == null || !c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                continue;
            }

            FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(percentToDecibels(clampedPercent, gain));
        }
    }

    private float percentToDecibels(int percent, FloatControl gain) {
        if (percent <= 0) {
            return gain.getMinimum();
        }

        double normalized = percent / 100.0;
        double perceptualAmplitude = normalized * normalized;
        double decibels = 20.0 * Math.log10(perceptualAmplitude);
        return (float) Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels));
    }
}
