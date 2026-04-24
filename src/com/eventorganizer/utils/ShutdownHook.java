package com.eventorganizer.utils;

import com.eventorganizer.store.DataStore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs a JVM shutdown hook that flushes the log file, logs a final "shutting down"
 * line, and clears any dangling session pointer in the DataStore. No persistence work
 * is performed — persistence is out of scope for this project.
 */
public final class ShutdownHook {
    private static boolean installed = false;

    private ShutdownHook() {}

    public static synchronized void install() {
        if (installed) return;
        installed = true;

        Thread hook = new Thread(() -> {
            Logger log = LogConfig.forClass(ShutdownHook.class);
            try {
                log.log(Level.INFO, "shutting down");
                DataStore.INSTANCE.clearSession();
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "Shutdown hook error: " + e.getMessage());
            } finally {
                LogConfig.flush();
            }
        }, "eventorganizer-shutdown");
        hook.setDaemon(false);
        Runtime.getRuntime().addShutdownHook(hook);
    }
}
