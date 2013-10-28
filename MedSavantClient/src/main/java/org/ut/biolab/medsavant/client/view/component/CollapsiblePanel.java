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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CollapsiblePanel extends JPanel {

    private final JPanel titlePanel;
    private final JPanel titleButtonsPanel;
    private final JPanel contentPanel;

    private final boolean isCollapsable;
    private final JLabel expandButton;
    private final JLabel collapseButton;

    private final MouseListener toggler = new MouseListener() {

            public void mouseClicked(MouseEvent me) {
                setContentPaneVisible(!contentPanel.isVisible());
            }

            public void mousePressed(MouseEvent me) {
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }

        };
    private final JLabel descriptionLabel;
    private final JLabel titleLabel;

    public CollapsiblePanel(String title) {
        this(title,true);
    }

    public CollapsiblePanel(String title, boolean isCollapsable) {
        ViewUtil.applyVerticalBoxLayout(this);
        ViewUtil.clear(this);

        titlePanel = ViewUtil.getSecondaryBannerPanel();//new JPanel();
        ViewUtil.applyHorizontalBoxLayout(titlePanel);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
                ViewUtil.getTinyLineBorder(),
                ViewUtil.getMediumBorder()));

        this.isCollapsable = isCollapsable;

        if (isCollapsable) {
            expandButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPAND));
            collapseButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.COLLAPSE));

            titlePanel.add(expandButton);
            titlePanel.add(collapseButton);

            expandButton.addMouseListener(toggler);
            collapseButton.addMouseListener(toggler);


        } else {
            expandButton = null;
            collapseButton = null;
        }

        // title label
        titleLabel = new JLabel(title);
        titleLabel.setFont(ViewUtil.getMediumTitleFont());
        titlePanel.add(titleLabel);

        descriptionLabel = new JLabel("");
        descriptionLabel.setFont(ViewUtil.getMediumTitleFont());
        //titleLabel.setForeground(Color.white);

        // place for buttons on the right
        titleButtonsPanel = (JPanel) ViewUtil.clear(new JPanel());

        ViewUtil.applyHorizontalBoxLayout(titleButtonsPanel);
        titlePanel.add(titleLabel);
        titlePanel.add(ViewUtil.getMediumSeparator());
        titlePanel.add(descriptionLabel);
        titlePanel.add(Box.createHorizontalGlue());
        titlePanel.add(titleButtonsPanel);

        contentPanel = new JPanel();
        contentPanel.setBorder(ViewUtil.getMediumBorder());
        ViewUtil.applyVerticalBoxLayout(contentPanel);

        contentPanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0,1,1,1,Color.lightGray),
                    ViewUtil.getMediumBorder()));
        contentPanel.setBackground(Color.white);

        setContentPaneVisible(true);

        this.add(titlePanel);
        this.add(contentPanel);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public JPanel getContentPane() {
        return contentPanel;
    }

    public void addTitleComponent(Component c) {
        titleButtonsPanel.add(c);
    }

    public final void setContentPaneVisible(boolean b) {
        if (isCollapsable) {
            contentPanel.setVisible(b);
            expandButton.setVisible(!b);
            collapseButton.setVisible(b);
        }
    }

    public final void setDescription(String s) {
        if (s.isEmpty()) {
            this.descriptionLabel.setText("");
        } else {
            this.descriptionLabel.setText("(" + s + ")");
        }
    }
}
