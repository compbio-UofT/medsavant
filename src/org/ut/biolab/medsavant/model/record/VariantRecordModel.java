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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.db.Database;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class VariantRecordModel {

    public static final int INDEX_OF_SAMPLEID = 0;
    public static final int INDEX_OF_CHROM = INDEX_OF_SAMPLEID + 1;
    public static final int INDEX_OF_POS = INDEX_OF_CHROM + 1;
    public static final int INDEX_OF_ID = INDEX_OF_POS + 1;
    public static final int INDEX_OF_REF = INDEX_OF_ID + 1;
    public static final int INDEX_OF_ALT = INDEX_OF_REF + 1;
    public static final int INDEX_OF_QUAL = INDEX_OF_ALT + 1;
    public static final int INDEX_OF_FILTER = INDEX_OF_QUAL + 1;
    public static final int INDEX_OF_INFO = INDEX_OF_FILTER + 1;
    private static final Class CLASS_OF_SAMPLEID = String.class;
    private static final Class CLASS_OF_CHROM = VariantRecord.CLASS_OF_CHROM;
    private static final Class CLASS_OF_POS = VariantRecord.CLASS_OF_POS;
    private static final Class CLASS_OF_ID = VariantRecord.CLASS_OF_ID;
    private static final Class CLASS_OF_REF = VariantRecord.CLASS_OF_REF;
    private static final Class CLASS_OF_ALT = VariantRecord.CLASS_OF_ALT;
    private static final Class CLASS_OF_QUAL = VariantRecord.CLASS_OF_QUAL;
    private static final Class CLASS_OF_FILTER = VariantRecord.CLASS_OF_FILTER;
    private static final Class CLASS_OF_INFO = VariantRecord.CLASS_OF_INFO;
    private static final int NUM_FIELDS = INDEX_OF_INFO + 1; // index of the last field + 1

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
                /*case INDEX_OF_CALLDETAILS:
                    v.add(r.getCallDetails());
                    break;
                 *
                 */
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
                /*case INDEX_OF_CALLDETAILS:
                    v.add(CLASS_OF_CALLDETAILS);
                    break;*/
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
            }
        }
        return v;
    }

    public static List<String> getFieldNames() {
        List<String> v = new ArrayList<String>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add(VariantTableSchema.ALIAS_SAMPLEID);
                    break;
                /*case INDEX_OF_CALLDETAILS:
                    v.add(VariantTableSchema.ALIAS_INFORMATION);
                    break;
                 *
                 */
                case INDEX_OF_CHROM:
                    v.add(VariantTableSchema.ALIAS_CHROM);
                    break;
                case INDEX_OF_POS:
                    v.add(VariantTableSchema.ALIAS_POSITION);
                    break;
                case INDEX_OF_ID:
                    v.add(VariantTableSchema.ALIAS_ID);
                    break;
                case INDEX_OF_REF:
                    v.add(VariantTableSchema.ALIAS_REFERENCE);
                    break;
                case INDEX_OF_ALT:
                    v.add(VariantTableSchema.ALIAS_ALTERNATE);
                    break;
                case INDEX_OF_QUAL:
                    v.add(VariantTableSchema.ALIAS_QUALITY);
                    break;
                case INDEX_OF_FILTER:
                    v.add(VariantTableSchema.ALIAS_FILTER);
                    break;
                case INDEX_OF_INFO:
                    v.add(VariantTableSchema.ALIAS_INFORMATION);
                    break;
            }
        }
        return v;
    }

    public static String getFieldNameForIndex(int index) {
        switch (index) {
            case INDEX_OF_SAMPLEID:
                return VariantTableSchema.ALIAS_SAMPLEID;
            /*case INDEX_OF_CALLDETAILS:
                return "Details";*/
            case INDEX_OF_CHROM:
                return VariantTableSchema.ALIAS_CHROM;
            case INDEX_OF_POS:
                return VariantTableSchema.ALIAS_POSITION;
            case INDEX_OF_ID:
                return VariantTableSchema.ALIAS_POSITION;
            case INDEX_OF_REF:
                return VariantTableSchema.ALIAS_REFERENCE;
            case INDEX_OF_ALT:
                return VariantTableSchema.ALIAS_ALTERNATE;
            case INDEX_OF_QUAL:
                return VariantTableSchema.ALIAS_QUALITY;
            case INDEX_OF_FILTER:
                return VariantTableSchema.ALIAS_FILTER;
            case INDEX_OF_INFO:
                return VariantTableSchema.ALIAS_INFORMATION;
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
            /*case INDEX_OF_CALLDETAILS:
                return r.getCallDetails();
             * 
             */
            case INDEX_OF_CHROM:
                return r.getChrom();
            case INDEX_OF_FILTER:
                return r.getFilter();
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
