/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients;

import org.ut.biolab.medsavant.view.util.WaitPanel;
import com.jidesoft.utils.SwingWorker;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.ut.biolab.medsavant.view.patients.cohorts.CohortDetailedView;
import org.ut.biolab.medsavant.view.patients.cohorts.CohortListModel;
import org.ut.biolab.medsavant.view.patients.individual.IndividualDetailedView;
import org.ut.biolab.medsavant.view.patients.individual.IndividualListModel;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SplitScreenView extends JPanel {

    private final DetailedListModel detailedListModel;
    private ListView listView;
    private DetailedView detailedView;

    private static class ListView extends JPanel {

        private static final String CARD_WAIT = "wait";
        private static final String CARD_SHOW = "show";
        private final DetailedListModel listModel;
        private final CardLayout cl;
        private List<Vector> list;
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

        private synchronized void setList(List<Vector> list) {
            this.list = list;
            updateShowCard();
            showShowCard();
        }
                   
        public void refreshList(){
            showWaitCard();
            fetchList();
        }

        private void fetchList() {

            SwingWorker sw = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    return listModel.getList(limit);
                }

                @Override
                protected void done() {
                    List<Vector> list;
                    try {
                        list = (List<Vector>) get();
                        setList(list);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            sw.execute();
        }

        private void updateShowCard() {
            showCard.removeAll();

            showCard.setLayout(new BorderLayout());

            final List<Vector> data = list;
            List<String> columnNames = listModel.getColumnNames();
            List<Class> columnClasses = listModel.getColumnClasses();
            List<Integer> columnVisibility = listModel.getHiddenColumns();

            stp = new SearchableTablePanel(Util.listToVector(data), columnNames, columnClasses, columnVisibility, limit){
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
                    List<Vector> selected = new ArrayList<Vector>();
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
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           detailedView, listView);
        split.setOneTouchExpandable(true);
        this.add(split,BorderLayout.CENTER);
        
        //this.add(detailedView, BorderLayout.NORTH);
        //this.add(listView, BorderLayout.CENTER);
    }
    
    public void refresh(){
        listView.refreshList();
    }
}
