/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.vcfstorage.VCFFileOld;
import org.medsavant.api.variantstorage.VariantFilterBuilder.VariantFilter;


/**
 *
 * @author jim
 */
public interface VariantStorageEngine {           
    /**
     * Counts all variants that satisfy the given condition, and that possess the given status.
     * 
     * @param filter - Conditions on which to restrict the search
     */ 
    public int countVariants(VariantFilter filter);

    /**
     * Counts all variants that satisfy the given condition, AND that originated from one 
     * of the given files.
     */
    public int countVariantsInFile(VariantFilter filter, Collection<VCFFileOld> files);
				    
    /**
     * Exports variants to a file.
     * 
     * @param filter - Restricts the variants that are exported according to the given filter.
     * @param orderedByPosition - if true, the VCFFileOld will be sorted by position.
     *
     * @return The VCF file containing the exported variants.
     */
    public VCFFileOld exportVariants(VariantFilter filter, boolean orderedByPosition, boolean compressOutput);

    /**
     * Returns a list of variants that satisfy the filtering criteria.  'offset' and 'limit'
     * control pagination.  orderBy may be null if no particular ordering is desired.
     * 
     * @param filter - Conditions to restrict the query.
     * @param offset - Database offset, controls pagination together with 'limit'.
     * @param limit - Number of variants to return.
     * @param orderBy - Orders the results by the given fields.
     */
    //public List<GenomicVariant> getVariants(VariantFilter filter, long offset, int limit, VariantField[] orderBy);
    
     /**
     * Returns a list of variant records that satisfy the filtering criteria.  'offset' and 'limit'
     * control pagination.  orderBy may be null if no particular ordering is desired.
    
     * 
     * @param filter - Conditions to restrict the query.
     * @param offset - Database offset, controls pagination together with 'limit'.
     * @param limit - Number of variants to return.
     * @param orderBy - Orders the results by the given fields.
     */
    public List<GenomicVariantRecord> getVariantRecords(VariantFilter filter, long offset, int limit, VariantField[] orderBy);
  
    
    /**
     * Returns a 1d (field.length=1), 2d (field.length=2), etc. histogram by querying with the given filter.
     * If numBins[i] is 0, then field[i] is assumed to be categorical, and the number of bins created for that
     * field will be equal to the number of distinct values for the field.  If field[i] is numeric and
     * numBins[i] > 0, then field[i] will be binned into numBins[i] ranges.  
     **/
    public Map<String[], Integer> getHistogram(VariantFilter filter, VariantField[] field, int[] numBins);
  
    /**
     * Counts all patients that have variants satisfying the given filter
     */
    public int countPatientsWithVariants(VariantFilter filter);

    /**
     * Sets the status of the variant batch with the given updateID, for the project and reference given.
     */
    public void setVariantStatus(int projectId, int referenceId, int updateID, PublicationStatus status);

    
    public void addVariants(Collection<GenomicVariantRecord> variantRecords, int projectId, int updateId);
    
    /**
     * Uploads a batch of variants.  This method might be called multiple times with the same variantFile, each time with a new
     * collection of variants that were processed from that file.
     * The variantFile contains an updateId and fileId that should be recorded.  Although a mapping from file ids to filenames will be maintained
     * in a different layer of MedSavant, it may nonetheless be a good idea to record it here also to help with debugging.
     * Added variants should have publication status 'PENDING'.
     * 
     * deprecated
     */
    public int addVariants(int projectId, int referenceId, Collection<GenomicVariantRecord> variantRecords, VCFFileOld variantFile);    

    /**
     * Adds the given annotation.  If there are existing variants, they should be annotated with these annotations.
     * @param ann 
     */
    public void addAnnotations(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException;
    
    public void removeAnnotation(Collection<TabixAnnotation> annotations) throws UnsupportedOperationException;
    
    public boolean isAddingAnnotationSupported();
        
    
    /////////////////
    //UNCERTAIN PARTS
    /////////////////    
    //public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(VariantFilterBuilder filter, List<String> dnaIds, int limit);
    //public Map<String, Integer> getDNAIDHeatMap(VariantFilterBuilder filter, Collection<String> dnaIDs);
}
