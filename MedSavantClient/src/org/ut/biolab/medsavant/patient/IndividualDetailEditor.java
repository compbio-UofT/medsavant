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

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.ColumnType;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.ProgressStatus;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.view.dialog.CancellableProgressDialog;
import org.ut.biolab.medsavant.view.dialog.ProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class IndividualDetailEditor extends DetailedListEditor {

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
            new AddPatientsForm().setVisible(true);
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
                                LoginController.sessionId,
                                ProjectController.getInstance().getCurrentProjectID(),
                                patients);
                        setVisible(false);
                        DialogUtils.displayMessage("Successfully removed " + items.size() + " individual(s)");
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
                MedSavantClient.PatientManager.clearPatients(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());

                new ImportProgressDialog(file).showDialog();
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to import individuals.", ex);
        }
    }

    @Override
    public void exportItems() {
        try {
            File file = DialogUtils.chooseFileForSave("Export Individuals", "individuals.csv", ExtensionFileFilter.createFilters(new String[]{"csv"}), null);
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
            CustomField[] fields = MedSavantClient.PatientManager.getPatientFields(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());

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

                    MedSavantClient.PatientManager.addPatient(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), headerToField, values);
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

            CustomField[] fields = MedSavantClient.PatientManager.getPatientFields(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());
            List<Object[]> patients = MedSavantClient.PatientManager.getPatients(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID());

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
