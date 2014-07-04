/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import java.util.List;
import java.util.ListIterator;
import org.medsavant.api.common.GenomicVariant;

/**
 * Defines a block of contiguous variants on the same chromosome, and 
 * provides a few methods for querying various statistics.
 * @author jim
 */
public interface VariantWindow extends Iterable<GenomicVariant> {
    public int getStartPosition();
    public int getEndPosition();
    public double getAvgDensity();
    public String getChromosomeString(); //returns a string representation of the chromosome.  should be same format tabix files use.    
    public void addVariant(GenomicVariant gv) throws IllegalArgumentException;
    public ListIterator<GenomicVariant> listIterator();
    public ListIterator<GenomicVariant> listIterator(int index);
    public int getNumVariants();
    public void clear();
}
