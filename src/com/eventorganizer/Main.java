package com.eventorganizer;

import com.eventorganizer.store.DataStore;
import com.eventorganizer.ui.App;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
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
