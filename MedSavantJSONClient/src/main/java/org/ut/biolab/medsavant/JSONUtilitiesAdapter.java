package org.ut.biolab.medsavant;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
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
}
