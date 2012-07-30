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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.rmi.RemoteException;
import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;

/**
 *
 * @author Andrew
 */
public class FilterUtils {

    public enum WhichTable {
        PATIENT,
        VARIANT;

        public String getName() throws RemoteException, SQLException {
            return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableName() : ProjectController.getInstance().getCurrentPatientTableName();
        }

        public TableSchema getSchema() {
            return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableSchema() : ProjectController.getInstance().getCurrentPatientTableSchema();
        }
    };

    /*
     * This should generally be used for any filter applications external
     * to the TablePanel.
     */
    public static void createAndApplyGenericFixedFilter(String title, String description, Condition c) {
/*
        SearchBar fp = getFilterPanel();
        FilterController.setAutoCommit(false);

        //create and apply filter to each subquery
        List<FilterPanelSubItem> filterPanels = new ArrayList<FilterPanelSubItem>();
        for (SearchConditionsPanel fps : fp.getFilterPanelSubs()) {
            String filterId = Long.toString(System.nanoTime());
            FilterView view = new GenericFixedFilterView(title, c, description, fps.getID(), filterId);
            filterPanels.add(fps.addNewSubItem(view, filterId));
        }

        fp.refreshSubPanels();

        FilterController.commit(title, FilterController.FilterAction.ADDED);
        FilterController.setAutoCommit(true);*/
    }
}
