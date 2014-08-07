/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.medsavant.api.annotation.MedSavantAnnotation;
import org.medsavant.api.common.GenomicVariant;

public class GenomicVariantRecord {
    private final GenomicVariant genomicVariant;    
    private Map<MedSavantAnnotation, List<String[]>> valueMap;                
    private final String dnaId;
    private final int fileId;
    private final int variantId;    
    
    public GenomicVariantRecord(GenomicVariant genomicVariant, int fileId, String dnaId){
        this(genomicVariant, dnaId, fileId, -1);
    }
    
    public GenomicVariantRecord(GenomicVariant genomicVariant, String dnaId, int fileId, int variantId){
        this.genomicVariant = genomicVariant;
        this.dnaId = dnaId;  
        this.variantId = variantId;    
        this.fileId = fileId;
        valueMap = new HashMap<MedSavantAnnotation, List<String[]>>();
    }

    public void addAnnotationValues(MedSavantAnnotation annotation, List<String[]> annotationValues) {        
        valueMap.put(annotation, annotationValues);
    }
    
    public GenomicVariant getGenomicVariant() {
        return genomicVariant;        
    }
    
    public int getFileID() {
        return fileId;
    }
   
    public String getDNAId() {
        return dnaId;
    }
   
    public int getVariantId() {
        return variantId;
    }
   
    public Set<MedSavantAnnotation> getAppliedAnnotations() {
        return valueMap.keySet();
    }

    public List<String[]> getAnnotationValues(MedSavantAnnotation a) {
        return valueMap.get(a);        
    }
    
   /*
    public String toTabString(List<MedSavantAnnotation> annotations){    
        String s = fileId
        +"\t"+variantId        
        +"\t"+dnaId
        +"\t"+genomicVariant.toTabString();
        
        if(annotations != null && annotations.size() > 0){
            for(MedSavantAnnotation msa : annotations){
                List<String[]> annotationVal = getAnnotationValues(msa);
            }
        }
    }*/
 
}
