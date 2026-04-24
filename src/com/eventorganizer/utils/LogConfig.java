package com.eventorganizer.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LogConfig {
    private static final String LOG_DIR  = "logs";
    private static final String LOG_FILE = "eventorganizer.log";
    private static final int FILE_SIZE_LIMIT_BYTES = 1024 * 1024;
    private static final int FILE_ROTATION_COUNT   = 5;

    private static boolean initialized = false;
    private static FileHandler fileHandler;

    private LogConfig() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) root.removeHandler(h);

        Formatter fmt = new SafeFormatter();

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.WARNING);
        console.setFormatter(fmt);
        root.addHandler(console);

        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdirs();
            String pattern = LOG_DIR + "/" + LOG_FILE + ".%g";
            fileHandler = new FileHandler(pattern, FILE_SIZE_LIMIT_BYTES, FILE_ROTATION_COUNT, true);
            fileHandler.setLevel(Level.INFO);
            fileHandler.setFormatter(fmt);
            root.addHandler(fileHandler);
        } catch (IOException e) {
            // File logging is best-effort; console remains authoritative.
            console.publish(new LogRecord(Level.WARNING,
                "Unable to initialize file log handler: " + e.getMessage()));
        }

        root.setLevel(Level.INFO);
    }

    /** Flushes buffered file-log output. Safe to call during shutdown. */
    public static synchronized void flush() {
        if (fileHandler != null) fileHandler.flush();
    }

    public static Logger forClass(Class<?> cls) {
        init();
        return Logger.getLogger(cls.getName());
    }

    /**
     * Formatter that escapes CR/LF/control characters from every logged message so a
     * user-controlled input (bio, event name) cannot forge additional log lines.
     * {@code \r} and {@code \n} become literal {@code \r}/{@code \n}; other control
     * bytes become {@code \xNN} hex escapes.
     */
    private static final class SafeFormatter extends Formatter {
        @Override
        public String format(LogRecord r) {
            String msg = escape(r.getMessage());
            return "[" + r.getLevel() + "] " + r.getLoggerName() + ": " + msg
                + System.lineSeparator();
        }

        private static String escape(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '\r')      sb.append("\\r");
                else if (c == '\n') sb.append("\\n");
                else if (c == '\t') sb.append("\\t");
                else if (c < 0x20 || c == 0x7F) {
                    sb.append(String.format("\\x%02X", (int) c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
