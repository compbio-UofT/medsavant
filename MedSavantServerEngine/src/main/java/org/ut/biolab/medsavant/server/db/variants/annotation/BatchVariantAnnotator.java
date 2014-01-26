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
package org.ut.biolab.medsavant.server.db.variants.annotation;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.IOJob;
import org.ut.biolab.medsavant.server.MedSavantIOController;
import org.ut.biolab.medsavant.server.db.variants.VariantManagerUtils;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 *
 * @author mfiume
 */
public class BatchVariantAnnotator {

    // the log
    private static final Log LOG = LogFactory.getLog(BatchVariantAnnotator.class);
    // input file to be annotated
    private File inputTDFFile;
    // output file
    private File outputTDFFile;
    // annotations to perform
    private final Annotation[] annotations;
    // the session ID requesting the annotation
    private final String sid;
    // the total number of lines read from the input file
    private int totalNumLinesRead;
    // the total number of warnings raised while annotating
    private int totalNumWarnings;

    /**
     * Perform a set of annotations on a tab delimited genomic input file, and
     * output a tab delimited file to a specified path.
     *
     * A few assumptions are made: The input file is in tab delimited format.
     * The annotation files are in Tabix format. The input and annotation files
     * are sorted by chromosome, position, reference, alternate. The annotation,
     * if interval, is not inclusive of the end position.
     *
     * @param tdfFilename The tab delimited file to be annotated
     * @param outputFilename The resulting tab delimited file
     * @param annotIds The annotations to apply
     * @param sid The session ID that requested the annotation
     */
    public BatchVariantAnnotator(File inputFile, File outputFile, Annotation[] annotations, String sid) throws RemoteException, SQLException {
        this.inputTDFFile = inputFile;
        this.outputTDFFile = outputFile;
        this.sid = sid;
        this.annotations = annotations;
    }
    private int PROGRESS_STEP_SIZE = 10;

    private int logProgress(int totalNumLinesRead, int numLines, int oldp) {
        int x = Math.round(totalNumLinesRead / (float) numLines * 100);
        if ((x - oldp) >= PROGRESS_STEP_SIZE) {
            oldp = x;
            LOG.info("\t" + oldp + "% (" + totalNumLinesRead + " of " + numLines + " lines)");
        }
        return oldp;
    }

    /**
     * Perform the prepared batch annotation in parallel
     *
     * @throws IOException
     * @throws SQLException
     */
    public void performBatchAnnotationInParallel() throws IOException, SQLException, SessionExpiredException, IllegalArgumentException {

        org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                sid,
                LogManagerAdapter.LogType.INFO,
                "Annotation of " + inputTDFFile.getAbsolutePath() + " was started. "
                + annotations.length + " annotation(s) will be performed.");

        LOG.info("Annotation of " + inputTDFFile.getAbsolutePath() + " was started. " + annotations.length + " annotation(s) will be performed.");
        //EmailLogger.logByEmail("Annotation started", "Annotation of " + inputTDFFile.getAbsolutePath() + " was started. " + annotations.length + " annotation(s) will be performed.");

        CSVReader recordReader = null;
        CSVWriter recordWriter = null;
        AnnotationCursor[] cursors = null;
        try {
            // no annotations to perform, copy input to output
            if (annotations.length == 0) {
                MedSavantIOController.requestIO(new IOJob("Copy File") {
                    @Override
                    protected void doIO() throws IOException {
                        IOUtils.copyFile(inputTDFFile, outputTDFFile);
                    }
                });

                return;
            }

            // otherwise, perform annotations
            LOG.info("Performing " + annotations.length + " annotations");

            // the number of columns in the input file
            int numFieldsInInputFile = getNumFieldsInTDF(inputTDFFile);
            if (numFieldsInInputFile == 0) {
                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                        sid,
                        LogManagerAdapter.LogType.ERROR,
                        "Error parsing input file " + inputTDFFile.getAbsolutePath() + " . Is it tab delimited?");
                throw new IOException("Error parsing input file. Is it tab delimited?");
            }

            // the number of fields that will be in the output file
            int numFieldsInOutputFile = numFieldsInInputFile;

            // create cursors for all annotations
            cursors = new AnnotationCursor[annotations.length];
            for (int i = 0; i < annotations.length; i++) {
                AnnotationCursor ac = new AnnotationCursor(sid, annotations[i]);
                numFieldsInOutputFile += ac.getNumNonDefaultFields();
                cursors[i] = ac;
            }

            final int numlines[] = new int[1];

