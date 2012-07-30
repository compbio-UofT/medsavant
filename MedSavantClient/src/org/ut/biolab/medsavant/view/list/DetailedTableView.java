/*
 *    Copyright 2012 University of Toronto
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
package org.ut.biolab.medsavant.view.list;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.view.component.StripyTable;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 * DetailedView variant which displays the contents of a table.
 * Currently used by Reference and Standard Genes pages.
 *
 * @author tarkvara
 */
public abstract class DetailedTableView extends DetailedView {

    private final String[] columnNames;
    private final String multipleTitle;

    private final JPanel details;
    private final CollapsiblePane infoPanel;

    private SwingWorker worker;

    public DetailedTableView(String title, String multTitle, String[] colNames) {
        multipleTitle = multTitle;
        columnNames = colNames;

        JPanel viewContainer = (JPanel)ViewUtil.clear(getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);


        CollapsiblePanes p = new CollapsiblePanes();
        p.setOpaque(false);

        infoPanel = new CollapsiblePane(title);
        infoPanel.setCollapsible(false);
        infoPanel.setStyle(CollapsiblePane.TREE_STYLE);
        p.add(infoPanel);

        p.addExpansion();

        infoContainer.add(p);
        infoContainer.add(Box.createVerticalGlue());

        details = new JPanel();
        details.setLayout(new BorderLayout());
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(details,BorderLayout.CENTER);

    }

    @Override
    public void setSelectedItem(Object[] item) {
        //setTitle(item[0].toString());
        infoPanel.setTitle(item[0].toString());

        details.removeAll();
        details.updateUI();

        if (worker != null) {
            worker.cancel(true);
        }
        worker = createWorker();
        worker.execute();
    }

    @Override
    public JPopupMenu createPopup() {
        return null;
    }

    public synchronized void setData(Object[][] data) {

        details.removeAll();

        JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(p);

        JTable table = new StripyTable(data, columnNames);
        table.setBorder(null);
        table.setGridColor(new Color(235,235,235));
        table.setRowHeight(21);

        p.add(ViewUtil.alignLeft(new JLabel(ViewUtil.numToString(table.getRowCount()) + " entries")));
        p.add(Box.createRigidArea(new Dimension(10,10)));
        p.add((table.getTableHeader()));
        p.add(ViewUtil.getClearBorderedScrollPane(table));

        details.add(p,BorderLayout.CENTER);

        details.updateUI();
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        if (items.isEmpty()) {
            infoPanel.setTitle("");
        } else {
            infoPanel.setTitle(String.format(multipleTitle, items.size()));
        }
        details.removeAll();
        details.updateUI();
    }

    public abstract SwingWorker createWorker();
}
