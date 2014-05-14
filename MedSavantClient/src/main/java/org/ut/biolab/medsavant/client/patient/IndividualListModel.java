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
package org.ut.biolab.medsavant.client.patient;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.list.DetailedListModel;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

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
                                                                AFFECTED.getAlias(),
                                                                DNA_IDS.getAlias(),
                                                                PHENOTYPES.getAlias() };
    private static final Class[] COLUMN_CLASSES = new Class[] { Integer.class, String.class, String.class, String.class, String.class, Integer.class, Integer.class, String.class, String.class };
    private static final int[] HIDDEN_COLUMNS = new int[] { 0, 1, 3, 4, 5, 6, 7, 8 };
    static final int NAME_INDEX = 2;        // Hospital ID is the closest we have to a name.
    static final int KEY_INDEX = 0;         // Patient ID as unique key.

    @Override
    public Object[][] getList(int limit) throws RemoteException, SQLException {
        try {
            return MedSavantClient.PatientManager.getBasicPatientInfo(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), limit).toArray(new Object[0][0]);

        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
    }

    @Override
    public String[] getColumnNames() {
        return COLUMN_NAMES;
    }

    @Override
    public Class[] getColumnClasses() {
        return COLUMN_CLASSES;
    }

    public int[] getHiddenColumns() {
        return HIDDEN_COLUMNS;
    }
}
