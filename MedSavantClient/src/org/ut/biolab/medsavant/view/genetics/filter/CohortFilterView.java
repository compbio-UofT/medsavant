/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;

/**
 *
 * @author mfiume
 */
class CohortFilterView extends FilterView{

    public static final String FILTER_NAME = "Cohort";
    public static final String FILTER_ID = "cohort";
    private static final String COHORT_ALL = "All Individuals";

    static FilterView getCohortFilterView(int queryId) {
        return new CohortFilterView(queryId, new JPanel());
    }

    public CohortFilterView(FilterState state, int queryId) throws SQLException {
        this(queryId, new JPanel());
        if(state.getValues().get("value") != null){
            applyFilter(Integer.parseInt(state.getValues().get("value")));
        }
    }

    private Integer appliedId;
    private ActionListener al;
    private JComboBox b;

    public void applyFilter(int cohortId){
        for(int i = 0; i < b.getItemCount(); i++){
            if(b.getItemAt(i) instanceof Cohort && ((Cohort)b.getItemAt(i)).getId() == cohortId){
                b.setSelectedIndex(i);
                al.actionPerformed(new ActionEvent(this, 0, null));
                return;
            }
        }
    }

    private CohortFilterView(int queryId, JPanel container){
        super(FILTER_NAME, container, queryId);
        createContentPanel(container);
    }

    private List<Cohort> getDefaultValues() {
        try {
            return MedSavantClient.CohortQueryUtilAdapter.getCohorts(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId());
        } catch (SQLException ex) {
            Logger.getLogger(CohortFilterView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(CohortFilterView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Cohort>();
    }

    private void createContentPanel(JPanel p) {

        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(1000,80));

        b = new JComboBox();
        b.setMaximumSize(new Dimension(1000,30));

        b.addItem(COHORT_ALL);

        List<Cohort> cohorts = getDefaultValues();
        for(Cohort c : cohorts){
            b.addItem(c);
        }

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {

                        if(b.getSelectedItem().equals(COHORT_ALL)){
                            return new Condition[0];
                        }

                        Cohort cohort = (Cohort) b.getSelectedItem();
                        appliedId = cohort.getId();

                        try {

                            List<String> dnaIds = MedSavantClient.CohortQueryUtilAdapter.getDNAIdsInCohort(
                                    LoginController.sessionId,
                                    cohort.getId());

                            Condition[] results = new Condition[dnaIds.size()];
                            int i = 0;
                            for (String dnaId : dnaIds) {
                                results[i] = BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaId);
                                i++;
                            }

                            Condition[] resultsCombined = new Condition[1];
                            resultsCombined[0] = ComboCondition.or(results);

                            return resultsCombined;

                        } catch (SQLException ex) {
                            MiscUtils.checkSQLException(ex);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public String getName() {
                        return FILTER_NAME;
                    }

                    @Override
                    public String getId() {
                        return FILTER_ID;
                    }
                };
                ClientLogger.log(ClientLogger.class,"Adding filter: " + f.getName());
                FilterController.addFilter(f, getQueryId());

            }
        };
        applyButton.addActionListener(al);

        b.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(1000,30));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        if(appliedId != null) map.put("value", Integer.toString(appliedId));
        return new FilterState(FilterType.COHORT, FILTER_NAME, FILTER_ID, map);
    }
}
