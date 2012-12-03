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

package org.ut.biolab.medsavant.client.view.list;

import java.awt.BorderLayout;
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
        setOpaque(false);
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setBorder(ViewUtil.getMediumBorder());

        add(contentPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(bottomPanel);
        bottomPanel.add(Box.createHorizontalGlue());

        glue = Box.createHorizontalGlue();
        bottomPanel.add(glue);

        add(bottomPanel, BorderLayout.SOUTH);
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
    }

    public String getPageName() {
        return pageName;
    }
}
