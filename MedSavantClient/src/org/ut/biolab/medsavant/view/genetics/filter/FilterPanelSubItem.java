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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author Andrew
 */
public class FilterPanelSubItem extends JPanel {

    private FilterView filterView;
    private boolean isRemoved;
    private SearchConditionsPanel parent;
    private String filterID;

    private static Color BAR_COLOUR = Color.darkGray;
    private static Color BUTTON_OVER_COLOUR = Color.gray;


    public FilterPanelSubItem(FilterView view, SearchConditionsPanel parent, String id) {

        this.parent = parent;
        this.filterView = view;
        this.filterID = id;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(ViewUtil.getTinyLineBorder());

        //title bar
        JPanel titlePanel = ViewUtil.getSecondaryBannerPanel();
        //titlePanel.setBackground(BAR_COLOUR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        JLabel testLabel = new JLabel(filterView.getTitle());
        testLabel.setForeground(Color.black);
        titlePanel.add(Box.createRigidArea(new Dimension(10,16)));
        titlePanel.add(testLabel);
        titlePanel.add(Box.createHorizontalGlue());

        final JLabel removeLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE));
        removeLabel.setToolTipText("Remove filter");
        removeLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                removeThis();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                removeLabel.setBackground(BUTTON_OVER_COLOUR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                removeLabel.setBackground(BAR_COLOUR);
            }
        });
        titlePanel.add(removeLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(10,20)));

        this.add(titlePanel);

        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                filterView.setVisible(!filterView.isVisible());
                filterView.invalidate();
            }
        });
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        this.add(filterView);

    }

    public void removeThis() {
        FilterController.removeFilter(filterID, parent.getID());
        isRemoved = true;
        filterView.cleanup();       // Give derived classes (e.g. plugin filters) a chance to clean up.
        parent.refreshSubItems();
    }

    public void removeThisSilent() {
        isRemoved = true;
        filterView.cleanup();       // Give derived classes (e.g. plugin filters) a chance to clean up.
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public String getFilterID() {
        return filterID;
    }

    public FilterView getFilterView() {
        return filterView;
    }

    @Override
    public String getName() {
        return getFilterView().getTitle();
    }
}
