/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import java.sql.SQLException;
import java.util.Collection;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.InCondition;
import java.rmi.RemoteException;

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;


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
                    BinaryConditionMS.equalTo(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.DNA_ID), dnaIDs.iterator().next())
                };
            } else {
                return new Condition[] {
                    new InCondition(ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(BasicVariantColumns.DNA_ID), dnaIDs)
                };
            }
        }
        return FALSE_CONDITION;
    }
}
