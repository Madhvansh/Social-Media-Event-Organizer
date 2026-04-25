package com.eventorganizer.store;

import java.time.Clock;

public final class TestHooks {
    private TestHooks() {}

    public static void reset() {
        DataStore.INSTANCE.resetForTests();
    }

    public static void setClock(Clock c) {
        DataStore.INSTANCE.setClock(c);
    }
}
