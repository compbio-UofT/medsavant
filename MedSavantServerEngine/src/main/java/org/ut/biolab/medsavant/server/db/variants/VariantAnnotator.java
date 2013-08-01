package org.ut.biolab.medsavant.server.db.variants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.variants.annotation.BatchVariantAnnotator;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;

/**
 *
 * @author mfiume
 */
public class VariantAnnotator extends Thread implements BasicVariantColumns {

    private static final Log LOG = LogFactory.getLog(VariantAnnotator.class);
    private final File inFile;
    private File outFile;
    private final String sessID;
    private final Annotation[] annotations;
    private final String baseDir;
    private final CustomField[] customFields;
    private Exception exception;
    private boolean success;

    public VariantAnnotator(String sessID, File inputFile, File outFile, Annotation[] annotations, CustomField[] customFields) throws FileNotFoundException, IOException {
        this.inFile = inputFile;
        this.outFile = outFile;
        this.customFields = customFields;
        this.annotations = annotations;
        this.sessID = sessID;
        this.baseDir = outFile.getParentFile().getAbsolutePath();
    }

    public String getOutputFilePath() {
        return outFile.getAbsolutePath();
    }

    public Exception getException() {
        return exception;
    }

    public boolean didSucceed() {
        return success;
    }

    @Override
    public void run() {

        List<String> filesUsed = new ArrayList<String>();

        try {

            String inputFilePath = inFile.getAbsolutePath();
            filesUsed.add(inputFilePath);

            String workingFilePath = inputFilePath;

            //add custom fields
            if (customFields.length > 0) {
                //LOG.info("Adding " + customFields.length + " custom VCF fields");
                String customFieldFilename = workingFilePath + "_plusfields";
                filesUsed.add(customFieldFilename);
                VariantManagerUtils.addCustomVCFFields(workingFilePath, customFieldFilename, customFields, INDEX_OF_CUSTOM_INFO); //last of the default fields
                workingFilePath = customFieldFilename;
            }

            //annotate
            if (annotations.length > 0) {

                String annotatedFilename = workingFilePath + "_annotated";
                filesUsed.add(annotatedFilename);
                //LOG.info("File containing annotated variants, sorted by position: " + annotatedFilename);

                //LOG.info("Annotating variants in " + workingFilePath + ", destination " + annotatedFilename);

                long startTime = System.currentTimeMillis();

                BatchVariantAnnotator bva = new BatchVariantAnnotator(new File(workingFilePath), new File(annotatedFilename), annotations, sessID);
                bva.performBatchAnnotationInParallel();

                long endTime = System.currentTimeMillis();

                long duration = (endTime - startTime) / 1000 / 60;

                LOG.info("Completed annotation, taking " + duration + " minutes");

                VariantManagerUtils.logFileSize(annotatedFilename);
                workingFilePath = annotatedFilename;

            }

            outFile = new File(workingFilePath);

            success = true;

        } catch (Exception e) {
            EmailLogger.logByEmail("Error running annotator on " + inFile.getAbsolutePath(), "Here is the object: " + toString() + ". Here is the message: " + ExceptionUtils.getStackTrace(e));
            LOG.error(e);
            success = false;
            exception = e;
        }

        //cleanup
        System.gc();

        /*if (VariantManager.REMOVE_TMP_FILES) {
            for (String filename : filesUsed) {
                boolean deleted = (new File(filename)).delete();
                LOG.info("Deleting " + filename + " - " + (deleted ? "successful" : "failed"));
            }
        }
        */

    }
}
