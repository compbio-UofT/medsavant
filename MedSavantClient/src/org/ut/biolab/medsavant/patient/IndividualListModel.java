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

import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.format.BasicPatientColumns;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.view.list.DetailedListModel;

/**
 *
 * @author mfiume
 */
public class IndividualListModel implements DetailedListModel, BasicPatientColumns {
    private static final String[] COLUMN_NAMES = new String[] { PATIENT_ID.getAlias(),
                                                                FAMILY_ID.getAlias(),
                                                                HOSPITAL_ID.getAlias(),
                                                                IDBIOMOM.getAlias(),
                                                                IDBIODAD.getAlias(),
                                                                GENDER.getAlias(),
                                                                DNA_IDS.getAlias(),
                                                                PHENOTYPES.getAlias() };
    private static final Class[] COLUMN_CLASSES = new Class[] { Integer.class, String.class, String.class, String.class, String.class, Integer.class, String.class, String.class };
    private static final int[] HIDDEN_COLUMNS = new int[] { 0, 1, 3, 4, 5, 6, 7 };
    static final int NAME_INDEX = 2;        // Hospital ID is the closest we have to a name.
    static final int KEY_INDEX = 0;         // Patient ID as unique key.

    @Override
    public Object[][] getList(int limit) throws RemoteException, SQLException {
        return MedSavantClient.PatientManager.getBasicPatientInfo(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), limit).toArray(new Object[0][0]);
    }

    @Override
    public String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @Override
    public Class[] getColumnClasses() {
        return COLUMN_CLASSES;
    }

    @Override
    public int[] getHiddenColumns() {
        return HIDDEN_COLUMNS;
    }
}
