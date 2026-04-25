package com.eventorganizer;

import com.eventorganizer.store.DataStore;
import com.eventorganizer.ui.App;
import com.eventorganizer.ui.theme.FontLoader;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.LogConfig;
import com.eventorganizer.utils.ShutdownHook;

import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        LogConfig.init();
        ShutdownHook.install();

        // Register vendored Inter / JetBrains Mono before the LAF + Theme read them.
        FontLoader.load();

        try {
            Class<?> flat = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            flat.getMethod("setup").invoke(null);
        } catch (ReflectiveOperationException e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
        }

        Theme.applyToUIManager();

        DataStore.INSTANCE.seed();
        new App().start();
    }
}
