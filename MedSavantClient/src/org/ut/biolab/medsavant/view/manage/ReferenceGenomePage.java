/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellRenderer;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.model.Reference;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.view.MedSavantFrame;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.dialog.NewReferenceDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.DetailedListModel;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ReferenceGenomePage extends SubSectionView implements ReferenceListener {

    private SplitScreenView panel;
    private boolean updateRequired = false;

    public ReferenceGenomePage(SectionView parent){
        super(parent);
        ReferenceController.getInstance().addReferenceListener(this);
    }

    @Override
    public String getName() {
        return "Reference Genomes";
    }

    @Override
    public JPanel getView(boolean update) {
        if (panel == null || updateRequired) {
            setPanel();
        }
        return panel;
    }

    @Override
    public void viewDidLoad() {}

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }

    public void setPanel() {
        panel = new SplitScreenView(
                new ReferenceListModel(),
                new ReferenceDetailedView(),
                new ReferenceDetailedListEditor());
    }

    @Override
    public void referenceAdded(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void referenceRemoved(String name) {
        if (panel != null) {
            panel.refresh();
        }
    }

    @Override
    public void referenceChanged(String prnameojectName) {
        if (panel != null) {
            panel.refresh();
        }
    }

    public void update(){
        panel.refresh();
    }


    /*
     * REFERENCE GENOMES LIST MODEL
     */
    private static class ReferenceListModel implements DetailedListModel {

        private List<String> cnames;
        private List<Class> cclasses;
        private List<Integer> chidden;

        public ReferenceListModel() {}

        public List<Object[]> getList(int limit) throws Exception {
            List<Reference> refs = MedSavantClient.ReferenceQueryUtilAdapter.getReferences(LoginController.sessionId);
            List<Object[]> refVector = new ArrayList<Object[]>();
            for (int i = 0; i < refs.size(); i++) {
                Object[] v = new Object[] {refs.get(i)};
                refVector.add(v);
            }
            return refVector;
        }

        public List<String> getColumnNames() {
            if (cnames == null) {
                cnames = new ArrayList<String>();
                cnames.add("Reference");
            }
            return cnames;
        }

        public List<Class> getColumnClasses() {
            if (cclasses == null) {
                cclasses = new ArrayList<Class>();
                cclasses.add(String.class);
            }
            return cclasses;
        }

        public List<Integer> getHiddenColumns() {
            if (chidden == null) {
                chidden = new ArrayList<Integer>();
            }
            return chidden;
        }
    }


    /*
     * REFERENCE GENOMES DETAILED VIEW
     */
    private static class ReferenceDetailedView extends DetailedView {

        private final JPanel details;
        private Reference ref;
        private DetailsSW sw;
        private CollapsiblePanel infoPanel;

        private Object[] columnNames;

        public ReferenceDetailedView() {

            columnNames = new Object[]{"Contig Name", "Contig Length", "Centromere Position"};

            JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
            viewContainer.setLayout(new BorderLayout());

            JPanel infoContainer = ViewUtil.getClearPanel();
            ViewUtil.applyVerticalBoxLayout(infoContainer);

            viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

            infoPanel = new CollapsiblePanel("Reference Information");
            infoContainer.add(infoPanel);
            infoContainer.add(Box.createVerticalGlue());

            details = infoPanel.getContentPane();

        }

        @Override
        public void setSelectedItem(Object[] item) {
            ref = (Reference) item[0];
            setTitle(ref.toString());

            details.removeAll();
            details.updateUI();

            if (sw != null) {
                sw.cancel(true);
            }
            sw = new DetailsSW(ref);
            sw.execute();
        }

        @Override
        public void setRightClick(MouseEvent e) {
            //nothing yet
        }

        public synchronized void setReferenceInfoTable(Object[][] data) {

            details.removeAll();

            JTable table = new JTable(data, columnNames) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
                    Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
                    if (!isCellSelected(Index_row, Index_col)) {
                        if (Index_row % 2 == 0) {
                            comp.setBackground(ViewUtil.evenRowColor);
                        } else {
                            comp.setBackground(ViewUtil.oddRowColor);
                        }
                    }
                    return comp;
                }
            };
            table.setBorder(null);
            table.setGridColor(new Color(235,235,235));
            table.setRowHeight(21);


            details.add((table.getTableHeader()));
            details.add(table);

            details.updateUI();

        }

        private class DetailsSW extends SwingWorker {

            private Reference ref;

            public DetailsSW(Reference ref) {
                this.ref = ref;
            }

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    return MedSavantClient.ChromosomeQueryUtilAdapter.getContigs(LoginController.sessionId, ref.getId());
                } catch (SQLException ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                //List<Object[]> list = new ArrayList<Object[]>();
                Object[][] list = null;
                try {
                    List<Chromosome> result = (List<Chromosome>) get();
                    list = new Object[result.size()][];
                    for(int i = 0; i < result.size(); i++){
                        Chromosome c = result.get(i);
                        list[i] = new Object[]{c.getName(), c.getLength(), c.getCentromerepos()};
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ReferenceGenomePage.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(ReferenceGenomePage.class.getName()).log(Level.SEVERE, null, ex);
                }
                setReferenceInfoTable(list);
            }
        }

        @Override
        public void setMultipleSelections(List<Object[]> items) {
            if (items.isEmpty()) {
                setTitle("");
            } else {
                setTitle("Multiple users (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }
    }



    /*
     * REFERENCE GENOMES DETAILED LIST EDITOR
     */
    private static class ReferenceDetailedListEditor extends DetailedListEditor {

        @Override
        public boolean doesImplementAdding() {
            return true;
        }

        @Override
        public boolean doesImplementDeleting() {
            return true;
        }

        @Override
        public void addItems() {
            NewReferenceDialog npd = new NewReferenceDialog(MedSavantFrame.getInstance(), true);
            npd.setVisible(true);
        }

        @Override
        public void editItems(Object[] results) {
        }

        @Override
        public void deleteItems(List<Object[]> items) {

            int result;

            if (items.size() == 1) {
                String name = ((Reference) items.get(0)[0]).getName();
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            } else {
                result = JOptionPane.showConfirmDialog(MedSavantFrame.getInstance(),
                             "Are you sure you want to remove these " + items.size() + " references?\nThis cannot be undone.",
                             "Confirm", JOptionPane.YES_NO_OPTION);
            }

            if (result == JOptionPane.YES_OPTION) {
                int numCouldntRemove = 0;
                for (Object[] v : items) {
                    String refName = ((Reference)v[0]).getName();
                    ReferenceController.getInstance().removeReference(refName);
                }

                if (items.size() != numCouldntRemove) {
                    DialogUtils.displayMessage("Successfully removed " + (items.size()-numCouldntRemove) + " reference(s)");
                }
            }
        }
    }

}
