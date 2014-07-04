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
import org.medsavant.api.annotation.InvalidAnnotationPipelineException;
import org.medsavant.api.annotation.VCFPreProcessor;
import org.medsavant.api.annotation.VariantAnnotator;
import org.medsavant.api.annotation.VariantDispatcher;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.storage.MedSavantFile;
import org.medsavant.api.variantstorage.VariantStorageEngine;
import org.medsavant.api.vcfstorage.MedSavantFileDirectory;
import org.medsavant.api.vcfstorage.VCFFileOld;

/**
 *
 * @author jim
 */
public class AnnotationPipelineImpl implements AnnotationPipeline {
    private final Log LOG = LogFactory.getLog(AnnotationPipelineImpl.class);
    private static final String JOB_NAME = "Annotation Pipeline";
    //called sequentially on each file before
    private final List<VCFPreProcessor> vpps = new ArrayList<VCFPreProcessor>();
    private final List<VariantAnnotator> vanns = new ArrayList<VariantAnnotator>();
    
    private MedSavantServerContext serverContext;
    private MedSavantFileDirectory dir; 
    private VariantStorageEngine variantStorageEngine;        
    
    
    
    private MedSavantFileDirectory getMedSavantFileDirectory(){
        throw new UnsupportedOperationException("TODO: AnnotationPipelineImpl.getMedSavantFileDirectory");
    }
    
    private VariantStorageEngine getVariantStorageEngine(){
        throw new UnsupportedOperationException("TODO: AnnotationPipelineImpl.getVariantStorageEngine");
    }
    
    public void addVCFPreProcessor(VCFPreProcessor vpp) throws InvalidAnnotationPipelineException {
        vpps.add(vpp);
    }

    public void addVariantAnnotator(VariantAnnotator ann) throws InvalidAnnotationPipelineException {
        vanns.add(ann);
    }
            
