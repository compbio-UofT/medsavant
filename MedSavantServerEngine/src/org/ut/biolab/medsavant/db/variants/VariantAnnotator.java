/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.db.variants;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.broad.tabix.TabixReader;
import org.broad.tabix.TabixReader.Iterator;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.log.ServerLogger;
import org.ut.biolab.medsavant.util.IOUtils;

/**
 *
 * @author mfiume
 */
public class VariantAnnotator {

    private static final Log LOG = LogFactory.getLog(VariantAnnotator.class);
    private static final int POS_ANNOT_INDEX_OF_CHR = 0;
    private static final int POS_ANNOT_INDEX_OF_POS = 1;
    private static final int POS_ANNOT_INDEX_OF_REF = 2;
    private static final int POS_ANNOT_INDEX_OF_ALT = 3;

    private static final int INT_ANNOT_INDEX_OF_CHR = 0;
    private static final int INT_ANNOT_INDEX_OF_START = 1;
    private static final int INT_ANNOT_INDEX_OF_END = 2;

    private static final int VARIANT_INDEX_OF_CHR = 4;
    private static final int VARIANT_INDEX_OF_POS = 5;
    private static final int VARIANT_INDEX_OF_REF = 7;
    private static final int VARIANT_INDEX_OF_ALT = 8;

    //private static File variantFile = new File("/Users/mfiume/Desktop/temp_proj10_ref1_update20");
    private static final int JUMP_DISTANCE = 100000;

