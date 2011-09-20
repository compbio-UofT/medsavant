/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.oldcontroller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.model.record.FileRecord;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsController {

    private static LibraryVariantsController instance;
    private ArrayList<ChangeListener> listeners;

    private LibraryVariantsController() {
        try {
            File f = FileController.getLibraryVariantFile();
            if (!f.exists()) { f.createNewFile(); }
            fileRecords = FileRecord.getFileRecordsFromFile(f);
            listeners = new ArrayList<ChangeListener>();
        } catch (IOException ex) {
            System.err.println("Error retriving variant library");
        }
    }

    public static LibraryVariantsController getInstance() {
        if (instance == null) {
            instance = new LibraryVariantsController();
        }
        return instance;
    }

    private List<FileRecord> fileRecords = new ArrayList<FileRecord>();

    public List<FileRecord> getFileRecords() {
        return fileRecords;
    }

    public void clearFileRecords() {
        fileRecords = new ArrayList<FileRecord>();
        fireChangeEvent();
        saveRecords();
    }

    public void addFileRecord(FileRecord r) {
        fileRecords.add(r);
        fireChangeEvent();
        saveRecords();
    }

    private void saveRecords() {
        File f = FileController.getLibraryVariantFile();
        try {
            FileRecord.writeFileRecordsToFile(f, fileRecords);
        } catch (IOException ex) {
            System.err.println("Error saving variant library");
        }
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void fireChangeEvent() {
        for (ChangeListener l : listeners) {
            l.stateChanged(null);
        }
    }

    public void removeRecordAtIndex(int row) {
        fileRecords.remove(row);
        fireChangeEvent();
        saveRecords();
    }
}
