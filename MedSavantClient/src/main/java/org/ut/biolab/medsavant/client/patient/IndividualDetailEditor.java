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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.broad.igv.tdf.TDFDataset.DataType;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.shared.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.client.view.dialog.CancellableProgressDialog;
import org.ut.biolab.medsavant.client.view.dialog.FormEditorDialog;
import org.ut.biolab.medsavant.client.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author mfiume
 */
public class IndividualDetailEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementDeleting() {
        return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementImporting() {
        return LoginController.getInstance().getUserLevel() == UserLevel.ADMIN;
    }

    @Override
    public boolean doesImplementExporting() {
        return true;
    }

    @Override
    public void addItems() {
        try {
            /*
            AddPatientsForm jd = new AddPatientsForm();
            jd.setModal(true);
            jd.setVisible(true);
            */
            PatientFormController pfc = new PatientFormController();
            FormEditorDialog fed = new FormEditorDialog(pfc);
            fed.setTitle("Add Patient");
            fed.setVisible(true);

        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to present Add Individual form: %s", ex);
        }
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            result = DialogUtils.askYesNo("Confirm", "<html>Are you sure you want to remove <i>%s</i>?<br>This cannot be undone.</html>", items.get(0)[IndividualListModel.NAME_INDEX]);
        } else {
            result = DialogUtils.askYesNo("Confirm", "Are you sure you want to remove these %d individuals?\nThis cannot be undone.", items.size());
        }

        if (result == DialogUtils.YES) {
            final int[] patients = new int[items.size()];
            int index = 0;
            for (Object[] v : items) {
                patients[index++] = (Integer)v[IndividualListModel.KEY_INDEX];
            }

            new ProgressDialog("Removing Individual(s)", patients.length + " individual(s) being removed. Please wait.") {
                @Override
                public void run() {
                    try {
                        MedSavantClient.PatientManager.removePatient(
                                LoginController.getSessionID(),
                                ProjectController.getInstance().getCurrentProjectID(),
                                patients);
                        DialogUtils.displayMessage("Successfully removed " + items.size() + " individual(s)");
                        setVisible(false);
                    } catch (Exception ex) {
                        setVisible(false);
                        ClientMiscUtils.reportError("Error removing individual(s): %s", ex);
                    }

                }
            }.setVisible(true);
        }
    }

    @Override
    public void importItems(){

        //Warn that data will be replaced
        if (DialogUtils.askYesNo("Confirm", "<html>Importing individuals will REPLACE all existing individuals.<br>Are you sure you want to do this?</html>") == DialogUtils.NO) {
            return;
        }

        try {
            File file = DialogUtils.chooseFileForOpen("Import File", new ExtensionsFileFilter(new String[] { "csv" }), null);
            if (file != null) {
                //remove current data
                MedSavantClient.PatientManager.clearPatients(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                new ImportProgressDialog(file).showDialog();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to import individuals. Please make sure the file is in CSV format and that Hospital IDs are unique.", ex);
        }
    }

    @Override
    public void exportItems() {
        try {
            String filename = ProjectController.getInstance().getCurrentProjectName().replace(" ", "") + "-patients-" + System.currentTimeMillis() + ".csv";
            File file = DialogUtils.chooseFileForSave("Export Individuals", filename, ExtensionFileFilter.createFilters(new String[]{"csv"}), null);
            if (file != null) {
                new ExportProgressDialog(file).showDialog();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to export individuals.", ex);
        }
    }

    private class ImportProgressDialog extends CancellableProgressDialog {
        private File importFile;

        private ImportProgressDialog(File f) {
            super("Importing Individuals", String.format("<html>Importing from <i>%s</i></html>", f.getName()));
            importFile = f;
        }

        @Override
        public void run() throws InterruptedException, SQLException, RemoteException, IOException {
            lastStatus = new ProgressStatus("Reading records...", -1.0);
            CustomField[] fields = null;
            try {
                fields = MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
            }

            CSVReader in = new CSVReader(new BufferedReader(new FileReader(importFile)));

            String[] header = in.readNext();
            if (header != null) {
                List<CustomField> headerToField = new ArrayList<CustomField>();
                for (String s: header) {
                    boolean found = false;
                    //TODO: This is inefficient. Use a sorted data structure to do searches
                    for (CustomField f: fields) {
                        if (s.equals(f.getAlias())) {
                            headerToField.add(f);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new IOException(String.format("No field found for \"%s\".", s));
                    }
                }

                String[] line;
                while ((line = in.readNext()) != null && !cancelled) {
                    List<String> values = new ArrayList<String>();
                    values.addAll(Arrays.asList(line));

                    // replace empty strings for nulls and booleans with 0,1
                    for (int i = 0; i < values.size(); i++) {
                        String s = values.get(i);
                        if (s.equals("") || s.equals("null")) {
                            values.set(i, null);
                        } else if (headerToField.get(i).getColumnType() == ColumnType.BOOLEAN) {
                            if (s.toLowerCase().equals("true")) {
                                values.set(i, "1");
                            } else if (s.toLowerCase().equals("false")) {
                                values.set(i, "0");
                            }
                        }
                    }
                    try {
                        MedSavantClient.PatientManager.addPatient(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), headerToField, values);
                    } catch (SessionExpiredException ex) {
                        MedSavantExceptionHandler.handleSessionExpiredException(ex);
                    }
                }

                in.close();
            }
        }
    }

    private class ExportProgressDialog extends CancellableProgressDialog {
        private final File exportFile;

        private ExportProgressDialog(File f) {
            super("Exporting Individuals", String.format("<html>Exporting individuals to <i>%s</i></html>", f.getName()));
            exportFile = f;
        }

        @Override
        public void run() throws SQLException, RemoteException, IOException, InterruptedException {
            lastStatus = new ProgressStatus("Writing records...", 0.0);

            CustomField[] fields = null;
            List<Object[]> patients = null;

            try {
                fields = MedSavantClient.PatientManager.getPatientFields(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
                patients = MedSavantClient.PatientManager.getPatients(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID());
            } catch (SessionExpiredException ex) {
                MedSavantExceptionHandler.handleSessionExpiredException(ex);
                return;
            }

            CSVWriter out = new CSVWriter(new BufferedWriter(new FileWriter(exportFile, false)), ',', '"');

            //write header
            String[] headerList = new String[fields.length - 1];
            for (int i = 1; i < fields.length; i++) { //skip patientId
                headerList[i - 1] = fields[i].getAlias();
            }
            out.writeNext(headerList);

            // write patients
            int j = 0;
            for (Object[] patient: patients) {
                if (cancelled) {
                    break;
                }
                String[] line = new String[patient.length - 1];
                for (int i = 1; i < patient.length; i++) {
                    line[i - 1] = valueToString(patient[i]);
                }
                out.writeNext(line);
                Thread.sleep(50);
                lastStatus.fractionCompleted = (double)(++j) / patients.size();
            }

            out.close();
            lastStatus.fractionCompleted = 1.0;
        }

        private String valueToString(Object val) {
            if (val == null) {
                return "";
            }
            return val.toString();
        }
    }
}
