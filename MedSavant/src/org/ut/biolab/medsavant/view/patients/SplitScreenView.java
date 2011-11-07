/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.patients;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel.DataRetriever;
import org.ut.biolab.medsavant.view.util.WaitPanel;


/**
 *
 * @author mfiume
 */
public class SplitScreenView extends JPanel {
    private static final Logger LOG = Logger.getLogger(SplitScreenView.class.getName());

    private final DetailedListModel detailedListModel;
    private ListView listView;
    private DetailedView detailedView;

    private static class ListView extends JPanel {

        private static final String CARD_WAIT = "wait";
        private static final String CARD_SHOW = "show";
        private final DetailedListModel listModel;
        private final CardLayout cl;
        private List<Object[]> list;
        private final JPanel showCard;
        private final DetailedView detailedView;
        private SearchableTablePanel stp;
        private int limit = 10000;

        private ListView(DetailedListModel listModel, DetailedView detailedView) {
            this.listModel = listModel;
            this.detailedView = detailedView;

            cl = new CardLayout();
            this.setLayout(cl);

            this.add(new WaitPanel("Getting list"), CARD_WAIT);
            showCard = new JPanel();
            this.add(showCard, CARD_SHOW);

            showWaitCard();
            fetchList();
        }

        private void showWaitCard() {
            cl.show(this, CARD_WAIT);
        }

        private void showShowCard() {
            cl.show(this, CARD_SHOW);
        }

        private synchronized void setList(List<Object[]> list) {
            this.list = list;
            updateShowCard();
            showShowCard();
        }
                   
        public void refreshList(){
            showWaitCard();
            fetchList();
        }

        private void fetchList() {

            new SwingWorker<List<Object[]>, Object>() {

                @Override
                protected List<Object[]> doInBackground() throws Exception {
                    return listModel.getList(limit);
                }

                @Override
                protected void done() {
                    try {
                        setList(get());
                    } catch (Exception x) {
                        // TODO: #90
                        LOG.log(Level.SEVERE, null, x);
                    }
                }
            }.execute();
        }

        private void updateShowCard() {
            showCard.removeAll();

            showCard.setLayout(new BorderLayout());

            final List<Object[]> data = list;
            List<String> columnNames = listModel.getColumnNames();
            List<Class> columnClasses = listModel.getColumnClasses();
            List<Integer> columnVisibility = listModel.getHiddenColumns();

            stp = new SearchableTablePanel(columnNames, columnClasses, columnVisibility, limit, SearchableTablePanel.createPrefetchedDataRetriever(data)) {
                @Override
                public void forceRefreshData(){
                    limit = stp.getRetrievalLimit();
                    refreshList();
                }
            };

            //stp.updateData(Util.listToVector(data));

            stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    //set last selection
                    int row = stp.getTable().getSelectedRow(); 
                    int adjustedRow = row + ((stp.getPageNumber() - 1) * stp.getRowsPerPage());
                    if(row != -1 && !data.isEmpty() && adjustedRow >= 0 && adjustedRow < data.size()){
                        detailedView.setSelectedItem(data.get(adjustedRow));
                    }
                    
                    //set all selected
                    //TODO: adjust for page
                    int[] allRows = stp.getTable().getSelectedRows();
                    int length = allRows.length;
                    if(allRows.length > 0 && allRows[allRows.length-1] >= data.size()) length--;
                    List<Object[]> selected = new ArrayList<Object[]>();
                    for(int i = 0; i < length; i++){
                        int currentRow = allRows[i] + ((stp.getPageNumber() - 1) * stp.getRowsPerPage());
                        if(currentRow >= 0 && !data.isEmpty() && currentRow < data.size()){
                            selected.add(data.get(currentRow));
                        }
                    }
                    detailedView.setMultipleSelections(selected);
                }
            });
            
            stp.getTable().getSelectionModel().setSelectionInterval(0, 1);
            //detailedView.setSelectedItem(data.get(0));

            showCard.add(stp, BorderLayout.CENTER);
        }
    }
 

    public SplitScreenView(DetailedListModel lm, DetailedView view) {
        this.detailedListModel = lm;
        this.detailedView = view;        
        initGUI();
        detailedView.setSplitScreenParent(this);
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());

        listView = new ListView(detailedListModel,detailedView);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailedView, listView);
        split.setOneTouchExpandable(true);
        this.add(split,BorderLayout.CENTER);
    }
    
    public void refresh(){
        listView.refreshList();
    }
}
