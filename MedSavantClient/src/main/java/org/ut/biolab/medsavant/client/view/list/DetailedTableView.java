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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * DetailedView variant which displays the contents of a table. Currently used
 * by Reference and Standard Genes pages.
 *
 * @author tarkvara
 */
public abstract class DetailedTableView<T> extends DetailedView {

    private final String[] columnNames;
    private final String multipleTitle;
    private final JPanel details;
    private final CollapsiblePane infoPanel;
    protected List<T> selected = new ArrayList<T>();
    private MedSavantWorker worker;
    private final BlockingPanel blockPanel;
    

    public DetailedTableView(String page, String title, String multTitle, String[] colNames) {
        super(page);
        multipleTitle = multTitle;
        columnNames = colNames;

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        contentPanel.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

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
        infoPanel.add(details, BorderLayout.CENTER);

        blockPanel = new BlockingPanel("No item selected", contentPanel);
        viewContainer.add(blockPanel, BorderLayout.CENTER);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        if (item.length == 0) {
            blockPanel.block();
        } else {
            selected.clear();
            selected.add((T) item[0]);
            infoPanel.setTitle(item[0].toString());

            details.removeAll();
            details.updateUI();

            if (worker != null) {
                worker.cancel(true);
            }
            worker = createWorker();
            worker.execute();

            // unblock when the worker is done
            worker.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    if (worker.isDone()) {
                        blockPanel.unblock();
                    }
                }

            });
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        if (items.isEmpty()) {
            blockPanel.block();
        } else {
            selected.clear();
            for (Object[] item : items) {
                selected.add((T) item[0]);
            }
            if (items.isEmpty()) {
                infoPanel.setTitle("");
            } else {
                infoPanel.setTitle(String.format(multipleTitle, items.size()));
            }
            details.removeAll();
            details.updateUI();
            blockPanel.unblock();
        }
    }
      
    @Override
    public JPopupMenu createPopup(){
        return null;
    }
    
    public JPopupMenu createTablePopup(Object[][] selected){
        return null;
    }

    public synchronized void setData(final Object[][] data) {

        details.removeAll();

        JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(p);

        final JTable table = new StripyTable(data, columnNames); //changed to 'final'.
        table.setBorder(null);
        table.setGridColor(new Color(235, 235, 235));
        table.setRowHeight(21);

        p.add(ViewUtil.alignLeft(new JLabel(ViewUtil.numToString(table.getRowCount()) + " entries")));
        p.add(Box.createRigidArea(new Dimension(10, 10)));
        p.add((table.getTableHeader()));
        p.add(ViewUtil.getClearBorderedScrollPane(table));

        details.add(p, BorderLayout.CENTER);

        details.updateUI();
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    
                    int[] selection = table.getSelectedRows();
                    Object[][] selections = new Object[selection.length][table.getSize().width];
                    for (int i = 0; i < selection.length; i++) {
                        int j = table.convertRowIndexToModel(selection[i]);
                        selections[i] = data[j];
                        //selection[i] = table.convertRowIndexToModel(selection[i]);
                        
                    } 
                    JPopupMenu m = createTablePopup(selections);
                    if(m != null){
                        m.show(e.getComponent(), e.getX(), e.getY());
                    }                                          
                }
            }
        });
    }

    
    public abstract MedSavantWorker createWorker();
}
