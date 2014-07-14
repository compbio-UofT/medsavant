package org.ut.biolab.medsavant.server.db.variants;

import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 *
 * @author mfiume
 */
public class Jannovar {

    private String reference;

    // use the reference to url map when Jannovar links break
    // TODO: have the map be compiled from an xml file hosted online, to support changes after deployment
    {
        referenceToRefSeqSerURL = new HashMap<String, URL>();
        referenceToRefSeqSerURL.put("hg19", WebResources.JANNOVAR_HG19_SERFILE_URL);

    }

    private static Map<String, URL> referenceToRefSeqSerURL;

    private static final Log LOG = LogFactory.getLog(Jannovar.class);

    private static SerializationManager sManager = new SerializationManager();
    private static HashMap<Byte, Chromosome> chromosomeMap;
    private static String dirPath;
    private static ArrayList<TranscriptModel> transcriptModelList = null;
    //private static final String serializationFileName = ;

    public Jannovar(String reference) {
        this.reference = reference;
    }

    /**
     * The main entry point to this class
     *
     * @param vcfFiles An array of VCF files to be annoted with Jannovar
     * @return An array of files that have been annotated with Jannovar
     * @throws JannovarException
     */
    public File[] annotateVCFFiles(File[] vcfFiles, String database, int projectID, File destDir) throws JannovarException, IOException {

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
    }

    private File getJannovarDataDirectory() {
        File parent = new File(DirectorySettings.getCacheDirectory().getPath(), "jannovar");
        File f = new File(parent, "data");
        f.mkdirs();
        return f;
    }

    private File getRefSeqSerializedFile() {
        return new File(getJannovarDataDirectory(), "refseq_" + reference + ".ser");
    }

    /**
     * Initialize Jannovar
     */
    private boolean initialize() throws IOException, JannovarException {

        // download the serizalized files, if needed
        if (!hasSerializedFile()) {
            LOG.info("Creating serialized RefSeq file...");

            // use the reference to url map when Jannovar links break
            // TODO: have the map be compiled from an xml file hosted online, to support changes after deployment
            /*
             if (referenceToRefSeqSerURL.containsKey(reference)) {
             LOG.info("Downloading serialized file from genomesavant.com");
             NetworkUtils.downloadFile(referenceToRefSeqSerURL.get(reference), getJannovarDataDirectory(), "refseq_" + reference + ".ser");
             } else {
             */
            LOG.info("Compiling serialized file with Jannovar");
            jannovar.Jannovar.main(new String[]{
                    "--create-refseq",
                    "-d", getJannovarDataDirectory().getAbsolutePath(),
                    "-g", this.reference
            });
            /*
             }
             */
        }
        return true;
    }

    /**
     * Check if the Jannovar serialized annotation file has been downloaded.
     */
    private boolean hasSerializedFile() {
        return getRefSeqSerializedFile().exists();
    }

    /**
     * Uses Jannovar to create a new VCF file and sends that file to server. The
     * Jannovar VCF file is subsequently removed (treated as temporary data)
     *
     * Code modified from Jannovar class.
     */
    private File annotateVCFWithJannovar(File sourceVCF, File destDir) throws JannovarException, IOException {
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
            "-D", getRefSeqSerializedFile().getAbsolutePath(),
            "-V", sourceVCF.getAbsolutePath(),
            "-g", this.reference,
            "-O", destDir.getAbsolutePath() /*outFile.getAbsolutePath()*/ //, "-a" // get all annotations for this variant - currently causing infobright errors.
        });

        LOG.info("[Jannovar] Wrote annotated VCF file to \"" + sourceVCF.getParent() + "/" + outFile.getAbsolutePath() + "\"");

        return outFile;
    }

}
