/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.annotation.impl;

import org.medsavant.api.common.MedSavantUpdate;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.annotation.AnnotationPipeline;
import org.medsavant.api.annotation.InvalidAnnotationPipelineException;
import org.medsavant.api.annotation.VCFPreProcessor;
import org.medsavant.api.annotation.VCFPreProcessorException;
import org.medsavant.api.annotation.VariantAnnotator;
import org.medsavant.api.annotation.VariantWindowException;
import org.medsavant.api.common.GlobalWrapper;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;
import org.medsavant.api.common.impl.MedSavantServerJob;
import org.medsavant.api.filestorage.MedSavantFile;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;

public class AnnotationPipelineImpl implements AnnotationPipeline {

    private final Log LOG = LogFactory.getLog(AnnotationPipelineImpl.class);
    private static final String JOB_NAME = "Annotation Pipeline";
    //called sequentially on each file before
    private final List<VCFPreProcessor> vpps = new ArrayList<VCFPreProcessor>();
    private final List<VariantAnnotator> vanns = new ArrayList<VariantAnnotator>();

    private MedSavantServerContext serverContext;
    private MedSavantVariantStorageEngine variantStorageEngine;

    private MedSavantVariantStorageEngine getVariantStorageEngine() {
        return variantStorageEngine;
    }

