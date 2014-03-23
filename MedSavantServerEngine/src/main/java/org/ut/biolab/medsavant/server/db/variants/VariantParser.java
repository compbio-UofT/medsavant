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
import org.ut.biolab.medsavant.server.MedSavantServerJob;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.serverapi.SessionManager;
import org.ut.biolab.medsavant.server.vcf.VCFParser;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume
 */
public class VariantParser extends MedSavantServerJob{

    private static final Log LOG = LogFactory.getLog(VariantParser.class);
    private final boolean includeHomoRef;
    private final int updateID;
    private final File vcfFile;
    private final BufferedReader reader;
    private final File outFile;
    private final int fileID;
    private boolean success = false;
    private Exception exception;
    private String sessID;
    private int numVariants;
    public VariantParser(String sessID, MedSavantServerJob parentJob, File vcfFile, File outFile, int updateID, int fileID, boolean includeHomoRef) throws FileNotFoundException, IOException {
        super(SessionManager.getInstance().getUserForSession(sessID), "VariantLoader", parentJob);
        this.vcfFile = vcfFile;
        this.outFile = outFile;
        this.fileID = fileID;
        this.updateID = updateID;
        this.includeHomoRef = includeHomoRef;
        this.sessID = sessID;
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

    public int getNumVariants(){
        return numVariants;       
    }
    
    @Override
    public String toString() {
        return "VariantParser{" + "includeHomoRef=" + includeHomoRef + ", updateID=" + updateID + ", vcfFile=" + vcfFile + ", reader=" + reader + ", outFile=" + outFile + ", fileID=" + fileID + ", success=" + success + ", exception=" + exception + '}';
    }

    @Override
    public boolean run() {
        try {
            VCFParser vcfParser = new VCFParser(sessID, vcfFile, getJobProgress());
            numVariants = vcfParser.parseVariantsFromReader(reader, outFile, updateID, fileID, includeHomoRef);
            success = true;
            
            LOG.info("VCF file "+vcfFile+" was successfully imported.");
            return true;
        } catch (Exception e) {
            EmailLogger.logByEmail("Error running parser on " + vcfFile.getAbsolutePath(), "Here is the object: " + toString() + ". Here is the message: " + ExceptionUtils.getStackTrace(e));
            LOG.error(e);
            success = false;
            exception = e;
        }
        return false;
        //TODO: should we delete vcf file?
    }
}
