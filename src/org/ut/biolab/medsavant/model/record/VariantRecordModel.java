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
    public static final int INDEX_OF_GT = INDEX_OF_CUSTOMINFO + 1;
    public static final int INDEX_OF_GPHASED = INDEX_OF_GT + 1;
    public static final int INDEX_OF_GDP = INDEX_OF_GPHASED + 1;
    public static final int INDEX_OF_GFT = INDEX_OF_GDP + 1;
    public static final int INDEX_OF_GLHOMOREF = INDEX_OF_GFT + 1;
    public static final int INDEX_OF_GLHET = INDEX_OF_GLHOMOREF + 1;
    public static final int INDEX_OF_GLHOMOALT = INDEX_OF_GLHET + 1;
    public static final int INDEX_OF_GQ = INDEX_OF_GLHOMOALT + 1;
    public static final int INDEX_OF_HQA = INDEX_OF_GQ + 1;
    public static final int INDEX_OF_HQB = INDEX_OF_HQA + 1;
    public static final int INDEX_OF_position_a = INDEX_OF_HQB + 1;
    public static final int INDEX_OF_position_b = INDEX_OF_position_a + 1;
    public static final int INDEX_OF_o_acc = INDEX_OF_position_b + 1;
    public static final int INDEX_OF_o_pos = INDEX_OF_o_acc + 1;
    public static final int INDEX_OF_o_aa1 = INDEX_OF_o_pos + 1;
    public static final int INDEX_OF_o_aa2 = INDEX_OF_o_aa1 + 1;
    public static final int INDEX_OF_snp_id = INDEX_OF_o_aa2 + 1;
    public static final int INDEX_OF_acc = INDEX_OF_snp_id + 1;
    public static final int INDEX_OF_pos = INDEX_OF_acc + 1;
    public static final int INDEX_OF_aa1 = INDEX_OF_pos + 1;
    public static final int INDEX_OF_aa2 = INDEX_OF_aa1 + 1;
    public static final int INDEX_OF_nt1 = INDEX_OF_aa2 + 1;
    public static final int INDEX_OF_nt2 = INDEX_OF_nt1 + 1;
    public static final int INDEX_OF_prediction = INDEX_OF_nt2 + 1;
    public static final int INDEX_OF_pph2_class = INDEX_OF_prediction + 1;
    public static final int INDEX_OF_pph2_prob = INDEX_OF_pph2_class + 1;
    public static final int INDEX_OF_pph2_FPR = INDEX_OF_pph2_prob + 1;
    public static final int INDEX_OF_pph2_TPR = INDEX_OF_pph2_FPR + 1;
    public static final int INDEX_OF_pph2_FDR = INDEX_OF_pph2_TPR + 1;
    public static final int INDEX_OF_Transv = INDEX_OF_pph2_FDR + 1;
    public static final int INDEX_OF_CodPos = INDEX_OF_Transv + 1;
    public static final int INDEX_OF_CpG = INDEX_OF_CodPos + 1;
    public static final int INDEX_OF_MinDJnc = INDEX_OF_CpG + 1;
    public static final int INDEX_OF_PfamHit = INDEX_OF_MinDJnc + 1;
    public static final int INDEX_OF_IdPmax = INDEX_OF_PfamHit + 1;
    public static final int INDEX_OF_IdPSNP = INDEX_OF_IdPmax + 1;
    public static final int INDEX_OF_IdQmin = INDEX_OF_IdPSNP + 1;
    public static final int INDEX_OF_sift_prediction = INDEX_OF_IdQmin + 1;
    public static final int INDEX_OF_name = INDEX_OF_sift_prediction + 1;
    public static final int INDEX_OF_name2 = INDEX_OF_name + 1;
    public static final int INDEX_OF_transcriptStrand = INDEX_OF_name2 + 1;
    public static final int INDEX_OF_positionType = INDEX_OF_transcriptStrand + 1;
    public static final int INDEX_OF_frame = INDEX_OF_positionType + 1;
    public static final int INDEX_OF_mrnaCoord = INDEX_OF_frame + 1;
    public static final int INDEX_OF_codonCoord = INDEX_OF_mrnaCoord + 1;
    public static final int INDEX_OF_spliceDist = INDEX_OF_codonCoord + 1;
    public static final int INDEX_OF_referenceCodon = INDEX_OF_spliceDist + 1;
    public static final int INDEX_OF_referenceAA = INDEX_OF_referenceCodon + 1;
    public static final int INDEX_OF_variantCodon = INDEX_OF_referenceAA + 1;
    public static final int INDEX_OF_variantAA = INDEX_OF_variantCodon + 1;
    public static final int INDEX_OF_changesAA = INDEX_OF_variantAA + 1;
    public static final int INDEX_OF_functionalClass = INDEX_OF_changesAA + 1;
    public static final int INDEX_OF_codingCoordStr = INDEX_OF_functionalClass + 1;
    public static final int INDEX_OF_proteinCoordStr = INDEX_OF_codingCoordStr + 1;
    public static final int INDEX_OF_inCodingRegion = INDEX_OF_proteinCoordStr + 1;
    public static final int INDEX_OF_spliceInfo = INDEX_OF_inCodingRegion + 1;
    public static final int INDEX_OF_uorfChange = INDEX_OF_spliceInfo + 1;
    
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
    private static final Class CLASS_OF_GT = VariantRecord.CLASS_OF_GT;
    private static final Class CLASS_OF_GPHASED = VariantRecord.CLASS_OF_GPHASED;
    private static final Class CLASS_OF_GDP = VariantRecord.CLASS_OF_GDP;
    private static final Class CLASS_OF_GFT = VariantRecord.CLASS_OF_GFT;
    private static final Class CLASS_OF_GLHOMOREF = VariantRecord.CLASS_OF_GLHOMOREF;
    private static final Class CLASS_OF_GLHET = VariantRecord.CLASS_OF_GLHET;
    private static final Class CLASS_OF_GLHOMOALT = VariantRecord.CLASS_OF_GLHOMOALT;
    private static final Class CLASS_OF_GQ = VariantRecord.CLASS_OF_GQ;
    private static final Class CLASS_OF_HQA = VariantRecord.CLASS_OF_HQA;
    private static final Class CLASS_OF_HQB = VariantRecord.CLASS_OF_HQB;
    private static final Class CLASS_OF_position_a = VariantRecord.CLASS_OF_position_a;
    private static final Class CLASS_OF_position_b = VariantRecord.CLASS_OF_position_b;
    private static final Class CLASS_OF_o_acc = VariantRecord.CLASS_OF_o_acc;
    private static final Class CLASS_OF_o_pos = VariantRecord.CLASS_OF_o_pos;
    private static final Class CLASS_OF_o_aa1 = VariantRecord.CLASS_OF_o_aa1;
    private static final Class CLASS_OF_o_aa2 = VariantRecord.CLASS_OF_o_aa2;
    private static final Class CLASS_OF_snp_id = VariantRecord.CLASS_OF_snp_id;
    private static final Class CLASS_OF_acc = VariantRecord.CLASS_OF_acc;
    private static final Class CLASS_OF_pos = VariantRecord.CLASS_OF_pos;
    private static final Class CLASS_OF_aa1 = VariantRecord.CLASS_OF_aa1;
    private static final Class CLASS_OF_aa2 = VariantRecord.CLASS_OF_aa2;
    private static final Class CLASS_OF_nt1 = VariantRecord.CLASS_OF_nt1;
    private static final Class CLASS_OF_nt2 = VariantRecord.CLASS_OF_nt2;
    private static final Class CLASS_OF_prediction = VariantRecord.CLASS_OF_prediction;
    private static final Class CLASS_OF_pph2_class = VariantRecord.CLASS_OF_pph2_class;
    private static final Class CLASS_OF_pph2_prob = VariantRecord.CLASS_OF_pph2_prob;
    private static final Class CLASS_OF_pph2_FPR = VariantRecord.CLASS_OF_pph2_FPR;
    private static final Class CLASS_OF_pph2_TPR = VariantRecord.CLASS_OF_pph2_TPR;
    private static final Class CLASS_OF_pph2_FDR = VariantRecord.CLASS_OF_pph2_FDR;
    private static final Class CLASS_OF_Transv = VariantRecord.CLASS_OF_Transv;
    private static final Class CLASS_OF_CodPos = VariantRecord.CLASS_OF_CodPos;
    private static final Class CLASS_OF_CpG = VariantRecord.CLASS_OF_CpG;
    private static final Class CLASS_OF_MinDJnc = VariantRecord.CLASS_OF_MinDJnc;
    private static final Class CLASS_OF_PfamHit = VariantRecord.CLASS_OF_PfamHit;
    private static final Class CLASS_OF_IdPmax = VariantRecord.CLASS_OF_IdPmax;
    private static final Class CLASS_OF_IdPSNP = VariantRecord.CLASS_OF_IdPSNP;
    private static final Class CLASS_OF_IdQmin = VariantRecord.CLASS_OF_IdQmin;
    private static final Class CLASS_OF_sift_prediction = VariantRecord.CLASS_OF_sift_prediction;
    private static final Class CLASS_OF_name = VariantRecord.CLASS_OF_name;
    private static final Class CLASS_OF_name2 = VariantRecord.CLASS_OF_name2;
    private static final Class CLASS_OF_transcriptStrand = VariantRecord.CLASS_OF_transcriptStrand;
    private static final Class CLASS_OF_positionType = VariantRecord.CLASS_OF_positionType;
    private static final Class CLASS_OF_frame = VariantRecord.CLASS_OF_frame;
    private static final Class CLASS_OF_mrnaCoord = VariantRecord.CLASS_OF_mrnaCoord;
    private static final Class CLASS_OF_codonCoord = VariantRecord.CLASS_OF_codonCoord;
    private static final Class CLASS_OF_spliceDist = VariantRecord.CLASS_OF_spliceDist;
    private static final Class CLASS_OF_referenceCodon = VariantRecord.CLASS_OF_referenceCodon;
    private static final Class CLASS_OF_referenceAA = VariantRecord.CLASS_OF_referenceAA;
    private static final Class CLASS_OF_variantCodon = VariantRecord.CLASS_OF_variantCodon;
    private static final Class CLASS_OF_variantAA = VariantRecord.CLASS_OF_variantAA;
    private static final Class CLASS_OF_changesAA = VariantRecord.CLASS_OF_changesAA;
    private static final Class CLASS_OF_functionalClass = VariantRecord.CLASS_OF_functionalClass;
    private static final Class CLASS_OF_codingCoordStr = VariantRecord.CLASS_OF_codingCoordStr;
    private static final Class CLASS_OF_proteinCoordStr = VariantRecord.CLASS_OF_proteinCoordStr;
    private static final Class CLASS_OF_inCodingRegion = VariantRecord.CLASS_OF_inCodingRegion;
    private static final Class CLASS_OF_spliceInfo = VariantRecord.CLASS_OF_spliceInfo;
    private static final Class CLASS_OF_uorfChange = VariantRecord.CLASS_OF_uorfChange;
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
    private static final boolean DEFAULT_GT = false;
    private static final boolean DEFAULT_GPHASED = false;
    private static final boolean DEFAULT_GDP = false;
    private static final boolean DEFAULT_GFT = false;
    private static final boolean DEFAULT_GLHOMOREF = false;
    private static final boolean DEFAULT_GLHET = false;
    private static final boolean DEFAULT_GLHOMOALT = false;
    private static final boolean DEFAULT_GQ = false;
    private static final boolean DEFAULT_HQA = false;
    private static final boolean DEFAULT_HQB = false;
    private static final boolean DEFAULT_position_a = false;
    private static final boolean DEFAULT_position_b = false;
    private static final boolean DEFAULT_o_acc = false;
    private static final boolean DEFAULT_o_pos = false;
    private static final boolean DEFAULT_o_aa1 = false;
    private static final boolean DEFAULT_o_aa2 = false;
    private static final boolean DEFAULT_snp_id = false;
    private static final boolean DEFAULT_acc = false;
    private static final boolean DEFAULT_pos = false;
    private static final boolean DEFAULT_aa1 = false;
    private static final boolean DEFAULT_aa2 = false;
    private static final boolean DEFAULT_nt1 = false;
    private static final boolean DEFAULT_nt2 = false;
    private static final boolean DEFAULT_prediction = false;
    private static final boolean DEFAULT_pph2_class = false;
    private static final boolean DEFAULT_pph2_prob = false;
    private static final boolean DEFAULT_pph2_FPR = false;
    private static final boolean DEFAULT_pph2_TPR = false;
    private static final boolean DEFAULT_pph2_FDR = false;
    private static final boolean DEFAULT_Transv = false;
    private static final boolean DEFAULT_CodPos = false;
    private static final boolean DEFAULT_CpG = false;
    private static final boolean DEFAULT_MinDJnc = false;
    private static final boolean DEFAULT_PfamHit = false;
    private static final boolean DEFAULT_IdPmax = false;
    private static final boolean DEFAULT_IdPSNP = false;
    private static final boolean DEFAULT_IdQmin = false;
    private static final boolean DEFAULT_sift_prediction = false;
    private static final boolean DEFAULT_name = false;
    private static final boolean DEFAULT_name2 = false;
    private static final boolean DEFAULT_transcriptStrand = false;
    private static final boolean DEFAULT_positionType = false;
    private static final boolean DEFAULT_frame = false;
    private static final boolean DEFAULT_mrnaCoord = false;
    private static final boolean DEFAULT_codonCoord = false;
    private static final boolean DEFAULT_spliceDist = false;
    private static final boolean DEFAULT_referenceCodon = false;
    private static final boolean DEFAULT_referenceAA = false;
    private static final boolean DEFAULT_variantCodon = false;
    private static final boolean DEFAULT_variantAA = false;
    private static final boolean DEFAULT_changesAA = false;
    private static final boolean DEFAULT_functionalClass = false;
    private static final boolean DEFAULT_codingCoordStr = false;
    private static final boolean DEFAULT_proteinCoordStr = false;
    private static final boolean DEFAULT_inCodingRegion = false;
    private static final boolean DEFAULT_spliceInfo = false;
    private static final boolean DEFAULT_uorfChange = false;
    private static final int NUM_FIELDS = INDEX_OF_uorfChange + 1; // index of the last field + 1

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
                case INDEX_OF_GT:
                    v.add(VariantRecord.ALIAS_ZYGOSITY[r.getGT()]);
                    break;
                case INDEX_OF_GPHASED:
                    v.add(r.getGPhased());
                    break;
                case INDEX_OF_GDP:
                    v.add(r.getGDP());
                    break;
                case INDEX_OF_GFT:
                    v.add(r.getGFT());
                    break;
                case INDEX_OF_GLHOMOREF:
                    v.add(r.getGLHomoRef());
                    break;
                case INDEX_OF_GLHET:
                    v.add(r.getGLHet());
                    break;
                case INDEX_OF_GLHOMOALT:
                    v.add(r.getGLHomoAlt());
                    break;
                case INDEX_OF_GQ:
                    v.add(r.getGQ());
                    break;
                case INDEX_OF_HQA:
                    v.add(r.getHQA());
                    break;
                case INDEX_OF_HQB:
                    v.add(r.getHQB());
                    break;
                case INDEX_OF_position_a:
                    v.add(r.getposition_a());
                    break;
                case INDEX_OF_position_b:
                    v.add(r.getposition_b());
                    break;
                case INDEX_OF_o_acc:
                    v.add(r.geto_acc());
                    break;
                case INDEX_OF_o_pos:
                    v.add(r.geto_pos());
                    break;
                case INDEX_OF_o_aa1:
                    v.add(r.geto_aa1());
                    break;
                case INDEX_OF_o_aa2:
                    v.add(r.geto_aa2());
                    break;
                case INDEX_OF_snp_id:
                    v.add(r.getsnp_id());
                    break;
                case INDEX_OF_acc:
                    v.add(r.getacc());
                    break;
                case INDEX_OF_pos:
                    v.add(r.getpos());
                    break;
                case INDEX_OF_aa1:
                    v.add(r.getaa1());
                    break;
                case INDEX_OF_aa2:
                    v.add(r.getaa2());
                    break;
                case INDEX_OF_nt1:
                    v.add(r.getnt1());
                    break;
                case INDEX_OF_nt2:
                    v.add(r.getnt2());
                    break;
                case INDEX_OF_prediction:
                    v.add(r.getprediction());
                    break;
                case INDEX_OF_pph2_class:
                    v.add(r.getpph2_class());
                    break;
                case INDEX_OF_pph2_prob:
                    v.add(r.getpph2_prob());
                    break;
                case INDEX_OF_pph2_FPR:
                    v.add(r.getpph2_FPR());
                    break;
                case INDEX_OF_pph2_TPR:
                    v.add(r.getpph2_TPR());
                    break;
                case INDEX_OF_pph2_FDR:
                    v.add(r.getpph2_FDR());
                    break;
                case INDEX_OF_Transv:
                    v.add(r.getTransv());
                    break;
                case INDEX_OF_CodPos:
                    v.add(r.getCodPos());
                    break;
                case INDEX_OF_CpG:
                    v.add(r.getCpG());
                    break;
                case INDEX_OF_MinDJnc:
                    v.add(r.getMinDJnc());
                    break;
                case INDEX_OF_PfamHit:
                    v.add(r.getPfamHit());
                    break;
                case INDEX_OF_IdPmax:
                    v.add(r.getIdPmax());
                    break;
                case INDEX_OF_IdPSNP:
                    v.add(r.getIdPSNP());
                    break;
                case INDEX_OF_IdQmin:
                    v.add(r.getIdQmin());
                    break;
                case INDEX_OF_sift_prediction:
                    v.add(r.getsift_prediction());
                    break;
                case INDEX_OF_name:
                    v.add(r.getname());
                    break;
                case INDEX_OF_name2:
                    v.add(r.getname2());
                    break;
                case INDEX_OF_transcriptStrand:
                    v.add(r.gettranscriptStrand());
                    break;
                case INDEX_OF_positionType:
                    v.add(r.getpositionType());
                    break;
                case INDEX_OF_frame:
                    v.add(r.getframe());
                    break;
                case INDEX_OF_mrnaCoord:
                    v.add(r.getmrnaCoord());
                    break;
                case INDEX_OF_codonCoord:
                    v.add(r.getcodonCoord());
                    break;
                case INDEX_OF_spliceDist:
                    v.add(r.getspliceDist());
                    break;
                case INDEX_OF_referenceCodon:
                    v.add(r.getreferenceCodon());
                    break;
                case INDEX_OF_referenceAA:
                    v.add(r.getreferenceAA());
                    break;
                case INDEX_OF_variantCodon:
                    v.add(r.getvariantCodon());
                    break;
                case INDEX_OF_variantAA:
                    v.add(r.getvariantAA());
                    break;
                case INDEX_OF_changesAA:
                    v.add(r.getchangesAA());
                    break;
                case INDEX_OF_functionalClass:
                    v.add(r.getfunctionalClass());
                    break;
                case INDEX_OF_codingCoordStr:
                    v.add(r.getcodingCoordStr());
                    break;
                case INDEX_OF_proteinCoordStr:
                    v.add(r.getproteinCoordStr());
                    break;
                case INDEX_OF_inCodingRegion:
                    v.add(r.getinCodingRegion());
                    break;
                case INDEX_OF_spliceInfo:
                    v.add(r.getspliceInfo());
                    break;
                case INDEX_OF_uorfChange:
                    v.add(r.getuorfChange());
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
                case INDEX_OF_GT:
                    v.add(String.class);
                    break;
                case INDEX_OF_GPHASED:
                    v.add(CLASS_OF_GPHASED);
                    break;
                case INDEX_OF_GDP:
                    v.add(CLASS_OF_GDP);
                    break;
                case INDEX_OF_GFT:
                    v.add(CLASS_OF_GFT);
                    break;
                case INDEX_OF_GLHOMOREF:
                    v.add(CLASS_OF_GLHOMOREF);
                    break;
                case INDEX_OF_GLHET:
                    v.add(CLASS_OF_GLHET);
                    break;
                case INDEX_OF_GLHOMOALT:
                    v.add(CLASS_OF_GLHOMOALT);
                    break;
                case INDEX_OF_GQ:
                    v.add(CLASS_OF_GQ);
                    break;
                case INDEX_OF_HQA:
                    v.add(CLASS_OF_HQA);
                    break;
                case INDEX_OF_HQB:
                    v.add(CLASS_OF_HQB);
                    break;
                case INDEX_OF_position_a:
                    v.add(CLASS_OF_position_a);
                    break;
                case INDEX_OF_position_b:
                    v.add(CLASS_OF_position_b);
                    break;
                case INDEX_OF_o_acc:
                    v.add(CLASS_OF_o_acc);
                    break;
                case INDEX_OF_o_pos:
                    v.add(CLASS_OF_o_pos);
                    break;
                case INDEX_OF_o_aa1:
                    v.add(CLASS_OF_o_aa1);
                    break;
                case INDEX_OF_o_aa2:
                    v.add(CLASS_OF_o_aa2);
                    break;
                case INDEX_OF_snp_id:
                    v.add(CLASS_OF_snp_id);
                    break;
                case INDEX_OF_acc:
                    v.add(CLASS_OF_acc);
                    break;
                case INDEX_OF_pos:
                    v.add(CLASS_OF_pos);
                    break;
                case INDEX_OF_aa1:
                    v.add(CLASS_OF_aa1);
                    break;
                case INDEX_OF_aa2:
                    v.add(CLASS_OF_aa2);
                    break;
                case INDEX_OF_nt1:
                    v.add(CLASS_OF_nt1);
                    break;
                case INDEX_OF_nt2:
                    v.add(CLASS_OF_nt2);
                    break;
                case INDEX_OF_prediction:
                    v.add(CLASS_OF_prediction);
                    break;
                case INDEX_OF_pph2_class:
                    v.add(CLASS_OF_pph2_class);
                    break;
                case INDEX_OF_pph2_prob:
                    v.add(CLASS_OF_pph2_prob);
                    break;
                case INDEX_OF_pph2_FPR:
                    v.add(CLASS_OF_pph2_FPR);
                    break;
                case INDEX_OF_pph2_TPR:
                    v.add(CLASS_OF_pph2_TPR);
                    break;
                case INDEX_OF_pph2_FDR:
                    v.add(CLASS_OF_pph2_FDR);
                    break;
                case INDEX_OF_Transv:
                    v.add(CLASS_OF_Transv);
                    break;
                case INDEX_OF_CodPos:
                    v.add(CLASS_OF_CodPos);
                    break;
                case INDEX_OF_CpG:
                    v.add(CLASS_OF_CpG);
                    break;
                case INDEX_OF_MinDJnc:
                    v.add(CLASS_OF_MinDJnc);
                    break;
                case INDEX_OF_PfamHit:
                    v.add(CLASS_OF_PfamHit);
                    break;
                case INDEX_OF_IdPmax:
                    v.add(CLASS_OF_IdPmax);
                    break;
                case INDEX_OF_IdPSNP:
                    v.add(CLASS_OF_IdPSNP);
                    break;
                case INDEX_OF_IdQmin:
                    v.add(CLASS_OF_IdQmin);
                    break;
                case INDEX_OF_sift_prediction:
                    v.add(CLASS_OF_sift_prediction);
                    break;
                case INDEX_OF_name:
                    v.add(CLASS_OF_name);
                    break;
                case INDEX_OF_name2:
                    v.add(CLASS_OF_name2);
                    break;
                case INDEX_OF_transcriptStrand:
                    v.add(CLASS_OF_transcriptStrand);
                    break;
                case INDEX_OF_positionType:
                    v.add(CLASS_OF_positionType);
                    break;
                case INDEX_OF_frame:
                    v.add(CLASS_OF_frame);
                    break;
                case INDEX_OF_mrnaCoord:
                    v.add(CLASS_OF_mrnaCoord);
                    break;
                case INDEX_OF_codonCoord:
                    v.add(CLASS_OF_codonCoord);
                    break;
                case INDEX_OF_spliceDist:
                    v.add(CLASS_OF_spliceDist);
                    break;
                case INDEX_OF_referenceCodon:
                    v.add(CLASS_OF_referenceCodon);
                    break;
                case INDEX_OF_referenceAA:
                    v.add(CLASS_OF_referenceAA);
                    break;
                case INDEX_OF_variantCodon:
                    v.add(CLASS_OF_variantCodon);
                    break;
                case INDEX_OF_variantAA:
                    v.add(CLASS_OF_variantAA);
                    break;
                case INDEX_OF_changesAA:
                    v.add(CLASS_OF_changesAA);
                    break;
                case INDEX_OF_functionalClass:
                    v.add(CLASS_OF_functionalClass);
                    break;
                case INDEX_OF_codingCoordStr:
                    v.add(CLASS_OF_codingCoordStr);
                    break;
                case INDEX_OF_proteinCoordStr:
                    v.add(CLASS_OF_proteinCoordStr);
                    break;
                case INDEX_OF_inCodingRegion:
                    v.add(CLASS_OF_inCodingRegion);
                    break;
                case INDEX_OF_spliceInfo:
                    v.add(CLASS_OF_spliceInfo);
                    break;
                case INDEX_OF_uorfChange:
                    v.add(CLASS_OF_uorfChange);
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
                case INDEX_OF_GT:
                    v.add(VariantTableSchema.ALIAS_GT);
                    break;
                case INDEX_OF_GPHASED:
                    v.add(VariantTableSchema.ALIAS_GPHASED);
                    break;
                case INDEX_OF_GDP:
                    v.add(VariantTableSchema.ALIAS_GDP);
                    break;
                case INDEX_OF_GFT:
                    v.add(VariantTableSchema.ALIAS_GFT);
                    break;
                case INDEX_OF_GLHOMOREF:
                    v.add(VariantTableSchema.ALIAS_GLHOMOREF);
                    break;
                case INDEX_OF_GLHET:
                    v.add(VariantTableSchema.ALIAS_GLHET);
                    break;
                case INDEX_OF_GLHOMOALT:
                    v.add(VariantTableSchema.ALIAS_GLHOMOALT);
                    break;
                case INDEX_OF_GQ:
                    v.add(VariantTableSchema.ALIAS_GQ);
                    break;
                case INDEX_OF_HQA:
                    v.add(VariantTableSchema.ALIAS_HQA);
                    break;
                case INDEX_OF_HQB:
                    v.add(VariantTableSchema.ALIAS_HQB);
                    break;
                case INDEX_OF_position_a:
                    v.add(VariantTableSchema.ALIAS_position_a);
                    break;
                case INDEX_OF_position_b:
                    v.add(VariantTableSchema.ALIAS_position_b);
                    break;
                case INDEX_OF_o_acc:
                    v.add(VariantTableSchema.ALIAS_o_acc);
                    break;
                case INDEX_OF_o_pos:
                    v.add(VariantTableSchema.ALIAS_o_pos);
                    break;
                case INDEX_OF_o_aa1:
                    v.add(VariantTableSchema.ALIAS_o_aa1);
                    break;
                case INDEX_OF_o_aa2:
                    v.add(VariantTableSchema.ALIAS_o_aa2);
                    break;
                case INDEX_OF_snp_id:
                    v.add(VariantTableSchema.ALIAS_snp_id);
                    break;
                case INDEX_OF_acc:
                    v.add(VariantTableSchema.ALIAS_acc);
                    break;
                case INDEX_OF_pos:
                    v.add(VariantTableSchema.ALIAS_pos);
                    break;
                case INDEX_OF_aa1:
                    v.add(VariantTableSchema.ALIAS_aa1);
                    break;
                case INDEX_OF_aa2:
                    v.add(VariantTableSchema.ALIAS_aa2);
                    break;
                case INDEX_OF_nt1:
                    v.add(VariantTableSchema.ALIAS_nt1);
                    break;
                case INDEX_OF_nt2:
                    v.add(VariantTableSchema.ALIAS_nt2);
                    break;
                case INDEX_OF_prediction:
                    v.add(VariantTableSchema.ALIAS_prediction);
                    break;
                case INDEX_OF_pph2_class:
                    v.add(VariantTableSchema.ALIAS_pph2_class);
                    break;
                case INDEX_OF_pph2_prob:
                    v.add(VariantTableSchema.ALIAS_pph2_prob);
                    break;
                case INDEX_OF_pph2_FPR:
                    v.add(VariantTableSchema.ALIAS_pph2_FPR);
                    break;
                case INDEX_OF_pph2_TPR:
                    v.add(VariantTableSchema.ALIAS_pph2_TPR);
                    break;
                case INDEX_OF_pph2_FDR:
                    v.add(VariantTableSchema.ALIAS_pph2_FDR);
                    break;
                case INDEX_OF_Transv:
                    v.add(VariantTableSchema.ALIAS_Transv);
                    break;
                case INDEX_OF_CodPos:
                    v.add(VariantTableSchema.ALIAS_CodPos);
                    break;
                case INDEX_OF_CpG:
                    v.add(VariantTableSchema.ALIAS_CpG);
                    break;
                case INDEX_OF_MinDJnc:
                    v.add(VariantTableSchema.ALIAS_MinDJnc);
                    break;
                case INDEX_OF_PfamHit:
                    v.add(VariantTableSchema.ALIAS_PfamHit);
                    break;
                case INDEX_OF_IdPmax:
                    v.add(VariantTableSchema.ALIAS_IdPmax);
                    break;
                case INDEX_OF_IdPSNP:
                    v.add(VariantTableSchema.ALIAS_IdPSNP);
                    break;
                case INDEX_OF_IdQmin:
                    v.add(VariantTableSchema.ALIAS_IdQmin);
                    break;
                case INDEX_OF_sift_prediction:
                    v.add(VariantTableSchema.ALIAS_sift_prediction);
                    break;
                case INDEX_OF_name:
                    v.add(VariantTableSchema.ALIAS_name);
                    break;
                case INDEX_OF_name2:
                    v.add(VariantTableSchema.ALIAS_name2);
                    break;
                case INDEX_OF_transcriptStrand:
                    v.add(VariantTableSchema.ALIAS_transcriptStrand);
                    break;
                case INDEX_OF_positionType:
                    v.add(VariantTableSchema.ALIAS_positionType);
                    break;
                case INDEX_OF_frame:
                    v.add(VariantTableSchema.ALIAS_frame);
                    break;
                case INDEX_OF_mrnaCoord:
                    v.add(VariantTableSchema.ALIAS_mrnaCoord);
                    break;
                case INDEX_OF_codonCoord:
                    v.add(VariantTableSchema.ALIAS_codonCoord);
                    break;
                case INDEX_OF_spliceDist:
                    v.add(VariantTableSchema.ALIAS_spliceDist);
                    break;
                case INDEX_OF_referenceCodon:
                    v.add(VariantTableSchema.ALIAS_referenceCodon);
                    break;
                case INDEX_OF_referenceAA:
                    v.add(VariantTableSchema.ALIAS_referenceAA);
                    break;
                case INDEX_OF_variantCodon:
                    v.add(VariantTableSchema.ALIAS_variantCodon);
                    break;
                case INDEX_OF_variantAA:
                    v.add(VariantTableSchema.ALIAS_variantAA);
                    break;
                case INDEX_OF_changesAA:
                    v.add(VariantTableSchema.ALIAS_changesAA);
                    break;
                case INDEX_OF_functionalClass:
                    v.add(VariantTableSchema.ALIAS_functionalClass);
                    break;
                case INDEX_OF_codingCoordStr:
                    v.add(VariantTableSchema.ALIAS_codingCoordStr);
                    break;
                case INDEX_OF_proteinCoordStr:
                    v.add(VariantTableSchema.ALIAS_proteinCoordStr);
                    break;
                case INDEX_OF_inCodingRegion:
                    v.add(VariantTableSchema.ALIAS_inCodingRegion);
                    break;
                case INDEX_OF_spliceInfo:
                    v.add(VariantTableSchema.ALIAS_spliceInfo);
                    break;
                case INDEX_OF_uorfChange:
                    v.add(VariantTableSchema.ALIAS_uorfChange);
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
            case INDEX_OF_GT:
                return VariantTableSchema.ALIAS_GT;
            case INDEX_OF_GPHASED:
                return VariantTableSchema.ALIAS_GPHASED;
            case INDEX_OF_GDP:
                return VariantTableSchema.ALIAS_GDP;
            case INDEX_OF_GFT:
                return VariantTableSchema.ALIAS_GFT;
            case INDEX_OF_GLHOMOREF:
                return VariantTableSchema.ALIAS_GLHOMOREF;
            case INDEX_OF_GLHET:
                return VariantTableSchema.ALIAS_GLHET;
            case INDEX_OF_GLHOMOALT:
                return VariantTableSchema.ALIAS_GLHOMOALT;
            case INDEX_OF_GQ:
                return VariantTableSchema.ALIAS_GQ;
            case INDEX_OF_HQA:
                return VariantTableSchema.ALIAS_HQA;
            case INDEX_OF_HQB:
                return VariantTableSchema.ALIAS_HQB;

            case INDEX_OF_position_a:
                return VariantTableSchema.ALIAS_position_a;
            case INDEX_OF_position_b:
                return VariantTableSchema.ALIAS_position_b;
            case INDEX_OF_o_acc:
                return VariantTableSchema.ALIAS_o_acc;
            case INDEX_OF_o_pos:
                return VariantTableSchema.ALIAS_o_pos;
            case INDEX_OF_o_aa1:
                return VariantTableSchema.ALIAS_o_aa1;
            case INDEX_OF_o_aa2:
                return VariantTableSchema.ALIAS_o_aa2;
            case INDEX_OF_snp_id:
                return VariantTableSchema.ALIAS_snp_id;
            case INDEX_OF_acc:
                return VariantTableSchema.ALIAS_acc;
            case INDEX_OF_pos:
                return VariantTableSchema.ALIAS_pos;
            case INDEX_OF_aa1:
                return VariantTableSchema.ALIAS_aa1;
            case INDEX_OF_aa2:
                return VariantTableSchema.ALIAS_aa2;
            case INDEX_OF_nt1:
                return VariantTableSchema.ALIAS_nt1;
            case INDEX_OF_nt2:
                return VariantTableSchema.ALIAS_nt2;
            case INDEX_OF_prediction:
                return VariantTableSchema.ALIAS_prediction;
            case INDEX_OF_pph2_class:
                return VariantTableSchema.ALIAS_pph2_class;
            case INDEX_OF_pph2_prob:
                return VariantTableSchema.ALIAS_pph2_prob;
            case INDEX_OF_pph2_FPR:
                return VariantTableSchema.ALIAS_pph2_FPR;
            case INDEX_OF_pph2_TPR:
                return VariantTableSchema.ALIAS_pph2_TPR;
            case INDEX_OF_pph2_FDR:
                return VariantTableSchema.ALIAS_pph2_FDR;
            case INDEX_OF_Transv:
                return VariantTableSchema.ALIAS_Transv;
            case INDEX_OF_CodPos:
                return VariantTableSchema.ALIAS_CodPos;
            case INDEX_OF_CpG:
                return VariantTableSchema.ALIAS_CpG;
            case INDEX_OF_MinDJnc:
                return VariantTableSchema.ALIAS_MinDJnc;
            case INDEX_OF_PfamHit:
                return VariantTableSchema.ALIAS_PfamHit;
            case INDEX_OF_IdPmax:
                return VariantTableSchema.ALIAS_IdPmax;
            case INDEX_OF_IdPSNP:
                return VariantTableSchema.ALIAS_IdPSNP;
            case INDEX_OF_IdQmin:
                return VariantTableSchema.ALIAS_IdQmin;
            case INDEX_OF_sift_prediction:
                return VariantTableSchema.ALIAS_sift_prediction;
            case INDEX_OF_name:
                return VariantTableSchema.ALIAS_name;
            case INDEX_OF_name2:
                return VariantTableSchema.ALIAS_name2;
            case INDEX_OF_transcriptStrand:
                return VariantTableSchema.ALIAS_transcriptStrand;
            case INDEX_OF_positionType:
                return VariantTableSchema.ALIAS_positionType;
            case INDEX_OF_frame:
                return VariantTableSchema.ALIAS_frame;
            case INDEX_OF_mrnaCoord:
                return VariantTableSchema.ALIAS_mrnaCoord;
            case INDEX_OF_codonCoord:
                return VariantTableSchema.ALIAS_codonCoord;
            case INDEX_OF_spliceDist:
                return VariantTableSchema.ALIAS_spliceDist;
            case INDEX_OF_referenceCodon:
                return VariantTableSchema.ALIAS_referenceCodon;
            case INDEX_OF_referenceAA:
                return VariantTableSchema.ALIAS_referenceAA;
            case INDEX_OF_variantCodon:
                return VariantTableSchema.ALIAS_variantCodon;
            case INDEX_OF_variantAA:
                return VariantTableSchema.ALIAS_variantAA;
            case INDEX_OF_changesAA:
                return VariantTableSchema.ALIAS_changesAA;
            case INDEX_OF_functionalClass:
                return VariantTableSchema.ALIAS_functionalClass;
            case INDEX_OF_codingCoordStr:
                return VariantTableSchema.ALIAS_codingCoordStr;
            case INDEX_OF_proteinCoordStr:
                return VariantTableSchema.ALIAS_proteinCoordStr;
            case INDEX_OF_inCodingRegion:
                return VariantTableSchema.ALIAS_inCodingRegion;
            case INDEX_OF_spliceInfo:
                return VariantTableSchema.ALIAS_spliceInfo;
            case INDEX_OF_uorfChange:
                return VariantTableSchema.ALIAS_uorfChange;

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
            case INDEX_OF_GT:
                return r.getGT();
            case INDEX_OF_GPHASED:
                return r.getGPhased();
            case INDEX_OF_GDP:
                return r.getGDP();
            case INDEX_OF_GFT:
                return r.getGFT();
            case INDEX_OF_GLHOMOREF:
                return r.getGLHomoRef();
            case INDEX_OF_GLHET:
                return r.getGLHet();
            case INDEX_OF_GLHOMOALT:
                return r.getGLHomoAlt();
            case INDEX_OF_GQ:
                return r.getGQ();
            case INDEX_OF_HQA:
                return r.getHQA();
            case INDEX_OF_HQB:
                return r.getHQB();
            case INDEX_OF_position_a:
                return r.getposition_a();
            case INDEX_OF_position_b:
                return r.getposition_b();
            case INDEX_OF_o_acc:
                return r.geto_acc();
            case INDEX_OF_o_pos:
                return r.geto_pos();
            case INDEX_OF_o_aa1:
                return r.geto_aa1();
            case INDEX_OF_o_aa2:
                return r.geto_aa2();
            case INDEX_OF_snp_id:
                return r.getsnp_id();
            case INDEX_OF_acc:
                return r.getacc();
            case INDEX_OF_pos:
                return r.getpos();
            case INDEX_OF_aa1:
                return r.getaa1();
            case INDEX_OF_aa2:
                return r.getaa2();
            case INDEX_OF_nt1:
                return r.getnt1();
            case INDEX_OF_nt2:
                return r.getnt2();
            case INDEX_OF_prediction:
                return r.getprediction();
            case INDEX_OF_pph2_class:
                return r.getpph2_class();
            case INDEX_OF_pph2_prob:
                return r.getpph2_prob();
            case INDEX_OF_pph2_FPR:
                return r.getpph2_FPR();
            case INDEX_OF_pph2_TPR:
                return r.getpph2_TPR();
            case INDEX_OF_pph2_FDR:
                return r.getpph2_FDR();
            case INDEX_OF_Transv:
                return r.getTransv();
            case INDEX_OF_CodPos:
                return r.getCodPos();
            case INDEX_OF_CpG:
                return r.getCpG();
            case INDEX_OF_MinDJnc:
                return r.getMinDJnc();
            case INDEX_OF_PfamHit:
                return r.getPfamHit();
            case INDEX_OF_IdPmax:
                return r.getIdPmax();
            case INDEX_OF_IdPSNP:
                return r.getIdPSNP();
            case INDEX_OF_IdQmin:
                return r.getIdQmin();
            case INDEX_OF_sift_prediction:
                return r.getsift_prediction();
            case INDEX_OF_name:
                return r.getname();
            case INDEX_OF_name2:
                return r.getname2();
            case INDEX_OF_transcriptStrand:
                return r.gettranscriptStrand();
            case INDEX_OF_positionType:
                return r.getpositionType();
            case INDEX_OF_frame:
                return r.getframe();
            case INDEX_OF_mrnaCoord:
                return r.getmrnaCoord();
            case INDEX_OF_codonCoord:
                return r.getcodonCoord();
            case INDEX_OF_spliceDist:
                return r.getspliceDist();
            case INDEX_OF_referenceCodon:
                return r.getreferenceCodon();
            case INDEX_OF_referenceAA:
                return r.getreferenceAA();
            case INDEX_OF_variantCodon:
                return r.getvariantCodon();
            case INDEX_OF_variantAA:
                return r.getvariantAA();
            case INDEX_OF_changesAA:
                return r.getchangesAA();
            case INDEX_OF_functionalClass:
                return r.getfunctionalClass();
            case INDEX_OF_codingCoordStr:
                return r.getcodingCoordStr();
            case INDEX_OF_proteinCoordStr:
                return r.getproteinCoordStr();
            case INDEX_OF_inCodingRegion:
                return r.getinCodingRegion();
            case INDEX_OF_spliceInfo:
                return r.getspliceInfo();
            case INDEX_OF_uorfChange:
                return r.getuorfChange();
        }

        return null;
    }

    public static List<Integer> getDefaultColumns() {
        List<Integer> v = new ArrayList<Integer>();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
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
                case INDEX_OF_POS:
                    if (!DEFAULT_POS) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_ID:
                    if (!DEFAULT_ID) {
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
                case INDEX_OF_GT:
                    if (!DEFAULT_GT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GPHASED:
                    if (!DEFAULT_GPHASED) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GDP:
                    if (!DEFAULT_GDP) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GFT:
                    if (!DEFAULT_GFT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GLHOMOREF:
                    if (!DEFAULT_GLHOMOREF) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GLHET:
                    if (!DEFAULT_GLHET) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GLHOMOALT:
                    if (!DEFAULT_GLHOMOALT) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_GQ:
                    if (!DEFAULT_GQ) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_HQA:
                    if (!DEFAULT_HQA) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_HQB:
                    if (!DEFAULT_HQB) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_position_a:
                    if (!DEFAULT_position_a) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_position_b:
                    if (!DEFAULT_position_b) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_o_acc:
                    if (!DEFAULT_o_acc) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_o_pos:
                    if (!DEFAULT_o_pos) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_o_aa1:
                    if (!DEFAULT_o_aa1) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_o_aa2:
                    if (!DEFAULT_o_aa2) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_snp_id:
                    if (!DEFAULT_snp_id) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_acc:
                    if (!DEFAULT_acc) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pos:
                    if (!DEFAULT_pos) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_aa1:
                    if (!DEFAULT_aa1) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_aa2:
                    if (!DEFAULT_aa2) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_nt1:
                    if (!DEFAULT_nt1) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_nt2:
                    if (!DEFAULT_nt2) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_prediction:
                    if (!DEFAULT_prediction) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pph2_class:
                    if (!DEFAULT_pph2_class) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pph2_prob:
                    if (!DEFAULT_pph2_prob) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pph2_FPR:
                    if (!DEFAULT_pph2_FPR) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pph2_TPR:
                    if (!DEFAULT_pph2_TPR) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_pph2_FDR:
                    if (!DEFAULT_pph2_FDR) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_Transv:
                    if (!DEFAULT_Transv) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CodPos:
                    if (!DEFAULT_CodPos) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_CpG:
                    if (!DEFAULT_CpG) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_MinDJnc:
                    if (!DEFAULT_MinDJnc) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_PfamHit:
                    if (!DEFAULT_PfamHit) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_IdPmax:
                    if (!DEFAULT_IdPmax) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_IdPSNP:
                    if (!DEFAULT_IdPSNP) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_IdQmin:
                    if (!DEFAULT_IdQmin) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_sift_prediction:
                    if (!DEFAULT_sift_prediction) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_name:
                    if (!DEFAULT_name) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_name2:
                    if (!DEFAULT_name2) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_transcriptStrand:
                    if (!DEFAULT_transcriptStrand) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_positionType:
                    if (!DEFAULT_positionType) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_frame:
                    if (!DEFAULT_frame) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_mrnaCoord:
                    if (!DEFAULT_mrnaCoord) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_codonCoord:
                    if (!DEFAULT_codonCoord) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_spliceDist:
                    if (!DEFAULT_spliceDist) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_referenceCodon:
                    if (!DEFAULT_referenceCodon) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_referenceAA:
                    if (!DEFAULT_referenceAA) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_variantCodon:
                    if (!DEFAULT_variantCodon) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_variantAA:
                    if (!DEFAULT_variantAA) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_changesAA:
                    if (!DEFAULT_changesAA) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_functionalClass:
                    if (!DEFAULT_functionalClass) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_codingCoordStr:
                    if (!DEFAULT_codingCoordStr) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_proteinCoordStr:
                    if (!DEFAULT_proteinCoordStr) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_inCodingRegion:
                    if (!DEFAULT_inCodingRegion) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_spliceInfo:
                    if (!DEFAULT_spliceInfo) {
                        v.add(i);
                    }
                    break;
                case INDEX_OF_uorfChange:
                    if (!DEFAULT_uorfChange) {
                        v.add(i);
                    }
                    break;
            }
        }
        return v;
    }
}
