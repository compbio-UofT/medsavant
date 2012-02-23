/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;
import org.ut.biolab.medsavant.view.genetics.filter.ont.OntologyRetriever;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class HPOFilterView extends FilterView {

    public static final String FILTER_NAME = "Human Phenotype Ontology";
    public static final String FILTER_ID = "hpo";
    //private static final String COHORT_ALL = "All Individuals";
    private final Object lock = new Object();

    static FilterView getHPOFilterView(int queryId) {
        return new HPOFilterView(queryId, new JPanel());
    }
    private JButton applyButton;
    private JLabel labelSelected;

    public HPOFilterView(FilterState state, int queryId) throws SQLException {
        this(queryId, new JPanel());
        if (state.getValues().get("value") != null) {
            applyFilter(state.getValues().get("value"));
        }
    }
    private Integer appliedId;
    private ActionListener al;

    public void applyFilter(String selectedID) {

        applyID(selectedID);



        /*
        for (int i = 0; i < b.getItemCount(); i++) {
        if (b.getItemAt(i) instanceof Cohort && ((Cohort) b.getItemAt(i)).getId() == cohortId) {
        b.setSelectedIndex(i);
        al.actionPerformed(new ActionEvent(this, 0, null));
        return;
        }
        }
         *
         */
    }

    private HPOFilterView(int queryId, JPanel container) {
        super(FILTER_NAME, container, queryId);
        createContentPanel(container);
    }
    private String selectedID = "";
    private String lastAppliedID = "";
    private OntologyRetriever retriever;

    private void setSelectedID(String id) {
        if (id == null) {
            return;
        }
        this.selectedID = id;
        if (lastAppliedID.equals(selectedID)) {
            this.applyButton.setEnabled(false);
        } else {
            this.applyButton.setEnabled(true);
        }

        if (id.equals("")) {
            this.labelSelected.setText("Select a row to filter by HPO ID");
        } else {
            this.labelSelected.setText(stem + id);
        }
    }
    private String stem = "Filter individuals with HPO ID: ";

    private void applyID(final String id) {
        if (id == null) {
            return;
        }
        lastAppliedID = id;
        setSelectedID(id);

        final TableSchema variantTable = ProjectController.getInstance().getCurrentVariantTableSchema();

        Filter f = new QueryFilter() {

            @Override
            public Condition[] getConditions() {


                try {
                    List<String> dnaIds = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForHPOID(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), id);

                    Condition[] conditions = new Condition[dnaIds.size()];
                    for (int i = 0; i < dnaIds.size(); i++) {
                        if (dnaIds.get(i) == null || dnaIds.get(i).equals("")) { continue; }
                        conditions[i] = BinaryCondition.equalTo(variantTable.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
                    }

                    if (dnaIds.isEmpty()) {
                        // always false
                        return new Condition[] {BinaryCondition.equalTo(1, 0)};
                    }
                    Condition[] cs = new Condition[1];

                    cs[0] = ComboCondition.or(conditions);

                    return cs;
                } catch (SQLException ex) {
                    Logger.getLogger(HPOFilterView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(HPOFilterView.class.getName()).log(Level.SEVERE, null, ex);
                }

                return new Condition[0];
            }

            @Override
            public String getName() {
                return HPOFilterView.FILTER_NAME;
            }

            @Override
            public String getId() {
                return HPOFilterView.FILTER_ID;
            }
        };
        FilterController.addFilter(f, getQueryId());
    }

    private String getSelectedID(SearchableTablePanel stp) {
        if (stp.getTable().getSelectedRow() == -1) {
            return null;
        }
        String selectedID = (String) retriever.getTerms().get(stp.getActualRowAt(stp.getTable().getSelectedRow()))[0];
        return selectedID;
    }

    private void createContentPanel(JPanel p) {

        JPanel t = new JPanel();
        t.setBackground(Color.yellow);
        //t.setLayout(new GridLayout(3,0));

        ViewUtil.applyVerticalBoxLayout(t);

        p.add(t);
        p.setBorder(ViewUtil.getMediumBorder());
        p.setMaximumSize(new Dimension(360, 250));

        try {

            retriever = new OntologyRetriever("/org/ut/biolab/medsavant/data/hpo/human-phenotype-ontology.obo");

            List<String> columnNames = Arrays.asList(new String[]{"ID", "Name"});
            List<Class> columnClasses = Arrays.asList(new Class[]{String.class, String.class});
            final SearchableTablePanel stp = new SearchableTablePanel("***HPO***", columnNames, columnClasses, new ArrayList<Integer>(), 10000, retriever);
            stp.setBottomBarVisible(false);
            stp.setChooseColumnsButtonVisible(false);
            stp.setExportButtonVisible(false);

            Dimension d = new Dimension(340, 190);
            Dimension d2 = new Dimension(d.width, d.height - 50);

            stp.setMinimumSize(d);
            stp.setPreferredSize(d);
            stp.setMaximumSize(d);

            /**
            stp.getTable().setMinimumSize(d2);
            stp.getTable().setPreferredSize(d2);
            stp.getTable().setMaximumSize(d2);
             */
            stp.forceRefreshData();
            stp.setNumRowsPerPage(99999);

            t.add(stp);

            stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent lse) {
                    setSelectedID(getSelectedID(stp));
                    // applyButton.setEnabled(true);
                }
            });

            JPanel topBar = new JPanel();
            labelSelected = new JLabel("");
            topBar.add(ViewUtil.center(labelSelected));

            t.add(topBar);

            JPanel bottomBar = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(bottomBar);

            JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

            selectNone.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setSelectedID("");
                    stp.getTable().getSelectionModel().clearSelection();
                }
            });
            bottomBar.add(selectNone);

            bottomBar.add(Box.createHorizontalGlue());
            applyButton = new JButton("Apply");
            applyButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    applyID(getSelectedID(stp));
                }
            });

            bottomBar.add(applyButton);

            t.add(bottomBar);

            applyButton.setEnabled(false);

            setSelectedID(this.lastAppliedID);

        } catch (Exception ex) {
            t.add(new JLabel("Problem parsing ontology"));
        }
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        if (appliedId != null) {
            map.put("value", Integer.toString(appliedId));
        }
        return new FilterState(FilterType.STRING, FILTER_NAME, FILTER_ID, map);
    }

    public static void main(String[] argv) throws FileNotFoundException, IOException {
        //loadOntology();
    }
}
