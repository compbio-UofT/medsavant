/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.view.util;

import com.explodingpixels.macwidgets.SourceListControlBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.images.IconFactory;

/**
 *
 * @author mfiume
 */
public class PeekingPanelContainer extends JPanel {

    private final JPanel container;

    private PeekingPanel NORTH_SIDE_PANEL;
    private PeekingPanel SOUTH_SIDE_PANEL;
    private PeekingPanel WEST_SIDE_PANEL;
    private PeekingPanel EAST_SIDE_PANEL;
    private JComponent lastComponent;

    public PeekingPanelContainer(JPanel child) {

        this.container = new JPanel();
        this.container.setLayout(new BorderLayout());
        this.container.add(child, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);

        updateControlBar();

    }

    public PeekingPanel addPeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded) {
        return addPeekingPanel(label, borderLayoutPosition, panel, isExpanded, -1);
    }

    public PeekingPanel addPeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded, int size) {

        PeekingPanel p;
        if (size == -1) {
            p = new PeekingPanel(label, borderLayoutPosition, panel, isExpanded);
        } else {
            p = new PeekingPanel(label, borderLayoutPosition, panel, isExpanded, size);
        }
        p.setToggleBarVisible(false);

        if (borderLayoutPosition.equals(BorderLayout.NORTH)) {
            NORTH_SIDE_PANEL = p;
        } else if (borderLayoutPosition.equals(BorderLayout.EAST)) {
            EAST_SIDE_PANEL = p;
        } else if (borderLayoutPosition.equals(BorderLayout.SOUTH)) {
            SOUTH_SIDE_PANEL = p;
        } else if (borderLayoutPosition.equals(BorderLayout.WEST)) {
            WEST_SIDE_PANEL = p;
        }

        container.add(p, borderLayoutPosition);
        updateControlBar();

        return p;
    }

    public static void main(String[] argv) {
        JFrame f = new JFrame();
        PeekingPanelContainer pc = new PeekingPanelContainer(new JPanel());

        f.setLayout(new BorderLayout());

        pc.addPeekingPanel("Blah", BorderLayout.WEST, new JPanel(), true);
        pc.addPeekingPanel("Blah", BorderLayout.NORTH, new JPanel(), true);
        pc.addPeekingPanel("Blah", BorderLayout.EAST, new JPanel(), true);
        pc.addPeekingPanel("Blah", BorderLayout.SOUTH, new JPanel(), true);

        f.add(pc, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }

    private void updateControlBar() {

        SourceListControlBar controlbar = new SourceListControlBar();

        int counter = 0;
        for (final PeekingPanel p : new PeekingPanel[]{WEST_SIDE_PANEL, NORTH_SIDE_PANEL, SOUTH_SIDE_PANEL, EAST_SIDE_PANEL}) {
            counter++;
            if (p == null) {
                continue;
            }

            boolean isExpanded = p.isExpanded();

            String iconroot = "/org/ut/biolab/medsavant/client/view/images/icon/view-sidebar";
            String side = "";
            String selected = isExpanded ? "-selected" : "";

            switch (counter) {
                case 1: // west
                    side = "west";
                    break;
                case 2: // north
                    side = "north";
                    break;
                case 3: // south
                    side = "south";
                    break;
                case 4: // east
                    side = "east";
                    break;

            }

            String path = String.format("%s-%s%s.png", iconroot, side, selected);
            ImageIcon icon = IconFactory.getInstance().getIcon(path);

            controlbar.createAndAddButton(icon, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    p.setExpanded(!p.isExpanded());
                    updateControlBar();
                }
            });
        }

        if (lastComponent != null) {
            this.remove(lastComponent);
        }
        lastComponent = controlbar.getComponent();
        this.add(lastComponent, BorderLayout.SOUTH);
        this.updateUI();
    }

}
