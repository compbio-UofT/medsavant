/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;

import java.util.List;
import java.util.Set;
import org.medsavant.api.annotation.MedSavantAnnotation;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.common.storage.MedSavantFile;

/**
 * A genomic variant record parsed from a VCF file.  This is a genomic variant
 * with additional fields storing which patients had this variant, and the source 
 * file from which this variant is parsed.
 * @author jim
 */
public interface GenomicVariantRecord {
    /**
     * 
     * @return The DNA Identifiers that have this variant.
     */
    /*
    public Collection<String> getDNAIds();
    public Collection<VCFFile> getSourceFiles();
    public GenomicVariant getGenomicVariant();    
    public GenomicVariantRecord create(GenomicVariant v, Collection<VCFFile> sourceFiles, Collection<String> dnaIds);
    public void addAnnotation(VaTabixAnnotation, String val);    
    public Map<Annotation, String> getAnnotations();
    */
    
    public GenomicVariant getGenomicVariant();
    public int getUploadId(); //job id
    public MedSavantFile getVCFFile();
    public String getDNAId();   
    public int getVariantId();    
    public void addAnnotationValues(MedSavantAnnotation annotation, List<String[]> annotationValues);
    public Set<MedSavantAnnotation> getAppliedAnnotations();
    public List<String[]> getAnnotationValues(MedSavantAnnotation a);        
}
