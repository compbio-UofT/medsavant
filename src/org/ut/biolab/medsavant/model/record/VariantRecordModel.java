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
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationGatkTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationPolyphenTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationSiftTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;

/**
 *
 * @author mfiume
 */
public class VariantRecordModel {

    //variant
    public static final int INDEX_OF_VARIANTID = 0;
    public static final int INDEX_OF_GENOMEID = INDEX_OF_VARIANTID + 1;
    public static final int INDEX_OF_PIPELINEID = INDEX_OF_GENOMEID + 1;
    public static final int INDEX_OF_DNAID = INDEX_OF_PIPELINEID + 1;
    public static final int INDEX_OF_CHROM = INDEX_OF_DNAID + 1;
    public static final int INDEX_OF_POSITION = INDEX_OF_CHROM + 1;
    public static final int INDEX_OF_DBSNPID = INDEX_OF_POSITION + 1;
    public static final int INDEX_OF_REF = INDEX_OF_DBSNPID + 1;
    public static final int INDEX_OF_ALT = INDEX_OF_REF + 1;
    public static final int INDEX_OF_QUAL = INDEX_OF_ALT + 1;
    public static final int INDEX_OF_FILTER = INDEX_OF_QUAL + 1;
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
    
    //sift
    public static final int INDEX_OF_NAME_SIFT = INDEX_OF_CUSTOMINFO + 1;
    public static final int INDEX_OF_NAME2_SIFT = INDEX_OF_NAME_SIFT + 1;
    public static final int INDEX_OF_DAMAGEPROBABILITY = INDEX_OF_NAME2_SIFT + 1;

    //polyphen
    public static final int INDEX_OF_CDNACOORD = INDEX_OF_DAMAGEPROBABILITY + 1;
    public static final int INDEX_OF_OPOS = INDEX_OF_CDNACOORD + 1;
    public static final int INDEX_OF_OAA1 = INDEX_OF_OPOS + 1;
    public static final int INDEX_OF_OAA2 = INDEX_OF_OAA1 + 1;
    public static final int INDEX_OF_SNPID = INDEX_OF_OAA2 + 1;
    public static final int INDEX_OF_ACC = INDEX_OF_SNPID + 1;
    public static final int INDEX_OF_POS = INDEX_OF_ACC + 1;
    public static final int INDEX_OF_PREDICTION = INDEX_OF_POS + 1;
    public static final int INDEX_OF_PPH2CLASS = INDEX_OF_PREDICTION + 1;
    public static final int INDEX_OF_PPH2PROB = INDEX_OF_PPH2CLASS + 1;
    public static final int INDEX_OF_PPH2FPR = INDEX_OF_PPH2PROB + 1;
    public static final int INDEX_OF_PPH2TPR = INDEX_OF_PPH2FPR + 1;
    public static final int INDEX_OF_PPH2FDR = INDEX_OF_PPH2TPR + 1;
    public static final int INDEX_OF_TRANSV = INDEX_OF_PPH2FDR + 1;
    public static final int INDEX_OF_CODPOS = INDEX_OF_TRANSV + 1;
    public static final int INDEX_OF_CPG = INDEX_OF_CODPOS + 1;
    public static final int INDEX_OF_MINDJNC = INDEX_OF_CPG + 1;
    public static final int INDEX_OF_IDPMAX = INDEX_OF_MINDJNC + 1;
    public static final int INDEX_OF_IDPSNP = INDEX_OF_IDPMAX + 1;
    public static final int INDEX_OF_IDQMIN = INDEX_OF_IDPSNP + 1;    
    
    //gatk
    public static final int INDEX_OF_NAME_GATK = INDEX_OF_IDQMIN + 1;
    public static final int INDEX_OF_NAME2_GATK = INDEX_OF_NAME_GATK + 1;
    public static final int INDEX_OF_TRANSCRIPTSTRAND = INDEX_OF_NAME2_GATK + 1;
    public static final int INDEX_OF_POSITIONTYPE = INDEX_OF_TRANSCRIPTSTRAND + 1;
    public static final int INDEX_OF_FRAME = INDEX_OF_POSITIONTYPE + 1;
    public static final int INDEX_OF_MRNACOORD = INDEX_OF_FRAME + 1;
    public static final int INDEX_OF_CODONCOORD = INDEX_OF_MRNACOORD + 1;
    public static final int INDEX_OF_SPLICEDIST = INDEX_OF_CODONCOORD + 1;
    public static final int INDEX_OF_REFERENCECODON = INDEX_OF_SPLICEDIST + 1;
    public static final int INDEX_OF_REFERENCEAA = INDEX_OF_REFERENCECODON + 1;
    public static final int INDEX_OF_VARIANTCODON = INDEX_OF_REFERENCEAA + 1;
    public static final int INDEX_OF_VARIANTAA = INDEX_OF_VARIANTCODON + 1;
    public static final int INDEX_OF_CHANGESAA = INDEX_OF_VARIANTAA + 1;
    public static final int INDEX_OF_FUNCTIONALCLASS = INDEX_OF_CHANGESAA + 1;
    public static final int INDEX_OF_CODINGCOORDSTR = INDEX_OF_FUNCTIONALCLASS + 1;
    public static final int INDEX_OF_PROTEINCOORDSTR = INDEX_OF_CODINGCOORDSTR + 1;
    public static final int INDEX_OF_INCODINGREGION = INDEX_OF_PROTEINCOORDSTR + 1;
    public static final int INDEX_OF_SPLICEINFO = INDEX_OF_INCODINGREGION + 1;
    public static final int INDEX_OF_UORFCHANGE = INDEX_OF_SPLICEINFO + 1;
    public static final int INDEX_OF_SPLICEINFOCOPY = INDEX_OF_UORFCHANGE + 1;
    
