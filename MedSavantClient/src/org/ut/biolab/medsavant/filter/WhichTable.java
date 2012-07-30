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

import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.project.ProjectController;


/**
 * Enum which identifies which type of data is being filtered:  variant data or patient data.
 *
 * @author tarkvara
 */
public enum WhichTable {
    PATIENT,
    VARIANT;

    public String getName() throws RemoteException, SQLException {
        return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableName() : ProjectController.getInstance().getCurrentPatientTableName();
    }

    public TableSchema getSchema() {
        return this == WhichTable.VARIANT ? ProjectController.getInstance().getCurrentVariantTableSchema() : ProjectController.getInstance().getCurrentPatientTableSchema();
    }
}
