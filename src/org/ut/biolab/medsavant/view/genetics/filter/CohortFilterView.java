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
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.model.Cohort;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;

/**
 *
 * @author mfiume
 */
class CohortFilterView {

    public static final String FILTER_NAME = "Cohort";
    public static final String FILTER_ID = "cohort";
    private static final String COHORT_ALL = "All Individuals";

    static FilterView getCohortFilterView(int queryId) {
        return new FilterView("Cohort", getContentPanel(queryId));
    }

    public static List<Cohort> getDefaultValues() {
        try {
            return CohortQueryUtil.getCohorts(ProjectController.getInstance().getCurrentProjectId());
        } catch (SQLException ex) {
            Logger.getLogger(CohortFilterView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Cohort>();
    }

    private static JComponent getContentPanel(final int queryId) {

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(1000,80));

        final JComboBox b = new JComboBox();
        b.setMaximumSize(new Dimension(1000,30));

        b.addItem(COHORT_ALL);

        List<Cohort> cohorts = getDefaultValues();
        for(Cohort c : cohorts){
            b.addItem(c);
        }

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {

                        if(b.getSelectedItem().equals(COHORT_ALL)){
                            return new Condition[0];
                        }

                        Cohort cohort = (Cohort) b.getSelectedItem();

                        try {

                            List<String> dnaIds = CohortQueryUtil.getDNAIdsInCohort(cohort.getId());

                            Condition[] results = new Condition[dnaIds.size()];
                            int i = 0;
                            for (String dnaId : dnaIds) {   
                                results[i] = BinaryCondition.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaId);
                                i++;
                            }

                            Condition[] resultsCombined = new Condition[1];
                            resultsCombined[0] = ComboCondition.or(results);

                            return resultsCombined;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
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
                FilterController.addFilter(f, queryId);

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
        
        //al.actionPerformed(null);
        return p;
    }
}
