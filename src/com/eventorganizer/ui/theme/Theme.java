package com.eventorganizer.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * Warm-obsidian palette + muted-bronze accent. The single source of truth for
 * every color, font, and L&F override the UI uses. Custom panels read the
 * constants below directly; FlatLaf-rendered widgets (tabs, buttons, inputs,
 * scrollbars) are reached via {@link #applyToUIManager()}.
 */
public final class Theme {
    private Theme() {}

    public static final Color BG_PRIMARY    = new Color(0x1B1612);
    public static final Color BG_ELEVATED   = new Color(0x231E19);
    public static final Color BG_HOVER      = new Color(0x2D2620);
    public static final Color BG_OVERLAY    = new Color(15, 12, 10, 153);
    public static final Color BORDER        = new Color(0x3A322B);
    public static final Color BORDER_SUBTLE = new Color(0x2A251F);

    public static final Color TEXT_PRIMARY  = new Color(0xEAE2D4);
    public static final Color TEXT_MUTED    = new Color(0xA89B8B);
    public static final Color TEXT_TERTIARY = new Color(0x7A6E61);

    public static final Color ACCENT         = new Color(0xB88D5E);
    public static final Color ACCENT_HOVER   = new Color(0xCA9E6C);
    public static final Color ACCENT_PRESSED = new Color(0x9C774E);
    public static final Color ACCENT_SOFT    = new Color(184, 141, 94, 46);

    public static final Color SUCCESS = new Color(0x7D9A6E);
    public static final Color WARNING = new Color(0xC9A264);
    public static final Color DANGER  = new Color(0xB06654);
    public static final Color INFO    = new Color(0x8A9BA8);

    public static final Color SUCCESS_SOFT = new Color(125, 154, 110, 46);
    public static final Color WARNING_SOFT = new Color(201, 162, 100, 46);
    public static final Color DANGER_SOFT  = new Color(176, 102, 84, 46);
    public static final Color INFO_SOFT    = new Color(138, 155, 168, 46);

    private static final String PREFERRED_SANS = "Inter";
    private static final String PREFERRED_MONO = "JetBrains Mono";

    private static final String SANS = resolveSans();
    private static final String MONO = resolveMono();

    public static final Font FONT_DISPLAY   = new Font(SANS, Font.BOLD,  22);
    public static final Font FONT_TITLE     = new Font(SANS, Font.BOLD,  16);
    public static final Font FONT_SUBTITLE  = new Font(SANS, Font.PLAIN, 14);
    public static final Font FONT_BODY      = new Font(SANS, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(SANS, Font.BOLD,  13);
    public static final Font FONT_SMALL     = new Font(SANS, Font.PLAIN, 11);
    public static final Font FONT_MONO      = new Font(MONO, Font.PLAIN, 12);

    private static String resolveSans() {
        String[] chain = { PREFERRED_SANS, "Segoe UI", "SF Pro Text", "SF Pro Display", Font.SANS_SERIF };
        return resolveFromChain(chain);
    }

    private static String resolveMono() {
        String[] chain = { PREFERRED_MONO, "Consolas", "Menlo", Font.MONOSPACED };
        return resolveFromChain(chain);
    }

    private static String resolveFromChain(String[] chain) {
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String name : chain) {
                if (available.contains(name)) return name;
            }
        } catch (RuntimeException ignored) { }
        return chain[chain.length - 1];
    }

    public static void applyToUIManager() {
        ColorUIResource bgPrimary  = new ColorUIResource(BG_PRIMARY);
        ColorUIResource bgElevated = new ColorUIResource(BG_ELEVATED);
        ColorUIResource bgHover    = new ColorUIResource(BG_HOVER);
        ColorUIResource border     = new ColorUIResource(BORDER);
        ColorUIResource borderSub  = new ColorUIResource(BORDER_SUBTLE);
        ColorUIResource textPri    = new ColorUIResource(TEXT_PRIMARY);
        ColorUIResource textMuted  = new ColorUIResource(TEXT_MUTED);
        ColorUIResource textTert   = new ColorUIResource(TEXT_TERTIARY);
        ColorUIResource accent     = new ColorUIResource(ACCENT);
        ColorUIResource accentHov  = new ColorUIResource(ACCENT_HOVER);
        ColorUIResource accentPr   = new ColorUIResource(ACCENT_PRESSED);
        ColorUIResource accentSoft = new ColorUIResource(ACCENT_SOFT);

        FontUIResource bodyFont  = new FontUIResource(FONT_BODY);
        FontUIResource smallFont = new FontUIResource(FONT_SMALL);

        UIManager.put("Panel.background",      bgPrimary);
        UIManager.put("RootPane.background",   bgPrimary);
        UIManager.put("OptionPane.background", bgPrimary);
        UIManager.put("Viewport.background",   bgPrimary);

        UIManager.put("Label.foreground",     textPri);
        UIManager.put("Label.disabledForeground", textTert);
        UIManager.put("Label.font",           bodyFont);

        UIManager.put("Button.background",         bgElevated);
        UIManager.put("Button.foreground",         textPri);
        UIManager.put("Button.hoverBackground",    bgHover);
        UIManager.put("Button.pressedBackground",  new ColorUIResource(0x1F1A15));
        UIManager.put("Button.disabledBackground", bgElevated);
        UIManager.put("Button.disabledText",       textTert);
        UIManager.put("Button.focusedBorderColor", accent);
        UIManager.put("Button.focusColor",         accent);
        UIManager.put("Button.borderColor",        border);
        UIManager.put("Button.arc",                Integer.valueOf(16));
        UIManager.put("Button.innerFocusWidth",    Integer.valueOf(0));
        UIManager.put("Button.font",               bodyFont);
        UIManager.put("Button.default.background",       accent);
        UIManager.put("Button.default.foreground",       new ColorUIResource(0x1B1612));
        UIManager.put("Button.default.hoverBackground",  accentHov);
        UIManager.put("Button.default.pressedBackground", accentPr);
        UIManager.put("Button.default.borderColor",      accent);
        UIManager.put("Button.default.focusedBorderColor", accentHov);
        UIManager.put("Button.default.boldText",         Boolean.TRUE);

        UIManager.put("ToggleButton.background",      bgElevated);
        UIManager.put("ToggleButton.foreground",      textPri);
        UIManager.put("ToggleButton.selectedBackground", accentSoft);
        UIManager.put("ToggleButton.selectedForeground", textPri);
        UIManager.put("ToggleButton.hoverBackground", bgHover);
        UIManager.put("ToggleButton.arc",             Integer.valueOf(16));
        UIManager.put("ToggleButton.borderColor",     border);
        UIManager.put("ToggleButton.focusedBorderColor", accent);

        UIManager.put("TabbedPane.background",           bgPrimary);
        UIManager.put("TabbedPane.foreground",           textMuted);
        UIManager.put("TabbedPane.selectedForeground",   textPri);
        UIManager.put("TabbedPane.selectedBackground",   bgPrimary);
        UIManager.put("TabbedPane.hoverColor",           bgHover);
        UIManager.put("TabbedPane.focusColor",           accent);
        UIManager.put("TabbedPane.underlineColor",       accent);
        UIManager.put("TabbedPane.disabledUnderlineColor", border);
        UIManager.put("TabbedPane.inactiveUnderlineColor", border);
        UIManager.put("TabbedPane.contentAreaColor",     bgPrimary);
        UIManager.put("TabbedPane.tabSeparatorColor",    borderSub);
        UIManager.put("TabbedPane.tabHeight",            Integer.valueOf(36));
        UIManager.put("TabbedPane.tabInsets",            new java.awt.Insets(8, 16, 8, 16));
        UIManager.put("TabbedPane.hasFullBorder",        Boolean.FALSE);
        UIManager.put("TabbedPane.tabsOverlapBorder",    Boolean.TRUE);
        UIManager.put("TabbedPane.showTabSeparators",    Boolean.FALSE);
        UIManager.put("TabbedPane.font",                 bodyFont);

        UIManager.put("TextField.background",          bgElevated);
        UIManager.put("TextField.foreground",          textPri);
        UIManager.put("TextField.caretForeground",     accent);
        UIManager.put("TextField.selectionBackground", accentSoft);
        UIManager.put("TextField.selectionForeground", textPri);
        UIManager.put("TextField.borderColor",         border);
        UIManager.put("TextField.focusedBorderColor",  accent);
        UIManager.put("TextField.placeholderForeground", textTert);
        UIManager.put("TextField.arc",                 Integer.valueOf(16));
        UIManager.put("TextField.font",                bodyFont);
        UIManager.put("TextField.disabledBackground",  bgPrimary);
        UIManager.put("TextField.disabledForeground",  textTert);

        UIManager.put("PasswordField.background",          bgElevated);
        UIManager.put("PasswordField.foreground",          textPri);
        UIManager.put("PasswordField.caretForeground",     accent);
        UIManager.put("PasswordField.selectionBackground", accentSoft);
        UIManager.put("PasswordField.selectionForeground", textPri);
        UIManager.put("PasswordField.borderColor",         border);
        UIManager.put("PasswordField.focusedBorderColor",  accent);
        UIManager.put("PasswordField.arc",                 Integer.valueOf(16));
        UIManager.put("PasswordField.font",                bodyFont);

        UIManager.put("TextArea.background",          bgElevated);
        UIManager.put("TextArea.foreground",          textPri);
        UIManager.put("TextArea.caretForeground",     accent);
        UIManager.put("TextArea.selectionBackground", accentSoft);
        UIManager.put("TextArea.selectionForeground", textPri);
        UIManager.put("TextArea.font",                bodyFont);

        UIManager.put("ScrollBar.thumb",            border);
        UIManager.put("ScrollBar.track",            bgPrimary);
        UIManager.put("ScrollBar.hoverThumbColor",  textTert);
        UIManager.put("ScrollBar.pressedThumbColor", textMuted);
        UIManager.put("ScrollBar.width",            Integer.valueOf(10));
        UIManager.put("ScrollBar.thumbArc",         Integer.valueOf(10));
        UIManager.put("ScrollBar.showButtons",      Boolean.FALSE);
        UIManager.put("ScrollPane.background",      bgPrimary);
        UIManager.put("ScrollPane.borderColor",     borderSub);

        UIManager.put("List.background",              bgPrimary);
        UIManager.put("List.foreground",              textPri);
        UIManager.put("List.selectionBackground",     accentSoft);
        UIManager.put("List.selectionForeground",     textPri);
        UIManager.put("List.selectionInactiveBackground", bgHover);
        UIManager.put("List.font",                    bodyFont);

        UIManager.put("ComboBox.background",           bgElevated);
        UIManager.put("ComboBox.foreground",           textPri);
        UIManager.put("ComboBox.buttonBackground",     bgElevated);
        UIManager.put("ComboBox.buttonEditableBackground", bgElevated);
        UIManager.put("ComboBox.buttonArrowColor",     textMuted);
        UIManager.put("ComboBox.buttonHoverArrowColor", textPri);
        UIManager.put("ComboBox.popupBackground",      bgElevated);
        UIManager.put("ComboBox.selectionBackground",  accentSoft);
        UIManager.put("ComboBox.selectionForeground",  textPri);
        UIManager.put("ComboBox.borderColor",          border);
        UIManager.put("ComboBox.focusedBorderColor",   accent);
        UIManager.put("ComboBox.arc",                  Integer.valueOf(16));
        UIManager.put("ComboBox.font",                 bodyFont);

        UIManager.put("Spinner.background",         bgElevated);
        UIManager.put("Spinner.foreground",         textPri);
        UIManager.put("Spinner.buttonBackground",   bgElevated);
        UIManager.put("Spinner.buttonArrowColor",   textMuted);
        UIManager.put("Spinner.buttonHoverArrowColor", textPri);
        UIManager.put("Spinner.borderColor",        border);
        UIManager.put("Spinner.focusedBorderColor", accent);
        UIManager.put("Spinner.arc",                Integer.valueOf(16));
        UIManager.put("Spinner.font",               bodyFont);

        UIManager.put("CheckBox.background", bgPrimary);
        UIManager.put("CheckBox.foreground", textPri);
        UIManager.put("CheckBox.icon.borderColor",        border);
        UIManager.put("CheckBox.icon.selectedBorderColor", accent);
        UIManager.put("CheckBox.icon.focusedBorderColor",  accent);
        UIManager.put("CheckBox.icon.checkmarkColor",      new ColorUIResource(0x1B1612));
        UIManager.put("CheckBox.icon.selectedBackground",  accent);

        UIManager.put("RadioButton.background", bgPrimary);
        UIManager.put("RadioButton.foreground", textPri);
        UIManager.put("RadioButton.icon.borderColor",         border);
        UIManager.put("RadioButton.icon.selectedBorderColor", accent);
        UIManager.put("RadioButton.icon.focusedBorderColor",  accent);
        UIManager.put("RadioButton.icon.centerDiameter",      Integer.valueOf(8));
        UIManager.put("RadioButton.icon.selectedBackground",  accent);

        UIManager.put("ToolTip.background", bgElevated);
        UIManager.put("ToolTip.foreground", textPri);
        UIManager.put("ToolTip.borderColor", border);
        UIManager.put("ToolTip.font",        smallFont);

        UIManager.put("Separator.foreground", borderSub);
        UIManager.put("Separator.background", bgPrimary);

        UIManager.put("TitledBorder.titleColor", textMuted);
        UIManager.put("TitledBorder.font",       smallFont);

        UIManager.put("ProgressBar.background",       bgElevated);
        UIManager.put("ProgressBar.foreground",       accent);
        UIManager.put("ProgressBar.selectionForeground", textPri);
        UIManager.put("ProgressBar.selectionBackground", textPri);
        UIManager.put("ProgressBar.arc",              Integer.valueOf(10));
    }
}
