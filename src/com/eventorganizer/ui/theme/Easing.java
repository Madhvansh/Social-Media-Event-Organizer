package com.eventorganizer.ui.theme;

import java.util.function.DoubleUnaryOperator;

/** Normalised [0..1] easing curves. Stateless and branch-free. */
public final class Easing {
    private Easing() {}

    public static final DoubleUnaryOperator LINEAR = t -> t;

    public static final DoubleUnaryOperator EASE_OUT_CUBIC = t -> {
        double u = 1.0 - clamp01(t);
        return 1.0 - u * u * u;
    };

    public static final DoubleUnaryOperator EASE_OUT_QUINT = t -> {
        double u = 1.0 - clamp01(t);
        double u2 = u * u;
        return 1.0 - u2 * u2 * u;
    };

    public static final DoubleUnaryOperator EASE_IN_OUT_CUBIC = t -> {
        double c = clamp01(t);
        return c < 0.5
            ? 4.0 * c * c * c
            : 1.0 - Math.pow(-2.0 * c + 2.0, 3) / 2.0;
    };

    /** Slight overshoot spring — lands at 1.0 with a small bounce. */
    public static final DoubleUnaryOperator SPRING = t -> {
        double c = clamp01(t);
        double damping = 0.55;
        double frequency = 0.18;
        if (c >= 1.0) return 1.0;
        return 1.0 - Math.exp(-c / damping) * Math.cos(c * Math.PI * 2.0 / frequency * 0.3);
    };

    private static double clamp01(double t) {
        return t < 0.0 ? 0.0 : (t > 1.0 ? 1.0 : t);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }
}
