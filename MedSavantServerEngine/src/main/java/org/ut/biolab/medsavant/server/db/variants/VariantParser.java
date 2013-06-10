package org.ut.biolab.medsavant.server.db.variants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.vcf.VCFParser;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume
 */
public class VariantParser extends Thread {

    private static final Log LOG = LogFactory.getLog(VariantParser.class);

    private final boolean includeHomoRef;
    private final int updateID;
    private final File vcfFile;
    private final BufferedReader reader;
    private final File outFile;
    private final int fileID;
    private boolean success = false;
    private Exception exception;

    public VariantParser(File vcfFile, File outFile, int updateID, int fileID, boolean includeHomoRef) throws FileNotFoundException, IOException {
        this.vcfFile = vcfFile;
        this.outFile = outFile;
        this.fileID = fileID;
        this.updateID = updateID;
        this.includeHomoRef = includeHomoRef;

        if (IOUtils.isGZipped(vcfFile)) {
            reader = new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(vcfFile)));
        } else {
            reader = new BufferedReader(new FileReader(vcfFile));
        }
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
    public String toString() {
        return "VariantParser{" + "includeHomoRef=" + includeHomoRef + ", updateID=" + updateID + ", vcfFile=" + vcfFile + ", reader=" + reader + ", outFile=" + outFile + ", fileID=" + fileID + ", success=" + success + ", exception=" + exception + '}';
    }

    @Override
    public void run() {

        try {
            VCFParser.parseVariantsFromReader(reader, outFile, updateID, fileID, includeHomoRef);
            success = true;
        } catch (Exception e) {
            EmailLogger.logByEmail("Error running parser on " + vcfFile.getAbsolutePath(), "Here is the object: " + toString() + ". Here is the message: " + ExceptionUtils.getStackTrace(e));
            LOG.error(e);
            success = false;
            exception = e;
        }

        //TODO: should we delete vcf file?
    }
}
