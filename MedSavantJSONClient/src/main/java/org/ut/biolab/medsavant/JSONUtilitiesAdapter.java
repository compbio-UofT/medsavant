package org.ut.biolab.medsavant;

import com.healthmarketscience.sqlbuilder.Condition;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

/**
 * Provides various utility methods usable by the JSON client.  These methods
 * may be invoked in the same way as other Adapter methods (e.g. via the 
 * 'msInvoke' javascript method, using 'JSONUtilities' as the adapter name).
 *  
 */
public interface JSONUtilitiesAdapter {
    /**
     * Removes all the files in 'files', and imports all the transferred files with
     * identifiers given by 'fileIDs'.  The latter are uploaded to the MedSavantServer by
     * posting an appropriate multipart form to /medsavant-json-client/medsavant/UploadManager/upload.  
     * 
     * @param sessID - The session ID - automatically filled in by the Servlet (does not need to be passed via javascript).
     * @param files - The VCF files to be removed.
     * @param fileIDs - The file IDs of VCFs that have been uploaded, but not yet imported.
     * @param projID - The Project identifier.
     * @param refID - The identifier of the reference genome.
     * @param variantTags - A 2d-array of key/value pairs to be associated with the variant {{"key", "val"}, {"key", "val"}, ...}
     * @param email - if not blank/null, this email will be notified when the removal and import are completed.
     * @return An integer identifier for this update.  Unused, and may be removed.
     * @throws RemoteException
     * @throws IOException 
     * @throws LockException - if the project is locked (e.g. if someone is already updating the project)
     * @throws Exception      
     */
    public int replaceWithTransferredVCF(String sessID, int projID, int refID, List<SimpleVariantFile> files, int[] fileIDs, String[][] variantTags, String email) throws RemoteException, IOException, LockException, Exception;
    
    /**
     * Fetches variants subject to the given conditions, and returns an instance of JSONVariants with those results and statistics 
     * about the results.  (Statistics are computed over all results, not just those with an offset in the
     * interval [offset offset+limit].
     * 
     * @param sessID - The sessionID, automatically filed in by the Servlet
     * @param projID - The project identifier
     * @param refID - The identifier of the reference genome.
     * @param conditions - row major matrix of conditions where rows are OR'd together, and the cells of each row are AND'd together.
     * @param start - offset
     * @param limit - number of items to return.
     * @return JSONVariants object with summary of results.
     * @throws SQLException
     * @throws RemoteException
     * @throws SessionExpiredException 
     * @see JSONVariants
     */
    public JSONVariants getVariantsWithStatistics(String sessID, int projID, int refID, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException, SessionExpiredException;
}
