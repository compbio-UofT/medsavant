/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.annotation.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.annotation.AnnotationPipeline;
import org.medsavant.api.annotation.AnnotationPipelineBuilder;
import org.medsavant.api.annotation.InvalidAnnotationPipelineException;
import org.medsavant.api.annotation.VCFPreProcessor;
import org.medsavant.api.annotation.VariantAnnotator;
import org.medsavant.api.annotation.VariantDispatcher;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.storage.MedSavantFile;
import org.medsavant.api.vcfstorage.VCFFileOld;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.MedSavantServerJob;
import org.ut.biolab.medsavant.server.db.variants.TSVFile;
import org.ut.biolab.medsavant.shared.model.MedSavantServerJobProgressMonitor;

/**
 *
 * @author jim
 */
public class AnnotationPipelineBuilderImpl implements AnnotationPipelineBuilder {

    private AnnotationPipelineImpl annotationPipeline = new AnnotationPipelineImpl(this);

    @Override
    public AnnotationPipelineBuilder addVCFPreProcessor(VCFPreProcessor vpp) throws InvalidAnnotationPipelineException {
        annotationPipeline.add(vpp);
        return this;
    }

    @Override
    public AnnotationPipelineBuilder addVariantAnnotator(VariantAnnotator ann) {
        annotationPipeline.add(ann);
        return this;
    }

    @Override
    public AnnotationPipeline build() throws IllegalStateException {
        return annotationPipeline;
    }


}