    //num fields
    private static final int NUM_FIELDS = INDEX_OF_SPLICEINFOCOPY + 1; // index of the last field + 1
    
    //variant
    private static final Class CLASS_OF_VARIANTID = VariantRecord.CLASS_OF_VARIANTID;
    private static final Class CLASS_OF_GENOMEID = VariantRecord.CLASS_OF_GENOMEID;
    private static final Class CLASS_OF_PIPELINEID = VariantRecord.CLASS_OF_PIPELINEID;
    private static final Class CLASS_OF_DNAID = VariantRecord.CLASS_OF_DNAID;
    private static final Class CLASS_OF_CHROM = VariantRecord.CLASS_OF_CHROM;
    private static final Class CLASS_OF_POSITION = VariantRecord.CLASS_OF_POSITION;
    private static final Class CLASS_OF_DBSNPID = VariantRecord.CLASS_OF_DBSNPID;
    private static final Class CLASS_OF_REF = VariantRecord.CLASS_OF_REF;
    private static final Class CLASS_OF_ALT = VariantRecord.CLASS_OF_ALT;
    private static final Class CLASS_OF_QUAL = VariantRecord.CLASS_OF_QUAL;
    private static final Class CLASS_OF_FILTER = VariantRecord.CLASS_OF_FILTER;
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
    
    //sift
    private static final Class CLASS_OF_NAME_SIFT = VariantRecord.CLASS_OF_NAME_SIFT;
    private static final Class CLASS_OF_NAME2_SIFT = VariantRecord.CLASS_OF_NAME2_SIFT;
    private static final Class CLASS_OF_DAMAGEPROBABILITY = VariantRecord.CLASS_OF_DAMAGEPROBABILITY;

    //polyphen
    private static final Class CLASS_OF_CDNACOORD = VariantRecord.CLASS_OF_CDNACOORD;
    private static final Class CLASS_OF_OPOS = VariantRecord.CLASS_OF_OPOS;
    private static final Class CLASS_OF_OAA1 = VariantRecord.CLASS_OF_OAA1;
    private static final Class CLASS_OF_OAA2 = VariantRecord.CLASS_OF_OAA2;
    private static final Class CLASS_OF_SNPID = VariantRecord.CLASS_OF_SNPID;
    private static final Class CLASS_OF_ACC = VariantRecord.CLASS_OF_ACC;
    private static final Class CLASS_OF_POS = VariantRecord.CLASS_OF_POS;
    private static final Class CLASS_OF_PREDICTION = VariantRecord.CLASS_OF_PREDICTION;
    private static final Class CLASS_OF_PPH2CLASS = VariantRecord.CLASS_OF_PPH2CLASS;
    private static final Class CLASS_OF_PPH2PROB = VariantRecord.CLASS_OF_PPH2PROB;
    private static final Class CLASS_OF_PPH2FPR = VariantRecord.CLASS_OF_PPH2FPR;
    private static final Class CLASS_OF_PPH2TPR = VariantRecord.CLASS_OF_PPH2TPR;
    private static final Class CLASS_OF_PPH2FDR = VariantRecord.CLASS_OF_PPH2FDR;
    private static final Class CLASS_OF_TRANSV = VariantRecord.CLASS_OF_TRANSV;
    private static final Class CLASS_OF_CODPOS = VariantRecord.CLASS_OF_CODPOS;
    private static final Class CLASS_OF_CPG = VariantRecord.CLASS_OF_CPG;
    private static final Class CLASS_OF_MINDJNC = VariantRecord.CLASS_OF_MINDJNC;
    private static final Class CLASS_OF_IDPMAX = VariantRecord.CLASS_OF_IDPMAX;
    private static final Class CLASS_OF_IDPSNP = VariantRecord.CLASS_OF_IDPSNP;
    private static final Class CLASS_OF_IDQMIN = VariantRecord.CLASS_OF_IDQMIN;    
    
    //gatk
    private static final Class CLASS_OF_NAME_GATK = VariantRecord.CLASS_OF_NAME_GATK;
    private static final Class CLASS_OF_NAME2_GATK = VariantRecord.CLASS_OF_NAME_GATK;
    private static final Class CLASS_OF_TRANSCRIPTSTRAND = VariantRecord.CLASS_OF_TRANSCRIPTSTRAND;
    private static final Class CLASS_OF_POSITIONTYPE = VariantRecord.CLASS_OF_POSITIONTYPE;
    private static final Class CLASS_OF_FRAME = VariantRecord.CLASS_OF_FRAME;
    private static final Class CLASS_OF_MRNACOORD = VariantRecord.CLASS_OF_MRNACOORD;
    private static final Class CLASS_OF_CODONCOORD = VariantRecord.CLASS_OF_CODONCOORD;
    private static final Class CLASS_OF_SPLICEDIST = VariantRecord.CLASS_OF_SPLICEDIST;
    private static final Class CLASS_OF_REFERENCECODON = VariantRecord.CLASS_OF_REFERENCECODON;
    private static final Class CLASS_OF_REFERENCEAA = VariantRecord.CLASS_OF_REFERENCEAA;
    private static final Class CLASS_OF_VARIANTCODON = VariantRecord.CLASS_OF_VARIANTCODON;
    private static final Class CLASS_OF_VARIANTAA = VariantRecord.CLASS_OF_VARIANTAA;
    private static final Class CLASS_OF_CHANGESAA = VariantRecord.CLASS_OF_CHANGESAA;
    private static final Class CLASS_OF_FUNCTIONALCLASS = VariantRecord.CLASS_OF_FUNCTIONALCLASS;
    private static final Class CLASS_OF_CODINGCOORDSTR = VariantRecord.CLASS_OF_CODINGCOORDSTR;
    private static final Class CLASS_OF_PROTEINCOORDSTR = VariantRecord.CLASS_OF_PROTEINCOORDSTR;
    private static final Class CLASS_OF_INCODINGREGION = VariantRecord.CLASS_OF_INCODINGREGION;
    private static final Class CLASS_OF_SPLICEINFO = VariantRecord.CLASS_OF_SPLICEINFO;
    private static final Class CLASS_OF_UORFCHANGE = VariantRecord.CLASS_OF_UORFCHANGE;
    private static final Class CLASS_OF_SPLICEINFOCOPY = VariantRecord.CLASS_OF_SPLICEINFOCOPY;
    
