/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 * Adapter class for mapping Solr documents to VariantRecord objects.
 */
public class SearcheableVariant {

    private VariantRecord variantRecord;

    public SearcheableVariant() {
        variantRecord = new VariantRecord();
    }

    public SearcheableVariant(VariantRecord variantRecord) {
        this.variantRecord = variantRecord;
    }

    public VariantRecord getVariantRecord() {
        return variantRecord;
    }

    @Field("upload_id")
    public void setUploadID(int uploadID) {
          variantRecord.setUploadIDID(uploadID);
    }

    @Field("file_id")
    public void setFileID(int fileID) {
        variantRecord.setFileID(fileID);
    }

    @Field("variant_id")
    public void setVariantID(int variantID) {
        variantRecord.setVariantID(variantID);
    }

    @Field("dna_id")
    public void setDnaID(String dnaID) {
        variantRecord.setDnaID(dnaID);
    }

    @Field("chrom")
    public void setChrom(String chrom) {
        variantRecord.setChrom(chrom);
    }

    @Field("pos")
    public void setPosition(Float position) {
        variantRecord.setPosition(position.longValue());
    }

    @Field("dbsnp_id")
    public void setDbSNPID(String[] dbSNPID) {
        variantRecord.setDbSNPID(StringUtils.join(dbSNPID, ","));
    }

    @Field("ref")
    public void setRef(String ref) {
        variantRecord.setRef(ref);
    }

    @Field("alt")
    public void setAlt(String[] alt) {
        variantRecord.setAlt(StringUtils.join(alt, ","));
    }

    @Field("qual")
    public void setQual(Float qual) {
        variantRecord.setQual(qual);
    }

    @Field("filter")
    public void setFilter(String[] filter) {
        variantRecord.setFilter(StringUtils.join(filter, ","));
    }

    @Field("variant_type")
    public void setVariantType(String variantType) {
        variantRecord.setType(VariantRecord.VariantType.valueOf(variantType));
    }

    @Field("zygosity")
    public void setZygosity(String zygo) {
        for (VariantRecord.Zygosity zygosity : VariantRecord.Zygosity.values()) {
            if (zygosity.toString().equals(zygo)) {
                variantRecord.setZygosity(zygosity);
            }
        }
    }

    @Field("gt")
    public void setGenotype(String genotype) {
        variantRecord.setGenotype(genotype);
    }

    @Field("custom_info")
    public void setCustomInfo(String customInfo) {
        variantRecord.setCustomInfo(customInfo);
    }

    @Field("info_AA")
    public void setAncestralAllele(String[] ancestralAllele) {
        variantRecord.setAncestralAllele(StringUtils.join(ancestralAllele, ","));
    }

    @Field("info_AC")
    public void setAlleleCount(String[] alleleCount) {
        variantRecord.setAncestralAllele(StringUtils.join(alleleCount, ","));
    }

    @Field("info_AF")
    public void setAlleleFrequency(String[] alleleFrequency) {
        variantRecord.setAlleleFrequency(Float.valueOf(alleleFrequency[0]));
    }

    @Field("info_AN")
    public void setNumberOfAlleles(String[] numberOfAlleles) {
        variantRecord.setNumberOfAlleles(Integer.valueOf(numberOfAlleles[0]));
    }

    @Field("info_BQ")
    public void setBaseQuality(String[] baseQuality) {
        variantRecord.setBaseQuality(Double.valueOf(baseQuality[0]).intValue());
    }

    @Field("info_CIGAR")
    public void setCigar(String[] cigar) {
        variantRecord.setCigar(StringUtils.join(cigar, ","));
    }

    @Field("info_DB")
    public void setDbSNPMembership(String[] dbSNPMembership) {
        variantRecord.setDbSNPMembership(Boolean.valueOf(dbSNPMembership[0]));
    }

    @Field("info_DP")
    public void setDepthOfCoverage(String[] depthOfCoverage) {
        variantRecord.setDepthOfCoverage(Integer.valueOf(depthOfCoverage[0]));
    }

    @Field("info_END")
    public void setEndPosition(String[] endPosition) {
        variantRecord.setEndPosition(Long.valueOf(endPosition[0]));
    }

    @Field("info_H2")
    public void setHapmap2Membership(String[] hapmap2Membership) {
        variantRecord.setHapmap2Membership(Boolean.valueOf(hapmap2Membership[0]));
    }

    @Field("info_H3")
    public void setHapmap3Membership(String[] hapmap3Membership) {
        variantRecord.setHapmap3Membership(Boolean.valueOf(hapmap3Membership[0]));
    }

    @Field("info_MQ")
    public void setMappingQuality(String[] mappingQuality) {
        variantRecord.setMappingQuality(Integer.valueOf(mappingQuality[0]));
    }

    @Field("info_MQ0")
    public void setNumberOfZeroMQ(String[] numberOfZeroMQ) {
        variantRecord.setNumberOfZeroMQ(Integer.valueOf(numberOfZeroMQ[0]));
    }

    @Field("info_NS")
    public void setNumberOfSamplesWithData(String[] numberOfSamplesWithData) {
        variantRecord.setNumberOfSamplesWithData(Integer.valueOf(numberOfSamplesWithData[0]));
    }

    @Field("info_SB")
    public void setStrandBias(String[] strandBias) {
        variantRecord.setStrandBias(Float.valueOf(strandBias[0]));
    }

    @Field("info_SOMATIC")
    public void setSomatic(String[] somatic) {
        variantRecord.setSomatic(Boolean.valueOf(somatic[0]));
    }

    @Field("info_VALIDATED")
    public void setValidated(String[] validated) {
        variantRecord.setValidated(Boolean.valueOf(validated[0]));
    }

    @Field("info_1000G")
    public void setInThousandGenomes(String[] inThousandGenomes) {
        variantRecord.setInThousandGenomes(Boolean.valueOf(inThousandGenomes[0]));
    }
}