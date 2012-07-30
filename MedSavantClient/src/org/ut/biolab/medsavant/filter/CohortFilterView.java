/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.ClientMiscUtils;

/**
 *
 * @author mfiume
 */
class CohortFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(CohortFilterView.class);
    public static final String FILTER_NAME = "Cohort";
    public static final String FILTER_ID = "cohort";
    private static final String COHORT_ALL = "All Individuals";

    private Integer appliedId;
    private ActionListener al;
    private JComboBox b;
    private final JButton applyButton;

    public CohortFilterView(FilterState state, int queryId) throws SQLException, RemoteException {
        this(queryId);
        if (state.getValues().get("value") != null) {
            applyFilter(Integer.parseInt(state.getValues().get("value")));
        }
    }

    public CohortFilterView(int queryId) throws SQLException, RemoteException {
        super(FILTER_NAME, queryId);
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(1000,80));

        b = new JComboBox();
        b.setMaximumSize(new Dimension(1000,30));

        b.addItem(COHORT_ALL);

        Cohort[] cohorts = getDefaultValues();
        for (Cohort c : cohorts) {
            b.addItem(c);
        }

        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                Filter f = new Filter() {

                    @Override
                    public Condition[] getConditions() {

                        if (b.getSelectedItem().equals(COHORT_ALL)) {
                            return new Condition[0];
                        }

                        Cohort cohort = (Cohort) b.getSelectedItem();
                        appliedId = cohort.getId();

                        try {

                            List<String> dnaIds = MedSavantClient.CohortManager.getDNAIDsInCohort(
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

                        } catch (Exception ex) {
                            ClientMiscUtils.reportError("Error getting DNA IDs for cohort: %s", ex);
                        }
                        return null;
                    }

                    @Override
                    public String getName() {
                        return FILTER_NAME;
                    }

                    @Override
                    public String getID() {
                        return FILTER_ID;
                    }
                };
                LOG.info("Adding filter: " + f.getName());
                FilterController.getInstance().addFilter(f, queryID);

            }
        };
        applyButton.addActionListener(al);

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(1000,30));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);

        add(b, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);
    }

    public final void applyFilter(int cohortId) {
        for (int i = 0; i < b.getItemCount(); i++) {
            if (b.getItemAt(i) instanceof Cohort && ((Cohort)b.getItemAt(i)).getId() == cohortId) {
                b.setSelectedIndex(i);
                al.actionPerformed(new ActionEvent(this, 0, null));
                return;
            }
        }
    }

    private Cohort[] getDefaultValues() throws SQLException, RemoteException {
        return MedSavantClient.CohortManager.getCohorts(
                LoginController.sessionId,
                ProjectController.getInstance().getCurrentProjectID());
    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        if (appliedId != null) map.put("value", Integer.toString(appliedId));
        return new FilterState(Filter.Type.COHORT, FILTER_NAME, FILTER_ID, map);
    }
}
