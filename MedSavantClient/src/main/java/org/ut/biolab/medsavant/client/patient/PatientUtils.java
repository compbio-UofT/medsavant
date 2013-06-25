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
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.filter.SearchBar;

import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.filter.StringListFilterView;
import org.ut.biolab.medsavant.client.filter.WhichTable;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsFilterPage;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.mfiume.query.QueryViewController;
//import org.ut.biolab.mfiume.query.SearchConditionItem;
import org.ut.biolab.mfiume.query.SearchConditionGroupItem.QueryRelation;
import org.ut.biolab.mfiume.query.value.encode.NumericConditionEncoder;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;



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
    
    public static JPopupMenu createPopup(final String[] hospitalIDs){
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", hospitalIDs.length == 1 ? "Hospital ID <i>" + hospitalIDs[0] + "</i>" : "Selected Hospital IDs"));
        filter1Item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QueryViewController qvc = SearchBar.getInstance().getQueryViewController();                 
                 
                 String encodedConditions = StringConditionEncoder.encodeConditions(Arrays.asList(hospitalIDs));
                 String description = StringConditionEncoder.getDescription(Arrays.asList(hospitalIDs));
                                 
                 qvc.replaceFirstLevelItem(BasicPatientColumns.HOSPITAL_ID.getAlias(), encodedConditions, description);
                 MedSavantFrame.getInstance().searchAnimationFromMousePos("Selected Hospital IDS have been added to query.  Click 'Variants' to review and execute search.");    
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
                QueryUtils.addQueryOnPatients(patIDs);                                
            }
        });
        popupMenu.add(filter1Item);
        
        return popupMenu;              
        
    }
}
