package com.eventorganizer.ui.theme;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registers vendored Inter + JetBrains Mono TTFs with the local
 * {@link GraphicsEnvironment} so the FONT_* tokens in {@link Theme}/
 * {@link Typography} resolve cleanly on every host.
 *
 * <p>Looks up files in two locations:
 * <ol>
 *   <li>Classpath resources under {@code /fonts/} (packaged inside a jar or
 *       alongside the compiled classes).</li>
 *   <li>Filesystem at {@code lib/fonts/} relative to the working directory,
 *       then {@code fonts/} (for dev runs out of {@code out/}).</li>
 * </ol>
 *
 * <p>Missing files log a warning and are skipped — the OS fallback chain in
 * {@link Theme#resolveFromChain(String[])} handles the visual degradation.
 *
 * <p>Call {@link #load()} exactly once before building any UIManager fonts.
 * Subsequent calls are no-ops.
 */
public final class FontLoader {
    private FontLoader() {}

    private static final Logger LOG = Logger.getLogger(FontLoader.class.getName());

    private static final String[] FONT_FILES = {
        "Inter-Regular.ttf",
        "Inter-Medium.ttf",
        "Inter-SemiBold.ttf",
        "Inter-Bold.ttf",
        "Inter-ExtraBold.ttf",
        "JetBrainsMono-Regular.ttf",
        "JetBrainsMono-Medium.ttf",
    };

    private static volatile boolean loaded = false;

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        int registered = 0;
        for (String name : FONT_FILES) {
            if (tryRegister(ge, name)) registered++;
        }
        if (registered == 0) {
            LOG.info("FontLoader: no vendored fonts registered — relying on system fallback chain.");
        } else {
            LOG.info("FontLoader: registered " + registered + "/" + FONT_FILES.length + " fonts.");
        }
    }

    private static boolean tryRegister(GraphicsEnvironment ge, String fileName) {
        try (InputStream in = open(fileName)) {
            if (in == null) return false;
            Font font = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(in));
            return ge.registerFont(font);
        } catch (IOException | FontFormatException ex) {
            LOG.log(Level.WARNING, "FontLoader: failed to register " + fileName, ex);
            return false;
        }
    }

    private static InputStream open(String fileName) throws IOException {
        InputStream cp = FontLoader.class.getResourceAsStream("/fonts/" + fileName);
        if (cp != null) return cp;
        for (String base : new String[] { "lib/fonts", "fonts", "out/fonts" }) {
            File f = new File(base, fileName);
            if (f.isFile() && f.canRead()) {
                return new FileInputStream(f);
            }
        }
        return null;
    }
}
