/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.api;

import java.sql.SQLException;

import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;


/**
 * API for project-related functionality exposed to plugins.  Expect this to be refactored at
 * some point.
 * 
 * TODO: Give this class a better name.
 *
 * @author tarkvara
 */
public class ProjectUtils {
    /**
     * @return an integer which uniquely identifies the current project
     */
    public static int getCurrentProjectID() {
        return ProjectController.getInstance().getCurrentProjectId();
    }

    /**
     * @return an integer which uniquely identifies the current reference
     */
    public static int getCurrentReferenceID() {
        return ReferenceController.getInstance().getCurrentReferenceId();
    }
    
    public static TableSchema getCustomVariantTableSchema(int projectID, int refID) throws SQLException {
        return CustomTables.getCustomTableSchema(ProjectQueryUtil.getVariantTablename(projectID, refID));
    }
    
    public static void addFilterConditions(final Condition[] conditions, final String filterName, final String filterID, int queryID) {
        FilterController.addFilter(new QueryFilter() {
            @Override
            public Condition[] getConditions() {
                return conditions;
            }

            @Override
            public String getName() {
                return filterName;
            }

            @Override
            public String getId() {
                return filterID;
            }
        }, queryID);
    }
    
    public static void removeFilter(String filterID, int queryID) {
        FilterController.removeFilter(filterID, queryID);
    }
    
    public static void addFilterListener(FiltersChangedListener l) {
        FilterController.addFilterListener(l);
    }

    public static boolean isFilterActive(String filterID, int queryID) {
        return FilterController.isFilterActive(queryID, filterID);
    }
}
