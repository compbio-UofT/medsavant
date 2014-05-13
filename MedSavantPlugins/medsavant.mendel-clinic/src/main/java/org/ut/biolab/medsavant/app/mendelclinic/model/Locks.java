package org.ut.biolab.medsavant.app.mendelclinic.model;

import java.io.File;
import javax.swing.JDialog;

/**
 *
 * @author mfiume
 */
public class Locks {

    public static class DialogLock {

        private JDialog resultsDialog;

        public JDialog getResultsDialog() {
            return resultsDialog;
        }

        public void setResultsFrame(JDialog resultsDialog) {
            this.resultsDialog = resultsDialog;
        }
    }

    public static class FileResultLock {

        File f;

        public File getFile() {
            return f;
        }

        public void setFile(File f) {
            this.f = f;
        }
    }
}
