/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.patients.cohorts;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.model.SimplePatient;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.component.CollapsiblePanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSubItem;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.list.DetailedView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class CohortDetailedView extends DetailedView {

    //private List<Object> fieldValues;
    private CohortDetailsSW sw;
    //private final JPanel content;
    private final JPanel details;
    private final JPanel menu;
    //private String[] cohortNames;
    private JTable list;
    private Cohort cohort;
    private Cohort[] cohorts;
    private final CollapsiblePanel membersPane;
    private boolean multipleSelected = false;
    private static List<FilterPanelSubItem> filterPanels;
    
    public CohortDetailedView() {

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        viewContainer.add(ViewUtil.getClearBorderlessJSP(infoContainer), BorderLayout.CENTER);

        membersPane = new CollapsiblePanel("Members");
        infoContainer.add(membersPane);
        infoContainer.add(Box.createVerticalGlue());

        details = membersPane.getContentPane();

        menu = ViewUtil.getClearPanel();

        menu.add(removeIndividualsButton());
        menu.setVisible(false);

        this.addBottomComponent(menu);
    }

    private class CohortDetailsSW extends SwingWorker {

        private final Cohort cohort;

        public CohortDetailsSW(Cohort cohort) {
            this.cohort = cohort;
        }

        @Override
        protected Object doInBackground() throws Exception {
            List<SimplePatient> patientList = MedSavantClient.CohortQueryUtilAdapter.getIndividualsInCohort(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    cohort.getId());
            return patientList;
        }

        @Override
        protected void done() {
            try {
                List<SimplePatient> result = (List<SimplePatient>) get();
                setPatientList(result);

            } catch (Exception ex) {
                return;
            }
        }
    }

    public synchronized void setPatientList(List<SimplePatient> patients) {

        details.removeAll();
        
        Object[][] data = new Object[patients.size()][1];
        for(int i = 0; i < patients.size(); i++){
            data[i][0] = patients.get(i);
        }
        
        list = new JTable(data, new String[]{MedSavantDatabase.DefaultpatientTableSchema.getFieldAlias(DefaultpatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID)}) {
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
        list.setBorder(null);
        list.setShowGrid(false);
        list.setRowHeight(21);

        details.add(list);
        
        membersPane.setDescription(ViewUtil.numToString(patients.size()));

        details.updateUI();
    }

    @Override
    public void setSelectedItem(Object[] item) {
        multipleSelected = false;
        cohort = ((Cohort) item[0]);
        setTitle(cohort.getName());

        details.removeAll();
        details.updateUI();

        if (sw != null) {
            sw.cancel(true);
        }
        sw = new CohortDetailsSW(cohort);
        sw.execute();

        if (menu != null) {
            menu.setVisible(true);
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        multipleSelected = true;
        cohorts = new Cohort[items.size()];
        for (int i = 0; i < items.size(); i++) {
            cohorts[i] = (Cohort) items.get(i)[0];
        }
        if (items.isEmpty()) {
            setTitle("");
        } else {
            setTitle("Multiple cohorts (" + items.size() + ")");
        }
        details.removeAll();
        details.updateUI();
    }

    @Override
    public void setRightClick(MouseEvent e) {
        Cohort[] selected;
        if(multipleSelected){
            selected = cohorts;
        } else {
            selected = new Cohort[1];
            selected[0] = cohort;
        }

        JPopupMenu popup = createPopup(selected);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /*
    private JButton setDefaultCaseButton(){
    JButton button = new JButton("Set default Case cohort");
    button.setBackground(ViewUtil.getDetailsBackgroundColor());
    button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
    //TODO
    }
    });
    return button;
    }

    private JButton setDefaultControlButton(){
    JButton button = new JButton("Set default Control cohort");
    button.setBackground(ViewUtil.getDetailsBackgroundColor());
    button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
    //TODO
    }
    });
    return button;
    }
     *
     */
    private JButton removeIndividualsButton() {
        JButton button = new JButton("Remove individual(s) from cohort");
        button.setBackground(ViewUtil.getDetailsBackgroundColor());
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                
                int[] rows = list.getSelectedRows();
                int[] patientIds = new int[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    patientIds[i] = ((SimplePatient) list.getModel().getValueAt(rows[i], 0)).getId();
                }
                if (patientIds != null && patientIds.length > 0) {

                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, "Are you sure you want to remove these individual(s)?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        return;
                    }

                    try {
                        MedSavantClient.CohortQueryUtilAdapter.removePatientsFromCohort(LoginController.sessionId, patientIds, cohort.getId());
                    } catch (SQLException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (RemoteException ex) {
                        Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    sw = new CohortDetailsSW(cohort);
                    sw.execute();
                }
            }
        });
        return button;
    }

    private JPopupMenu createPopup(final Cohort[] cohorts){
        JPopupMenu popupMenu = new JPopupMenu();

        if(ProjectController.getInstance().getCurrentVariantTableSchema() == null){
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem("Filter by Cohort(s)");
            filter1Item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {

                    List<String> dnaIds = new ArrayList<String>();

                    for(Cohort c : cohorts){
                        try {
                            List<String> current = MedSavantClient.CohortQueryUtilAdapter.getDNAIdsInCohort(LoginController.sessionId, c.getId());
                            for(String s : current){
                                if(!dnaIds.contains(s)){
                                    dnaIds.add(s);
                                }
                            }
                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                            Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (RemoteException ex) {
                            Logger.getLogger(CohortDetailedView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }


                    DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID);
                    Condition[] conditions = new Condition[dnaIds.size()];
                    for(int i = 0; i < dnaIds.size(); i++){
                        conditions[i] = BinaryConditionMS.equalTo(col, dnaIds.get(i));
                    }
                    removeExistingFilters();
                    filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                            "Cohorts - Filter by Cohort(s)",
                            cohorts.length + " Cohort(s) (" + dnaIds.size() + " DNA Id(s))",
                            ComboCondition.or(conditions));

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }

    private void removeExistingFilters(){
        if(filterPanels != null){
            for(FilterPanelSubItem panel : filterPanels){
                panel.removeThis();
            }
        }
    }
}
