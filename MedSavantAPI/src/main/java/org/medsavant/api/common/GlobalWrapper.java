package org.medsavant.api.common;

import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;

/**
 * This class is temporary, used during development/testing of the initial draft
 * of 1.4.  It will be deleted, and should NOT be used.
 */
public class GlobalWrapper {
    private static MedSavantServerContext serverContext;
    private static MedSavantVariantStorageEngine variantStorageEngine;

    public static MedSavantServerContext getServerContext() {
        return serverContext;
    }

    public static void setServerContext(MedSavantServerContext serverContext) {
        GlobalWrapper.serverContext = serverContext;
    }

    public static MedSavantVariantStorageEngine getVariantStorageEngine() {
        return variantStorageEngine;
    }

    public static void setVariantStorageEngine(MedSavantVariantStorageEngine variantStorageEngine) {
        GlobalWrapper.variantStorageEngine = variantStorageEngine;
    }
    
    
}
