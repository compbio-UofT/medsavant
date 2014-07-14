package org.medsavant.annotation.impl;

//import VCFPreProcessor
import jannovar.exception.JannovarException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.annotation.VCFPreProcessor;
import org.medsavant.api.annotation.VCFPreProcessorException;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantFileUtils;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;
import org.medsavant.api.common.storage.MedSavantFile;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.medsavant.api.filestorage.MedSavantFileType;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jim
 */
//Used to be called 'Jannovar'. 
public class JannovarVCFPreProcessor implements VCFPreProcessor {

    private MedSavantServerContext serverContext;

    //private String reference;
    // use the reference to url map when Jannovar links break
    // TODO: have the map be compiled from an xml file hosted online, to support changes after deployment
   /* {
     referenceToRefSeqSerURL = new HashMap<String, URL>();
     referenceToRefSeqSerURL.put("hg19", WebResources.JANNOVAR_HG19_SERFILE_URL);

     }*/
    //private static Map<String, URL> referenceToRefSeqSerURL;
    private static final Log LOG = LogFactory.getLog(JannovarVCFPreProcessor.class);

    /*
     private static SerializationManager sManager = new SerializationManager();
     private static HashMap<Byte, Chromosome> chromosomeMap;
     private static String dirPath;
     private static ArrayList<TranscriptModel> transcriptModelList = null;
     */
    //private static final String serializationFileName = ;

    /*
     public JannovarVCFPreProcessor(String reference) {
     this.reference = reference;
     }*/
    /**
     * The main entry point to this class
     *
     * @param vcfFiles An array of VCF files to be annoted with Jannovar
     * @return An array of files that have been annotated with Jannovar
     * @throws JannovarException
     * @deprecated
     */
    /*public File[] annotateVCFFiles(File[] vcfFiles, String database, int projectID, File destDir) throws JannovarException, IOException {

     initialize();

     File[] jvFiles = new File[vcfFiles.length];

     int counter = 0;
     //File destDir = VariantManager.getVCFDestinationDir(database, projectID);

     // annotate each file
     for (File file : vcfFiles) {
     LOG.info("Annotating " + file.getAbsolutePath() + " with Jannovar");
     jvFiles[counter++] = annotateVCFWithJannovar(file, destDir);
     LOG.info("Done annotating " + file.getAbsolutePath() + " with Jannovar");
     }

     return jvFiles;
     }*/

    /*
     private File getJannovarDataDirectory() {
        
     File parent = new File(DirectorySettings.getCacheDirectory().getPath(), "jannovar");
     File f = new File(parent, "data");
     f.mkdirs();
     return f;
     }*/
    private File getRefSeqSerializedFile(String reference) throws IOException {
        String resourceStr = "/ser/refseq_" + reference + ".ser";
        URL serFile = JannovarVCFPreProcessor.class.getResource(resourceStr);
        try {
            return new File(serFile.toURI());
        } catch (URISyntaxException ex) {
            throw new IOException("Couldn't load required resource " + resourceStr, ex);
        }
    }

