/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.util;

import com.jidesoft.swing.JideButton;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.swing.JButton;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.record.FileRecord;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;

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
    
    public static VariantRecord convertToVariantRecord(Vector dbResult) {
        
        VariantTableSchema V = (VariantTableSchema)MedSavantDatabase.getInstance().getVariantTableSchema();      
        //VariantAnnotationSiftTableSchema S = (VariantAnnotationSiftTableSchema)MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        int i = 0;
        
        return new VariantRecord(
                
                //variant
                (Integer) dbResult.get(V.INDEX_VARIANTID-1),
                (Integer) dbResult.get(V.INDEX_GENOMEID-1),
                (Integer) dbResult.get(V.INDEX_PIPELINEID-1),
                (String) dbResult.get(V.INDEX_DNAID-1),
                (String) dbResult.get(V.INDEX_CHROM-1),
                new Long((Integer) dbResult.get(V.INDEX_POSITION-1)),
                (String) dbResult.get(V.INDEX_DBSNPID-1),
                (String) dbResult.get(V.INDEX_REFERENCE-1),
                (String) dbResult.get(V.INDEX_ALTERNATE-1),
                (Float)  dbResult.get(V.INDEX_QUALITY-1),
                (String) dbResult.get(V.INDEX_FILTER-1),
                (String) dbResult.get(V.INDEX_AA-1),
                (String) dbResult.get(V.INDEX_AC-1),
                (String) dbResult.get(V.INDEX_AF-1),
                (Integer) dbResult.get(V.INDEX_AN-1),
                (Float) dbResult.get(V.INDEX_BQ-1),
                (String) dbResult.get(V.INDEX_CIGAR-1),
                (Boolean) dbResult.get(V.INDEX_DB-1),
                (Integer) dbResult.get(V.INDEX_DP-1),
                new Long((Integer) dbResult.get(V.INDEX_END-1)),
                (Boolean) dbResult.get(V.INDEX_H2-1),
                (Float) dbResult.get(V.INDEX_MQ-1),
                (Integer) dbResult.get(V.INDEX_MQ0-1),
                (Integer) dbResult.get(V.INDEX_NS-1),
                (Float) dbResult.get(V.INDEX_SB-1),
                (Boolean) dbResult.get(V.INDEX_SOMATIC-1),
                (Boolean) dbResult.get(V.INDEX_VALIDATED-1),
                (String) dbResult.get(V.INDEX_CUSTOMINFO-1),

                //sift
                (String) dbResult.get(V.getNumFields() + i++), //name
                (String) dbResult.get(V.getNumFields() + i++), //name2
                (Double) dbResult.get(V.getNumFields() + i++), //damage probability
                
                //polyphen
                (String) dbResult.get(V.getNumFields() + i++), //cdnacoord
                (Integer) dbResult.get(V.getNumFields() + i++), //opos
                (String) dbResult.get(V.getNumFields() + i++), //oaa1
                (String) dbResult.get(V.getNumFields() + i++), //oaa2
                (String) dbResult.get(V.getNumFields() + i++), //snpid
                (String) dbResult.get(V.getNumFields() + i++), //acc
                (Integer) dbResult.get(V.getNumFields() + i++), //pos
                (String) dbResult.get(V.getNumFields() + i++), //prediction
                (String) dbResult.get(V.getNumFields() + i++), //pph2class
                (Float) dbResult.get(V.getNumFields() + i++), //pph2prob
                (Float) dbResult.get(V.getNumFields() + i++), //pph2fdr
                (Float) dbResult.get(V.getNumFields() + i++), //pph2tpr
                (Float) dbResult.get(V.getNumFields() + i++), //pph2fdr
                (Integer) dbResult.get(V.getNumFields() + i++), //transv
                (Integer) dbResult.get(V.getNumFields() + i++), //codpos
                (Integer) dbResult.get(V.getNumFields() + i++), //cpg
                (Float) dbResult.get(V.getNumFields() + i++), //mindjnc
                (Float) dbResult.get(V.getNumFields() + i++), //idpmax
                (Float) dbResult.get(V.getNumFields() + i++), //idpsnp
                (Float) dbResult.get(V.getNumFields() + i++), //idqmin
                
                //gatk
                (String) dbResult.get(V.getNumFields() + i++), //name
                (String) dbResult.get(V.getNumFields() + i++), //name2
                (String) dbResult.get(V.getNumFields() + i++), //transcriptStrand
                (String) dbResult.get(V.getNumFields() + i++), //positionType
                (Integer) dbResult.get(V.getNumFields() + i++), //frame
                (Integer) dbResult.get(V.getNumFields() + i++), //mrnaCoord
                (Integer) dbResult.get(V.getNumFields() + i++), //codonCoord
                (Integer) dbResult.get(V.getNumFields() + i++), //spliceDist
                (String) dbResult.get(V.getNumFields() + i++), //referenceCodon
                (String) dbResult.get(V.getNumFields() + i++), //referenceAA
                (String) dbResult.get(V.getNumFields() + i++), //variantCodon
                (String) dbResult.get(V.getNumFields() + i++), //variantAA
                (String) dbResult.get(V.getNumFields() + i++), //changesAA
                (String) dbResult.get(V.getNumFields() + i++), //functionalClass
                (String) dbResult.get(V.getNumFields() + i++), //codingCoordStr
                (String) dbResult.get(V.getNumFields() + i++), //proteinCoordStr
                (String) dbResult.get(V.getNumFields() + i++), //inCodingRegion
                (String) dbResult.get(V.getNumFields() + i++), //spliceInfo
                (String) dbResult.get(V.getNumFields() + i++), //uorfChange
                (String) dbResult.get(V.getNumFields() + i++) //spliceInfoCopy
                
                );
    }


}
