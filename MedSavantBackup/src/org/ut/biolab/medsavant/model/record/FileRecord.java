/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.model.record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import org.ut.biolab.medsavant.util.Util;

/**
 *
 * @author mfiume
 */
public class FileRecord {

    private static final int INDEX_OF_FILENAME = 0;
    private static final int INDEX_OF_MODDATE = INDEX_OF_FILENAME + 1;
    public static final Class CLASS_OF_FILENAME = String.class;
    public static final Class CLASS_OF_MODDATE = String.class;
    public static final int NUM_FIELDS = INDEX_OF_MODDATE + 1; // index of last field + 1

    public static final char DELIMITER = '\t';

    private String fileName;
    private String moddate;

    public FileRecord(String[] line) {
        fileName =     (String)    Util.parseStringValueAs(CLASS_OF_FILENAME, line[INDEX_OF_FILENAME]);
        moddate =       (String)  Util.parseStringValueAs(CLASS_OF_MODDATE, line[INDEX_OF_MODDATE]);
    }

    public FileRecord(String path, String comments) {
        this.fileName = path;
        this.moddate = comments;
    }

    public String getModificationDate() {
        return moddate;
    }

    public void setModificationDate(String moddate) {
        this.moddate = moddate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean getExists() {
        return (new File(fileName)).exists();
    }

    public static List<FileRecord> getFileRecordsFromFile(File f) throws IOException {

        CSVReader r = getFileReader(f, DELIMITER);
        
        String [] nextLine;

        List<FileRecord> results = new ArrayList<FileRecord>();

        while ((nextLine = r.readNext()) != null) {

            if (nextLine.length > 0) {
                    results.add(new FileRecord(nextLine));
            }
        }

        r.close();

        return results;
    }

    public static void writeFileRecordsToFile(File f, List<FileRecord> records) throws IOException {

        CSVWriter w = getFileWriter(f, DELIMITER);

        for (FileRecord r : records) {
            
            String[] arr = new String[NUM_FIELDS];
            for (int i = 0; i < NUM_FIELDS; i++) {
                switch (i) {
                    case INDEX_OF_FILENAME:
                        arr[i] = r.getFileName().replace("\\", "\\\\");
                        break;
                    case INDEX_OF_MODDATE:
                        arr[i] = r.getModificationDate();
                        break;
                }
            }

            w.writeNext(arr);
        }

        w.close();
    }

    private static CSVReader getFileReader(File f, char delim) throws FileNotFoundException {
        return new CSVReader(new FileReader(f), delim);
    }

    private static CSVWriter getFileWriter(File f, char delim) throws FileNotFoundException, IOException {
        return new CSVWriter(new FileWriter(f), delim);
    }

    public static List<String> getFieldNames() {
        List<String> v = new ArrayList<String>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_FILENAME:
                    v.add("File name");
                    break;
                case INDEX_OF_MODDATE:
                    v.add("Added on");
                    break;
            }
        }
        return v;
    }

    public static List<Class> getFieldClasses() {
        List<Class> v = new ArrayList<Class>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_FILENAME:
                    v.add(CLASS_OF_FILENAME);
                    break;
                case INDEX_OF_MODDATE:
                    v.add(CLASS_OF_MODDATE);
                    break;
            }
        }
        return v;
    }

    public static List<Boolean> getDefaultColumns(){
        List<Boolean> list = new ArrayList<Boolean>();
        for(int i = 0; i < NUM_FIELDS; i++){
            list.add(true);
        }
        return list;
    }

}
