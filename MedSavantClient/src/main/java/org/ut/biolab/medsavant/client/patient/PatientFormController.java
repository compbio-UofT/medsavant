/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.patient;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.EnumConstraint;
import org.ut.biolab.medsavant.client.util.FormController;
import org.ut.biolab.medsavant.client.util.RegexpConstraint;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 * A controller for a form that adds Patients.
 *
 * @author jim
 */
public class PatientFormController extends FormController {

    public PatientFormController() throws SQLException, RemoteException, SessionExpiredException {
        super(MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(),
                ProjectController.getInstance().getCurrentProjectID()),
                BasicPatientColumns.PATIENT_ID);

        initConstraints();
    }

    /**
     * Submits the patient form, first replacing any blank strings with nulls.
     *
     * @param fields List of Custom Fields. The index of each validated value in
     * 'validatedValues' corresponds to the index of the custom field in
     * 'fields'.
     *
     * @param validatedValues List of values that have already been validated,
     * e.g. by FormEditorDialog
     */
    @Override
    public void submitForm(List<CustomField> fields, List<String> validatedValues) {
        try {
            //replace empty strings with nulls.
            for (int i = 0; i < validatedValues.size(); ++i) {
                String val = validatedValues.get(i);

                if (val.trim().isEmpty()) {
                    validatedValues.remove(i);
                    validatedValues.add(i, null);
                }
            }

            MedSavantClient.PatientManager.addPatient(LoginController.getSessionID(),
                    ProjectController.getInstance().getCurrentProjectID(), fields, validatedValues);
            DialogUtils.displayMessage("Individual Added");            
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error adding individual: %s", ex);
        }
    }

    //Sets up custom constraints that override the default (inferred by columnType) constraints.
    private void initConstraints() {
        setConstraint(BasicPatientColumns.BAM_URL,
                new RegexpConstraint(
                RegexpConstraint.REGEXP_URL_WEB,
                BasicPatientColumns.BAM_URL.getColumnLength(),
                "One or more " + BasicPatientColumns.BAM_URL.getAlias() + " is invalid.  URLs must be entered one per line, and begin with http://"));

        setConstraint(BasicPatientColumns.GENDER,
                new EnumConstraint(
                new String[][]{{"0", "Undisclosed"}, {"1", "Male"}, {"2", "Female"}},
                BasicPatientColumns.GENDER.getColumnLength(),
                "Invalid Gender: please choose either Male, Female, or Undisclosed"));

        setConstraint(BasicPatientColumns.AFFECTED,
                new EnumConstraint(
                new String[][]{{"1", "Yes"}, {"0", "No"}},
                BasicPatientColumns.AFFECTED.getColumnLength(),
                "Affected must be set to either Yes or No"));

        setRequiredField(BasicPatientColumns.HOSPITAL_ID);
        //setRequiredFields(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);

    }
}