    //variant
    private static final boolean DEFAULT_VARIANTID = false;
    private static final boolean DEFAULT_GENOMEID = false;
    private static final boolean DEFAULT_PIPELINEID = false;
    private static final boolean DEFAULT_DNAID = true;
    private static final boolean DEFAULT_CHROM = true;
    private static final boolean DEFAULT_POSITION = true;
    private static final boolean DEFAULT_DBSNPID = true;
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
    
    //sift
    private static final boolean DEFAULT_NAME_SIFT = false;
    private static final boolean DEFAULT_NAME2_SIFT = false;
    private static final boolean DEFAULT_DAMAGEPROBABILITY = false;
    
    //polyphen
    private static final boolean DEFAULT_CDNACOORD = false;
    private static final boolean DEFAULT_OPOS = false;
    private static final boolean DEFAULT_OAA1 = false;
    private static final boolean DEFAULT_OAA2 = false;
    private static final boolean DEFAULT_SNPID = false;
    private static final boolean DEFAULT_ACC = false;
    private static final boolean DEFAULT_POS = false;
    private static final boolean DEFAULT_PREDICTION = false;
    private static final boolean DEFAULT_PPH2CLASS = false;
    private static final boolean DEFAULT_PPH2PROB = false;
    private static final boolean DEFAULT_PPH2FPR = false;
    private static final boolean DEFAULT_PPH2TPR = false;
    private static final boolean DEFAULT_PPH2FDR = false;
    private static final boolean DEFAULT_TRANSV = false;
    private static final boolean DEFAULT_CODPOS = false;
    private static final boolean DEFAULT_CPG = false;
    private static final boolean DEFAULT_MINDJNC = false;
    private static final boolean DEFAULT_IDPMAX = false;
    private static final boolean DEFAULT_IDPSNP = false;
    private static final boolean DEFAULT_IDQMIN = false;   
    
    //gatk
    private static final boolean DEFAULT_NAME_GATK = false;
    private static final boolean DEFAULT_NAME2_GATK = false;
    private static final boolean DEFAULT_TRANSCRIPTSTRAND = false;
    private static final boolean DEFAULT_POSITIONTYPE = false;
    private static final boolean DEFAULT_FRAME = false;
    private static final boolean DEFAULT_MRNACOORD = false;
    private static final boolean DEFAULT_CODONCOORD = false;
    private static final boolean DEFAULT_SPLICEDIST = false;
    private static final boolean DEFAULT_REFERENCECODON = false;
    private static final boolean DEFAULT_REFERENCEAA = false;
    private static final boolean DEFAULT_VARIANTCODON = false;
    private static final boolean DEFAULT_VARIANTAA = false;
    private static final boolean DEFAULT_CHANGESAA = false;
    private static final boolean DEFAULT_FUNCTIONALCLASS = false;
    private static final boolean DEFAULT_CODINGCOORDSTR = false;
    private static final boolean DEFAULT_PROTEINCOORDSTR = false;
    private static final boolean DEFAULT_INCODINGREGION = false;
    private static final boolean DEFAULT_SPLICEINFO = false;
    private static final boolean DEFAULT_UORFCHANGE = false;
    private static final boolean DEFAULT_SPLICEINFOCOPY = false;

    public static int getNumberOfFields() {
        return NUM_FIELDS;
    }