    private VariantRecord annotateForChromosome(String chrom, VariantRecord currentVariant, CSVReader recordReader, TabixReader annotationReader, CSVWriter writer, boolean annotationHasRef, boolean annotationHasAlt, int numFieldsInOutputFile, boolean isInterval) throws EOFException, IOException {

        String[] outLine = new String[numFieldsInOutputFile];

        AnnotationRecord currentAnnotation = new AnnotationRecord(annotationHasRef, annotationHasAlt, isInterval);

        // CASE 1: this chrom cannot be annotated
        if (!isChrAnnotatable(chrom, annotationReader)) {
            return skipToNextChr(currentVariant, recordReader, writer, outLine);
        }

        // CASE 2: this chrom can be annotated
        String lastChr = currentVariant.chrom;
        int lastPosition = -1;

        TabixReader.Iterator it = annotationReader.query(currentVariant.chrom);

        while (lastChr.equals(currentVariant.chrom)) {

            //if (lastPosition == currentVariant.position && lastChr.equals(currentVariant.chrom)) {
            //    ServerLogger.log(Annotate.class,"Parser does not support multiple lines per position (" + lastChr + " " + lastPosition + ")", Level.WARNING);
            //    numWarnings++;
            //}

            boolean annotationHitEnd = false;

            // skip
            //log(currentVariant.position + " " + lastPosition);
            if (currentVariant.position > lastPosition + JUMP_DISTANCE) {
                //log("Skipping chunk in annotations to " + currentVariant.position + " in " + currentVariant.chrom);
                it = annotationReader.query(annotationReader.chr2tid(currentVariant.chrom), currentVariant.position, Integer.MAX_VALUE);

                // happens when there are no more annotations for this chrom
                if (it == null) {
                    return skipToNextChr(currentVariant, recordReader, writer, outLine);
                }
            }

            // update annotation pointer
            //while (currentAnnotation.position < currentVariant.position) {
            while (currentAnnotation.isBefore(currentVariant.position)){

                String nextannot = readNextFromIterator(it);

                // happens when there are no more annotations for this chrom
                if (nextannot == null) {
                    LOG.info("No more annotations for this chromosome");
                    annotationHitEnd = true;
                    break;
                }

                currentAnnotation.setFromLine(nextannot.split("\t"));
            }

            // CASE 2A: We hit the end of the annotations for this chromosome.
            // We can't annotate the variants in the rest of the chromosome
            if (annotationHitEnd) {
                return skipToNextChr(currentVariant, recordReader, writer, outLine);
            }

            // CASE 2B: We found the first annotation position >= current variant position

            // CASE 2B(i): There is no annotation at this position
            //if (currentAnnotation.position > currentVariant.position) {
            if (currentAnnotation.isAfter(currentVariant.position)) {
                numLinesWrittenForChromosome++;
                writer.writeNext(copyArray(currentVariant.line, outLine));

                // CASE 2B(ii): There is an annotation at this position
            } else {

                boolean annotationHitEnd1 = false;
                boolean foundMatch = false;

                // look for a matching ref / alt pair
                while (true) {

                    // found a match
                    if((isInterval || (!annotationHasRef && !annotationHasAlt)) ||
                            (annotationHasRef && annotationHasAlt && currentAnnotation.matchesRef(currentVariant.ref) && currentAnnotation.matchesAlt(currentVariant.alt)) ||
                            (annotationHasRef && !annotationHasAlt && currentAnnotation.matchesRef(currentVariant.ref)) ||
                            (!annotationHasRef && annotationHasAlt && currentAnnotation.matchesAlt(currentVariant.alt))){
                        foundMatch = true;
                        break;
                        // no match exists
                    //} else if (currentAnnotation.position != currentVariant.position) {
                    } else if (!currentAnnotation.matchesPosition(currentVariant.position)){
                        foundMatch = false;
                        break;
                        // not sure, keep looking
                    } else {
                        String nextannot = readNextFromIterator(it);

                        // happens when there are no more annotations for this chrom
                        if (nextannot == null) {
                            LOG.info("Annotation hit end; skipping chrom");
                            annotationHitEnd1 = true;
                            break;
                        }

                        currentAnnotation.setFromLine(nextannot.split("\t"));
                    }
                }

                // We hit the end of the annotations for this chromsome.
                // We can't annotate the variants in the rest of the chromosome
                if (annotationHitEnd1) {
                    return skipToNextChr(currentVariant, recordReader, writer, outLine);
                }

                numLinesWrittenForChromosome++;
                if (foundMatch) {
                    numMatchesForChromosome++;
                    //log("Matched " + currentVariant + " with " + currentAnnotation);
                    // write current line with current annotation

                    writer.writeNext(copyArraysExcludingEntries(currentVariant.line, currentAnnotation.line, outLine, currentAnnotation.getNumRelevantFields()));
                } else {
                    // write current line without annotation
                    writer.writeNext(copyArray(currentVariant.line, outLine));
                }

            }

            lastChr = currentVariant.chrom;
            lastPosition = currentVariant.position;

            String[] nextLine = readNext(recordReader);
            // throw an exception if we hit the end of the variant file
            if (nextLine == null) {
                throw new EOFException("e1");
            }

            totalNumLinesRead++;

            currentVariant.setFromLine(nextLine);
        }

        return currentVariant;
    }

    private static String[] copyArraysExcludingEntries(String[] line0, String[] line1, String[] outLine, int numFieldsFromLine1ToExclude) {
        System.arraycopy(line0, 0, outLine, 0, line0.length);
        System.arraycopy(line1, numFieldsFromLine1ToExclude, outLine, line0.length, line1.length - numFieldsFromLine1ToExclude);
        return outLine;
    }

    private static String[] readNext(CSVReader recordReader) throws IOException {
        String[] line = recordReader.readNext();
        if (line == null) {
            return null;
        } else {
            line[line.length - 1] = removeNewLinesAndCarriageReturns(line[line.length - 1]);
        }

        return line;
    }

    private static String readNextFromIterator(Iterator it) throws IOException {
        String next = it.next();
        if (next == null) {
            return next;
        }
        return removeNewLinesAndCarriageReturns(next);
    }

    private static String removeNewLinesAndCarriageReturns(String next) {

        //next = next.replaceAll("\r\n","");
        next = next.replaceAll("\n","");
        next = next.replaceAll("\r","");
        /*
        if (next.length() > 2 && next.substring(next.length() - 2).equals("\r\n")) {
            next = next.substring(0, next.length() - 2);
        } else if (next.length() > 1 && next.charAt(next.length() - 1) == '\n') {
            next = next.substring(0, next.length() - 1);
        }
         *
         */
        return next;
    }

