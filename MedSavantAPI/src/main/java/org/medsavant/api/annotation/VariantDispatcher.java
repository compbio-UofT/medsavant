/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

import java.io.IOException;
import java.util.List;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.variantstorage.VariantStorageEngine;
import org.medsavant.api.vcfstorage.VCFFileOld;

/**
 *
 * @author jim
 */
public interface VariantDispatcher {
    public void dispatch(MedSavantSession session, int updId, int projectId, VCFFileOld vcfFile, JobProgressMonitor jpm, List<VariantAnnotator> annotators) throws IOException, VariantWindowException;            
}
