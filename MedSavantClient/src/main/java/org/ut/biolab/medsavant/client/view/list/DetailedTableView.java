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
import java.util.Arrays;
import net.miginfocom.swing.MigLayout;

import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
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
    protected List<T> selected = new ArrayList<T>();
    private MedSavantWorker worker;
    private final BlockingPanel blockPanel;
    private final JPanel detailView;
    private JPanel tableArea;
    

    public DetailedTableView(String page, String title, String multTitle, String[] colNames) {
        super(page);
        multipleTitle = multTitle;
        columnNames = colNames;

        detailView = new JPanel();
        detailView.setBackground(Color.white);
        blockPanel = new BlockingPanel("No item selected", detailView);
        
        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());
        viewContainer.add(blockPanel, BorderLayout.CENTER);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        if (item.length == 0) {
            blockPanel.block();
        } else {
            
            selected.clear();
            selected.add((T) item[0]);
            
            // block for loading
            //TODO: show the load panel...
            //blockPanel.setBlockText("Loading details..");
            
            blockPanel.block();
            
            detailView.removeAll();
            detailView.setLayout(new BorderLayout());
            
            JPanel p = ViewUtil.getClearPanel();
            p.setLayout(new MigLayout());
            JLabel l = new JLabel(item[0].toString());
            l.setFont(ViewUtil.getBigTitleFont());
            l.setForeground(ViewUtil.getSemiBlackColor());
            p.add(l);
            
            detailView.add(p, BorderLayout.NORTH);
            
            tableArea = ViewUtil.getClearPanel();
            detailView.add(tableArea,BorderLayout.CENTER);
          
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
            
            detailView.updateUI();
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
    }
      
    @Override
    public JPopupMenu createPopup(){
        return null;
    }
    
    public JPopupMenu createTablePopup(Object[][] selected){
        return null;
    }

    public synchronized void setData(final Object[][] data) {

        JPanel p = ViewUtil.getClearPanel();
        p.setLayout(new MigLayout("insets 0, gapy 0"));

        final JTable table = new StripyTable(data, columnNames); 
        table.setBorder(null);
        table.setGridColor(new Color(235, 235, 235));
        table.setRowHeight(21);

        p.add(table.getTableHeader(),"width 100%, wrap");
        p.add(ViewUtil.getClearBorderlessScrollPane(table),"width 100%, height 100%");

        tableArea.setLayout(new BorderLayout());
        tableArea.add(p, BorderLayout.CENTER);

        tableArea.updateUI();
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    
                    int[] selection = table.getSelectedRows();
                    Object[][] selections = new Object[selection.length][table.getSize().width];
                    for (int i = 0; i < selection.length; i++) {
                        int j = table.convertRowIndexToModel(selection[i]);
                        selections[i] = data[j];
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
