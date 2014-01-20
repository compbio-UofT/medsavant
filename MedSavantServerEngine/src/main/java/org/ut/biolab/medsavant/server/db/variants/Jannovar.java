package org.ut.biolab.medsavant.server.db.variants;

import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume
 */
class Jannovar {

    private static final Log LOG = LogFactory.getLog(Jannovar.class);

    private static SerializationManager sManager = new SerializationManager();
    private static HashMap<Byte, Chromosome> chromosomeMap;
    private static String dirPath;
    private static ArrayList<TranscriptModel> transcriptModelList = null;
    private static final String serializationFileName = "refseq_hg19.ser";

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

    /**
     * Initialize Jannovar
     */
    private static boolean initialize() {
        // download the serizalized files, if needed
        if (!hasSerializedFile(serializationFileName)) {
            LOG.info("Creating serialized RefSeq file...");
            
            // create the file in the current directory
            jannovar.Jannovar.main(new String[]{"--create-refseq"});
            
            // move the file into the Jannovar directory
            File dir = getJannovarDataDirectory();
            dir.mkdirs();
            try {
                LOG.info("Copying " + serializationFileName + " to " + new File(dir,serializationFileName).getAbsolutePath());
                IOUtils.copyFile(new File(serializationFileName), new File(dir,serializationFileName));
                LOG.info("Done creating serialized RefSeq file");
            } catch (IOException ex) {
                LOG.info("Error creating serialized RefSeq file");
                LOG.error(ex);
            }
        }
        return true;
    }

    /**
     * Check if the Jannovar serialized annotation file has been downloaded.
     */
    private static boolean hasSerializedFile(String filename) {
        File serFile = new File(Jannovar.getJannovarDataDirectory(), filename);
        return serFile.exists();
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
            "-D", new File(getJannovarDataDirectory(),serializationFileName).getAbsolutePath(), 
            "-V", sourceVCF.getAbsolutePath()});

        /* Since we can't seem to specify the output directory for Jannovar
         * VCF files, once the file is created, move it to the temp directory. */
        outFile.renameTo(new File(DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory()), outname));

        LOG.info("[Jannovar] Wrote annotated VCF file to \"" + outFile.getAbsolutePath() + "\"");

        return outFile;
    }

}