    public static Vector convertToVector(VariantRecord r) {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_VARIANTID:
                    v.add(r.getVariantID());
                    break;
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
                case INDEX_OF_POSITION:
                    v.add(r.getPosition());
                    break;
                case INDEX_OF_DBSNPID:
                    v.add(r.getDbSNPID());
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
                case INDEX_OF_NAME_SIFT:
                    v.add(r.getNameSift());
                    break;
                case INDEX_OF_NAME2_SIFT:
                    v.add(r.getName2Sift());
                    break;
                case INDEX_OF_DAMAGEPROBABILITY:
                    v.add(r.getDamageProbability());
                    break; 
                case INDEX_OF_CDNACOORD:
                    v.add(r.getCdnacoord());
                    break;
                case INDEX_OF_OPOS:
                    v.add(r.getOpos());
                    break;
                case INDEX_OF_OAA1:
                    v.add(r.getOaa1());
                    break;
                case INDEX_OF_OAA2:
                    v.add(r.getOaa2());
                    break;
                case INDEX_OF_SNPID:
                    v.add(r.getSnpid());
                    break;
                case INDEX_OF_ACC:
                    v.add(r.getAcc());
                    break;
                case INDEX_OF_POS:
                    v.add(r.getPos());
                    break;
                case INDEX_OF_PREDICTION:
                    v.add(r.getPrediction());
                    break;
                case INDEX_OF_PPH2CLASS:
                    v.add(r.getPph2class());
                    break;
                case INDEX_OF_PPH2PROB:
                    v.add(r.getPph2prob());
                    break;
                case INDEX_OF_PPH2FPR:
                    v.add(r.getPph2fpr());
                    break;
                case INDEX_OF_PPH2TPR:
                    v.add(r.getPph2tpr());
                    break;
                case INDEX_OF_PPH2FDR:
                    v.add(r.getPph2fdr());
                    break;
                case INDEX_OF_TRANSV:
                    v.add(r.getTransv());
                    break;
                case INDEX_OF_CODPOS:
                    v.add(r.getCodpos());
                    break;
                case INDEX_OF_CPG:
                    v.add(r.getCpg());
                    break;
                case INDEX_OF_MINDJNC:
                    v.add(r.getMindjnc());
                    break;
                case INDEX_OF_IDPMAX:
                    v.add(r.getIdpmax());
                    break;
                case INDEX_OF_IDPSNP:
                    v.add(r.getIdpsnp());
                    break;
                case INDEX_OF_IDQMIN:
                    v.add(r.getIdqmin());
                    break;    
                case INDEX_OF_NAME_GATK:
                    v.add(r.getName_gatk());
                    break;
                case INDEX_OF_NAME2_GATK:
                    v.add(r.getName2_gatk());
                    break;
                case INDEX_OF_TRANSCRIPTSTRAND:
                    v.add(r.getTranscriptStrand());
                    break;
                case INDEX_OF_POSITIONTYPE:
                    v.add(r.getPositionType());
                    break;
                case INDEX_OF_FRAME:
                    v.add(r.getFrame());
                    break;
                case INDEX_OF_MRNACOORD:
                    v.add(r.getMrnaCoord());
                    break;
                case INDEX_OF_CODONCOORD:
                    v.add(r.getCodonCoord());
                    break;
                case INDEX_OF_SPLICEDIST:
                    v.add(r.getSpliceDist());
                    break;
                case INDEX_OF_REFERENCECODON:
                    v.add(r.getReferenceCodon());
                    break;
                case INDEX_OF_REFERENCEAA:
                    v.add(r.getReferenceAA());
                    break;
                case INDEX_OF_VARIANTCODON:
                    v.add(r.getVariantCodon());
                    break;
                case INDEX_OF_VARIANTAA:
                    v.add(r.getVariantAA());
                    break;
                case INDEX_OF_CHANGESAA:
                    v.add(r.getChangesAA());
                    break;
                case INDEX_OF_FUNCTIONALCLASS:
                    v.add(r.getFunctionalClass());
                    break;
                case INDEX_OF_CODINGCOORDSTR:
                    v.add(r.getCodingCoordStr());
                    break;
                case INDEX_OF_PROTEINCOORDSTR:
                    v.add(r.getProteinCoordStr());
                    break;
                case INDEX_OF_INCODINGREGION:
                    v.add(r.getInCodingRegion());
                    break;
                case INDEX_OF_SPLICEINFO:
                    v.add(r.getSpliceInfo());
                    break;
                case INDEX_OF_UORFCHANGE:
                    v.add(r.getUorfChange());
                    break;
                case INDEX_OF_SPLICEINFOCOPY:
                    v.add(r.getSpliceInfoCopy());
                    break;
                    

            }
        }
        return v;
    }

    public static Vector getFieldClasses() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_VARIANTID:
                    v.add(CLASS_OF_VARIANTID);
                    break;
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
                case INDEX_OF_POSITION:
                    v.add(CLASS_OF_POSITION);
                    break;
                case INDEX_OF_DBSNPID:
                    v.add(CLASS_OF_DBSNPID);
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
                case INDEX_OF_NAME_SIFT:
                    v.add(CLASS_OF_NAME_SIFT);
                    break;
                case INDEX_OF_NAME2_SIFT:
                    v.add(CLASS_OF_NAME2_SIFT);
                    break;
                case INDEX_OF_DAMAGEPROBABILITY:
                    v.add(CLASS_OF_DAMAGEPROBABILITY);
                    break;
                case INDEX_OF_CDNACOORD:
                    v.add(CLASS_OF_CDNACOORD);
                    break;
                case INDEX_OF_OPOS:
                    v.add(CLASS_OF_OPOS);
                    break;
                case INDEX_OF_OAA1:
                    v.add(CLASS_OF_OAA1);
                    break;
                case INDEX_OF_OAA2:
                    v.add(CLASS_OF_OAA2);
                    break;
                case INDEX_OF_SNPID:
                    v.add(CLASS_OF_SNPID);
                    break;
                case INDEX_OF_ACC:
                    v.add(CLASS_OF_ACC);
                    break;
                case INDEX_OF_POS:
                    v.add(CLASS_OF_POS);
                    break;
                case INDEX_OF_PREDICTION:
                    v.add(CLASS_OF_PREDICTION);
                    break;
                case INDEX_OF_PPH2CLASS:
                    v.add(CLASS_OF_PPH2CLASS);
                    break;
                case INDEX_OF_PPH2PROB:
                    v.add(CLASS_OF_PPH2PROB);
                    break;
                case INDEX_OF_PPH2FPR:
                    v.add(CLASS_OF_PPH2FPR);
                    break;
                case INDEX_OF_PPH2TPR:
                    v.add(CLASS_OF_PPH2TPR);
                    break;
                case INDEX_OF_PPH2FDR:
                    v.add(CLASS_OF_PPH2FDR);
                    break;
                case INDEX_OF_TRANSV:
                    v.add(CLASS_OF_TRANSV);
                    break;
                case INDEX_OF_CODPOS:
                    v.add(CLASS_OF_CODPOS);
                    break;
                case INDEX_OF_CPG:
                    v.add(CLASS_OF_CPG);
                    break;
                case INDEX_OF_MINDJNC:
                    v.add(CLASS_OF_MINDJNC);
                    break;
                case INDEX_OF_IDPMAX:
                    v.add(CLASS_OF_IDPMAX);
                    break;
                case INDEX_OF_IDPSNP:
                    v.add(CLASS_OF_IDPSNP);
                    break;
                case INDEX_OF_IDQMIN:
                    v.add(CLASS_OF_IDQMIN);
                    break;  
                case INDEX_OF_NAME_GATK:
                    v.add(CLASS_OF_NAME_GATK);
                    break;
                case INDEX_OF_NAME2_GATK:
                    v.add(CLASS_OF_NAME2_GATK);
                    break;
                case INDEX_OF_TRANSCRIPTSTRAND:
                    v.add(CLASS_OF_TRANSCRIPTSTRAND);
                    break;
                case INDEX_OF_POSITIONTYPE:
                    v.add(CLASS_OF_POSITIONTYPE);
                    break;
                case INDEX_OF_FRAME:
                    v.add(CLASS_OF_FRAME);
                    break;
                case INDEX_OF_MRNACOORD:
                    v.add(CLASS_OF_MRNACOORD);
                    break;
                case INDEX_OF_CODONCOORD:
                    v.add(CLASS_OF_CODONCOORD);
                    break;
                case INDEX_OF_SPLICEDIST:
                    v.add(CLASS_OF_SPLICEDIST);
                    break;
                case INDEX_OF_REFERENCECODON:
                    v.add(CLASS_OF_REFERENCECODON);
                    break;
                case INDEX_OF_REFERENCEAA:
                    v.add(CLASS_OF_REFERENCEAA);
                    break;
                case INDEX_OF_VARIANTCODON:
                    v.add(CLASS_OF_VARIANTCODON);
                    break;
                case INDEX_OF_VARIANTAA:
                    v.add(CLASS_OF_VARIANTAA);
                    break;
                case INDEX_OF_CHANGESAA:
                    v.add(CLASS_OF_CHANGESAA);
                    break;
                case INDEX_OF_FUNCTIONALCLASS:
                    v.add(CLASS_OF_FUNCTIONALCLASS);
                    break;
                case INDEX_OF_CODINGCOORDSTR:
                    v.add(CLASS_OF_CODINGCOORDSTR);
                    break;
                case INDEX_OF_PROTEINCOORDSTR:
                    v.add(CLASS_OF_PROTEINCOORDSTR);
                    break;
                case INDEX_OF_INCODINGREGION:
                    v.add(CLASS_OF_INCODINGREGION);
                    break;
                case INDEX_OF_SPLICEINFO:
                    v.add(CLASS_OF_SPLICEINFO);
                    break;
                case INDEX_OF_UORFCHANGE:
                    v.add(CLASS_OF_UORFCHANGE);
                    break;
                case INDEX_OF_SPLICEINFOCOPY:
                    v.add(CLASS_OF_SPLICEINFOCOPY);
                    break;

            }
        }
        return v;
    }

    public static List<String> getFieldNames() {
        List<String> v = new ArrayList<String>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_VARIANTID:
                    v.add(VariantTableSchema.ALIAS_VARIANTID);
                    break;
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
                case INDEX_OF_POSITION:
                    v.add(VariantTableSchema.ALIAS_POSITION);
                    break;
                case INDEX_OF_DBSNPID:
                    v.add(VariantTableSchema.ALIAS_DBSNPID);
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
                case INDEX_OF_NAME_SIFT:
                    v.add(VariantAnnotationSiftTableSchema.ALIAS_NAME_SIFT);
                    break;
                case INDEX_OF_NAME2_SIFT:
                    v.add(VariantAnnotationSiftTableSchema.ALIAS_NAME2_SIFT);
                    break;
                case INDEX_OF_DAMAGEPROBABILITY:
                    v.add(VariantAnnotationSiftTableSchema.ALIAS_DAMAGEPROBABILITY);
                    break;
                case INDEX_OF_CDNACOORD:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_CDNACOORD);
                    break;
                case INDEX_OF_OPOS:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_OPOS);
                    break;
                case INDEX_OF_OAA1:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_OAA1);
                    break;
                case INDEX_OF_OAA2:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_OAA2);
                    break;
                case INDEX_OF_SNPID:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_SNPID);
                    break;
                case INDEX_OF_ACC:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_ACC);
                    break;
                case INDEX_OF_POS:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_POS);
                    break;
                case INDEX_OF_PREDICTION:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PREDICTION);
                    break;
                case INDEX_OF_PPH2CLASS:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2CLASS);
                    break;
                case INDEX_OF_PPH2PROB:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2PROB);
                    break;
                case INDEX_OF_PPH2FPR:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FPR);
                    break;
                case INDEX_OF_PPH2TPR:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2TPR);
                    break;
                case INDEX_OF_PPH2FDR:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FDR);
                    break;
                case INDEX_OF_TRANSV:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_TRANSV);
                    break;
                case INDEX_OF_CODPOS:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_CODPOS);
                    break;
                case INDEX_OF_CPG:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_CPG);
                    break;
                case INDEX_OF_MINDJNC:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_MINDJNC);
                    break;
                case INDEX_OF_IDPMAX:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_IDPMAX);
                    break;
                case INDEX_OF_IDPSNP:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_IDPSNP);
                    break;
                case INDEX_OF_IDQMIN:
                    v.add(VariantAnnotationPolyphenTableSchema.ALIAS_IDQMIN);
                    break;
                case INDEX_OF_NAME_GATK:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_NAME_GATK);
                    break;
                case INDEX_OF_NAME2_GATK:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_NAME2_GATK);
                    break;
                case INDEX_OF_TRANSCRIPTSTRAND:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_TRANSCRIPTSTRAND);
                    break;
                case INDEX_OF_POSITIONTYPE:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_POSITIONTYPE);
                    break;
                case INDEX_OF_FRAME:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_FRAME);
                    break;
                case INDEX_OF_MRNACOORD:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_MRNACOORD);
                    break;
                case INDEX_OF_CODONCOORD:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_CODONCOORD);
                    break;
                case INDEX_OF_SPLICEDIST:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_SPLICEDIST);
                    break;
                case INDEX_OF_REFERENCECODON:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_REFERENCECODON);
                    break;
                case INDEX_OF_REFERENCEAA:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_REFERENCEAA);
                    break;
                case INDEX_OF_VARIANTCODON:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_VARIANTCODON);
                    break;
                case INDEX_OF_VARIANTAA:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_VARIANTAA);
                    break;
                case INDEX_OF_CHANGESAA:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_CHANGESAA);
                    break;
                case INDEX_OF_FUNCTIONALCLASS:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_FUNCTIONALCLASS);
                    break;
                case INDEX_OF_CODINGCOORDSTR:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_CODINGCOORDSTR);
                    break;
                case INDEX_OF_PROTEINCOORDSTR:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_PROTEINCOORDSTR);
                    break;
                case INDEX_OF_INCODINGREGION:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_INCODINGREGION);
                    break;
                case INDEX_OF_SPLICEINFO:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFO);
                    break;
                case INDEX_OF_UORFCHANGE:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_UORFCHANGE);
                    break;
                case INDEX_OF_SPLICEINFOCOPY:
                    v.add(VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFOCOPY);
                    break;

            }
        }
        return v;
    }

    public static String getFieldNameForIndex(int index) {
        switch (index) {
            case INDEX_OF_VARIANTID:
                return VariantTableSchema.ALIAS_VARIANTID;
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
            case INDEX_OF_POSITION:
                return VariantTableSchema.ALIAS_POSITION;
            case INDEX_OF_DBSNPID:
                return VariantTableSchema.ALIAS_POSITION;
            case INDEX_OF_REF:
                return VariantTableSchema.ALIAS_REFERENCE;
            case INDEX_OF_ALT:
                return VariantTableSchema.ALIAS_ALTERNATE;
            case INDEX_OF_QUAL:
                return VariantTableSchema.ALIAS_QUALITY;
            case INDEX_OF_FILTER:
                return VariantTableSchema.ALIAS_FILTER;
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
            case INDEX_OF_NAME_SIFT:
                return VariantAnnotationSiftTableSchema.ALIAS_NAME_SIFT;
            case INDEX_OF_NAME2_SIFT:
                return VariantAnnotationSiftTableSchema.ALIAS_NAME2_SIFT;
            case INDEX_OF_DAMAGEPROBABILITY:
                return VariantAnnotationSiftTableSchema.ALIAS_DAMAGEPROBABILITY;
            case INDEX_OF_CDNACOORD:
                return VariantAnnotationPolyphenTableSchema.ALIAS_CDNACOORD;
            case INDEX_OF_OPOS:
                return VariantAnnotationPolyphenTableSchema.ALIAS_OPOS;
            case INDEX_OF_OAA1:
                return VariantAnnotationPolyphenTableSchema.ALIAS_OAA1;
            case INDEX_OF_OAA2:
                return VariantAnnotationPolyphenTableSchema.ALIAS_OAA2;
            case INDEX_OF_SNPID:
                return VariantAnnotationPolyphenTableSchema.ALIAS_SNPID;
            case INDEX_OF_ACC:
                return VariantAnnotationPolyphenTableSchema.ALIAS_ACC;
            case INDEX_OF_POS:
                return VariantAnnotationPolyphenTableSchema.ALIAS_POS;
            case INDEX_OF_PREDICTION:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PREDICTION;
            case INDEX_OF_PPH2CLASS:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PPH2CLASS;
            case INDEX_OF_PPH2PROB:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PPH2PROB;
            case INDEX_OF_PPH2FPR:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FPR;
            case INDEX_OF_PPH2TPR:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PPH2TPR;
            case INDEX_OF_PPH2FDR:
                return VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FDR;
            case INDEX_OF_TRANSV:
                return VariantAnnotationPolyphenTableSchema.ALIAS_TRANSV;
            case INDEX_OF_CODPOS:
                return VariantAnnotationPolyphenTableSchema.ALIAS_CODPOS;
            case INDEX_OF_CPG:
                return VariantAnnotationPolyphenTableSchema.ALIAS_CPG;
            case INDEX_OF_MINDJNC:
                return VariantAnnotationPolyphenTableSchema.ALIAS_MINDJNC;
            case INDEX_OF_IDPMAX:
                return VariantAnnotationPolyphenTableSchema.ALIAS_IDPMAX;
            case INDEX_OF_IDPSNP:
                return VariantAnnotationPolyphenTableSchema.ALIAS_IDPSNP;
            case INDEX_OF_IDQMIN:
                return VariantAnnotationPolyphenTableSchema.ALIAS_IDQMIN;
            case INDEX_OF_NAME_GATK:
                return VariantAnnotationGatkTableSchema.ALIAS_NAME_GATK;
            case INDEX_OF_NAME2_GATK:
                return VariantAnnotationGatkTableSchema.ALIAS_NAME2_GATK;
            case INDEX_OF_TRANSCRIPTSTRAND:
                return VariantAnnotationGatkTableSchema.ALIAS_TRANSCRIPTSTRAND;
            case INDEX_OF_POSITIONTYPE:
                return VariantAnnotationGatkTableSchema.ALIAS_POSITIONTYPE;
            case INDEX_OF_FRAME:
                return VariantAnnotationGatkTableSchema.ALIAS_FRAME;
            case INDEX_OF_MRNACOORD:
                return VariantAnnotationGatkTableSchema.ALIAS_MRNACOORD;
            case INDEX_OF_CODONCOORD:
                return VariantAnnotationGatkTableSchema.ALIAS_CODONCOORD;
            case INDEX_OF_SPLICEDIST:
                return VariantAnnotationGatkTableSchema.ALIAS_SPLICEDIST;
            case INDEX_OF_REFERENCECODON:
                return VariantAnnotationGatkTableSchema.ALIAS_REFERENCECODON;
            case INDEX_OF_REFERENCEAA:
                return VariantAnnotationGatkTableSchema.ALIAS_REFERENCEAA;
            case INDEX_OF_VARIANTCODON:
                return VariantAnnotationGatkTableSchema.ALIAS_VARIANTCODON;
            case INDEX_OF_VARIANTAA:
                return VariantAnnotationGatkTableSchema.ALIAS_VARIANTAA;
            case INDEX_OF_CHANGESAA:
                return VariantAnnotationGatkTableSchema.ALIAS_CHANGESAA;
            case INDEX_OF_FUNCTIONALCLASS:
                return VariantAnnotationGatkTableSchema.ALIAS_FUNCTIONALCLASS;
            case INDEX_OF_CODINGCOORDSTR:
                return VariantAnnotationGatkTableSchema.ALIAS_CODINGCOORDSTR;
            case INDEX_OF_PROTEINCOORDSTR:
                return VariantAnnotationGatkTableSchema.ALIAS_PROTEINCOORDSTR;
            case INDEX_OF_INCODINGREGION:
                return VariantAnnotationGatkTableSchema.ALIAS_INCODINGREGION;
            case INDEX_OF_SPLICEINFO:
                return VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFO;
            case INDEX_OF_UORFCHANGE:
                return VariantAnnotationGatkTableSchema.ALIAS_UORFCHANGE;
            case INDEX_OF_SPLICEINFOCOPY:
                return VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFOCOPY;
            

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
            case INDEX_OF_DBSNPID:
                return r.getDbSNPID();
            case INDEX_OF_POSITION:
                return r.getPosition();
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
            case INDEX_OF_NAME_SIFT:
                return r.getNameSift();
            case INDEX_OF_NAME2_SIFT:
                return r.getName2Sift();
            case INDEX_OF_DAMAGEPROBABILITY:
                return r.getDamageProbability();
            case INDEX_OF_CDNACOORD:
                return r.getCdnacoord();
            case INDEX_OF_OPOS:
                return r.getOpos();
            case INDEX_OF_OAA1:
                return r.getOaa1();
            case INDEX_OF_OAA2:
                return r.getOaa2();
            case INDEX_OF_SNPID:
                return r.getSnpid();
            case INDEX_OF_ACC:
                return r.getAcc();
            case INDEX_OF_POS:
                return r.getPos();
            case INDEX_OF_PREDICTION:
                return r.getPrediction();
            case INDEX_OF_PPH2CLASS:
                return r.getPph2class();
            case INDEX_OF_PPH2PROB:
                return r.getPph2prob();
            case INDEX_OF_PPH2FPR:
                return r.getPph2fpr();
            case INDEX_OF_PPH2TPR:
                return r.getPph2tpr();
            case INDEX_OF_PPH2FDR:
                return r.getPph2fdr();
            case INDEX_OF_TRANSV:
                return r.getTransv();
            case INDEX_OF_CODPOS:
                return r.getCodpos();
            case INDEX_OF_CPG:
                return r.getCpg();
            case INDEX_OF_MINDJNC:
                return r.getMindjnc();
            case INDEX_OF_IDPMAX:
                return r.getIdpmax();
            case INDEX_OF_IDPSNP:
                return r.getIdpsnp();
            case INDEX_OF_IDQMIN:
                return r.getIdqmin();
            case INDEX_OF_NAME_GATK:
                return r.getName_gatk();
            case INDEX_OF_NAME2_GATK:
                return r.getName2_gatk();
            case INDEX_OF_TRANSCRIPTSTRAND:
                return r.getTranscriptStrand();
            case INDEX_OF_POSITIONTYPE:
                return r.getPositionType();
            case INDEX_OF_FRAME:
                return r.getFrame();
            case INDEX_OF_MRNACOORD:
                return r.getMrnaCoord();
            case INDEX_OF_CODONCOORD:
                return r.getCodonCoord();
            case INDEX_OF_SPLICEDIST:
                return r.getSpliceDist();
            case INDEX_OF_REFERENCECODON:
                return r.getReferenceCodon();
            case INDEX_OF_REFERENCEAA:
                return r.getReferenceAA();
            case INDEX_OF_VARIANTCODON:
                return r.getVariantCodon();
            case INDEX_OF_VARIANTAA:
                return r.getVariantAA();
            case INDEX_OF_CHANGESAA:
                return r.getChangesAA();
            case INDEX_OF_FUNCTIONALCLASS:
                return r.getFunctionalClass();
            case INDEX_OF_CODINGCOORDSTR:
                return r.getCodingCoordStr();
            case INDEX_OF_PROTEINCOORDSTR:
                return r.getProteinCoordStr();
            case INDEX_OF_INCODINGREGION:
                return r.getInCodingRegion();
            case INDEX_OF_SPLICEINFO:
                return r.getSpliceInfo();
            case INDEX_OF_UORFCHANGE:
                return r.getUorfChange();
            case INDEX_OF_SPLICEINFOCOPY:
                return r.getSpliceInfoCopy();
        }

        return null;
    }

    public static List<Integer> getDefaultColumns() {
        List<Integer> v = new ArrayList<Integer>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_VARIANTID:
                    if(!DEFAULT_VARIANTID){
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GENOMEID:
                    if (!DEFAULT_GENOMEID) {
                        v.add(i);
                    }
                    //v.add(DEFAULT_GENOMEID);
                    break;
                case INDEX_OF_PIPELINEID:
                    if (!DEFAULT_PIPELINEID) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DNAID:
                    if (!DEFAULT_DNAID) {
                        v.add(i);
                    }
                    break;
                /*case INDEX_OF_CALLDETAILS:
                v.add(VariantTableSchema.ALIAS_INFORMATION);
                break;
                 *
                 */
                case INDEX_OF_CHROM:
                    if (!DEFAULT_CHROM) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_POSITION:
                    if (!DEFAULT_POSITION) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DBSNPID:
                    if (!DEFAULT_DBSNPID) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_REF:
                    if (!DEFAULT_REF) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_ALT:
                    if (!DEFAULT_ALT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_QUAL:
                    if (!DEFAULT_QUAL) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_FILTER:
                    if (!DEFAULT_FILTER) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AA:
                    if (!DEFAULT_AA) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AC:
                    if (!DEFAULT_AC) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AF:
                    if (!DEFAULT_AF) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_AN:
                    if (!DEFAULT_AN) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_BQ:
                    if (!DEFAULT_BQ) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CIGAR:
                    if (!DEFAULT_CIGAR) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DB:
                    if (!DEFAULT_DB) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DP:
                    if (!DEFAULT_DP) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_END:
                    if (!DEFAULT_END) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_H2:
                    if (!DEFAULT_H2) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_MQ:
                    if (!DEFAULT_MQ) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_MQ0:
                    if (!DEFAULT_MQ0) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_NS:
                    if (!DEFAULT_NS) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_SB:
                    if (!DEFAULT_SB) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_SOMATIC:
                    if (!DEFAULT_SOMATIC) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_VALIDATED:
                    if (!DEFAULT_VALIDATED) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CUSTOMINFO:
                    if (!DEFAULT_CUSTOMINFO) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_NAME_SIFT:
                    if (!DEFAULT_NAME_SIFT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_NAME2_SIFT:
                    if (!DEFAULT_NAME2_SIFT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_DAMAGEPROBABILITY:
                    if (!DEFAULT_DAMAGEPROBABILITY) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CDNACOORD:
                    if (!DEFAULT_CDNACOORD) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_OPOS:
                    if (!DEFAULT_OPOS) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_OAA1:
                    if (!DEFAULT_OAA1) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_OAA2:
                    if (!DEFAULT_OAA2) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_SNPID:
                    if (!DEFAULT_SNPID) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_ACC:
                    if (!DEFAULT_ACC) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_POS:
                    if (!DEFAULT_POS) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PREDICTION:
                    if (!DEFAULT_PREDICTION) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PPH2CLASS:
                    if (!DEFAULT_PPH2CLASS) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PPH2PROB:
                    if (!DEFAULT_PPH2PROB) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PPH2FPR:
                    if (!DEFAULT_PPH2FPR) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PPH2TPR:
                    if (!DEFAULT_PPH2TPR) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PPH2FDR:
                    if (!DEFAULT_PPH2FDR) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_TRANSV:
                    if (!DEFAULT_TRANSV) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_CODPOS:
                    if (!DEFAULT_CODPOS) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_CPG:
                    if (!DEFAULT_CPG) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_MINDJNC:
                    if (!DEFAULT_MINDJNC) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_IDPMAX:
                    if (!DEFAULT_IDPMAX) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_IDPSNP:
                    if (!DEFAULT_IDPSNP) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_IDQMIN:
                    if (!DEFAULT_IDQMIN) {
			v.add(i);
                    }
                    break;   
                case INDEX_OF_NAME_GATK:
                    if (!DEFAULT_NAME_GATK) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_NAME2_GATK:
                    if (!DEFAULT_NAME2_GATK) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_TRANSCRIPTSTRAND:
                    if (!DEFAULT_TRANSCRIPTSTRAND) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_POSITIONTYPE:
                    if (!DEFAULT_POSITIONTYPE) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_FRAME:
                    if (!DEFAULT_FRAME) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_MRNACOORD:
                    if (!DEFAULT_MRNACOORD) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_CODONCOORD:
                    if (!DEFAULT_CODONCOORD) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_SPLICEDIST:
                    if (!DEFAULT_SPLICEDIST) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_REFERENCECODON:
                    if (!DEFAULT_REFERENCECODON) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_REFERENCEAA:
                    if (!DEFAULT_REFERENCEAA) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_VARIANTCODON:
                    if (!DEFAULT_VARIANTCODON) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_VARIANTAA:
                    if (!DEFAULT_VARIANTAA) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_CHANGESAA:
                    if (!DEFAULT_CHANGESAA) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_FUNCTIONALCLASS:
                    if (!DEFAULT_FUNCTIONALCLASS) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_CODINGCOORDSTR:
                    if (!DEFAULT_CODINGCOORDSTR) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_PROTEINCOORDSTR:
                    if (!DEFAULT_PROTEINCOORDSTR) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_INCODINGREGION:
                    if (!DEFAULT_INCODINGREGION) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_SPLICEINFO:
                    if (!DEFAULT_SPLICEINFO) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_UORFCHANGE:
                    if (!DEFAULT_UORFCHANGE) {
			v.add(i);
                    }
                    break;
                case INDEX_OF_SPLICEINFOCOPY:
                    if (!DEFAULT_SPLICEINFOCOPY) {
			v.add(i);
                    }
                    break;
                 
            }
        }
        return v;
    }
}
