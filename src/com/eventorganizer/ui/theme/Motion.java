package com.eventorganizer.ui.theme;

/**
 * Motion tokens. All durations in milliseconds. {@link #REDUCED} collapses every
 * duration to zero and freezes ambient animations; toggled by {@code -Duinimal=true}
 * and read in {@code App.start()}.
 */
public final class Motion {
    private Motion() {}

    public static volatile boolean REDUCED = Boolean.getBoolean("uinimal");

    public static final int MICRO = 120;
    public static final int SHORT = 180;
    public static final int MED   = 240;
    public static final int LONG  = 340;
    public static final int HERO  = 420;

    public static final int TOAST_FADE_IN_MS  = 200;
    public static final int TOAST_HOLD_MS     = 3000;
    public static final int TOAST_FADE_OUT_MS = 180;

    public static final int PULSE_PERIOD_MS = 1800;
    public static final int AMBIENT_FPS     = 24;
    public static final int ANIM_FPS        = 60;

    public static int d(int ms) { return REDUCED ? 0 : ms; }
}