    private String tdfFilename;
    private String outputFilename;
    private int[] annotationIds;
    //private ChromosomalPosition currentPosition;

    public VariantAnnotator(String tdfFilename, String outputFilename, int[] annotIds) {
        this.tdfFilename = tdfFilename;
        this.outputFilename = outputFilename;
        this.annotationIds = annotIds;

    }

    public void annotate(String sid) throws IOException, SQLException {
        ServerLogger.logByEmail("Annotation started", "Annotation of " + tdfFilename + " was started. " + annotationIds.length + " annotation(s) will be performed.\n\nYou will be notified again upon completion.");

        LOG.info("Annotation of " + tdfFilename + " was started. " + annotationIds.length + " annotation(s) will be performed.");

        // if no annotations to perform, copy input to output
        if (annotationIds.length == 0) {
            IOUtils.copyFile(new File(tdfFilename), new File(outputFilename));
            return;
        }

        // otherwise, perform annotations

        // get annotation objects for all ids (from db)
        Annotation[] annotations = new Annotation[annotationIds.length];
        for (int i = 0; i < annotationIds.length; i++) {
            annotations[i] = AnnotationManager.getInstance().getAnnotation(sid,annotationIds[i]);
        }


        File[] tmpFiles = new File[annotationIds.length];

        // perform each annotation in turn
        File inFile = new File(tdfFilename);
        File outFile = null;

        //ServerLogQueryUtil.getInstance().addServerLog(ServerLogQueryUtil.getInstance().LogType.INFO, "Annotating " + inFile.getAbsolutePath() + " with " + annotations.length + " annotations");

        for (int i = 0; i < annotations.length; i++) {

            this.totalNumLinesRead = 0;
            this.totalNumWarnings = 0;

            //ServerLogQueryUtil.getInstance().addServerLog(ServerLogQueryUtil.getInstance().LogType.INFO, "Annotating " + inFile.getAbsolutePath() + " with " + annotations[i].toString());
            outFile = new File(outputFilename + "_part" + i);
            tmpFiles[i] = outFile;
            annotate(sid, inFile, annotations[i], outFile);
            inFile = outFile;
        }

        // copy the output file to the appropriate destination
        IOUtils.copyFile(outFile, new File(outputFilename));

        // remove tmp files
        /*
        for (File f : tmpFiles) {
            f.delete();
        }
         */

        ServerLogger.logByEmail("Annotation complete", "Annotation of " + tdfFilename + " completed. " + annotationIds.length + " annotations were performed.");
    }

    /*
    private static VariantRecord parsePosFromLine(String[] recordLine) {
    VariantRecord p = new VariantRecord();
    p.chrom = recordLine[VARIANT_INDEX_OF_CHR];
    p.position = Integer.parseInt(recordLine[VARIANT_INDEX_OF_POS]);
    p.ref = recordLine[VARIANT_INDEX_OF_REF];
    p.alt = recordLine[VARIANT_INDEX_OF_ALT];
    return p;
    }
     *
     */
    private VariantRecord skipToNextChr(VariantRecord currentPos, CSVReader recordReader, CSVWriter writer, String[] outLine) throws EOFException, IOException {

        //ServerLogQueryUtil.getInstance().addServerLog(ServerLogQueryUtil.getInstance().LogType.INFO, "Done annotating " + currentPos.chrom);

        // skip to next chr
        String currentChr = currentPos.chrom;
        String nextLineChr;

        LOG.info("Flushing remaining variants in " + currentPos.chrom);

        String[] recordLine = null;

        writer.writeNext(copyArray(currentPos.line, outLine));
        numLinesWrittenForChromosome++;

        // loop until we get to the next chromosome
        while (true) {

            recordLine = readNext(recordReader);

            // we hit the end of the record file, bail
            // TODO: should exit cleaner
            if (recordLine == null) {
                throw new EOFException("Reached end of variant file");
            }
            totalNumLinesRead++;

            nextLineChr = recordLine[VARIANT_INDEX_OF_CHR];

            // write this record to file if we're on the same chr
            if (currentChr.equals(nextLineChr)) {

                writer.writeNext(copyArray(recordLine, outLine));
                numLinesWrittenForChromosome++;

                // if we hit a new chr, bail
            } else {
                break;
            }
        }

        //log("Last variant: { chr=" + lastLine[VARIANT_INDEX_OF_CHR] + " pos=" + lastLine[VARIANT_INDEX_OF_POS] + "}");

        VariantRecord rec = new VariantRecord(recordLine);
        LOG.info("Next variant: " + rec);

        return rec;
    }