    /**
     * Initialize Jannovar
     */
    /*    private synchronized boolean initialize(String reference) throws IOException {

     // download the serizalized files, if needed
     if (!hasSerializedFile(reference)) {
     LOG.info("Creating serialized RefSeq file...");

     // use the reference to url map when Jannovar links break
     // TODO: have the map be compiled from an xml file hosted online, to support changes after deployment
            
     if (referenceToRefSeqSerURL.containsKey(reference)) {
     LOG.info("Downloading serialized file from genomesavant.com");
     NetworkUtils.downloadFile(referenceToRefSeqSerURL.get(reference), getJannovarDataDirectory(), "refseq_" + reference + ".ser");
     } else {
            
     LOG.info("Compiling serialized file with Jannovar");
     jannovar.Jannovar.main(new String[]{"--create-refseq", "-d", getJannovarDataDirectory().getAbsolutePath()});
            
     }
     return true;
     }*/
    /**
     * Check if the Jannovar serialized annotation file has been downloaded.
     */
    /* private boolean hasSerializedFile(String reference) {
     return getRefSeqSerializedFile(reference).exists();
     }*/
    /**
     * Uses Jannovar to create a new VCF file and sends that file to server. The
     * Jannovar VCF file is subsequently removed (treated as temporary data)
     *
     * Code modified from Jannovar class.
     */
    private File annotateVCFWithJannovar(File sourceVCF, File destDir, String reference) throws JannovarException, IOException {
        /* Annotated VCF name as determined by Jannovar. */

        String outname = sourceVCF.getName();

        int i = outname.lastIndexOf("vcf");
        if (i < 0) {
            i = outname.lastIndexOf("VCF");
        }
        if (i < 0) {
            outname = outname + ".jv.vcf";
        } else {
            outname = outname.substring(0, i) + "jv.vcf";
        }

        File outFile = new File(destDir, outname);

        jannovar.Jannovar.main(new String[]{
            "-D", getRefSeqSerializedFile(reference).getAbsolutePath(),
            "-V", sourceVCF.getAbsolutePath(),
            "-O", destDir.getAbsolutePath() /*outFile.getAbsolutePath()*/ //, "-a" // get all annotations for this variant - currently causing infobright errors.
        });

        LOG.info("[Jannovar] Wrote annotated VCF file to \"" + sourceVCF.getParent() + "/" + outFile.getAbsolutePath() + "\"");

        return outFile;
    }

    /*
     private File dumpVariantsToTempFiVCFFileOldile variantFile) {
     File tmpFile = getTemporaryFile();
     BufferedWriter out = null;
     try {
     out = new BufferedWriter(new FileWriter(tmpFile));
     out.write(variantFile.getRawHeader() + "\n");
     for (Variant[] block : variantFile) {
     for (Variant v : block) {
     out.write(v.toString());
     out.write("\n");
     }
     }
     } catch (IOException ex) {
     } finally {
     if (out != null) {
     try {
     out.close();
     } catch (IOException ex) {
     }
     }
     }
     return tmpFile;
     }
     */
    public List<String> getPrerequisiteVCFPreProcessors() {
        return new ArrayList<String>();
    }

    public String getComponentID() {
        return JannovarVCFPreProcessor.class.getCanonicalName();
    }

    public String getComponentName() {
        return "Jannovar functional annotator";
    }

    public JannovarVCFPreProcessor(MedSavantServerContext serverContext) {
        this.serverContext = serverContext;
    }

  
    @Override
    public MedSavantFile preprocess(MedSavantSession session, JobProgressMonitor jpm, MedSavantFile toAnnotate, Reference reference) throws IOException, VCFPreProcessorException, MedSavantSecurityException {
        try {
            jpm.setMessage("Annotation variants with Jannovar...");
            
            File localFile = toAnnotate.moveToTmpFile(session, serverContext.getMedSavantFileDirectory(), serverContext.getTemporaryDirectory());
            
            File f = annotateVCFWithJannovar(localFile, serverContext.getTemporaryDirectory(), reference.getName());
            jpm.setMessage("Complete");            
            
            //This file copy is wasteful on non-distributed configurations.  
            MedSavantFile dstFile = serverContext.getMedSavantFileDirectory().createFile(session, MedSavantFileType.TEMPORARY);
            dstFile.replaceWithFile(session, serverContext.getMedSavantFileDirectory(), f);            
            return dstFile;                        
        } catch (JannovarException je) {
            throw new VCFPreProcessorException("Preprocessing error: " + je.getMessage());
        } catch(MedSavantFileDirectoryException mfde){
            throw new VCFPreProcessorException("Error registering temporary file, can't execute preprocessor "+getComponentName(), mfde);
        }
    }

    @Override
    public void configure(Dictionary dict) throws InvalidConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configure(String key, Object val) throws InvalidConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   

}
