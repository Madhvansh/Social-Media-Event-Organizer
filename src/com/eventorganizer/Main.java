package com.eventorganizer;

import com.eventorganizer.store.DataStore;
import com.eventorganizer.store.Persistence;
import com.eventorganizer.ui.App;
import com.eventorganizer.ui.theme.FontLoader;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.LogConfig;
import com.eventorganizer.utils.Preferences;
import com.eventorganizer.utils.ShutdownHook;

import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        LogConfig.init();
        ShutdownHook.install();

        // Load reduced-motion + other UI prefs before the rest of the UI initialises.
        Preferences.load();

        // load fonts before setting up the theme
        FontLoader.load();

        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            flat.getMethod("setup").invoke(null);
        } catch (ReflectiveOperationException e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
        }

        Theme.applyToUIManager();

        // load saved data if it exists, or create some sample accounts
        boolean restored = Persistence.load();
        if (!restored) {
            DataStore.INSTANCE.seed();
        }

        new App().start();
    }
}
