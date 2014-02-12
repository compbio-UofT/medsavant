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
package org.ut.biolab.medsavant.client.view.list;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.*;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public abstract class DetailedView extends JPanel {

    private final String pageName;
    private final JPanel contentPanel;
    protected SplitScreenView parent;
    private final JPanel bottomPanel;
    private final Component glue;

    public DetailedView(String page) {
        pageName = page;
        setPreferredSize(new Dimension(9999, 350));
        setOpaque(true);
        this.setBackground(Color.white);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        //contentPanel.setBorder(ViewUtil.getMediumBorder());

        add(contentPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomPanel);
        bottomPanel.add(Box.createHorizontalGlue());

        glue = Box.createHorizontalGlue();
        bottomPanel.add(glue);

        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.setVisible(true);
    }

    public abstract void setSelectedItem(Object[] selectedRow);

    public abstract void setMultipleSelections(List<Object[]> selectedRows);

    public abstract JPopupMenu createPopup();

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setSplitScreenParent(SplitScreenView parent) {
        this.parent = parent;
    }

    public void addBottomComponent(Component c) {
        bottomPanel.add(c);
        bottomPanel.add(glue);
        bottomPanel.setVisible(true);
    }

    public String getPageName() {
        return pageName;
    }
}
