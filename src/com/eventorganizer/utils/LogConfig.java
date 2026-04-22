package com.eventorganizer.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class LogConfig {
    private static boolean initialized = false;

    private LogConfig() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) root.removeHandler(h);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord r) {
                return "[" + r.getLevel() + "] " + r.getLoggerName() + ": " + r.getMessage()
                    + System.lineSeparator();
            }
        });
        root.addHandler(handler);
        root.setLevel(Level.WARNING);
    }

    public static Logger forClass(Class<?> cls) {
        init();
        return Logger.getLogger(cls.getName());
    }
}
