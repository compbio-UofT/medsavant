package org.ut.biolab.medsavant;

import java.util.List;

/**
 * Represents a result set and various statistics about those results.
 * @see JSONUtilities.getVariantsWithStatistics
 */
public class JSONVariants {
    List<Object[]> variants;
    int numTi;
    int numDBSNP; //overlap with dbsnp;
    int numVariants;    

    public JSONVariants(List<Object[]> variants, int numTi, int numDBSNP, int numVariants, final JSONUtilities outer) {        
        this.variants = variants;
        this.numTi = numTi;
        this.numDBSNP = numDBSNP;
        this.numVariants = numVariants;
    }
    
}
