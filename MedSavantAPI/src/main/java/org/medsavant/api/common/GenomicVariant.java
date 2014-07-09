package org.medsavant.api.common;

import java.io.Serializable;
import org.medsavant.api.annotation.MedSavantField;

/**
 * A genomic variant storing the variant and associated custom info.  Instances of this interface
 * are what annotators operate on, and form a core part of the GenomicVariantRecord used to communicate
 * variant information to the storage engine.
 * 
 * @see org.medsavant.api.annotation.VariantAnnotator
 * @see org.medsavant.api.variantstorage.GenomicVariantRecord
 * @see org.medsavant.api.variantstorage.VariantStorageEngine
 */
public interface GenomicVariant extends Serializable {     
    int compareTo(GenomicVariant other);
    int compareTo(String chrom, int startpos, int endpos);
    
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
