package com.eventorganizer.store;

import java.time.Clock;

/**
 * Package-internal bridge that exposes {@link DataStore}'s test-only hooks to
 * the JUnit suite without widening their visibility on the store itself.
 * Production code must not call these methods.
 */
public final class TestHooks {
    private TestHooks() {}

    public static void reset() {
        DataStore.INSTANCE.resetForTests();
    }

    public static void setClock(Clock c) {
        DataStore.INSTANCE.setClock(c);
    }
}
