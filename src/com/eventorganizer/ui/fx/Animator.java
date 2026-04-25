package com.eventorganizer.ui.fx;

import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

/**
 * Central tween runner. One shared 60 fps Swing {@link Timer} drives every
 * active {@link Tween}; components don't own timers. This avoids the timer-storm
 * that plagues naive Swing animation and keeps every callback safely on the EDT.
 *
 * <p>Usage:
 * <pre>{@code
 *   Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> {
 *       alpha = (float) t;
 *       component.repaint();
 *   }, () -> onDone());
 * }</pre>
 *
 * <p>When {@link Motion#REDUCED} is true, the tween runs exactly one frame at
 * {@code t = 1.0} and completes immediately — preserving the final state without
 * any intermediate motion.
 */
public final class Animator {
    private Animator() {}

    private static final int TICK_MS = 1000 / Motion.ANIM_FPS;
    private static final List<Tween> ACTIVE = new ArrayList<>();
    private static final Timer TIMER;

    static {
        TIMER = new Timer(TICK_MS, e -> tickAll());
        TIMER.setCoalesce(true);
    }

    /**
     * Start a tween. Returns a {@link Handle} that lets callers cancel early.
     * Always invoked on the EDT.
     */
    public static Handle tween(int durationMs, DoubleUnaryOperator easing,
                               DoubleConsumer onTick, Runnable onComplete) {
        if (easing == null) easing = Easing.LINEAR;
        if (onTick == null) onTick = t -> {};
        if (durationMs <= 0 || Motion.REDUCED) {
            onTick.accept(1.0);
            if (onComplete != null) onComplete.run();
            return Handle.DONE;
        }
        Tween tw = new Tween(System.nanoTime(), durationMs * 1_000_000L,
            easing, onTick, onComplete);
        ACTIVE.add(tw);
        if (!TIMER.isRunning()) TIMER.start();
        return tw;
    }

    public static Handle tween(int durationMs, DoubleUnaryOperator easing, DoubleConsumer onTick) {
        return tween(durationMs, easing, onTick, null);
    }

    private static void tickAll() {
        if (ACTIVE.isEmpty()) {
            TIMER.stop();
            return;
        }
        long now = System.nanoTime();
        for (int i = ACTIVE.size() - 1; i >= 0; i--) {
            Tween t = ACTIVE.get(i);
            if (t.cancelled) {
                ACTIVE.remove(i);
                continue;
            }
            double p = (now - t.startNanos) / (double) t.durationNanos;
            if (p >= 1.0) {
                t.onTick.accept(1.0);
                if (t.onComplete != null) t.onComplete.run();
                ACTIVE.remove(i);
            } else {
                t.onTick.accept(t.easing.applyAsDouble(p));
            }
        }
        if (ACTIVE.isEmpty()) TIMER.stop();
    }

    public interface Handle {
        void cancel();
        Handle DONE = () -> {};
    }

    private static final class Tween implements Handle {
        final long startNanos;
        final long durationNanos;
        final DoubleUnaryOperator easing;
        final DoubleConsumer onTick;
        final Runnable onComplete;
        volatile boolean cancelled;

        Tween(long startNanos, long durationNanos, DoubleUnaryOperator easing,
              DoubleConsumer onTick, Runnable onComplete) {
            this.startNanos = startNanos;
            this.durationNanos = durationNanos;
            this.easing = easing;
            this.onTick = onTick;
            this.onComplete = onComplete;
        }

        @Override public void cancel() { cancelled = true; }
    }
}
