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

import org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields;
import java.awt.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class IndividualDetailedView extends DetailedView implements PedigreeFields {

    private final BlockingPanel blockPanel;
    
    private final PatientView patientView;
    private DetailsWorker detailsWorker;

    public IndividualDetailedView(String page) throws RemoteException, SQLException {
        super(page);

        patientView = new PatientView();
        
        blockPanel = new BlockingPanel("No individual selected", patientView);
        
        this.setLayout(new BorderLayout());
        this.add(blockPanel, BorderLayout.CENTER);
    }
    
    public synchronized void setPatient(Patient patient) {
        patientView.setPatient(patient);
    }

    @Override
    public void setSelectedItem(Object[] item) {
        if (item.length == 0) {
            blockPanel.block();
        } else {
            int patientId = (Integer) item[0];
            String hospitalId = (String) item[2];
            setSelectedItem(patientId, hospitalId);
        }
    }

    public void setSelectedItem(int patientId, String hospitalId) {

        if (detailsWorker != null) {
            detailsWorker.cancel(true);
        }
        detailsWorker = new DetailsWorker(patientId);
        detailsWorker.execute();
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        if (items.isEmpty()) {
            blockPanel.block();
        }
    }

    private class DetailsWorker extends MedSavantWorker<Object[]> {

        private final int patientID;

        private DetailsWorker(int patID) {
            super(getPageName());
            patientID = patID;
        }

        @Override
        protected Object[] doInBackground() throws RemoteException, SQLException {
            try {
                return MedSavantClient.PatientManager.getPatientRecord(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patientID);
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return null;
            }
        }

        @Override
        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(Object[] result) {

            Integer patientIDInteger = (Integer) result[BasicPatientColumns.INDEX_OF_PATIENT_ID];
            
            Patient patient = new Patient(result[BasicPatientColumns.INDEX_OF_HOSPITAL_ID].toString());
            patient.setID(patientIDInteger);
            patient.setFamilyID(toStringProtected(result[BasicPatientColumns.INDEX_OF_FAMILY_ID]));
            patient.setMotherHospitalID(toStringProtected(result[BasicPatientColumns.INDEX_OF_IDBIOMOM]));
            patient.setFatherHospitalID(toStringProtected(result[BasicPatientColumns.INDEX_OF_IDBIODAD]));
            Integer genderInteger = (Integer) result[BasicPatientColumns.INDEX_OF_GENDER];
            if (genderInteger != null) {
                patient.setSex(toStringProtected(ClientMiscUtils.genderToString((Integer)genderInteger)));
            } else{
                patient.setSex(ClientMiscUtils.GENDER_UNKNOWN);
            }
            patient.setAffected((Boolean) result[BasicPatientColumns.INDEX_OF_AFFECTED]);
            patient.setBamURL(toStringProtected(result[BasicPatientColumns.INDEX_OF_BAM_URL]));
            patient.setDnaID(toStringProtected(result[BasicPatientColumns.INDEX_OF_DNA_IDS]));
            patient.setPhenotypes(toStringProtected(result[BasicPatientColumns.INDEX_OF_PHENOTYPES]));
            
            setPatient(patient);
            blockPanel.unblock();
        }

        private String toStringProtected(Object object) {
            if (object == null) {
                return null;
            } else {
                return object.toString();
            }
        }
    }
}
