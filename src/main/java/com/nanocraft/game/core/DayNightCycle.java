package com.nanocraft.game.core;

public class DayNightCycle {
    private static final int FPS = 60;
    private static final int DAY_DURATION_SECONDS = 120;
    private static final int NIGHT_DURATION_SECONDS = 120;
    private static final int TRANSITION_DURATION_SECONDS = 30;
    private static final int DEFAULT_CYCLE_TICKS = FPS * (
        DAY_DURATION_SECONDS
        + NIGHT_DURATION_SECONDS
        + (TRANSITION_DURATION_SECONDS * 2)
    );
    private static final float MAX_NIGHT_ALPHA = 0.88f;

    private final int cycleTicks;
    private int currentTick;

    public DayNightCycle() {
        this(DEFAULT_CYCLE_TICKS, sunriseStartTick() + (FPS * 12));
    }

    public DayNightCycle(int cycleTicks, int startingTick) {
        this.cycleTicks = Math.max(1, cycleTicks);
        this.currentTick = Math.floorMod(startingTick, this.cycleTicks);
    }

    public void update() {
        currentTick = (currentTick + 1) % cycleTicks;
    }

    public void advanceToNextPhase() {
        double progress = getProgress();

        if (progress < sunriseStartProgress()) {
            setProgress(sunriseStartProgress());
        } 
        else if (progress < dayStartProgress()) {
            setProgress(dayStartProgress());
        } 
        else if (progress < sunsetStartProgress()) {
            setProgress(sunsetStartProgress());
        } 
        else if (progress < nightStartProgress()) {
            setProgress(nightStartProgress());
        } 
        else {
            setProgress(0.0);
        }
    }

    public float getDarknessAlpha() {
        double progress = getProgress();

        if (progress < sunriseStartProgress()) {
            return MAX_NIGHT_ALPHA;
        }

        if (progress < dayStartProgress()) {
            double ratio = (progress - sunriseStartProgress()) / transitionProgress();
            return interpolate(MAX_NIGHT_ALPHA, 0f, ratio);
        }

        if (progress < sunsetStartProgress()) {
            return 0f;
        }

        if (progress < nightStartProgress()) {
            double ratio = (progress - sunsetStartProgress()) / transitionProgress();
            return interpolate(0f, MAX_NIGHT_ALPHA, ratio);
        }

        return MAX_NIGHT_ALPHA;
    }

    public float getMaxDarknessAlpha() {
        return MAX_NIGHT_ALPHA;
    }

    public String getPhaseName() {
        double progress = getProgress();

        if (progress < sunriseStartProgress() || progress >= nightStartProgress()) {
            return "Night";
        }

        if (progress < dayStartProgress()) {
            return "Dawn";
        }

        if (progress < sunsetStartProgress()) {
            return "Day";
        }

        return "Dusk";
    }

    public String getClockText() {
        int totalMinutes = (int) Math.round(getProgress() * 24 * 60) % (24 * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public double getProgress() {
        return (double) currentTick / cycleTicks;
    }

    private void setProgress(double progress) {
        currentTick = Math.floorMod((int) Math.round(progress * cycleTicks), cycleTicks);
    }

    private float interpolate(float start, float end, double ratio) {
        double clamped = Math.max(0d, Math.min(1d, ratio));
        return (float) (start + ((end - start) * clamped));
    }

    private static int sunriseStartTick() {
        return FPS * NIGHT_DURATION_SECONDS;
    }

    private static double sunriseStartProgress() {
        return (double) sunriseStartTick() / DEFAULT_CYCLE_TICKS;
    }

    private static double dayStartProgress() {
        return (double) (sunriseStartTick() + (FPS * TRANSITION_DURATION_SECONDS)) / DEFAULT_CYCLE_TICKS;
    }

    private static double sunsetStartProgress() {
        return (double) (sunriseStartTick() + (FPS * TRANSITION_DURATION_SECONDS) + (FPS * DAY_DURATION_SECONDS))
            / DEFAULT_CYCLE_TICKS;
    }

    private static double nightStartProgress() {
        return (double) (sunriseStartTick() + (FPS * TRANSITION_DURATION_SECONDS * 2) + (FPS * DAY_DURATION_SECONDS))
            / DEFAULT_CYCLE_TICKS;
    }

    private static double transitionProgress() {
        return (double) (FPS * TRANSITION_DURATION_SECONDS) / DEFAULT_CYCLE_TICKS;
    }
}
