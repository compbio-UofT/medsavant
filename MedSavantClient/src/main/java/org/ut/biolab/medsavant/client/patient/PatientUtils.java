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
package org.ut.biolab.medsavant.client.patient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.filter.StringListFilterView;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;


/**
 *
 * @author Andrew
 */
public class PatientUtils implements BasicPatientColumns {

    public static JPopupMenu createPopup(final String famID) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem filter1Item = new JMenuItem("Filter by Family");
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeneticsFilterPage.getSearchBar().loadFilters(StringListFilterView.wrapState(WhichTable.PATIENT, FAMILY_ID.getColumnName(), FAMILY_ID.getAlias(), Arrays.asList(famID)));
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }

    /**
     * Create a popup to filter by patient IDs.
     *
     * @param patIDs ids of selected patient(s)
     */
    public static JPopupMenu createPopup(final int[] patIDs) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", patIDs.length == 1 ? "Patient <i>" + patIDs[0] + "</i>" : "Selected Patients"));
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> patientIDStrings = new ArrayList<String>();
                for (int id: patIDs) {
                    patientIDStrings.add(Integer.toString(id));
                }
                GeneticsFilterPage.getSearchBar().loadFilters(StringListFilterView.wrapState(WhichTable.PATIENT, PATIENT_ID.getColumnName(), PATIENT_ID.getAlias(), patientIDStrings));
            }
        });
        popupMenu.add(filter1Item);

        return popupMenu;
    }
}
