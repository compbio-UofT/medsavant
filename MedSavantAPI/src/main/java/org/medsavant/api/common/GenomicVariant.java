/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common;

import org.medsavant.api.common.impl.GenomicVariantImpl2;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author jim
 */
public interface GenomicVariant extends Serializable {
    //Set<String> getDNAIds();
   // void addDNAId(String s);
    
    int compareTo(GenomicVariantImpl2 other);
    int compareTo(String chrom, int startpos, int endpos);

    /*
    public GenomicVariant create(
            String chrom,
            int start_position,
            int end_position,
            String dbSNPID,
            String ref,
            String alt,
            int altNumber,
            float qual,
            String filter,
            String customInfo);

    public GenomicVariant createFrom(GenomicVariant r);
*/
    /*
     * CUSTOM INFO
     */
    Integer getAlleleCount();
    Float getAlleleFrequency();
    String getAlt();
    int getAltNumber();
    String getAncestralAllele();
    Integer getBaseQuality();
    String getChrom();
    String getCigar();
    String getCustomInfo();
    String getDbSNPID();
    Boolean getDbSNPMembership();
    Integer getDepthOfCoverage();
    Integer getEndPosition();
    String getFilter();
    String getGenotype();
    Boolean getHapmap2Membership();
    Boolean getHapmap3Membership();
    Boolean getIsInThousandGenomes();
    Boolean getIsSomatic();
    Boolean getIsValidated();
    Integer getMappingQuality();
    Integer getNumberOfAlleles();
    Integer getNumberOfSamplesWithData();
    Integer getNumberOfZeroMQ();
    Float getQual();
    String getRef();
    Integer getStartPosition();
    Float getStrandBias();
    VariantType getType();
    Zygosity getZygosity();

    void setAlt(String alt);
    void setAltNumber(int a);
    void setChrom(String chrom);
    void setCustomInfo(String customInfo);
    void setDbSNPID(String id);
    void setEndPosition(Integer pos);
    void setFilter(String filter);
    void setGenotype(String genotype);
    void setQual(Float qual);
    void setRef(String ref);
    void setSampleInformation(String format, String info);
    void setStartPosition(Integer pos);
    void setType(VariantType type);
    void setZygosity(Zygosity zygosity);

    /**
     * END CUSTOM INFO
     */
    String toString();
    public String toTabString();

}
