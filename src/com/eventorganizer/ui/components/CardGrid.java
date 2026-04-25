package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Spacing;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * Responsive tile grid. Two columns when width ≥ {@value #TWO_COL_MIN} px,
 * one column below. Equal-width tiles, fixed gap, natural vertical flow.
 *
 * <p>Add cards with {@link #addCard(Component)} or the usual {@link #add} methods.
 */
public final class CardGrid extends JPanel {

    public static final int TWO_COL_MIN = 1100;

    private final int gap;

    public CardGrid() { this(Spacing.L); }
    public CardGrid(int gap) {
        super();
        this.gap = gap;
        setLayout(new GridLayout());
        setOpaque(false);
    }

    public void addCard(Component c) { add(c); }

    private final class GridLayout implements LayoutManager2 {
        @Override public void addLayoutComponent(String name, Component comp) {}
        @Override public void removeLayoutComponent(Component comp) {}
        @Override public void addLayoutComponent(Component comp, Object constraints) {}
        @Override public float getLayoutAlignmentX(Container target) { return 0f; }
        @Override public float getLayoutAlignmentY(Container target) { return 0f; }
        @Override public void invalidateLayout(Container target) {}

        @Override public Dimension preferredLayoutSize(Container parent) {
            return sizeFor(parent, true);
        }
        @Override public Dimension minimumLayoutSize(Container parent) {
            return sizeFor(parent, true);
        }
        @Override public Dimension maximumLayoutSize(Container parent) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        private Dimension sizeFor(Container parent, boolean preferred) {
            Insets in = parent.getInsets();
            int cols = parent.getWidth() >= TWO_COL_MIN ? 2 : 1;
            int cells = parent.getComponentCount();
            int rows = (cells + cols - 1) / Math.max(1, cols);
            int totalWidth = parent.getWidth() - in.left - in.right;
            if (totalWidth <= 0) totalWidth = 800;
            int cellW = (totalWidth - gap * (cols - 1)) / Math.max(1, cols);

            int tallest = 0;
            for (int i = 0; i < cells; i++) {
                Component c = parent.getComponent(i);
                int h = c.getPreferredSize().height;
                if (h > tallest) tallest = h;
            }
            int h = tallest * rows + gap * Math.max(0, rows - 1) + in.top + in.bottom;
            return new Dimension(cellW * cols + gap * (cols - 1) + in.left + in.right, h);
        }

        @Override public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int cols = parent.getWidth() >= TWO_COL_MIN ? 2 : 1;
            int totalWidth = parent.getWidth() - in.left - in.right;
            int cellW = (totalWidth - gap * (cols - 1)) / Math.max(1, cols);
            int y = in.top;
            int maxRowH = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                int col = i % cols;
                int row = i / cols;
                Component c = parent.getComponent(i);
                int h = c.getPreferredSize().height;
                if (col == 0) {
                    if (row > 0) y += maxRowH + gap;
                    maxRowH = h;
                } else if (h > maxRowH) {
                    maxRowH = h;
                }
                int x = in.left + col * (cellW + gap);
                c.setBounds(x, y, cellW, h);
            }
        }
    }
}