    private void checkVCFPreProcessorReqs(String cname, List<String> prereq) throws InvalidAnnotationPipelineException {
        if (prereq == null) {
            return;
        }
        for (String required : prereq) {
            boolean found = false;
            for (VCFPreProcessor p : vpps) {
                if (p.getComponentID().equals(required)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidAnnotationPipelineException("The VCFPreProcessor " + cname + " requires the VCFPreProcessor " + required + " to be applied first.");
            }
        }
    }
    
    //This method is temporary, used during development of 1.4.  It will be removed
    @Deprecated
    private void testInit(){
        this.serverContext = GlobalWrapper.getServerContext();
        this.variantStorageEngine = GlobalWrapper.getVariantStorageEngine();
    }

    public AnnotationPipelineImpl(){
        testInit();        
    }
    
    @Override
    public void addVariantAnnotator(VariantAnnotator ann) throws InvalidAnnotationPipelineException {
        List<String> prereq = ann.getPrerequisiteVCFPreProcessors();
        checkVCFPreProcessorReqs(ann.getComponentName(), prereq);
        vanns.add(ann);
    }

    @Override
    public void addVCFPreProcessor(VCFPreProcessor vpp) throws InvalidAnnotationPipelineException {
        //Note that we only check that pre-requisites have been applied.
        //The ORDER of pre-requisites is not enforced.
        List<String> prereq = vpp.getPrerequisiteVCFPreProcessors();
        checkVCFPreProcessorReqs(vpp.getComponentName(), prereq);
        vpps.add(vpp);
    }

    //runs the preprocessors, parallelized across files.  In a preprocessing pipline with N steps and M files,
    //there will be M threads created, each running through the full N-stage pipeline.
    private List<MedSavantFile> preprocess(final MedSavantSession session, final JobProgressMonitor jpm, List<MedSavantFile> files, final Reference reference) {
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
            MedSavantServerJob msj = new MedSavantServerJob(session.getUser().getUsername(), jobName, jpm) {
                @Override
                public boolean run() throws Exception {
                    MedSavantFile last = file;
                    try {
                        for (int j = 0; j < vpps.size(); ++j) {
                            //apply preprocessors serially.
                            last = vpps.get(j).preprocess(session, jpm, last, reference);
                        }                        
                        results.add(fileIndex, last);
                    } catch (VCFPreProcessorException vppe) {
                        LOG.error("VCF Preprocessing error with file " + file.getName() + ".  This file will be skipped", vppe);
                        results.add(fileIndex, null);
                    } catch (IOException ie) {
                        LOG.error("VCF Preprocessing error with file " + file.getName() + ".  This file will be skipped", ie);
                        results.add(fileIndex, null);
                    }
                    //results[fileIndex] = last;
                    return true;
                }
            };
            jobs.add(msj);
        }
        //submit preannotation jobs and block.
        try {
            serverContext.getExecutionService().submitLongJobs(jobs);
        } catch (InterruptedException ie) {
            LOG.error("Job interrupted!");
        }
        return results;
    }
/*
    private void dispatchResult(final MedSavantSession session, int updId, final JobProgressMonitor jpm, MedSavantFile newVCFFile, MedSavantFile originalVCF) throws IOException, VariantWindowException{
        jpm.setMessage("Parsing variants pre-processed from " + originalVCF.getName());
        VariantDispatcher vd = new VariantDispatcher(serverContext, getVariantStorageEngine());
        vd.dispatch(session, updId, newVCFFile, jpm, vanns);
        //VariantDispatcherOld vd = new VariantDispatcherOld(session, vanns, jpm, newVCFFile);
        //numVariants = vcfParser.parseVariantsFromReader(reader, outFile, updateID, fileID, includeHomoRef);
    }
*/
    private void dispatchResults(final MedSavantSession session, final JobProgressMonitor jpm, final MedSavantUpdate update, List<MedSavantFile> newFiles) {
        jpm.setMessage("Parsing pre-processed VCFs.");
        final List<Integer> fileIds = update.getFileIDs();
        final List<String> errors = new ArrayList<String>();
        // parse each vcf file in a separate thread with a separate file ID
        List<MedSavantServerJob> threads = new ArrayList<MedSavantServerJob>(newFiles.size());
        for (int i = 0; i < newFiles.size(); ++i) {
            final MedSavantFile vcfFile = newFiles.get(i);
            if (vcfFile != null) {
                final int fileIdIndex = i;
                //Parallelize on file level.            
                MedSavantServerJob msj = new MedSavantServerJob(session.getUser().getUsername(), "Variant Dispatcher", jpm) {

                    @Override
                    public boolean run() throws Exception {
                        try {                            
                            VariantDispatcher vd = new VariantDispatcher(serverContext, getVariantStorageEngine(), update);   
                            //dispatch(MedSavantSession session, MedSavantFile processedVCFFile, JobProgressMonitor jpm, List<VariantAnnotator> annotators, int fileId)
                            vd.dispatch(session, vcfFile, jpm, vanns, fileIds.get(fileIdIndex));
                        } catch (IOException ie) {
                            String msg = "ERROR: Couldn't annotate " + vcfFile.getName() + " -- this file was NOT imported.";
                            LOG.error(msg, ie);
                            errors.add(msg + ", " + ie.getMessage());
                            return false;
                        } catch (VariantWindowException vwe) {
                            String msg = "ERROR: Couldn't annotate " + vcfFile.getName() + " -- this file was NOT imported.";
                            LOG.error(msg, vwe);
                            errors.add(msg + ", " + vwe.getMessage());
                            return false;
                        }
                        return true;
                    }

                };
                threads.add(msj);
            }
        }

        //submit one thread per file and block until annotation is complete.
        try {
            serverContext.getExecutionService().submitLongJobs(threads);            
        } catch (InterruptedException ie) {
            LOG.error("ERROR: Interrupted while waiting for annotation threads to complete, aborting update.", ie);
            getVariantStorageEngine().cancelUpdate(session, update.getUpdateID());
        }

        int numImported = newFiles.size() - errors.size();
        int numAborted = errors.size();
        String errorStr = "\n" + StringUtils.join(errors, "\n");

        //Notify the user that the job is finished.
        EmailLogger.logByEmail("Upload finished", "Upload completed." + numImported + " file(s) were imported, and " + numAborted + " files were aborted. " + errorStr, session.getUser().getEmail());
        try {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(session.getSessionId(), LogManagerAdapter.LogType.INFO, "Done uploading variants for " + session.getProject().getProjectName());
        } catch (RemoteException ex) {
            LOG.error("Exception while logging final message", ex);
        } catch (SessionExpiredException see) {
            LOG.error("Exception while logging final message", see);
        }

    }

    
    @Override
    public void start(MedSavantSession session, JobProgressMonitor jpm, MedSavantUpdate update)/*List<MedSavantFile> files, Reference reference)*/ throws MedSavantSecurityException {
        //Here HERE HERE.  This method also assigns file Ids, which will need to be communicate back with GenomicVariantRecords.
        //the return type of this method will probably need to be more complex than an int.
        //MedSavantUpdate update = getVariantStorageEngine().startUpdate(session, project, reference, files);

        //Preprocess the VCFs as necessary.
        List<MedSavantFile> results = preprocess(session, jpm, update.getVCFFiles(), update.getReference());

        //Dispatch the results to the various annotators, via the VariantDispatcher.
        dispatchResults(session, jpm, update, results);

    }

}
