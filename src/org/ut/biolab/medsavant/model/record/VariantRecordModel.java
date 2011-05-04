/*
 *    Copyright 2009-2010 University of Toronto
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

import fiume.vcf.VariantRecord;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class VariantRecordModel {

    public static final int INDEX_OF_SAMPLEID = 0;
    public static final int INDEX_OF_CALLDETAILS = INDEX_OF_SAMPLEID + 1;
    public static final int INDEX_OF_CHROM = INDEX_OF_CALLDETAILS + 1;
    public static final int INDEX_OF_POS = INDEX_OF_CHROM + 1;
    public static final int INDEX_OF_ID = INDEX_OF_POS + 1;
    public static final int INDEX_OF_REF = INDEX_OF_ID + 1;
    public static final int INDEX_OF_ALT = INDEX_OF_REF + 1;
    public static final int INDEX_OF_QUAL = INDEX_OF_ALT + 1;
    public static final int INDEX_OF_FILTER = INDEX_OF_QUAL + 1;
    public static final int INDEX_OF_INFO = INDEX_OF_FILTER + 1;
    public static final int INDEX_OF_FORMAT = INDEX_OF_INFO + 1;
    private static final Class CLASS_OF_SAMPLEID = String.class;
    private static final Class CLASS_OF_CALLDETAILS = String.class;
    private static final Class CLASS_OF_CHROM = VariantRecord.CLASS_OF_CHROM;
    private static final Class CLASS_OF_POS = VariantRecord.CLASS_OF_POS;
    private static final Class CLASS_OF_ID = VariantRecord.CLASS_OF_ID;
    private static final Class CLASS_OF_REF = VariantRecord.CLASS_OF_REF;
    private static final Class CLASS_OF_ALT = VariantRecord.CLASS_OF_ALT;
    private static final Class CLASS_OF_QUAL = VariantRecord.CLASS_OF_QUAL;
    private static final Class CLASS_OF_FILTER = VariantRecord.CLASS_OF_FILTER;
    private static final Class CLASS_OF_INFO = VariantRecord.CLASS_OF_INFO;
    private static final Class CLASS_OF_FORMAT = VariantRecord.CLASS_OF_FORMAT;
    private static final int NUM_FIELDS = INDEX_OF_FORMAT + 1; // index of the last field + 1

    public static int getNumberOfFields() {
        return NUM_FIELDS;
    }

    public static Vector convertToVector(VariantRecord r) {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add(r.getSampleID());
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add(r.getCallDetails());
                    break;
                case INDEX_OF_CHROM:
                    v.add(r.getChrom());
                    break;
                case INDEX_OF_POS:
                    v.add(r.getPos());
                    break;
                case INDEX_OF_ID:
                    v.add(r.getId());
                    break;
                case INDEX_OF_REF:
                    v.add(r.getRef());
                    break;
                case INDEX_OF_ALT:
                    v.add(r.getAlt());
                    break;
                case INDEX_OF_QUAL:
                    v.add(r.getQual());
                    break;
                case INDEX_OF_FILTER:
                    v.add(r.getFilter());
                    break;
                case INDEX_OF_INFO:
                    v.add(r.getInfo());
                    break;
                case INDEX_OF_FORMAT:
                    v.add(r.getFormat());
                    break;
            }
        }
        return v;
    }

    public static Vector getFieldClasses() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add(CLASS_OF_SAMPLEID);
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add(CLASS_OF_CALLDETAILS);
                    break;
                case INDEX_OF_CHROM:
                    v.add(CLASS_OF_CHROM);
                    break;
                case INDEX_OF_POS:
                    v.add(CLASS_OF_POS);
                    break;
                case INDEX_OF_ID:
                    v.add(CLASS_OF_ID);
                    break;
                case INDEX_OF_REF:
                    v.add(CLASS_OF_REF);
                    break;
                case INDEX_OF_ALT:
                    v.add(CLASS_OF_ALT);
                    break;
                case INDEX_OF_QUAL:
                    v.add(CLASS_OF_QUAL);
                    break;
                case INDEX_OF_FILTER:
                    v.add(CLASS_OF_FILTER);
                    break;
                case INDEX_OF_INFO:
                    v.add(CLASS_OF_INFO);
                    break;
                case INDEX_OF_FORMAT:
                    v.add(CLASS_OF_FORMAT);
                    break;
            }
        }
        return v;
    }

    public static Vector getFieldNames() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add("Sample");
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add("Details");
                    break;
                case INDEX_OF_CHROM:
                    v.add("Chromosome");
                    break;
                case INDEX_OF_POS:
                    v.add("Position");
                    break;
                case INDEX_OF_ID:
                    v.add("ID");
                    break;
                case INDEX_OF_REF:
                    v.add("Reference");
                    break;
                case INDEX_OF_ALT:
                    v.add("Alternate");
                    break;
                case INDEX_OF_QUAL:
                    v.add("Quality");
                    break;
                case INDEX_OF_FILTER:
                    v.add("Filter");
                    break;
                case INDEX_OF_INFO:
                    v.add("Information");
                    break;
                case INDEX_OF_FORMAT:
                    v.add("Format");
                    break;
            }
        }
        return v;
    }

    public static String getFieldNameForIndex(int index) {
        switch (index) {
            case INDEX_OF_SAMPLEID:
                return "Sample";
            case INDEX_OF_CALLDETAILS:
                return "Details";
            case INDEX_OF_CHROM:
                return "Chromosome";
            case INDEX_OF_POS:
                return "Position";
            case INDEX_OF_ID:
                return "ID";
            case INDEX_OF_REF:
                return "Reference";
            case INDEX_OF_ALT:
                return "Alternate";
            case INDEX_OF_QUAL:
                return "Quality";
            case INDEX_OF_FILTER:
                return "Filter";
            case INDEX_OF_INFO:
                return "Information";
            case INDEX_OF_FORMAT:
                return "Format";
            default:
                return null;
        }
    }

    public static int getIndexOfField(String fieldName) {
        return getFieldNames().indexOf(fieldName);
    }

    public static Class getFieldClass(int index) {
        return (Class) getFieldClasses().get(index);
    }

    public static Object getValueOfFieldAtIndex(int keyIndex, VariantRecord r) {
        switch (keyIndex) {
            case INDEX_OF_ALT:
                return r.getAlt();
            case INDEX_OF_CALLDETAILS:
                return r.getCallDetails();
            case INDEX_OF_CHROM:
                return r.getChrom();
            case INDEX_OF_FILTER:
                return r.getFilter();
            case INDEX_OF_FORMAT:
                return r.getFormat();
            case INDEX_OF_ID:
                return r.getId();
            case INDEX_OF_POS:
                return r.getPos();
            case INDEX_OF_QUAL:
                return r.getQual();
            case INDEX_OF_REF:
                return r.getRef();
            case INDEX_OF_SAMPLEID:
                return r.getSampleID();
        }

        return null;
    }
}
