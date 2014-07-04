/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.medsavant.api.annotation.MedSavantAnnotation;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.variantstorage.GenomicVariantRecord;
import org.medsavant.api.vcfstorage.VCFFileOld;

public class GenomicVariantRecordImpl implements GenomicVariantRecord{
    private final GenomicVariant genomicVariant;    
    private Map<MedSavantAnnotation, List<String[]>> valueMap;        
    private final int uploadId;
    private final VCFFileOld vcfFile;
    private final String dnaId;
    private final int variantId;    
    
    public GenomicVariantRecordImpl(GenomicVariant genomicVariant, int uploadId, VCFFileOld file, String dnaId){
        this(genomicVariant, uploadId, file, dnaId, -1);
    }
    
    public GenomicVariantRecordImpl(GenomicVariant genomicVariant, int uploadId, VCFFileOld file, String dnaId, int variantId){
        this.genomicVariant = genomicVariant;
        this.uploadId = uploadId;
        this.vcfFile = file;
        this.dnaId = dnaId;  
        this.variantId = variantId;    
        valueMap = new HashMap<MedSavantAnnotation, List<String[]>>();
    }

    @Override
    public void addAnnotationValues(MedSavantAnnotation annotation, List<String[]> annotationValues) {        
        valueMap.put(annotation, annotationValues);
    }
    
    @Override
    public GenomicVariant getGenomicVariant() {
        return genomicVariant;        
    }

    @Override
    public int getUploadId() {
        return uploadId;
    }

    @Override
    public VCFFileOld getVCFFile() {
        return vcfFile;
    }

    @Override
    public String getDNAId() {
        return dnaId;
    }

    @Override
    public int getVariantId() {
        return variantId;
    }

    @Override
    public Set<MedSavantAnnotation> getAppliedAnnotations() {
        return valueMap.keySet();
    }

    @Override
    public List<String[]> getAnnotationValues(MedSavantAnnotation a) {
        return valueMap.get(a);        
    }
 
}
