/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.variantstorage;


import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.common.MedSavantDatabaseException;
import org.medsavant.api.common.MedSavantProject;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerComponent;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.MedSavantUpdate;
import org.medsavant.api.common.Reference;
import org.medsavant.api.filestorage.MedSavantFile;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;



/**
 *
 * @author jim
 */
public interface MedSavantVariantStorageEngine extends MedSavantServerComponent{      
        
    public MedSavantUpdate registerUpdate(MedSavantSession session, List<MedSavantFile> files, MedSavantProject project, Reference reference) throws MedSavantDatabaseException;
    
    /**
     * 
     * @return The version of this database.
     */
    public String getVersion() throws MedSavantDatabaseException;
    
    /**
     * Counts all variants that satisfy the given condition, and that possess the given status.
     * 
     * @param filter - Conditions on which to restrict the search
     * @param estimateAllowed - If true, then an estimate of the true count is sufficient.
     */ 
    public int countVariants(VariantFilter filter, boolean estimateAllowed) throws MedSavantDatabaseException;

    /**
     * Counts all variants that satisfy the given condition, AND that originated from one 
     * of the given files.
     * @param estimateAllowed - If true, then an estimate of the true count is sufficient.
     */
    public int countVariantsInFile(VariantFilter filter, Collection<MedSavantFile> files, boolean estimateAllowed) throws MedSavantDatabaseException;
			    
    /**
     * Exports variants to a file.
     * 
     * @param filter - Restricts the variants that are exported according to the given filter.
     * @param orderedByPosition - if true, the VCFFileOld will be sorted by position.
     *
     * @return The VCF file containing the exported variants.
     */
    public MedSavantFile exportVariants(MedSavantSession session, VariantFilter filter, boolean orderedByPosition, boolean compressOutput) throws MedSavantDatabaseException, MedSavantSecurityException, MedSavantFileDirectoryException;

    
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
    public List<GenomicVariantRecord> getVariantRecords(VariantFilter filter, long offset, int limit, VariantField[] orderBy) throws MedSavantDatabaseException;
  
    
    /**
     * Returns a 1d (field.length=1), 2d (field.length=2), etc. histogram by querying with the given filter.
     * If numBins[i] is 0, then field[i] is assumed to be categorical, and the number of bins created for that
     * field will be equal to the number of distinct values for the field.  If field[i] is numeric and
     * numBins[i] > 0, then field[i] will be binned into numBins[i] ranges.  
     **/
    public Map<String[], Integer> getHistogram(VariantFilter filter, VariantField[] field, int[] numBins) throws MedSavantDatabaseException;
  
    
    /**
     * Counts all patients that have variants satisfying the given filter
     */
    public int countPatientsWithVariants(VariantFilter filter) throws MedSavantDatabaseException;

    
    /**
     * Sets the status of the variant batch with the given updateID, for the project and reference given.
     */
    public void setVariantStatus(MedSavantSession session, int referenceId, int updateID, PublicationStatus status) throws MedSavantDatabaseException;

    
    public void addVariants(Collection<GenomicVariantRecord> variantRecords, MedSavantUpdate update) throws MedSavantDatabaseException;
    
    /**
     * Uploads a batch of variants.  This method might be called multiple times with the same variantFile, each time with a new
     * collection of variants that were processed from that file.
     * The variantFile contains an updateId and fileId that should be recorded.  Although a mapping from file ids to filenames will be maintained
     * in a different layer of MedSavant, it may nonetheless be a good idea to record it here also to help with debugging.
     * Added variants should have publication status 'PENDING'.
     * 
     * deprecated
     */
    public int addVariants(MedSavantSession session, int referenceId, Collection<GenomicVariantRecord> variantRecords, MedSavantFile variantFile) throws MedSavantDatabaseException;    

    /**
     * Adds the given annotation.  If there are existing variants, they should be annotated with these annotations.
     * @param ann 
     */
    public void addAnnotations(Collection<TabixAnnotation> annotations) throws MedSavantDatabaseException;;
    
    public void removeAnnotation(Collection<TabixAnnotation> annotations) throws MedSavantDatabaseException;
        
        
    
    /**
     * Starts a new update.  
     * 
     * @param session The user's session.
     * @param projectId The identity of the project for which the update will be applied.
     * @return A new update identifier used to identify this update.
     * @throws MedSavantSecurityException If the user doesn't have permission to update.     
     */
    //public int startUpdate(MedSavantSession session) throws MedSavantSecurityException, IllegalArgumentException;
    
    
    /**
     * Undoes/cancels an update.
     * @param session
     * @param projectId
     * @param updateId 
     */
    //alias: cancelPublish
    public void cancelUpdate(MedSavantSession session, int updateId);
    
    public void endUpdate(MedSavantSession session, MedSavantUpdate update) throws MedSavantDatabaseException;
    /////////////////
    //UNCERTAIN PARTS
    /////////////////    
    //public Map<String, List<String>> getSavantBookmarkPositionsForDNAIDs(VariantFilterBuilder filter, List<String> dnaIds, int limit);
    //public Map<String, Integer> getDNAIDHeatMap(VariantFilterBuilder filter, Collection<String> dnaIDs);
}
