/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
            title.setFont(ViewUtil.getTinyTitleFont());
            panel.setVisible(false);
        } else {
            this.title.setText(s);
            title.setFont(ViewUtil.getTinyTitleFont());
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
