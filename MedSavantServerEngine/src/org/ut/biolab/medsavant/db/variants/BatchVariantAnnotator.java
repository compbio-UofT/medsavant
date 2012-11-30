package org.ut.biolab.medsavant.db.variants;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.util.IOUtils;

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

    /**
     * Perform the prepared batch annotation in parallel
     *
     * @throws IOException
     * @throws SQLException
     */
    public void performBatchAnnotationInParallel() throws IOException, SQLException {

        LOG.info("Annotation of " + inputTDFFile.getAbsolutePath() + " was started. " + annotations.length + " annotation(s) will be performed.");

        // no annotations to perform, copy input to output
        if (annotations.length == 0) {
            IOUtils.copyFile(inputTDFFile, outputTDFFile);
            return;
        }

        // otherwise, perform annotations
        LOG.info("Performing " + annotations.length + " annotations");

        // the number of columns in the input file
        int numFieldsInInputFile = getNumFieldsInTDF(inputTDFFile);
        if (numFieldsInInputFile == 0) {
            throw new IOException("Error parsing input file. Is it tab delimited?");
        }

        // the number of fields that will be in the output file
        int numFieldsInOutputFile = numFieldsInInputFile;

        // create cursors for all annotations
        AnnotationCursor[] cursors = new AnnotationCursor[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            AnnotationCursor ac = new AnnotationCursor(sid, annotations[i]);
            numFieldsInOutputFile += ac.getNumNonDefaultFields();
            cursors[i] = ac;
        }

        // open the input and output files
        CSVReader recordReader = new CSVReader(new FileReader(inputTDFFile));
        CSVWriter recordWriter = new CSVWriter(new FileWriter(outputTDFFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, "\r\n");

        // read the input, line by line
        String[] inputLine;

        // container for the components of the output line (original line, plus annotations)
        String[] outputLine = new String[numFieldsInOutputFile];

        // whatever chromosome we're at in the input file
        String currentChrom = null;

        while ((inputLine = readNext(recordReader)) != null) {

            totalNumLinesRead++;

            // index into the output line
            int j = 0;

            // copy the input line
            for (int i = 0; i < inputLine.length; i++) {
                outputLine[j++] = inputLine[i];
            }

            // parse a simplified variant record from the line
            SimpleVariantRecord nextInputRecord = new SimpleVariantRecord(inputLine);

            // report whenever we get to the next chromosome
            if (currentChrom == null || !currentChrom.equals(nextInputRecord.chrom)) {
                currentChrom = nextInputRecord.chrom;
                LOG.info("Starting to annotate " + nextInputRecord.chrom + " at line " + totalNumLinesRead);
            }

            // perform each annotation, in turn
            for (int annotationIndex = 0; annotationIndex < annotations.length; annotationIndex++) {

                // get the annotation for this line
                String[] annotationForThisLine = cursors[annotationIndex].annotateVariant(nextInputRecord);

                // add it to the output buffer
                for (int k = 0; k < annotationForThisLine.length; k++) {
                    outputLine[j++] = annotationForThisLine[k];
                }

            }

            // write the output
            recordWriter.writeNext(outputLine);
        }

        // clean up
        recordReader.close();
        recordWriter.close();

        // report success
        LOG.info("Annotation of " + inputTDFFile.getAbsolutePath() + " completed. " + annotations.length + " annotations were performed.");
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
        CSVReader reader = new CSVReader(new FileReader(file));

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

    /**
     * Basic data structure for variants
     */
    public static class SimpleVariantRecord {

        // indexes of various columns in the input file
        // (there are a few) columns at the start pertaining to import / file ids
        private static final int VARIANT_INDEX_OF_CHR = 4;
        private static final int VARIANT_INDEX_OF_POS = 5;
        private static final int VARIANT_INDEX_OF_REF = 7;
        private static final int VARIANT_INDEX_OF_ALT = 8;

        // basic variant fields
        public String chrom;
        public int position;
        public String ref;
        public String alt;

        /**
         * Create a SimpleVariantRecord from an array, which has been
         * tokenized from a TDF file line
         * @param line The array from which to make this record
         */
        public SimpleVariantRecord(String[] line) {
            setFromLine(line);
        }

        /**
         * Set up variables based on the content of line
         * @param line The contents
         */
        private void setFromLine(String[] line) {
            chrom = line[VARIANT_INDEX_OF_CHR];
            position = Integer.parseInt(line[VARIANT_INDEX_OF_POS]);
            ref = line[VARIANT_INDEX_OF_REF];
            alt = line[VARIANT_INDEX_OF_ALT];
        }

        /**
         * Basic to string
         * @return String representation of this object
         */
        @Override
        public String toString() {
            return "SimpleVariantRecord{" + "chrom=" + chrom + ", position=" + position + ", ref=" + ref + ", alt=" + alt + '}';
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
            if (this.position != other.position) {
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
            hash = 79 * hash + this.position;
            hash = 79 * hash + (this.ref != null ? this.ref.hashCode() : 0);
            hash = 79 * hash + (this.alt != null ? this.alt.hashCode() : 0);
            return hash;
        }
    }
}
