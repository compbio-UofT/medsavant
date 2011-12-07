/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.model.record.FileRecord;

/**
 *
 * @author Andrew
 */
public class Util {
    
    public static Vector getFileRecordVector(List<FileRecord> list) {
        Vector result = new Vector();
        for (FileRecord r : list) {
            Vector v = FileRecord.convertToVector(r);
            result.add(v);
        }
        return result;
    }
    
}
