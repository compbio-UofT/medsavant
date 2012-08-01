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
package org.ut.biolab.medsavant.patient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.ut.biolab.medsavant.db.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.filter.StringListFilterView;
import org.ut.biolab.medsavant.filter.WhichTable;
import org.ut.biolab.medsavant.format.PatientFormat;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;


/**
 *
 * @author Andrew
 */
public class PatientUtils {

    public static JPopupMenu createPopup(final String familyId) {
        JPopupMenu popupMenu = new JPopupMenu();

        //Filter by patient
        JMenuItem filter1Item = new JMenuItem("Filter by Family");
/* TODO:        filter1Item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                List<String> dnaIds = new ArrayList<String>();
                int numPatients = 0;
                try {
                    Map<String, String> patientIDToDNAIDMap = MedSavantClient.PatientManager.getDNAIDsForFamily(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), familyId);
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
                    ClientMiscUtils.reportError("Error getting DNA IDs for family: %s", ex);
                }

                DbColumn col = ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID);
                Condition[] conditions = new Condition[dnaIds.size()];
                for (int i = 0; i < dnaIds.size(); i++) {
                    conditions[i] = BinaryConditionMS.equalTo(col, dnaIds.get(i));
                }
                FilterUtils.createAndApplyGenericFixedFilter("Individuals - Filter by Family", numPatients + " Patient(s) (" + dnaIds.size() + " DNA Id(s))",
                        ComboCondition.or(conditions));

            }
        });*/
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    /**
     * Create a popup to filter by patient IDs.
     *
     * @param patientIDs
     * @return 
     */
    public static JPopupMenu createPopup(final int[] patientIDs) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem filter1Item = new JMenuItem(String.format("Filter by %s", patientIDs.length == 1 ? "Patient " + patientIDs[0] : "Selected Patients"));
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> patientIDStrings = new ArrayList<String>();
                for (int id: patientIDs) {
                    patientIDStrings.add(Integer.toString(id));
                }
                GeneticsFilterPage.getSearchBar().loadFilters(StringListFilterView.wrapState(WhichTable.PATIENT, DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, PatientFormat.ALIAS_OF_PATIENT_ID, patientIDStrings));
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }
}
