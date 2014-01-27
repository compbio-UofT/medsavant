/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.db.variants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.variants.annotation.BatchVariantAnnotator;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;

/**
 *
 * @author mfiume
 */
public class VariantAnnotator implements BasicVariantColumns, Callable<Void> {

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
    public Void call() {

        List<String> filesUsed = new ArrayList<String>();
        try {

            String inputFilePath = inFile.getAbsolutePath();
            filesUsed.add(inputFilePath);

            String workingFilePath = inputFilePath;

            try {
                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                        sessID,
                        LogManagerAdapter.LogType.INFO,
                        "Annotating " + inputFilePath);
            } catch (RemoteException ex) {
            } catch (SessionExpiredException ex) {
            }

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
                //LOG.info("\tDEBUG: annotations.length is > 0: annotations.length="+annotations.length);
                String annotatedFilename = workingFilePath + "_annotated";
                filesUsed.add(annotatedFilename);
                long startTime = System.currentTimeMillis();

                BatchVariantAnnotator bva = new BatchVariantAnnotator(new File(workingFilePath), new File(annotatedFilename), annotations, sessID);
                bva.performBatchAnnotationInParallel();

                long endTime = System.currentTimeMillis();

                long duration = (endTime - startTime) / 1000 / 60;

                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                        sessID,
                        LogManagerAdapter.LogType.INFO,
                        "Completed annotation, taking " + duration + " minutes");
                LOG.info("Completed annotation, taking " + duration + " minutes");

                VariantManagerUtils.logFileSize(annotatedFilename);
                workingFilePath = annotatedFilename;

            }

            outFile = new File(workingFilePath);

            success = true;

        } catch (Exception e) {

            try {
                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                        sessID,
                        LogManagerAdapter.LogType.ERROR,
                        "Error running annotator on " + inFile.getAbsolutePath() + "." + ExceptionUtils.getStackTrace(e));
            } catch (Exception ex) {
            }

            EmailLogger.logByEmail("Error running annotator on " + inFile.getAbsolutePath(), "Here is the object: " + toString() + ". Here is the message: " + ExceptionUtils.getStackTrace(e));
            LOG.error(e);
            e.printStackTrace();
            success = false;
            exception = e;
        }

        //cleanup
        System.gc();

        return null;

    }
}
