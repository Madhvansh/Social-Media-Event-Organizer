package com.eventorganizer.utils;

import com.eventorganizer.store.DataStore;
import com.eventorganizer.store.Persistence;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Installs a JVM shutdown hook that:
 * <ol>
 *   <li>Persists the {@link DataStore} to disk (via {@link Persistence#save()})
 *       so accounts, events, friendships, and invitations survive between runs.</li>
 *   <li>Clears the dangling session pointer.</li>
 *   <li>Flushes the log file.</li>
 * </ol>
 *
 * <p>Persistence on shutdown is best-effort: a failure here is logged but
 * doesn't prevent the JVM from exiting. The save itself is atomic
 * (write-to-tmp then rename) so a crash mid-write can't corrupt the snapshot.
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
                try {
                    Persistence.save();
                    log.log(Level.INFO, "snapshot written to " + Persistence.snapshotPath());
                } catch (RuntimeException pe) {
                    log.log(Level.WARNING, "snapshot write failed: " + pe.getMessage());
                }
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
