/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.medsavant.api.annotation.VariantWindow;
import org.medsavant.api.common.GenomicVariant;

/**
 *
 * @author jim
 */
public class VariantWindowImpl implements VariantWindow{
   
    private  List<GenomicVariant> variants;
    private int minStartPosition = Integer.MAX_VALUE;
    private int maxEndPosition = -1;
    private String chrom = null;    
    
    public VariantWindowImpl(){
        variants = new ArrayList<GenomicVariant>();
    }
    
    public VariantWindowImpl(int blockSize){
        variants = new ArrayList<GenomicVariant>(blockSize);
    }
    
    @Override
    public void addVariant(GenomicVariant gv) throws IllegalArgumentException{
        if(chrom == null){
            chrom = gv.getChrom();
        }else if(!chrom.equals(gv.getChrom())){           
            throw new IllegalArgumentException("Cannot mix variants from different chromosomes within the same variant window.");
        }
        minStartPosition = Math.min(minStartPosition, gv.getStartPosition());
        maxEndPosition = Math.max(maxEndPosition, gv.getEndPosition());
    }
    
    @Override
    public int getStartPosition() {
        return minStartPosition;
    }

    @Override
    public int getEndPosition() {
        return maxEndPosition;
    }

    @Override
    public double getAvgDensity() {
        return (maxEndPosition - minStartPosition) / ((double)variants.size());        
    }

    @Override
    public String getChromosomeString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumVariants(){
        return variants.size();
    }      

    @Override
    public Iterator iterator() {
        return variants.iterator();
    }
    
    @Override
    public ListIterator<GenomicVariant> listIterator(){
        return variants.listIterator();
    }
    
    @Override
    public ListIterator<GenomicVariant> listIterator(int index){
        return variants.listIterator(index);
    }        
    
    @Override
    public void clear(){
        variants.clear();
    }
}
