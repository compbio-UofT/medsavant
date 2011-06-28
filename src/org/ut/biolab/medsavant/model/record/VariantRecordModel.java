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
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class VariantRecordModel {

    public static final int INDEX_OF_GENOMEID = 0;
    public static final int INDEX_OF_PIPELINEID = INDEX_OF_GENOMEID + 1;
    public static final int INDEX_OF_DNAID = INDEX_OF_PIPELINEID + 1;
    public static final int INDEX_OF_CHROM = INDEX_OF_DNAID + 1;
    public static final int INDEX_OF_POS = INDEX_OF_CHROM + 1;
    public static final int INDEX_OF_ID = INDEX_OF_POS + 1;
    public static final int INDEX_OF_REF = INDEX_OF_ID + 1;
    public static final int INDEX_OF_ALT = INDEX_OF_REF + 1;
    public static final int INDEX_OF_QUAL = INDEX_OF_ALT + 1;
    public static final int INDEX_OF_FILTER = INDEX_OF_QUAL + 1;
    //public static final int INDEX_OF_INFO = INDEX_OF_FILTER + 1;
    public static final int INDEX_OF_AA = INDEX_OF_FILTER + 1;
    public static final int INDEX_OF_AC = INDEX_OF_AA + 1;
    public static final int INDEX_OF_AF = INDEX_OF_AC + 1;
    public static final int INDEX_OF_AN = INDEX_OF_AF + 1;
    public static final int INDEX_OF_BQ = INDEX_OF_AN + 1;
    public static final int INDEX_OF_CIGAR = INDEX_OF_BQ + 1;
    public static final int INDEX_OF_DB = INDEX_OF_CIGAR + 1;
    public static final int INDEX_OF_DP = INDEX_OF_DB + 1;
    public static final int INDEX_OF_END = INDEX_OF_DP + 1;
    public static final int INDEX_OF_H2 = INDEX_OF_END + 1;
    public static final int INDEX_OF_MQ = INDEX_OF_H2 + 1;
    public static final int INDEX_OF_MQ0 = INDEX_OF_MQ + 1;
    public static final int INDEX_OF_NS = INDEX_OF_MQ0 + 1;
    public static final int INDEX_OF_SB = INDEX_OF_NS + 1;
    public static final int INDEX_OF_SOMATIC = INDEX_OF_SB + 1;
    public static final int INDEX_OF_VALIDATED = INDEX_OF_SOMATIC + 1;
    public static final int INDEX_OF_CUSTOMINFO = INDEX_OF_VALIDATED + 1;

    private static final Class CLASS_OF_GENOMEID = String.class;
    private static final Class CLASS_OF_PIPELINEID = String.class;
    private static final Class CLASS_OF_DNAID = String.class;
    private static final Class CLASS_OF_CHROM = VariantRecord.CLASS_OF_CHROM;
    private static final Class CLASS_OF_POS = VariantRecord.CLASS_OF_POS;
    private static final Class CLASS_OF_ID = VariantRecord.CLASS_OF_ID;
    private static final Class CLASS_OF_REF = VariantRecord.CLASS_OF_REF;
    private static final Class CLASS_OF_ALT = VariantRecord.CLASS_OF_ALT;
    private static final Class CLASS_OF_QUAL = VariantRecord.CLASS_OF_QUAL;
    private static final Class CLASS_OF_FILTER = VariantRecord.CLASS_OF_FILTER;
    //private static final Class CLASS_OF_INFO = VariantRecord.CLASS_OF_INFO;
    private static final Class CLASS_OF_AA = VariantRecord.CLASS_OF_AA;
    private static final Class CLASS_OF_AC = VariantRecord.CLASS_OF_AC;
    private static final Class CLASS_OF_AF = VariantRecord.CLASS_OF_AF;
    private static final Class CLASS_OF_AN = VariantRecord.CLASS_OF_AN;
    private static final Class CLASS_OF_BQ = VariantRecord.CLASS_OF_BQ;
    private static final Class CLASS_OF_CIGAR = VariantRecord.CLASS_OF_CIGAR;
    private static final Class CLASS_OF_DB = VariantRecord.CLASS_OF_DB;
    private static final Class CLASS_OF_DP = VariantRecord.CLASS_OF_DP;
    private static final Class CLASS_OF_END = VariantRecord.CLASS_OF_END;
    private static final Class CLASS_OF_H2 = VariantRecord.CLASS_OF_H2;
    private static final Class CLASS_OF_MQ = VariantRecord.CLASS_OF_MQ;
    private static final Class CLASS_OF_MQ0 = VariantRecord.CLASS_OF_MQ0;
    private static final Class CLASS_OF_NS = VariantRecord.CLASS_OF_NS;
    private static final Class CLASS_OF_SB = VariantRecord.CLASS_OF_SB;
    private static final Class CLASS_OF_SOMATIC = VariantRecord.CLASS_OF_SOMATIC;
    private static final Class CLASS_OF_VALIDATED = VariantRecord.CLASS_OF_VALIDATED;
    private static final Class CLASS_OF_CUSTOMINFO = VariantRecord.CLASS_OF_CUSTOMINFO;

    private static final boolean DEFAULT_GENOMEID = false;
    private static final boolean DEFAULT_PIPELINEID = false;
    private static final boolean DEFAULT_DNAID = true;
    private static final boolean DEFAULT_CHROM = true;
    private static final boolean DEFAULT_POS = true;
    private static final boolean DEFAULT_ID = true;
    private static final boolean DEFAULT_REF = true;
    private static final boolean DEFAULT_ALT = true;
    private static final boolean DEFAULT_QUAL = true;
    private static final boolean DEFAULT_FILTER = true;
    private static final boolean DEFAULT_AA = false;
    private static final boolean DEFAULT_AC = false;
    private static final boolean DEFAULT_AF = false;
    private static final boolean DEFAULT_AN = false;
    private static final boolean DEFAULT_BQ = false;
    private static final boolean DEFAULT_CIGAR = false;
    private static final boolean DEFAULT_DB = false;
    private static final boolean DEFAULT_DP = false;
    private static final boolean DEFAULT_END = false;
    private static final boolean DEFAULT_H2 = false;
    private static final boolean DEFAULT_MQ = false;
    private static final boolean DEFAULT_MQ0 = false;
    private static final boolean DEFAULT_NS = false;
    private static final boolean DEFAULT_SB = false;
    private static final boolean DEFAULT_SOMATIC = false;
    private static final boolean DEFAULT_VALIDATED = false;
    private static final boolean DEFAULT_CUSTOMINFO = false;


    private static final int NUM_FIELDS = INDEX_OF_CUSTOMINFO + 1; // index of the last field + 1

    public static int getNumberOfFields() {
        return NUM_FIELDS;
    }

    public static Vector convertToVector(VariantRecord r) {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_GENOMEID:
                    v.add(r.getGenomeID());
                    break;
                case INDEX_OF_PIPELINEID:
                    v.add(r.getPipelineID());
                    break;
                case INDEX_OF_DNAID:
                    v.add(r.getDnaID());
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
                //case INDEX_OF_INFO:
                //    v.add(r.getInfo());
                //    break;
                case INDEX_OF_AA:
                    v.add(r.getAA());
                    break;
                case INDEX_OF_AC:
                    v.add(r.getAC());
                    break;
                case INDEX_OF_AF:
                    v.add(r.getAF());
                    break;
                case INDEX_OF_AN:
                    v.add(r.getAN());
                    break;
                case INDEX_OF_BQ:
                    v.add(r.getBQ());
                    break;
                case INDEX_OF_CIGAR:
                    v.add(r.getCigar());
                    break;
                case INDEX_OF_DB:
                    v.add(r.getDB());
                    break;
                case INDEX_OF_DP:
                    v.add(r.getDP());
                    break;
                case INDEX_OF_END:
                    v.add(r.getEnd());
                    break;
                case INDEX_OF_H2:
                    v.add(r.getH2());
                    break;
                case INDEX_OF_MQ:
                    v.add(r.getMQ());
                    break;
                case INDEX_OF_MQ0:
                    v.add(r.getMQ0());
                    break;
                case INDEX_OF_NS:
                    v.add(r.getNS());
                    break;
                case INDEX_OF_SB:
                    v.add(r.getSB());
                    break;
                case INDEX_OF_SOMATIC:
                    v.add(r.getSomatic());
                    break;
                case INDEX_OF_VALIDATED:
                    v.add(r.getValidated());
                    break;
                case INDEX_OF_CUSTOMINFO:
                    v.add(r.getCustomInfo());
                    break;

            }
        }
        return v;
    }

    public static Vector getFieldClasses() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_GENOMEID:
                    v.add(CLASS_OF_GENOMEID);
                    break;
                case INDEX_OF_PIPELINEID:
                    v.add(CLASS_OF_PIPELINEID);
                    break;
                case INDEX_OF_DNAID:
                    v.add(CLASS_OF_DNAID);
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
                //case INDEX_OF_INFO:
                //    v.add(CLASS_OF_INFO);
                //    break;
                case INDEX_OF_AA:
                    v.add(CLASS_OF_AA);
                    break;
                case INDEX_OF_AC:
                    v.add(CLASS_OF_AC);
                    break;
                case INDEX_OF_AF:
                    v.add(CLASS_OF_AF);
                    break;
                case INDEX_OF_AN:
                    v.add(CLASS_OF_AN);
                    break;
                case INDEX_OF_BQ:
                    v.add(CLASS_OF_BQ);
                    break;
                case INDEX_OF_CIGAR:
                    v.add(CLASS_OF_CIGAR);
                    break;
                case INDEX_OF_DB:
                    v.add(CLASS_OF_DB);
                    break;
                case INDEX_OF_DP:
                    v.add(CLASS_OF_DP);
                    break;
                case INDEX_OF_END:
                    v.add(CLASS_OF_END);
                    break;
                case INDEX_OF_H2:
                    v.add(CLASS_OF_H2);
                    break;
                case INDEX_OF_MQ:
                    v.add(CLASS_OF_MQ);
                    break;
                case INDEX_OF_MQ0:
                    v.add(CLASS_OF_MQ0);
                    break;
                case INDEX_OF_NS:
                    v.add(CLASS_OF_NS);
                    break;
                case INDEX_OF_SB:
                    v.add(CLASS_OF_SB);
                    break;
                case INDEX_OF_SOMATIC:
                    v.add(CLASS_OF_SOMATIC);
                    break;
                case INDEX_OF_VALIDATED:
                    v.add(CLASS_OF_VALIDATED);
                    break;
                case INDEX_OF_CUSTOMINFO:
                    v.add(CLASS_OF_CUSTOMINFO);
                    break;
            }
        }
        return v;
    }

    public static List<String> getFieldNames() {
        List<String> v = new ArrayList<String>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_GENOMEID:
                    v.add(VariantTableSchema.ALIAS_GENOMEID);
                    break;
                case INDEX_OF_PIPELINEID:
                    v.add(VariantTableSchema.ALIAS_PIPELINEID);
                    break;
                case INDEX_OF_DNAID:
                    v.add(VariantTableSchema.ALIAS_DNAID);
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
                //case INDEX_OF_INFO:
                //    v.add(VariantTableSchema.ALIAS_INFORMATION);
                //    break;
                case INDEX_OF_AA:
                    v.add(VariantTableSchema.ALIAS_AA);
                    break;
                case INDEX_OF_AC:
                    v.add(VariantTableSchema.ALIAS_AC);
                    break;
                case INDEX_OF_AF:
                    v.add(VariantTableSchema.ALIAS_AF);
                    break;
                case INDEX_OF_AN:
                    v.add(VariantTableSchema.ALIAS_AN);
                    break;
                case INDEX_OF_BQ:
                    v.add(VariantTableSchema.ALIAS_BQ);
                    break;
                case INDEX_OF_CIGAR:
                    v.add(VariantTableSchema.ALIAS_CIGAR);
                    break;
                case INDEX_OF_DB:
                    v.add(VariantTableSchema.ALIAS_DB);
                    break;
                case INDEX_OF_DP:
                    v.add(VariantTableSchema.ALIAS_DP);
                    break;
                case INDEX_OF_END:
                    v.add(VariantTableSchema.ALIAS_END);
                    break;
                case INDEX_OF_H2:
                    v.add(VariantTableSchema.ALIAS_H2);
                    break;
                case INDEX_OF_MQ:
                    v.add(VariantTableSchema.ALIAS_MQ);
                    break;
                case INDEX_OF_MQ0:
                    v.add(VariantTableSchema.ALIAS_MQ0);
                    break;
                case INDEX_OF_NS:
                    v.add(VariantTableSchema.ALIAS_NS);
                    break;
                case INDEX_OF_SB:
                    v.add(VariantTableSchema.ALIAS_SB);
                    break;
                case INDEX_OF_SOMATIC:
                    v.add(VariantTableSchema.ALIAS_SOMATIC);
                    break;
                case INDEX_OF_VALIDATED:
                    v.add(VariantTableSchema.ALIAS_VALIDATED);
                    break;
                case INDEX_OF_CUSTOMINFO:
                    v.add(VariantTableSchema.ALIAS_CUSTOMINFO);
                    break;
            }
        }
        return v;
    }

    public static String getFieldNameForIndex(int index) {
        switch (index) {
            case INDEX_OF_GENOMEID:
                return VariantTableSchema.ALIAS_GENOMEID;
            case INDEX_OF_PIPELINEID:
                return VariantTableSchema.ALIAS_PIPELINEID;
            case INDEX_OF_DNAID:
                return VariantTableSchema.ALIAS_DNAID;
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
            //case INDEX_OF_INFO:
            //    return VariantTableSchema.ALIAS_INFORMATION;
            case INDEX_OF_AA:
                return VariantTableSchema.ALIAS_AA;
            case INDEX_OF_AC:
                return VariantTableSchema.ALIAS_AC;
            case INDEX_OF_AF:
                return VariantTableSchema.ALIAS_AF;
            case INDEX_OF_AN:
                return VariantTableSchema.ALIAS_AN;
            case INDEX_OF_BQ:
                return VariantTableSchema.ALIAS_BQ;
            case INDEX_OF_CIGAR:
                return VariantTableSchema.ALIAS_CIGAR;
            case INDEX_OF_DB:
                return VariantTableSchema.ALIAS_DB;
            case INDEX_OF_DP:
                return VariantTableSchema.ALIAS_DP;
            case INDEX_OF_END:
                return VariantTableSchema.ALIAS_END;
            case INDEX_OF_H2:
                return VariantTableSchema.ALIAS_H2;
            case INDEX_OF_MQ:
                return VariantTableSchema.ALIAS_MQ;
            case INDEX_OF_MQ0:
                return VariantTableSchema.ALIAS_MQ0;
            case INDEX_OF_NS:
                return VariantTableSchema.ALIAS_NS;
            case INDEX_OF_SB:
                return VariantTableSchema.ALIAS_SB;
            case INDEX_OF_SOMATIC:
                return VariantTableSchema.ALIAS_SOMATIC;
            case INDEX_OF_VALIDATED:
                return VariantTableSchema.ALIAS_VALIDATED;
            case INDEX_OF_CUSTOMINFO:
                return VariantTableSchema.ALIAS_CUSTOMINFO;
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
            case INDEX_OF_GENOMEID:
                return r.getGenomeID();
            case INDEX_OF_PIPELINEID:
                return r.getPipelineID();
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
            case INDEX_OF_DNAID:
                return r.getDnaID();
            case INDEX_OF_AA:
                return r.getAA();
            case INDEX_OF_AC:
                return r.getAC();
            case INDEX_OF_AF:
                return r.getAF();
            case INDEX_OF_AN:
                return r.getAN();
            case INDEX_OF_BQ:
                return r.getBQ();
            case INDEX_OF_CIGAR:
                return r.getCigar();
            case INDEX_OF_DB:
                return r.getDB();
            case INDEX_OF_DP:
                return r.getDP();
            case INDEX_OF_END:
                return r.getEnd();
            case INDEX_OF_H2:
                return r.getH2();
            case INDEX_OF_MQ:
                return r.getMQ();
            case INDEX_OF_MQ0:
                return r.getMQ0();
            case INDEX_OF_NS:
                return r.getNS();
            case INDEX_OF_SB:
                return r.getSB();
            case INDEX_OF_SOMATIC:
                return r.getSomatic();
            case INDEX_OF_VALIDATED:
                return r.getValidated();
            case INDEX_OF_CUSTOMINFO:
                return r.getCustomInfo();

        }

        return null;
    }

    public static List<Integer> getDefaultColumns(){
        List<Integer> v = new ArrayList<Integer>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_GENOMEID:
                    if(!DEFAULT_GENOMEID){
                        v.add(i);
                    }
                    //v.add(DEFAULT_GENOMEID);
                    break;
                case INDEX_OF_PIPELINEID:
                    if(!DEFAULT_PIPELINEID){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DNAID:
                    if(!DEFAULT_DNAID){
                        v.add(i);
                    }
                    break;
                /*case INDEX_OF_CALLDETAILS:
                    v.add(VariantTableSchema.ALIAS_INFORMATION);
                    break;
                 *
                 */
                case INDEX_OF_CHROM:
                    if(!DEFAULT_CHROM){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_POS:
                    if(!DEFAULT_POS){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_ID:
                    if(!DEFAULT_ID){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_REF:
                    if(!DEFAULT_REF){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_ALT:
                    if(!DEFAULT_ALT){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_QUAL:
                    if(!DEFAULT_QUAL){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_FILTER:
                    if(!DEFAULT_FILTER){
                        v.add(i);
                    }
                    break;
                //case INDEX_OF_INFO:
                //    v.add(VariantTableSchema.ALIAS_INFORMATION);
                //    break;
                case INDEX_OF_AA:
                    if(!DEFAULT_AA){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AC:
                    if(!DEFAULT_AC){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AF:
                    if(!DEFAULT_AF){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AN:
                    if(!DEFAULT_AN){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_BQ:
                    if(!DEFAULT_BQ){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CIGAR:
                    if(!DEFAULT_CIGAR){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DB:
                    if(!DEFAULT_DB){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DP:
                    if(!DEFAULT_DP){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_END:
                    if(!DEFAULT_END){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_H2:
                    if(!DEFAULT_H2){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_MQ:
                    if(!DEFAULT_MQ){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_MQ0:
                    if(!DEFAULT_MQ0){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_NS:
                    if(!DEFAULT_NS){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_SB:
                    if(!DEFAULT_SB){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_SOMATIC:
                    if(!DEFAULT_SOMATIC){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_VALIDATED:
                    if(!DEFAULT_VALIDATED){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CUSTOMINFO:
                    if(!DEFAULT_CUSTOMINFO){
                        v.add(i);
                    }
                    break;
            }
        }
        return v;
    }


}