    private static boolean isChrAnnotatable(String chr, TabixReader annotationReader) {
        boolean b = annotationReader.chr2tid(chr) != -1;
        return b;
    }

    private int numMatchesForChromosome;
    private int totalNumLinesRead;
    private int numLinesWrittenForChromosome;
    private int totalNumWarnings;

    private void annotate(String sid, File inFile, Annotation annot, File outFile) throws IOException, SQLException {
        LOG.info("Record file: " + inFile.getAbsolutePath());
        LOG.info("Annotation file: " + annot.getDataPath());
        LOG.info("Output file: " + outFile.getAbsolutePath());

        int numFieldsInInputFile = getNumFieldsInTDF(inFile);
        if(numFieldsInInputFile == 0){
            outFile.createNewFile();
            LOG.info("Done annotating file. Nothing to annotate.");
            return;
        }
        int numFieldsInOutputFile = numFieldsInInputFile + AnnotationManager.getInstance().getAnnotationFormat(sid, annot.getID()).getNumNonDefaultFields();

        //ServerLogger.log(Annotate.class,"input file: " + numFieldsInInputFile + " nondefault: " + annot.getAnnotationFormat().getNumNonDefaultFields() + " total: " + numFieldsInOutputFile);

        CSVReader recordReader = new CSVReader(new FileReader(inFile));
        TabixReader annotationReader = new TabixReader(annot.getDataPath());
        CSVWriter writer = new CSVWriter(new FileWriter(outFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, "\r\n");

        boolean annotationHasRef = AnnotationManager.getInstance().getAnnotationFormat(sid, annot.getID()).hasRef();
        boolean annotationHasAlt = AnnotationManager.getInstance().getAnnotationFormat(sid, annot.getID()).hasAlt();

        VariantRecord nextPosition = new VariantRecord(readNext(recordReader));
        totalNumLinesRead++;

        int totalLinesWritten = 0;
        totalNumWarnings = 0;

        boolean eof = false;

        while (true) {
            try {
                LOG.info("Annotating variants in " + nextPosition.chrom);
                LOG.info("First variant for chrom: " + nextPosition.toString());
                numMatchesForChromosome = 0;
                numLinesWrittenForChromosome = 0;

                nextPosition = annotateForChromosome(nextPosition.chrom, nextPosition, recordReader, annotationReader, writer, annotationHasRef, annotationHasAlt, numFieldsInOutputFile, annot.isInterval());

                totalLinesWritten += numLinesWrittenForChromosome;

                LOG.info("DONE THIS CHR:" + numMatchesForChromosome + " matches found, " + numLinesWrittenForChromosome + " written for chr");
                LOG.info("IN TOTAL: " + (totalNumLinesRead-1) + " read, " + totalLinesWritten + " written, " + totalNumWarnings + " warnings in total");

                if (totalLinesWritten + 1 + totalNumWarnings != totalNumLinesRead) {
                    throw new Exception("error: missed some lines");
                }

                // an exception is thrown when we hit the end of the input file
            } catch (Exception e) {
                eof = true;
                break;
            }
        }

        if (!eof) {
            numLinesWrittenForChromosome++;
            writer.writeNext(copyArray(nextPosition.line, new String[numFieldsInOutputFile]));
        }

        recordReader.close();
        writer.close();

        LOG.info("Done annotating file, " + totalNumLinesRead + " read " + totalLinesWritten + " written with " + totalNumWarnings + " warnings");
    }

    /**
     *
     */
    private static class VariantRecord {

        public String chrom;
        public int position;
        public String ref;
        public String alt;
        public String[] line;

        public VariantRecord(String[] line) {
            setFromLine(line);
        }

        private void setFromLine(String[] line) {
            this.line = line;
            chrom = line[VARIANT_INDEX_OF_CHR];
            position = Integer.parseInt(line[VARIANT_INDEX_OF_POS]);
            ref = line[VARIANT_INDEX_OF_REF];
            alt = line[VARIANT_INDEX_OF_ALT];
        }

        @Override
        public String toString() {
            return "VariantRecord{" + "chrom=" + chrom + ", position=" + position + ", ref=" + ref + ", alt=" + alt + '}';
        }
    }

    private static class AnnotationRecord {

        public String chrom;
        public int position;
        public int start;
        public int end;
        public String ref;
        public String alt;
        public String[] line;
        private final boolean hasRef;
        private final boolean hasAlt;
        private final boolean isInterval;

        public AnnotationRecord(boolean hasRef, boolean hasAlt, boolean isInterval) {
            this.hasRef = hasRef;
            this.hasAlt = hasAlt;
            this.isInterval = isInterval;
        }

        public void setFromLine(String[] line) {
            this.line = line;
            if(isInterval){
                setFromLineInterval(line);
            } else {
                setFromLinePosition(line);
            }
        }

        private void setFromLinePosition(String[] line) {
            chrom = line[POS_ANNOT_INDEX_OF_CHR];
            position = Integer.parseInt(line[POS_ANNOT_INDEX_OF_POS]);
            if (hasRef) {
                ref = line[POS_ANNOT_INDEX_OF_REF];
            } else {
                ref = null;
            }
            if (hasAlt) {
                alt = line[POS_ANNOT_INDEX_OF_ALT];
            } else {
                alt = null;
            }
        }

        private void setFromLineInterval(String[] line) {
            chrom = line[INT_ANNOT_INDEX_OF_CHR];
            start = Integer.parseInt(line[INT_ANNOT_INDEX_OF_START]);
            end = Integer.parseInt(line[INT_ANNOT_INDEX_OF_END]);
            ref = null;
            alt = null;
        }

        public int getNumRelevantFields(){
            if(isInterval){
                return 3;
            } else {
                return 4;
            }
        }

        public boolean isBefore(int position){
            if(isInterval){
                return this.end <= position;
            } else {
                return this.position < position;
            }
        }

        public boolean isAfter(int position){
            if(isInterval){
                return this.start > position;
            } else {
                return this.position > position;
            }
        }

        @Override
        public String toString() {
            if(isInterval){
                return "AnnotationRecord{" + "chrom=" + chrom + ", start=" + start + ", end=" + end + '}';
            } else {
                return "AnnotationRecord{" + "chrom=" + chrom + ", position=" + position + ", ref=" + ref + ", alt=" + alt + '}';
            }
        }

        public boolean matchesRef(String ref){
            return this.ref != null && this.ref.equals(ref);
        }

        public boolean matchesAlt(String alt){
            return this.alt != null && this.alt.equals(alt);
        }

        public boolean matchesPosition(int position) {
            if(isInterval){
                return this.start <= position && this.end > position;
            } else {
                return this.position == position;
            }
        }
    }

    /**
     * HELPER FUNCTIONS
     */
    private static int getNumFieldsInTDF(File inFile) throws FileNotFoundException, IOException {
        CSVReader reader = new CSVReader(new FileReader(inFile));
        String[] next = reader.readNext();
        if(next == null) return 0;
        int result = next.length;
        reader.close();
        return result;
    }

    private static String[] copyArray(String[] inLine, String[] outLine) {
        Arrays.fill(outLine, null);
        System.arraycopy(inLine, 0, outLine, 0, inLine.length);
        return outLine;
    }
}
