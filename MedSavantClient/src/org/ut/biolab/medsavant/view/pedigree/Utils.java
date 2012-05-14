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
package org.ut.biolab.medsavant.view.pedigree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultpatientTableSchema;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSubItem;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;

/**
 *
 * @author Andrew
 */
public class Utils {

    private static List<FilterPanelSubItem> filterPanels;

    public static JPopupMenu createPopup(final String familyId) {
        JPopupMenu popupMenu = new JPopupMenu();

        //Filter by patient
        JMenuItem filter1Item = new JMenuItem("Filter by Family");
        filter1Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                List<String> dnaIds = new ArrayList<String>();
                int numPatients = 0;
                try {
                    Map<String, String> patientIDToDNAIDMap = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsForFamily(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectId(), familyId);
                    numPatients = patientIDToDNAIDMap.size();
                    Object[] values = patientIDToDNAIDMap.values().toArray();
                    for (Object o : values) {
                        String[] d = ((String) o).split(",");
                        for (String id : d) {
                            if (!dnaIds.contains(id)) {
                                dnaIds.add(id);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error getting DNA IDs for family.", ex);
                }

                DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID);
                Condition[] conditions = new Condition[dnaIds.size()];
                for (int i = 0; i < dnaIds.size(); i++) {
                    conditions[i] = BinaryConditionMS.equalTo(col, dnaIds.get(i));
                }
                removeExistingFilters();
                filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                        "Individuals - Filter by Family",
                        numPatients + " Patient(s) (" + dnaIds.size() + " DNA Id(s))",
                        ComboCondition.or(conditions));

            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    public static JPopupMenu createPopup(final int[] patientIds) {
        JPopupMenu popupMenu = new JPopupMenu();

        //Filter by patient
        JMenuItem filter1Item = new JMenuItem("Filter by Selected Patient(s)");
        filter1Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    List<Object> values = new ArrayList<Object>();
                    for (int i = 0; i < patientIds.length; i++) {
                        values.add(patientIds[i]);
                    }

                    List<String> dnaIds = MedSavantClient.PatientQueryUtilAdapter.getDNAIdsFromField(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectId(),
                        DefaultpatientTableSchema.COLUMNNAME_OF_PATIENT_ID,
                        values);

                    DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID);
                    Condition[] conditions = new Condition[dnaIds.size()];
                    for (int i = 0; i < dnaIds.size(); i++) {
                        conditions[i] = BinaryConditionMS.equalTo(col, dnaIds.get(i));
                    }
                    removeExistingFilters();
                    filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                            "Individuals - Filter by Selected Patient(s)",
                            patientIds.length + " Patient(s) (" + dnaIds.size() + " DNA Id(s))",
                            ComboCondition.or(conditions));
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Error applying patient filters.", ex);
                }
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    private static void removeExistingFilters() {
        if (filterPanels != null) {
            for (FilterPanelSubItem panel : filterPanels) {
                panel.removeThis();
            }
            filterPanels.clear();
        }
    }
}