            MedSavantIOController.requestIO(new IOJob("Line counter") {
                @Override
                protected void doIO() throws IOException {
                    //LOG.info("DEBUG: Inside doIO of LineCounter, working with TDF File "+inputTDFFile+"\n");
                    int numLines = 0;
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(inputTDFFile));
                        while (reader.readLine() != null) {
                            numLines++;
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                    numlines[0] = numLines;
                    //LOG.info("DEBUG: Read "+numLines+" from "+inputTDFFile);
                }
            });

            // open the input and output files
            recordReader = new CSVReader(new FileReader(inputTDFFile), VariantManagerUtils.FIELD_DELIMITER.charAt(0), CSVWriter.DEFAULT_QUOTE_CHARACTER, '\\');
            recordWriter = new CSVWriter(new FileWriter(outputTDFFile), VariantManagerUtils.FIELD_DELIMITER.charAt(0), CSVWriter.DEFAULT_QUOTE_CHARACTER, '\\', "\r\n");

            //LOG.info("Reading from " + inputTDFFile.getAbsolutePath());
            //LOG.info("Writing to " + outputTDFFile.getAbsolutePath());
            // read the input, line by line
            String[] inputLine;

            MedSavantIOController.requestIO(new VariantAnnotatorIOJob(cursors, recordReader, recordWriter, numlines[0], numFieldsInOutputFile));

            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sid,
                    LogManagerAdapter.LogType.INFO,
                    "Annotation of " + inputTDFFile.getAbsolutePath() + " completed. " + annotations.length + " annotations were performed.");

            // report success
            LOG.info("Annotation of " + inputTDFFile.getAbsolutePath() + " completed. " + annotations.length + " annotations were performed.");
            //EmailLogger.logByEmail("Annotation completed", "Annotation of " + inputTDFFile.getAbsolutePath() + " completed. " + annotations.length + " annotations were performed.");
        } catch (InterruptedException ie) {

            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sid,
                    LogManagerAdapter.LogType.ERROR,
                    "Error performing annotation(s). " + ie.getLocalizedMessage());

            LOG.error("performBatchAnnotationInParallell interrupted: " + ie);
        } finally {
            // clean up
            try {
                if (cursors != null) {
                    for (AnnotationCursor c : cursors) {
                        //c.cleanup();
                    }
                }
                if (recordReader != null) {
                    recordReader.close();
                }
                if (recordWriter != null) {
                    recordWriter.close();
                }
            } catch (NullPointerException nex) {
                LOG.error("Caught nullpointerexception ");
                nex.printStackTrace();
            }
        }
    }

    /**
     * Read a line into a string array from the given reader
     *
     * @param recordReader the reader to read from
     * @return a line tokenized into a string array
     * @throws IOException
     */
    private static String[] readNext(CSVReader recordReader) throws IOException {

        // read a line
        String[] line = recordReader.readNext();
        if (line == null) {
            return null;
        } // if it has content, trim excess line-ending stuff from the last entry
        else {
            line[line.length - 1] = removeNewLinesAndCarriageReturns(line[line.length - 1]);
        }

        return line;
    }

    /**
     * Remove excess stuff from the ends of lines.
     *
     * @param line a line of text, possibly with new lines and carriage returns
     * @return the line without new lines or carriage returns
     */
    private static String removeNewLinesAndCarriageReturns(String line) {
        line = line.replaceAll("\n", "");
        line = line.replaceAll("\r", "");
        return line;
    }

    /**
     * Helper functions
     */
    /**
     * Infer the number of columns in the given TDF file by looking at the first
     * line only
     *
     * @param file The TDF file whose columns are to be counted
     * @return The number of columns in the first line in the TDF file
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static int getNumFieldsInTDF(File file) throws FileNotFoundException, IOException {

        // open the file
        CSVReader reader = new CSVReader(new FileReader(file), VariantManagerUtils.FIELD_DELIMITER.charAt(0), CSVWriter.DEFAULT_QUOTE_CHARACTER, '\\');

        //LOG.info("Analyzing file to be annotated " + file.getAbsolutePath());
        // read the first line
        String[] next = reader.readNext();
        if (next == null) {
            throw new IOException("Error determining number of columns in the annotation file.");
        }

        // return the results
        int result = next.length;

        // clean up and return
        reader.close();
        return result;
    }

    private boolean isAStandardSingleNucleotide(String base) {
        return base.equals("A") || base.equals("C") || base.equals("G") || base.equals("T");
    }

    /**
     * Basic data structure for variants stored in MedSavant database. Variants
     * are imported from VCF4 files and stored in .avinput format within the
     * database tables. To annotate these variants, they must be converted (via
     * this class) to a format compatible with Annovar annotations.
     *
     * This class should ONLY be used for variants read from database tables,
     * for the purposes of annotation.
     *
     * SimpleAnnotationRecord should be used to store annotations.
     *
     * @see SimpleAnnotationRecord
     */
    static class SimpleVariantRecord {

        // indexes of various columns in the input file
        // (there are a few) columns at the start pertaining to import / file ids
        public static final int VARIANT_INDEX_OF_CHR = 4;
        //private static final int VARIANT_INDEX_OF_POS = 5;
        public static final int VARIANT_INDEX_OF_START = 5;
        public static final int VARIANT_INDEX_OF_END = 6;
        public static final int VARIANT_INDEX_OF_REF = 8;
        public static final int VARIANT_INDEX_OF_ALT = 9;
        // basic variant fields
        public String chrom;
        //public int position;
        public long start;
        public long end;
        public String ref;
        public String alt;

        /**
         * Create a SimpleVariantRecord from an array, which has been tokenized
         * from a TDF file line
         *
         * @param line The array from which to make this record
         */
        public SimpleVariantRecord(String[] line) throws IllegalArgumentException {
            setFromLine(line);
        }

        /**
         * Set up variables based on the content of line
         *
         * @param line The contents
         */
        private void setFromLine(String[] line) throws IllegalArgumentException {
            chrom = line[VARIANT_INDEX_OF_CHR];
            String startString = line[VARIANT_INDEX_OF_START];
            String endString = line[VARIANT_INDEX_OF_END];
            try {
                start = Long.parseLong(startString);
                end = Long.parseLong(endString);
            } catch (NumberFormatException nex) {
                throw new NumberFormatException("Start or End Position is not an integer. Strings were '" + line[VARIANT_INDEX_OF_START] + "', '" + line[VARIANT_INDEX_OF_END] + "' Message: " + nex.getMessage() + "\n");
            }
            ref = line[VARIANT_INDEX_OF_REF];
            alt = line[VARIANT_INDEX_OF_ALT];

            //Convert .avinput format into the format that annovar annotations are stored in, 
            //to enable direct comparisons.
            if (start == end && ref.equals("-")) { //insertion
                alt = "0" + alt;
            } else if (alt.equals("-")) { //deletion
                alt = Long.toString(end - start + 1);
            } else if (end > start || start == end && alt.length() > 1) { //block substitution
                alt = Long.toString((end - start + 1)) + alt;
            }

        }

        /**
         * Basic to string
         *
         * @return String representation of this object
         */
        @Override
        public String toString() {
            return "SimpleVariantRecord{" + "chrom=" + chrom + ", start=" + start + ", ref=" + ref + ", alt=" + alt + '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleVariantRecord other = (SimpleVariantRecord) obj;
            if ((this.chrom == null) ? (other.chrom != null) : !this.chrom.equals(other.chrom)) {
                return false;
            }
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
                return false;
            }
            if ((this.alt == null) ? (other.alt != null) : !this.alt.equals(other.alt)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (this.chrom != null ? this.chrom.hashCode() : 0);

            int low = (int) (this.start & 0xFFFFFFFFl);
            hash = 79 * hash + (int) (this.start & 0xFFFFFFFFl) + (int) ((this.start >> 32) & 0xFFFFFFFFl);
            hash = 79 * hash + (int) (this.end & 0xFFFFFFFFl) + (int) ((this.end >> 32) & 0xFFFFFFFFl);
            hash = 79 * hash + (this.ref != null ? this.ref.hashCode() : 0);
            hash = 79 * hash + (this.alt != null ? this.alt.hashCode() : 0);
            return hash;
        }
    }

    class VariantAnnotatorIOJob extends IOJob {

        private final int VARIANT_WINDOW_SIZE = 5000; //MAXIMUM size of variant window         
        private String[] inputLine;
        private int oldp = 0;
        private int numLines;
        // container for the components of the output line (original line, plus annotations)
        private String[][] inputLines;
        private String[][] outputAnnotations;
        // whatever chromosome we're at in the input file
        private String currentChrom = null;
        private long previousStart = -1;
        private AnnotationCursor[] cursors;
        private CSVReader recordReader;
        private CSVWriter recordWriter;
        private int numVariantsInWindow = 0;
        private SimpleVariantRecord[] variantWindow;
        private long minStart = Integer.MAX_VALUE;
        private long maxEnd = 0;
        private int numFieldsInOutputFile = 0;

        private int variantsAnnotated = 0;

        public VariantAnnotatorIOJob(AnnotationCursor[] cursors, CSVReader recordReader, CSVWriter recordWriter, int numLines, int numFieldsInOutputFile) {
            super("Variant annotator");
            this.cursors = cursors;
            this.recordReader = recordReader;
            this.recordWriter = recordWriter;
            this.numLines = numLines;
            this.numFieldsInOutputFile = numFieldsInOutputFile;

            inputLines = new String[VARIANT_WINDOW_SIZE][];
            variantWindow = new SimpleVariantRecord[VARIANT_WINDOW_SIZE];
        }

        @Override
        protected boolean continueIO() throws IOException {
            inputLine = readNext(recordReader); //read next input line from variant file.
            return inputLine != null;
        }

        private void resetWindow() {
            minStart = Integer.MAX_VALUE;
            maxEnd = 0;
            numVariantsInWindow = 0;
        }

        private void annotateWindow() throws IllegalArgumentException {
            try {
                int ofs = 0;
                // perform each annotation, in turn
                for (int annotationIndex = 0; annotationIndex < annotations.length; annotationIndex++) {
                    // get the annotation for this line
                    String[][] annotationsForThisLine = cursors[annotationIndex].annotateVariants(variantWindow, minStart, maxEnd, numVariantsInWindow);
                    if (annotationsForThisLine == null) {
                        org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                                sid,
                                LogManagerAdapter.LogType.WARNING,
                                inputTDFFile.getName() + "Cannot annotate chromosome " + variantWindow[0].chrom + " with annotation " + annotations[annotationIndex]);
                    }
                    // add it to the output buffer                    
                    for (int k = 0; k < annotationsForThisLine.length; ++k) { //for each row
                        for (int l = 0; l < cursors[annotationIndex].getNumNonDefaultFields(); ++l) {
                            if (annotationsForThisLine[k] == null) {
                                outputAnnotations[k][l + ofs] = "";
                            } else {
                                outputAnnotations[k][l + ofs] = annotationsForThisLine[k][l];
                            }
                        }
                    }
                    ofs += cursors[annotationIndex].getNumNonDefaultFields();
                }

                for (int i = 0; i < numVariantsInWindow; ++i) {
                    recordWriter.writeNext((String[]) ArrayUtils.addAll(inputLines[i], outputAnnotations[i]));
                }
                variantsAnnotated += numVariantsInWindow;
                SimpleVariantRecord lv = variantWindow[numVariantsInWindow - 1];

                org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                        sid,
                        LogManagerAdapter.LogType.INFO,
                        inputTDFFile.getName() + ": Annotated " + variantsAnnotated + " variants so far.  Last variant considered: Chrom=" + lv.chrom + " Position=" + lv.start);
            } catch (Exception ex) {
                LOG.error("Couldn't communicate progress message to user: " + ex);
            }
        }

        @Override
        protected void finish() {
            annotateWindow();
        }

        @Override
        protected void doIO() throws IOException {
            try {
                //TODO: Log progress
                boolean chromMismatch = false;
                SimpleVariantRecord nextInputRecord = new SimpleVariantRecord(inputLine);

                if (outputAnnotations == null) {
                    outputAnnotations = new String[VARIANT_WINDOW_SIZE][numFieldsInOutputFile - inputLine.length];
                }

                if (numVariantsInWindow < VARIANT_WINDOW_SIZE) {
                    if ((currentChrom == null) || currentChrom.equals(nextInputRecord.chrom)) { //npe                         )
                        variantWindow[numVariantsInWindow] = nextInputRecord;
                        minStart = Math.min(nextInputRecord.start, minStart);
                        maxEnd = Math.max(nextInputRecord.end, maxEnd);
                        inputLines[numVariantsInWindow] = inputLine;
                        currentChrom = nextInputRecord.chrom;

                        //sanity check
                        if (previousStart >= 0 && (nextInputRecord.start < previousStart)) {
                            //We should never allow the database to enter this unsorted state, so unless 
                            //there's a bug or the database dump files are corrupt, this error shouldn't occur.
                            throw new IOException("File is out of order, start position " + nextInputRecord.start + " is less than previous position " + previousStart + " on chromosome " + nextInputRecord.chrom + ".  The variant table may be invalid -- please contact the system administrator.");
                        }
                        previousStart = nextInputRecord.start;
                        ++numVariantsInWindow;
                        return;
                    } else {
                        chromMismatch = true;
                    }
                }

                annotateWindow();

                resetWindow();

                numVariantsInWindow = 1;
                variantWindow[0] = nextInputRecord;
                inputLines[0] = inputLine;
                currentChrom = nextInputRecord.chrom;
                previousStart = chromMismatch ? -1 : nextInputRecord.start;
                minStart = nextInputRecord.start;
                maxEnd = nextInputRecord.end;
                chromMismatch = false;

            } catch (IllegalArgumentException iex) {
                LOG.error(iex);
            }

        }
    }
}
