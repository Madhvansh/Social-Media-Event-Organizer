package com.eventorganizer.utils;

import com.eventorganizer.ui.theme.Motion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lightweight on-disk preferences (.properties file) for accessibility +
 * UI options that survive between runs. Stored alongside the snapshot file
 * so a user's reduced-motion choice persists.
 *
 * <p>Currently tracks one key: {@code motion.reduced}. The
 * {@code -Duinimal=true} JVM flag still wins over the prefs file (system
 * property is read at class init and explicitly overrides anything saved).
 */
public final class Preferences {
    private Preferences() {}

    public static final String FILE_NAME = "eventorganizer.prefs";
    private static final String KEY_REDUCED = "motion.reduced";
    private static final Logger LOG = Logger.getLogger(Preferences.class.getName());

    /** Read prefs from disk and apply them. Safe to call exactly once at startup. */
    public static synchronized void load() {
        load(new File(FILE_NAME));
    }

    public static synchronized void load(File source) {
        // Honour `-Duinimal=true` over the saved value.
        if (Boolean.getBoolean("uinimal")) {
            Motion.REDUCED = true;
            return;
        }
        if (!source.isFile()) return;
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(source)) {
            p.load(in);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Preferences.load: " + ex.getMessage());
            return;
        }
        String reduced = p.getProperty(KEY_REDUCED);
        if (reduced != null) {
            Motion.REDUCED = Boolean.parseBoolean(reduced.trim());
        }
    }

    /** Persist the current value of {@link Motion#REDUCED} to disk. */
    public static synchronized void saveMotionReduced() {
        save(new File(FILE_NAME));
    }

    public static synchronized void save(File target) {
        Properties p = new Properties();
        p.setProperty(KEY_REDUCED, Boolean.toString(Motion.REDUCED));
        File parent = target.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        try (FileOutputStream out = new FileOutputStream(target)) {
            p.store(out, "Event Organizer user preferences");
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Preferences.save: " + ex.getMessage());
        }
    }
}
