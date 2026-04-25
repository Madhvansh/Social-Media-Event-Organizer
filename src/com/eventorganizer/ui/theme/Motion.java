package com.eventorganizer.ui.theme;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Motion tokens. All durations in milliseconds. {@link #REDUCED} collapses every
 * duration to zero and freezes ambient animations. The flag can be:
 * <ul>
 *   <li>seeded from the {@code -Duinimal=true} JVM property,</li>
 *   <li>persisted via {@link com.eventorganizer.utils.Preferences}, or</li>
 *   <li>flipped at runtime via {@link #setReduced(boolean)} — listeners
 *       (the aurora backdrop, constellation, etc.) are notified so they
 *       can pause/resume their own ambient timers without polling.</li>
 * </ul>
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

    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    public static int d(int ms) { return REDUCED ? 0 : ms; }

    /** Atomically flip the flag and notify every registered listener on the EDT. */
    public static void setReduced(boolean reduced) {
        if (REDUCED == reduced) return;
        REDUCED = reduced;
        for (Runnable r : LISTENERS) {
            try { r.run(); }
            catch (RuntimeException ignored) { /* listener errors are not fatal */ }
        }
    }

    /** Register a callback to run whenever {@link #setReduced} flips the flag. */
    public static void addListener(Runnable listener) {
        if (listener != null) LISTENERS.add(listener);
    }

    public static void removeListener(Runnable listener) {
        if (listener != null) LISTENERS.remove(listener);
    }
}
