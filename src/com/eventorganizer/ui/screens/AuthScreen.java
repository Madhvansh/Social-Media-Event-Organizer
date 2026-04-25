package com.eventorganizer.ui.screens;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.fx.Constellation;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraPasswordField;
import com.eventorganizer.ui.laf.AuroraTextField;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.PasswordHasher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Cinematic auth surface. A {@link Constellation} particle field drifts behind
 * a 520×580 glass card; the card hosts a segmented Login/Register toggle and
 * AuroraTextField inputs with focus underlines.
 */
public class AuthScreen extends JPanel {

    private final UIController controller;
    private final Consumer<Void> onAuthenticated;

    public AuthScreen(UIController controller, Consumer<Void> onAuthenticated) {
        this.controller = controller;
        this.onAuthenticated = onAuthenticated;
        setOpaque(false);
        setLayout(new OverlayLayout(this));

        // Constellation particle field
        Constellation stars = new Constellation();
        stars.setAlignmentX(0f);
        stars.setAlignmentY(0f);

        // Centered glass card
        JPanel centering = new JPanel(new GridBagLayout());
        centering.setOpaque(false);
        centering.setAlignmentX(0f);
        centering.setAlignmentY(0f);
        centering.add(buildCard(), new GridBagConstraints());

        // Overlay: content on top, constellation below
        add(centering);
        add(stars);
    }

    private JPanel buildCard() {
        GlassCard card = new GlassCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            Spacing.XXL + 8, Spacing.XXL + 8, Spacing.XXL, Spacing.XXL + 8));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel wordmark = new JLabel("Event Organizer", SwingConstants.LEFT);
        wordmark.setFont(Typography.DISPLAY);
        wordmark.setForeground(Theme.TEXT_PRIMARY);
        wordmark.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Plan · invite · remember.");
        tagline.setFont(Typography.BODY);
        tagline.setForeground(Theme.TEXT_SECONDARY);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));

        header.add(wordmark);
        header.add(tagline);

        // Segmented toggle + card body
        SegmentedControl toggle = new SegmentedControl();
        toggle.addSegment("Log in");
        toggle.addSegment("Register");

        CardLayout bodyCards = new CardLayout();
        JPanel body = new JPanel(bodyCards);
        body.setOpaque(false);
        JPanel login = buildLoginPanel();
        JPanel register = buildRegisterPanel();
        body.add(login, "login");
        body.add(register, "register");

        toggle.onChange(idx -> bodyCards.show(body, idx == 0 ? "login" : "register"));

        JPanel toggleWrap = new JPanel(new BorderLayout());
        toggleWrap.setOpaque(false);
        toggleWrap.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, 0, Spacing.L, 0));
        toggleWrap.add(toggle, BorderLayout.WEST);

        JLabel hint = new JLabel("Seeded demo users: alice · bob · carol");
        hint.setFont(Typography.SMALL);
        hint.setForeground(Theme.TEXT_TERTIARY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        hint.setBorder(BorderFactory.createEmptyBorder(Spacing.L, 0, 0, 0));

        card.add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(toggleWrap, BorderLayout.NORTH);
        center.add(body, BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        AuroraTextField username = new AuroraTextField("Username");
        AuroraPasswordField password = new AuroraPasswordField("Password");

        FormField userFF = new FormField("USERNAME", username);
        FormField passFF = new FormField("PASSWORD", password);

        AuroraButton login = new AuroraButton("Log in", AuroraButton.Variant.DEFAULT);
        login.setMnemonic('L');
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        login.addActionListener(e -> doLogin(username, password, userFF, passFF));

        panel.add(userFF);
        panel.add(passFF);
        panel.add(Box.createVerticalStrut(Spacing.S));
        panel.add(login);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        AuroraTextField username = new AuroraTextField("Username");
        AuroraTextField email = new AuroraTextField("you@example.com");
        AuroraPasswordField pw1 = new AuroraPasswordField("New password");
        AuroraPasswordField pw2 = new AuroraPasswordField("Confirm password");

        FormField uFF = new FormField("USERNAME", username);
        FormField eFF = new FormField("EMAIL", email);
        FormField p1FF = new FormField("PASSWORD", pw1);
        FormField p2FF = new FormField("CONFIRM PASSWORD", pw2);

        AuroraButton register = new AuroraButton("Create account", AuroraButton.Variant.DEFAULT);
        register.setMnemonic('C');
        register.setAlignmentX(Component.LEFT_ALIGNMENT);
        register.addActionListener(e ->
            doRegister(username, email, pw1, pw2, uFF, eFF, p1FF, p2FF));

        panel.add(uFF);
        panel.add(eFF);
        panel.add(p1FF);
        panel.add(p2FF);
        panel.add(Box.createVerticalStrut(Spacing.S));
        panel.add(register);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void doLogin(AuroraTextField username, AuroraPasswordField pw,
                         FormField userFF, FormField pwFF) {
        userFF.clearError();
        pwFF.clearError();
        char[] raw = pw.getPassword();
        try {
            controller.login(username.getText().trim(), raw);
            Toast.success(this, "Welcome back!");
            onAuthenticated.accept(null);
        } catch (AppException ex) {
            pwFF.setError(ex.getMessage());
            Toast.error(this, ex.getMessage());
        } finally {
            Arrays.fill(raw, '\0');
            pw.setText("");
        }
    }

    private void doRegister(AuroraTextField username, AuroraTextField email,
                            AuroraPasswordField pw1, AuroraPasswordField pw2,
                            FormField uFF, FormField eFF, FormField p1FF, FormField p2FF) {
        uFF.clearError(); eFF.clearError(); p1FF.clearError(); p2FF.clearError();
        char[] a = pw1.getPassword();
        char[] b = pw2.getPassword();
        try {
            if (!passwordsMatch(a, b)) {
                p2FF.setError("Passwords do not match.");
                Toast.error(this, "Passwords do not match.");
                return;
            }
            controller.register(username.getText().trim(), email.getText().trim(), a);
            controller.login(username.getText().trim(), b);
            Toast.success(this, "Account created.");
            onAuthenticated.accept(null);
        } catch (AppException ex) {
            p1FF.setError(ex.getMessage());
            Toast.error(this, ex.getMessage());
        } finally {
            PasswordHasher.zero(a);
            PasswordHasher.zero(b);
            pw1.setText("");
            pw2.setText("");
        }
    }

    private boolean passwordsMatch(char[] a, char[] b) {
        if (a == null || b == null) return false;
        byte[] ab = toBytes(a);
        byte[] bb = toBytes(b);
        try {
            return MessageDigest.isEqual(ab, bb);
        } finally {
            Arrays.fill(ab, (byte) 0);
            Arrays.fill(bb, (byte) 0);
        }
    }

    private byte[] toBytes(char[] chars) {
        byte[] out = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            out[i * 2]     = (byte) ((chars[i] >> 8) & 0xFF);
            out[i * 2 + 1] = (byte) (chars[i] & 0xFF);
        }
        return out;
    }

    /** Fixed-size glass card with a soft E3 shadow behind it. */
    private static final class GlassCard extends JPanel {
        GlassCard() {
            setOpaque(false);
        }
        @Override public Dimension getPreferredSize() { return new Dimension(520, 600); }
        @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int arc = Radius.XL;
                Elevation.paint(g2, 0, 0, w, h - 2, arc, Elevation.Tier.E3);
                g2.setPaint(Gradient.glassFill(w, h));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            } finally {
                g2.dispose();
            }
        }
    }
}
