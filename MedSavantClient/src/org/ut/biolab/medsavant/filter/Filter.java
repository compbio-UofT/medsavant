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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;

import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.BinaryConditionMS;


/**
 *
 * @author mfiume
 */
public abstract class Filter {

    public static enum Type { NUMERIC, STRING, BOOLEAN, COHORT, REGION_LIST, GENERIC, TAG, PLUGIN, ONTOLOGY };

    public static final Condition[] FALSE_CONDITION = new Condition[] { BinaryCondition.equalTo(0, 1) };

    public Filter() {
    }
    
    public abstract String getName();

    public abstract String getID();

    public abstract Condition[] getConditions() throws InterruptedException, SQLException, RemoteException;
    
    /**
     * Many filters need to make a filter based on a list of DNA IDs.
     */
    public Condition[] getDNAIDCondition(Collection<String> dnaIDs) {
        if (dnaIDs.size() > 0) {
            if (dnaIDs.size() == 1) {
                return new Condition[] {
                    BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIDs.iterator().next())
                };
            } else {
                return new Condition[] {
                    new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIDs)
                };
            }
        }
        return FALSE_CONDITION;
    }
}
