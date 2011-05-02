/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.ut.biolab.medsavant.model.record.FileRecordModel;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsController {

    private static LibraryVariantsController instance;

    private LibraryVariantsController() {
        try {
            File f = FileController.getLibraryVariantFile();
            if (!f.exists()) { f.createNewFile(); }
            fileRecords = FileRecordModel.getFileRecordsFromFile(f);
            addFileRecord(new FileRecordModel("path",(new Date()).toLocaleString()));
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

    private List<FileRecordModel> fileRecords = new ArrayList<FileRecordModel>();

    public List<FileRecordModel> getFileRecords() {
        return fileRecords;
    }

    public void clearFileRecords() {
        fileRecords = new ArrayList<FileRecordModel>();
        saveRecords();
    }

    public void addFileRecord(FileRecordModel r) {
        fileRecords.add(r);
        saveRecords();
    }

    private void saveRecords() {
        File f = FileController.getLibraryVariantFile();
        try {
            FileRecordModel.writeFileRecordsToFile(f, fileRecords);
        } catch (IOException ex) {
            System.err.println("Error saving variant library");
        }
    }
}
