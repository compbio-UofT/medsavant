package org.ut.biolab.medsavant.server.db.variants;

import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume
 */
public class Jannovar {

    private static final Log LOG = LogFactory.getLog(Jannovar.class);

    private static SerializationManager sManager = new SerializationManager();
    private static HashMap<Byte, Chromosome> chromosomeMap;
    private static String dirPath;
    private static ArrayList<TranscriptModel> transcriptModelList = null;
    //private static final String serializationFileName = ;
    
    /**
     * The main entry point to this class
     *
     * @param vcfFiles An array of VCF files to be annoted with Jannovar
     * @return An array of files that have been annotated with Jannovar
     * @throws JannovarException
     */
    public static File[] annotateVCFFiles(File[] vcfFiles) throws JannovarException, IOException {

        initialize();

        File[] jvFiles = new File[vcfFiles.length];

        int counter = 0;

        // annotate each file
        for (File file : vcfFiles) {
            LOG.info("Annotating " + file.getAbsolutePath() + " with Jannovar");
            jvFiles[counter++] = annotateVCFWithJannovar(file);
            LOG.info("Done annotating " + file.getAbsolutePath() + " with Jannovar");
        }

        return jvFiles;
    }
    
    private static File getJannovarDataDirectory() {
        File parent = new File(DirectorySettings.getCacheDirectory().getPath(), "jannovar");
        File f = new File(parent,"data");
        f.mkdirs();
        return f;
    }
    
    private static File getRefSeqSerializedFile() {
        return new File(getJannovarDataDirectory(), "refseq_hg19.ser");
    }

    /**
     * Initialize Jannovar
     */
    private static boolean initialize() {

        // download the serizalized files, if needed
        if (!hasSerializedFile()) {
            LOG.info("Creating serialized RefSeq file...");
            
            // create the file
            jannovar.Jannovar.main(new String[]{"--create-refseq","-d", getJannovarDataDirectory().getAbsolutePath()});
        }
        return true;
    }

    /**
     * Check if the Jannovar serialized annotation file has been downloaded.
     */
    private static boolean hasSerializedFile() {
        return getRefSeqSerializedFile().exists();
    }

    /**
     * Uses Jannovar to create a new VCF file and sends that file to server. The
     * Jannovar VCF file is subsequently removed (treated as temporary data)
     *
     * Code modified from Jannovar class.
     */
    private static File annotateVCFWithJannovar(File sourceVCF) throws JannovarException, IOException {
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

        File outFile = new File(outname);

        jannovar.Jannovar.main(new String[]{
            "-D", getRefSeqSerializedFile().getAbsolutePath(), 
            "-V", sourceVCF.getAbsolutePath(),
            "-O", sourceVCF.getParent()
        });

        LOG.info("[Jannovar] Wrote annotated VCF file to \"" + sourceVCF.getParent() + "/" + outFile.getAbsolutePath() + "\"");

        return outFile;
    }

}
