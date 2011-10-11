/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.range.Range;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.table.VariantTable;
import org.ut.biolab.medsavant.db.util.Cohort;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;

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
    
    /*private static List<String> getDefaultValues() {
        List<String> list = FilterCache.getDefaultValues(FILTER_NAME);
        if(list == null){
            try {
                list = QueryUtil.getDistinctCohortNames(-1);
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(CohortFilterView.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        FilterCache.addDefaultValues(MedSavantDatabase.getInstance().getCohortTableSchema().getTable().getTableNameSQL(), FILTER_NAME, list);
        return list;
    }*/
    
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
        /*List<String> cohortNames = getDefaultValues();
        for (String cohortName : cohortNames) {
            b.addItem(cohortName);
        }*/
        List<Cohort> cohorts = getDefaultValues();
        for(Cohort c : cohorts){
            b.addItem(c);
        }

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                //if (((String) b.getSelectedItem()).equals(COHORT_ALL)) {
                //if(b.getSelectedItem().equals(COHORT_ALL)){
                    //FilterController.removeFilter(FILTER_ID, queryId);
                    //TODO
                //} else {
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
                                    results[i] = BinaryCondition.equalTo(new DbColumn(ProjectController.getInstance().getCurrentVariantTable(), VariantTable.FIELDNAME_DNAID, "varchar", 100), dnaId);
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
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    ClientLogger.log(ClientLogger.class,"Adding filter: " + f.getName());
                    FilterController.addFilter(f, queryId);
                //}

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        });

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

        /*
        addChangeListener(new ChangeListener() {
        
        public void stateChanged(ChangeEvent e) {
        applyButton.setEnabled(true);
        }
        });
         * 
         */

        p.add(b, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

        return p;
    }
}
