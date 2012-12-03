/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.client.view.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;


/**
 *
 * @author mfiume
 */
public class PeekingPanel extends JPanel {

    private final JComponent panel;
    private boolean isExpanded;
    private final JLabel title;
    public final String titleString;
    private final JPanel titlePanel;

    public PeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded) {
        this(label, borderLayoutPosition, panel, isExpanded, 350);
    }

    public PeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded, int size) {

        final boolean isVertical = borderLayoutPosition.equals(BorderLayout.EAST) || borderLayoutPosition.equals(BorderLayout.WEST);

        this.setLayout(new BorderLayout());
        this.panel = panel;

        if (isVertical) {
            panel.setPreferredSize(new Dimension(size, 999));
        } else {
            panel.setPreferredSize(new Dimension(999, size));
        }
        titlePanel = new JPanel();


        titlePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titlePanel.setBorder(ViewUtil.getTinyBorder());
        if (isVertical) {
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        } else {
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        }
        titlePanel.add(Box.createHorizontalGlue());

        this.titleString = label.toUpperCase();
        title = new JLabel(" ");//titleString);

        title.setForeground(Color.darkGray);
        if (borderLayoutPosition.equals(BorderLayout.EAST)) {
            title.setUI(new VerticalLabelUI(true));
        } else if (borderLayoutPosition.equals(BorderLayout.WEST)) {
            title.setUI(new VerticalLabelUI(false));
        }
        titlePanel.add(title);

        if (!isVertical) {
            titlePanel.add(Box.createHorizontalGlue());
        }

        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpanded();
            }
        });

        this.add(titlePanel, borderLayoutPosition);
        this.add(panel, BorderLayout.CENTER);

        setExpanded(isExpanded);
    }

    public final void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        String s = (this.isExpanded ? " HIDE " + titleString : " SHOW " + titleString) + " ";
        titlePanel.setToolTipText(s);
        if (!this.isExpanded) {
            this.title.setText(s);
            title.setFont(ViewUtil.getSmallTitleFont());
        } else {
            this.title.setText(s);
            title.setFont(ViewUtil.getSmallTitleFont());
        }
        panel.setVisible(isExpanded);
    }

    public void toggleExpanded() {
        setExpanded(!this.isExpanded);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setToggleBarVisible(boolean b) {
        this.titlePanel.setVisible(b);
    }

    public static JCheckBox getCheckBoxForPanel(final PeekingPanel persistencePanel) {
        return getCheckBoxForPanel(persistencePanel,persistencePanel.titleString);
    }

    public static JCheckBox getCheckBoxForPanel(final PeekingPanel persistencePanel,String name) {
        final JCheckBox box = new JCheckBox("Show " + name);
        box.setFont(new Font("Arial", Font.BOLD, 12));
        box.setForeground(Color.white);
        box.setOpaque(false);
        box.setSelected(true);

        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                persistencePanel.setExpanded(box.isSelected());
            }
        });
        box.setSelected(persistencePanel.isExpanded);
        return box;
    }
}
