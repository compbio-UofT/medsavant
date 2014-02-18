package org.ut.biolab.medsavant;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import static org.ut.biolab.medsavant.shared.util.ModificationType.VARIANT;
import org.ut.biolab.medsavant.shared.util.Modifier;

/**
 * Implementation of JSONUtilitiesAdapter
 * 
 * @see org.ut.biolab.medsavant.JSONUtilitiesAdapter
 */
public class JSONUtilities implements JSONUtilitiesAdapter {

    private VariantManagerAdapter variantManager;

    public JSONUtilities(VariantManagerAdapter vma){
        variantManager = vma;
    }
     
    @Modifier(type = VARIANT)
    @Override
    public synchronized int replaceWithTransferredVCF(String sessID, int projID, int refID, List<SimpleVariantFile> files,
            int[] fileIDs, String[][] variantTags,
            String email) throws RemoteException, IOException, LockException, Exception {

        //always autopublish
        final boolean autoPublish = true;

        //never include homoref
        final boolean includeHomoRef = false;

        //always pre-annotatew ith jannovar
        final boolean preAnnotateWithJannovar = true;

        //remove existing vcfs.
        variantManager.removeVariants(sessID, projID, refID, files, autoPublish, email);

        //import new variants.
        int updateId = variantManager.uploadTransferredVariants(sessID, fileIDs, projID, refID, variantTags, includeHomoRef, email, autoPublish, preAnnotateWithJannovar);
        return updateId;
    }

}
