package org.ut.biolab.medsavant.view.manage;

import com.jidesoft.utils.SwingWorker;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.importfile.BedFormat;
import org.ut.biolab.medsavant.importfile.FileFormat;
import org.ut.biolab.medsavant.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.importfile.ImportFileView;
import org.ut.biolab.medsavant.view.MainFrame;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author mfiume
 */
class IntervalDetailedListEditor extends DetailedListEditor {

    @Override
    public boolean doesImplementAdding() {
        return true;
    }

    @Override
    public boolean doesImplementDeleting() {
        return true;
    }

    @Override
    public void addItems() {
        String geneListName = (String) JOptionPane.showInputDialog(
                null,
                "Name of Region List:",
                "Import Region List",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");

        if (geneListName == null) {
            return;
        }

        ImportFileView d = new ImportFileView(null, "Import List from File");
        d.addFileFormat(new BedFormat());
        d.setVisible(true);

        if (d.isImportAccepted()) {

            final ImportCohortSW importsw = new ImportCohortSW(
                    geneListName,
                    d.getPath(),
                    d.getDelimiter(),
                    d.getNumHeaderLines(),
                    d.getFileFormat());

            GenericProgressDialog importpd = new GenericProgressDialog("Importing gene list", "Importing gene list");

            importsw.setProgressDialog(importpd);
            importpd.setCancelListener(new GenericProgressDialog.CancelRequestListener() {

                public void cancelRequested() {
                    importsw.cancel(true);
                }
            });

            importsw.execute();

            importpd.setVisible(true);

        }

        d.dispose();
    }

    @Override
    public void editItems(Object[] results) {
    }

    @Override
    public void deleteItems(final List<Object[]> items) {

        int result;

        if (items.size() == 1) {
            String name = ((RegionSet) items.get(0)[0]).getName();
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove " + name + "?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        } else {
            result = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
                    "Are you sure you want to remove these " + items.size() + " lists?\nThis cannot be undone.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
        }

        if (result == JOptionPane.YES_OPTION) {

            final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                    "Removing Region List(s)",
                    "Removing region list(s). Please wait.",
                    true);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    int numCouldntRemove = 0;

                    for (Object[] v : items) {
                        String listName = ((RegionSet) v[0]).getName();
                        int listId = ((RegionSet) v[0]).getId();
                        try {
                            RegionQueryUtil.removeRegionList(listId);
                        } catch (SQLException ex) {
                            numCouldntRemove++;
                            DialogUtils.displayErrorMessage("Could remove " + listName + ".", ex);
                        }
                    }
                    dialog.close();
                    if (numCouldntRemove != items.size()) {
                        DialogUtils.displayMessage("Successfully removed " + (items.size()) + " list(s)");
                    }
                }
            };
            thread.start();
            dialog.setVisible(true);
        }
    }

    private class ImportCohortSW extends SwingWorker {

        private final String path;
        private final char delim;
        private final int numHeaderLines;
        private final FileFormat fileFormat;
        private GenericProgressDialog progressDialog;
        private final String geneListName;

        private ImportCohortSW(String genelistname, String path, char delimiter, int numHeaderLines, FileFormat ff) {
            this.geneListName = genelistname;
            this.path = path;
            this.delim = delimiter;
            this.numHeaderLines = numHeaderLines;
            this.fileFormat = ff;
        }

        @Override
        protected Object doInBackground() throws Exception {

            Iterator<String[]> i = ImportDelimitedFile.getFileIterator(path, delim, numHeaderLines, fileFormat);

            //DBUtil.addRegionList(geneListName,i);
            RegionQueryUtil.addRegionList(geneListName, ReferenceController.getInstance().getCurrentReferenceId(), i);

            return null;

        }

        @Override
        protected void done() {
            try {
                get();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (progressDialog != null) {
                if (!progressDialog.wasCancelRequested()) {
                    progressDialog.setComplete();
                }
            }
        }

        private void setProgressDialog(GenericProgressDialog importpd) {
            this.progressDialog = importpd;
        }

        private void setLinesProcessed(int i) {
            if (progressDialog != null) {
                progressDialog.setStatus(i + " lines parsed");
            }
        }
    }
}
