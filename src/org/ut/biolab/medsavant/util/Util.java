/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import org.ut.biolab.medsavant.model.record.FileRecord;

/**
 *
 * @author mfiume
 */
public class Util {

    public static Vector listToVector(List l) {
        Vector v = new Vector(l.size());
        v.addAll(l);
        return v;
    }

    public static Vector getFileRecordVector(List<FileRecord> list) {
        Vector result = new Vector();
        for (FileRecord r : list) {
            Vector v = FileRecord.convertToVector(r);
            result.add(v);
        }
        return result;
    }

   private static Random numGen = new Random();

   public static Color getRandomColor() {
      return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
   }

   public static boolean isQuantatitiveClass(Class c) {
        if (c == Integer.class || c == Long.class || c == Short.class || c == Double.class || c == Float.class) { return true; }
        return false;
    }


   public static Object parseStringValueAs(Class c, String value) {

        if (c == String.class) {
            return value;
        }
        if (c == Long.class) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                return null;
            }
        }

        if (c == Float.class) {
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                return null;
            }
        }

        throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
    }

    public static String getListFilterToString(String filtername, List<String> acceptableValues) {
        String s = filtername + " = (";
        for (String v : acceptableValues) {
            s += v + ",";
        }
        if (!acceptableValues.isEmpty()) {
            s = s.substring(0,s.length()-1);
        }
        s += ")";
        return s;
    }

    public static List<Vector> convertToListOfVectors(List<String[]> rest) {
        List<Vector> result = new ArrayList<Vector>(rest.size());
        for (String[] row : rest) {
            Vector v = new Vector();
            for (String s : row) {
                v.add(s);
            }
            result.add(v);
        }
        return result;
    }

    public static List<Boolean> getTrueList(int size) {
        List<Boolean> results = new ArrayList<Boolean>();
        for (int i = 0; i < size; i++) {
            results.add(true);
        }
        return results;
    }
}
