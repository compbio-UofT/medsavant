/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.filestorage;

import org.medsavant.api.common.Reference;
import org.medsavant.api.common.storage.MedSavantFile;

/**
 * A VCF file.
 *
 * @author jim
 */
@Deprecated
public interface VCFFileOld extends MedSavantFile {
    /**
     * @return The raw header contained in the VCF file, before any processing.
     * (includes all lines at the beginning of the file that start with #, up to
     * but not including the first line that doesn't contain a #).
     *
     */
    public String getRawHeader();

    public Reference getReference();

}
