/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model;

import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsRecordModel {

    public static final int INDEX_OF_FILENAME = 0;
    public static final int INDEX_OF_FILESIZE = INDEX_OF_FILENAME + 1;
    public static final int INDEX_OF_ADDEDDATE = INDEX_OF_FILESIZE + 1;
    private static final Class CLASS_OF_FILENAME = String.class;
    private static final Class CLASS_OF_FILESIZE = String.class;
    private static final Class CLASS_OF_ADDEDDATE = String.class;
    private static final int NUM_FIELDS = INDEX_OF_ADDEDDATE + 1; // index of the last field + 1

    public static Vector getFieldNames() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_FILENAME:
                    v.add("File name");
                    break;
                case INDEX_OF_FILESIZE:
                    v.add("Size");
                    break;
                case INDEX_OF_ADDEDDATE:
                    v.add("Added on");
                    break;
            }
        }
        return v;
    }

    public static Vector getFieldClasses() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_FILENAME:
                    v.add(CLASS_OF_FILENAME);
                    break;
                case INDEX_OF_FILESIZE:
                    v.add(CLASS_OF_FILESIZE);
                    break;
                case INDEX_OF_ADDEDDATE:
                    v.add(CLASS_OF_ADDEDDATE);
                    break;
            }
        }
        return v;
    }

}