    void add(VCFPreProcessor vpp) throws InvalidAnnotationPipelineException {
        //Note that we only check that pre-requisites have been applied.
        //The ORDER of pre-requisites is not enforced.
        List<String> prereq = vpp.getPrerequisiteVCFPreProcessors();
        for (String required : prereq) {
            boolean found = false;
            for (VCFPreProcessor p : vpps) {
                if (p.getComponentID().equals(required)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidAnnotationPipelineException("The VCFPreProcessor " + vpp.getComponentName() + " requires the VCFPreProcessor " + required + " to be applied first.");
            }
        }
        vpps.add(vpp);
    }

    private void add(VariantAnnotator ann) {
        vanns.add(ann);
    }

    //runs the preprocessors, parallelized across files.  In a preprocessing pipline with N steps and M files,
    //there will be M threads created, each running through the full N-stage pipeline.
    private List<MedSavantFile> preprocess(final MedSavantSession session, final JobProgressMonitor jpm, List<MedSavantFile> files) {
        List<MedSavantServerJob> jobs = new ArrayList<MedSavantServerJob>(files.size());
        final List<MedSavantFile> results = new ArrayList<MedSavantFile>(files.size());
        for (int i = 0; i < files.size(); ++i) {
            final MedSavantFile file = files.get(i);
            final int fileIndex = i;
            String jobName = JOB_NAME + ": " + file.getName();
            /*MedSavantServerJobProgressMonitor m = null;
            if (jpm != null && (jpm instanceof MedSavantServerJobProgressMonitor)) {
                m = (MedSavantServerJobProgressMonitor) jpm;
            }*/
            MedSavantServerJob msj = new MedSavantServerJob(session.getUsernameOfOwner(), jobName, m) {
                @Override
                public boolean run() throws Exception {
                    MedSavantFile last = file;
                    for (int j = 0; j < vpps.size(); ++j) {
                        last = vpps.get(j).preprocess(session.getUsernameOfOwner(), jpm, last);
                    }
                    dispatchResult(session, jpm, last, file);
                    results.add(fileIndex, last);
                    //results[fileIndex] = last;
                    return true;
                }
            };
            jobs.add(msj);
        }
        //submit preannotation jobs and block.
        try {
            MedSavantServerEngine.getInstance().submitLongJobs(jobs);
        } catch (InterruptedException ie) {
            LOG.error("Job interrupted!");
        }
        return results;
    }

    private void dispatchResult(final MedSavantSession session, final JobProgressMonitor jpm, MedSavantFile newVCFFile, MedSavantFile originalVCF) {
        jpm.setMessage("Parsing variants pre-processed from " + originalVCF.getName());
        VariantDispatcher vd = new VariantDispatcherImpl(serverContext, getVariantStorageEngine()); 
        //VariantDispatcherOld vd = new VariantDispatcherOld(session, vanns, jpm, newVCFFile);
        //numVariants = vcfParser.parseVariantsFromReader(reader, outFile, updateID, fileID, includeHomoRef);
    }

    private void dispatchResults(final MedSavantSession session, final JobProgressMonitor jpm, List<MedSavantFile> originalFiles, List<MedSavantFile> newFiles) {
        jpm.setMessage("Parsing pre-processed VCFs.");
        // parse each vcf file in a separate thread with a separate file ID
        List<MedSavantServerJob> threads = new ArrayList<MedSavantServerJob>(newFiles.size());
        String stamp = System.nanoTime() + "";
        for (int i = 0; i < newFiles.size(); ++i) {
            VCFFileOld vcfFile = newFiles.get(i);
            // VCFFileOld originalFile = originalFiles.get(i);
            //File outFile = new File(outDir, "tmp_" + stamp + "_" + fileID + ".tdf");
            threads.add(new VariantDispatcherOld(session, vanns, parentJob, vcfFile, outFile, updateID, fileID, includeHomozygousReferenceCalls));
            //thread   s[fileID] = t;
            //fileID++;
            //LOG.info("Queueing thread to parse " + vcfFile.getAbsolutePath());
        }
        MedSavantServerEngine.getInstance().submitLongJobs(threads);
        //VariantManagerUtils.processThreadsWithLimit(threads);
        // tab separated files
        TSVFile[] tsvFiles = new TSVFile[threads.size()];
        LOG.info("All parsing annotation threads done");
        int i = 0;
        for (MedSavantServerJob msg : threads) {
            VariantDispatcherOld t = (VariantDispatcherOld) msg;
            tsvFiles[i++] = new TSVFile(new File(t.getOutputFilePath()), t.getNumVariants());
            if (!t.didSucceed()) {
                LOG.info("At least one parser thread errored out");
                LOG.error("At least one parser thread (" + t.getVCF().getAbsolutePath() + ") errored out", t.getException());
                t.getException().printStackTrace(); //TEMPORARY
                throw t.getException();
            }
        }
        return tsvFiles;
    }

    /*
    @Override
    public void annotate(final MedSavantSession session, final JobProgressMonitor jpm, List<VCFFileOld> files) {
    //Preprocess the VCFs as necessary.
    List<VCFFileOld> results = preprocess(session.getUsernameOfOwner(), jpm, files);
    dispatchResults(session, jpm, files, results);
    //where to put this step??? //fileId is needed because this gets inserted into the infobright table!
    //int fileID = VariantManager.addEntryToFileTable(session, originalFile, updateID, projectID);//, referenceID, originalVCF);
    }
     */
    @Override
    public void annotate(MedSavantSession session, JobProgressMonitor jpm, List<MedSavantFile> files) {
        //Preprocess the VCFs as necessary.
        List<MedSavantFile> results = preprocess(session, jpm, files);
        dispatchResults(session, jpm, files, results);
        //where to put this step??? //fileId is needed because this gets inserted into the infobright table!
        //int fileID = VariantManager.addEntryToFileTable(session, originalFile, updateID, projectID);//, referenceID, originalVCF);
    }

    @Override
    public int getJobId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getUpdateId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getUploadId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
