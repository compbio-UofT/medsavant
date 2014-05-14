/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient.pedigree;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.AFFECTED;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.DAD;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.DNA_ID;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.GENDER;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.HOSPITAL_ID;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.MOM;
import static org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields.PATIENT_ID;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;

/**
 *
 * @author mfiume
 */
public class PedigreeWorker extends MedSavantWorker<File> {

    private final int patientID;
    private PedigreeCanvas canvas;

    public PedigreeWorker(int patID, PedigreeCanvas canvas) {
        super("Pedigree");
        this.patientID = patID;
        this.canvas = canvas;
    }

    @Override
    protected File doInBackground() throws Exception {

        System.out.println("Fetching family of patient " + patientID + "...");
        List<Object[]> results = MedSavantClient.PatientManager.getFamilyOfPatient(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), patientID);

        System.out.println("Starting pedigree worker...");
        File outfile = new File(DirectorySettings.getTmpDirectory(), "pedigree" + patientID + ".csv");
        
        if (outfile.exists()) {
            outfile.delete();
        }
        
        System.out.println("Writing to " + outfile.getAbsolutePath());
        CSVWriter w = new CSVWriter(new FileWriter(outfile), ',', CSVWriter.NO_QUOTE_CHARACTER);

        System.out.println("Writing header...");
        w.writeNext(new String[]{HOSPITAL_ID, MOM, DAD, PATIENT_ID, GENDER, AFFECTED, DNA_ID});

        System.out.println("Writing fields...");
        for (Object[] row : results) {
            String[] srow = new String[row.length];
            for (int i = 0; i < row.length; i++) {
                String entry = row[i] == null ? "" : row[i].toString();
                srow[i] = entry;
            }
            w.writeNext(srow);
        }
        w.close();
        return outfile;
    }

    @Override
    protected void showProgress(double ignored) {
    }

    @Override
    protected void showSuccess(final File result) {

        canvas.showPedigree(result, patientID);
        /*
        System.out.println("Showing pedigree on canvas...");
        if (!this.isCancelled()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Really showing pedigree on canvas...");
                    
                }
            });
        }*/
    }

    private void showPedigree(String absolutePath) {
        JFrame f = new JFrame("/Users/mfiume/.medsavant/tmp/pedigree1.csv");
        PedigreeCanvas pc = new PedigreeCanvas();
        pc.showPedigree(new File(absolutePath), patientID);
        f.add(pc);
        f.pack();
        f.setVisible(true);
    }
}
