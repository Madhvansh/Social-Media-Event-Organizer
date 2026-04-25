package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.ui.components.Avatar;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.components.TrackedLabel;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ChangePasswordDialog;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraTextField;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/**
 * Profile panel rebuilt as: hero block (96 px Avatar + display name +
 * @handle + member-since), two-column body (left = email/bio edit form,
 * right = stat tiles), danger zone at the bottom.
 */
public class ProfilePanel extends JPanel {

    private final UIController controller;
    private final JPanel body = new JPanel();
    private final JScrollPane scroll;

    public ProfilePanel(UIController controller) {
        this.controller = controller;
        setOpaque(false);
        setLayout(new BorderLayout());

        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL));

        scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        try {
            UserProfileDTO p = controller.getProfile();
            body.removeAll();

            body.add(buildHero(p));
            body.add(Box.createVerticalStrut(Spacing.XL));
            body.add(buildEditAndStats(p));
            body.add(Box.createVerticalStrut(Spacing.XL));
            body.add(buildAccessibilityRow());
            body.add(Box.createVerticalStrut(Spacing.M));
            body.add(buildDangerZone());
            body.add(Box.createVerticalGlue());

            body.revalidate();
            body.repaint();
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private JPanel buildHero(UserProfileDTO p) {
        JPanel hero = new JPanel(new BorderLayout(Spacing.XL, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int arc = Radius.XL;
                    Elevation.paint(g2, 0, 0, w, h - 1, arc, Elevation.Tier.E2);
                    g2.setPaint(Gradient.elevatedWash(w, h));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(Theme.BORDER_GLOW);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                } finally {
                    g2.dispose();
                }
            }
        };
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        hero.setPreferredSize(new Dimension(0, 200));

        Avatar avatar = new Avatar(p.getUsername(), Avatar.Size.S96);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        TrackedLabel kicker = new TrackedLabel("MEMBER",
            Typography.LABEL.deriveFont(11f), Theme.ACCENT, 0.14f);
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel name = new JLabel(SwingText.plain(p.getUsername()));
        name.setFont(Typography.DISPLAY);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel handle = new JLabel("@" + p.getUsername().toLowerCase());
        handle.setFont(Typography.MONO);
        handle.setForeground(Theme.TEXT_SECONDARY);
        handle.setAlignmentX(Component.LEFT_ALIGNMENT);
        handle.setBorder(BorderFactory.createEmptyBorder(2, 0, Spacing.S, 0));

        JLabel since = new JLabel("Member since " + DateUtil.format(p.getMemberSince()));
        since.setFont(Typography.SMALL);
        since.setForeground(Theme.TEXT_TERTIARY);
        since.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(kicker);
        text.add(Box.createVerticalStrut(Spacing.XS));
        text.add(name);
        text.add(handle);
        text.add(since);

        hero.add(avatar, BorderLayout.WEST);
        hero.add(text, BorderLayout.CENTER);
        return hero;
    }

    private JPanel buildEditAndStats(UserProfileDTO p) {
        JPanel two = new JPanel(new GridLayout(1, 2, Spacing.L, 0));
        two.setOpaque(false);
        two.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: edit form
        JPanel left = new JPanel();
        left.setOpaque(true);
        left.setBackground(Theme.BG_ELEVATED);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL)));

        JLabel editTitle = new JLabel("Edit profile");
        editTitle.setFont(Typography.H2);
        editTitle.setForeground(Theme.TEXT_PRIMARY);
        editTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        AuroraTextField email = new AuroraTextField(p.getEmail());
        email.setText(p.getEmail() == null ? "" : p.getEmail());
        JTextArea bio = new JTextArea(p.getBio() == null ? "" : p.getBio(), 4, 20);
        bio.setLineWrap(true); bio.setWrapStyleWord(true);
        JScrollPane bioScroll = new JScrollPane(bio);

        FormField emailFF = new FormField("EMAIL", email);
        FormField bioFF = new FormField("BIO", bioScroll);

        AuroraButton save = new AuroraButton("Save profile", AuroraButton.Variant.DEFAULT);
        save.setMnemonic('S');
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            try {
                controller.updateProfile(email.getText().trim(), bio.getText());
                Toast.success(this, "Profile updated.");
                refresh();
            } catch (AppException ex) {
                emailFF.setError(ex.getMessage());
                Toast.error(this, ex.getMessage());
            }
        });

        left.add(editTitle);
        left.add(Box.createVerticalStrut(Spacing.M));
        left.add(emailFF);
        left.add(bioFF);
        left.add(Box.createVerticalStrut(Spacing.M));
        left.add(save);

        // Right: stat tiles
        JPanel right = new JPanel(new GridLayout(2, 2, Spacing.M, Spacing.M));
        right.setOpaque(false);
        right.add(statTile("FRIENDS",        String.valueOf(p.getFriendCount())));
        right.add(statTile("EVENTS CREATED", String.valueOf(p.getEventsCreated())));
        right.add(statTile("USER ID",        truncate(p.getUserId(), 12)));
        right.add(statTile("ACCOUNT AGE",    accountAge(p)));

        two.add(left);
        two.add(right);
        return two;
    }

    private JPanel statTile(String label, String value) {
        JPanel tile = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    int arc = Radius.LG;
                    Elevation.paint(g2, 0, 0, w, h - 1, arc, Elevation.Tier.E1);
                    g2.setPaint(Gradient.elevatedWash(w, h));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                    g2.setColor(Theme.BORDER_SUBTLE);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                } finally {
                    g2.dispose();
                }
            }
        };
        tile.setOpaque(false);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.L, Spacing.L, Spacing.L));

        TrackedLabel l = new TrackedLabel(label, Typography.LABEL, Theme.TEXT_SECONDARY, 0.08f);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = new JLabel(value);
        v.setFont(Typography.H1);
        v.setForeground(Theme.TEXT_PRIMARY);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        v.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));
        tile.add(l);
        tile.add(v);
        return tile;
    }

    /**
     * Accessibility settings row. Currently hosts the reduced-motion toggle —
     * mirrors the AuthScreen control so users can flip it without logging out.
     */
    private JPanel buildAccessibilityRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(Theme.BG_ELEVATED);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel zoneLabel = new JLabel("Accessibility");
        zoneLabel.setFont(Typography.BODY_BOLD);
        zoneLabel.setForeground(Theme.TEXT_PRIMARY);
        zoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = new JLabel("Freeze the aurora backdrop and disable transitions.");
        hint.setFont(Typography.SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        left.add(zoneLabel);
        left.add(hint);

        com.eventorganizer.ui.components.MotionToggle toggle =
            new com.eventorganizer.ui.components.MotionToggle();
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(toggle);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel buildDangerZone() {
        JPanel d = new JPanel(new BorderLayout());
        d.setOpaque(true);
        d.setBackground(Theme.DANGER_SOFT);
        d.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DANGER, 1, true),
            BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L)));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel zoneLabel = new JLabel("Danger zone");
        zoneLabel.setFont(Typography.BODY_BOLD);
        zoneLabel.setForeground(Theme.DANGER);
        d.add(zoneLabel, BorderLayout.WEST);

        AuroraButton changePw = new AuroraButton("Change password", AuroraButton.Variant.GHOST);
        changePw.setMnemonic('P');
        changePw.addActionListener(e -> ChangePasswordDialog.show(this, controller));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        actions.setOpaque(false);
        actions.add(changePw);
        d.add(actions, BorderLayout.EAST);
        return d;
    }

    private static String accountAge(UserProfileDTO p) {
        if (p.getMemberSince() == null) return "—";
        long days = java.time.Duration.between(
            p.getMemberSince(), java.time.LocalDateTime.now()).toDays();
        if (days < 1) return "today";
        if (days < 30) return days + " days";
        if (days < 365) return (days / 30) + " months";
        return (days / 365) + " years";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
