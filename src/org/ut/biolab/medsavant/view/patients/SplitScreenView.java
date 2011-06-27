/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients;

import com.jidesoft.utils.SwingWorker;
import fiume.table.SearchableTablePanel;
import fiume.table.Util;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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

        private ListView(DetailedListModel listModel, DetailedView detailedView) {
            this.listModel = listModel;
            this.detailedView = detailedView;

            cl = new CardLayout();
            this.setLayout(cl);

            this.add(new WaitPanel(), CARD_WAIT);
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
                    return listModel.getList();
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
            List<Boolean> columnVisibility = listModel.getColumnVisibility();

            final SearchableTablePanel stp = new SearchableTablePanel(data, columnNames, columnClasses, columnVisibility);

            stp.updateData(Util.listToVector(data));

            stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    //set last selection
                    int row = stp.getTable().getSelectedRow();    
                    detailedView.setSelectedItem(data.get(row));
                    
                    //set all selected
                    int[] allRows = stp.getTable().getSelectedRows();
                    Vector[] selected = new Vector[allRows.length];
                    for(int i = 0; i < allRows.length; i++){
                        selected[i] = data.get(allRows[i]);
                    }
                    detailedView.setMultipleSelections(selected);
                }
            });

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
        
        this.add(detailedView, BorderLayout.NORTH);
        this.add(listView, BorderLayout.CENTER);
    }
    
    public void refresh(){
        listView.refreshList();
    }
}
