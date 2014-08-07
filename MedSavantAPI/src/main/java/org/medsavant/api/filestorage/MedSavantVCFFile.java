package org.medsavant.api.filestorage;

import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;

/**
 * A VCF file handle, possibly located remotely.  New instances are 
 * created via MedSavantVariantStorageEngine.registerVCF. 
 * @see MedSavantVariantStorageEngine.registerVCF
 */
public class MedSavantVCFFile extends MedSavantFileImpl {  
    
    public MedSavantVCFFile(MedSavantFileImpl f){
        super(f.getName(), f.getCreatorUsername(), f.getType(), f.path);
    }
    
    private MedSavantVCFFile(String name, String userName, MedSavantFileType fileType) {
        super(name, userName, fileType);
    }

    private MedSavantVCFFile(String name, String userName, MedSavantFileType fileType, String path) throws IllegalArgumentException {
        super(name, userName, fileType, path);
    }

    public MedSavantVCFFile(String name, String userName, int fileId) {
        super(name, userName, MedSavantFileType.VCF);
    }

    @Override
    public MedSavantFileType getType() {
        return MedSavantFileType.VCF;
    }

}
