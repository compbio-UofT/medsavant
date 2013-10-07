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
import org.apache.commons.httpclient.NameValuePair;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.savant.analytics.savantanalytics.AnalyticsAgent;

/**
 *
 * @author mfiume
 */
public class PeekingPanel extends JXCollapsiblePane {

    private final JComponent panel;
    private boolean isExpanded;
    private final JLabel title;
    public final String titleString;
    private final JPanel titlePanel;
    private final DockedSide dockedSide;

    public PeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded) {
        this(label, borderLayoutPosition, panel, isExpanded, 350);
    }

    public PeekingPanel(String label, String borderLayoutPosition, JComponent panel, boolean isExpanded, int size) {

        final boolean isVertical = borderLayoutPosition.equals(BorderLayout.EAST) || borderLayoutPosition.equals(BorderLayout.WEST);

        this.setAnimated(false);

        if (borderLayoutPosition.equals(BorderLayout.NORTH)) {
            dockedSide = DockedSide.NORTH;
            this.setDirection(JXCollapsiblePane.Direction.UP);
        } else if (borderLayoutPosition.equals(BorderLayout.SOUTH)) {
            dockedSide = DockedSide.SOUTH;
            this.setDirection(JXCollapsiblePane.Direction.DOWN);
        } else if (borderLayoutPosition.equals(BorderLayout.EAST)) {
            dockedSide = DockedSide.EAST;
            this.setDirection(JXCollapsiblePane.Direction.RIGHT);
        } else {
            dockedSide = DockedSide.WEST;
            this.setDirection(JXCollapsiblePane.Direction.LEFT);
        }

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
            panel.setVisible(false);
        } else {
            this.title.setText(s);
            title.setFont(ViewUtil.getSmallTitleFont());
            panel.setVisible(true);
        }
        //panel.setVisible(isExpanded);
        this.setCollapsed(!isExpanded);
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

    public static JToggleButton getToggleButtonForPanel(final PeekingPanel persistencePanel) {
        return getToggleButtonForPanel(persistencePanel, persistencePanel.titleString);
    }

    public static JToggleButton getToggleButtonForPanel(final PeekingPanel persistencePanel, final String name) {

        final RevealToggleButton b = new RevealToggleButton(name, persistencePanel.getDockedSide());

        b.setFocusable(false);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                persistencePanel.setExpanded(b.isSelected());

                try {
                    AnalyticsAgent.log(new NameValuePair[]{
                                new NameValuePair("view-event", b.isSelected() ? "PanelShown" : "PanelHidden"),
                                new NameValuePair("panel-name", name)
                            });
                } catch (Exception e) {
                }

            }
        });

        b.setSelected(persistencePanel.isExpanded);

        return b;
    }

    public DockedSide getDockedSide() {
        return dockedSide;
    }

    public enum DockedSide {

        EAST, WEST, NORTH, SOUTH
    };

    public static class RevealToggleButton extends JToggleButton {

        private final DockedSide side;

        public RevealToggleButton(String title, DockedSide side) {
            super(title);

            //this.setFont(new Font(this.getFont().getFamily(),10,Font.PLAIN));
            //this.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
            this.side = side;

            this.putClientProperty("JButton.buttonType", "segmentedTextured");
            //this.setForeground(Color.gray);

            switch (side) {
                case EAST:
                    ViewUtil.positionButtonFirst(this);
                    break;
                case WEST:
                    ViewUtil.positionButtonLast(this);
                    break;
                case NORTH:
                case SOUTH:
                    ViewUtil.positionButtonMiddle(this);
                    break;
            }
        }

        public DockedSide getSide() {
            return side;
        }
    }
}
