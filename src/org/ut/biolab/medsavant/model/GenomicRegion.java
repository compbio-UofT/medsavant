/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author mfiume
 */
public class GenomicRegion {
    
    private String chrom;
    private Range range;

    public GenomicRegion(String chrom, Range range) {
        this.chrom = chrom;
        this.range = range;
    }

    public String getChrom() {
        return chrom;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "GenomicRegion{" + "chrom=" + chrom + ", range=" + range + '}';
    }
    
}
