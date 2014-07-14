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
package org.medsavant.api.annotation.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broad.tabix.TabixReader;
import org.medsavant.api.annotation.TabixAnnotation;
import java.io.File;
import org.medsavant.api.annotation.VariantWindow;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.VariantUtils;
import org.medsavant.api.filestorage.MedSavantFileDirectory;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;

/**
 *
 * 
 */
public class AnnotationCursor {

    // the log

    private static final Log LOG = LogFactory.getLog(AnnotationCursor.class);
    // reader for the annotation
    private final TabixReader reader;
    // does the annotation have an alt column
    private final boolean annotationHasAlt;
    // does the annotation have a ref column
    private final boolean annotationHasRef;
    // does this annotation refer to positions or intervals
    private final boolean isInterval;

    // the annotation to apply
    private final TabixAnnotation annotation;

    //Setup default column indices for required columns.  These can be overridden
    //if the file has a header
    private int pos_annot_index_of_chr = 0;
    private int pos_annot_index_of_pos = 1;
    private int pos_annot_index_of_ref = 2;
    private int pos_annot_index_of_alt = 3;
    private int int_annot_index_of_chr = 0;
    private int int_annot_index_of_start = 1;
    private int int_annot_index_of_end = 2;

   // SimpleVariantRecord lastVariantAnnotated;
    //SimpleAnnotationRecord lastAnnotationConsidered;
    //String[] lastResult;
    //private final AnnotationFormat annotationFormat;
    public double getDensityThreshold(int variantWindowSize) {
        return 10.0f / (double) variantWindowSize;
    }

    /**
     * A file reader and cursor to be used to help in the annotation process
     *
     * @param session A valid MedSavant session, used to request the tabix annotation from the file directory.  
     * @param annotation The annotation that this instance refers to
     * @throws IOException
     * @throws SQLException
     */
    public AnnotationCursor(MedSavantSession session, TabixAnnotation annotation, MedSavantFileDirectory directory, File annotationCacheDir) throws IOException, MedSavantSecurityException, MedSavantFileDirectoryException {
        
        File tabixFile = annotation.getTabixFile().moveToTmpFile(session, directory, new File(annotationCacheDir, annotation.getProgram()));
        File indexFile = annotation.getTabixFileIndex().moveToTmpFile(session, directory, new File(annotationCacheDir, annotation.getProgram()));

        TabixReader headerReader = new TabixReader(tabixFile, indexFile);
        String header = headerReader.readLine().trim();
        headerReader.cleanup();

        reader = new TabixReader(tabixFile, indexFile);

        //If the tabix file has a header, then find the indices for all the 
        //required columns
        if (header.startsWith(String.valueOf(reader.getCommentChar()))) {
            String[] parts = header.split("\t");
            pos_annot_index_of_chr = int_annot_index_of_chr = -1;
            int_annot_index_of_start = -1;
            int_annot_index_of_end = -1;
            pos_annot_index_of_ref = -1;
            pos_annot_index_of_alt = -1;
            pos_annot_index_of_pos = -1;

            for (int i = parts.length - 1; i >= 0; --i) {
                parts[i] = parts[i].replace("#", "").trim().toUpperCase();
                if (parts[i].equalsIgnoreCase("CHR") || parts[i].equalsIgnoreCase("CHROM")) {
                    pos_annot_index_of_chr = int_annot_index_of_chr = i;
                } else if (parts[i].equalsIgnoreCase("START")) {
                    int_annot_index_of_start = i;
                    pos_annot_index_of_pos = i;
                } else if (parts[i].equalsIgnoreCase("END")) {
                    int_annot_index_of_end = i;
                } else if (parts[i].equalsIgnoreCase("REF")) {
                    pos_annot_index_of_ref = i;
                } else if (parts[i].equalsIgnoreCase("ALT")) {
                    pos_annot_index_of_alt = i;
                } else if (parts[i].equalsIgnoreCase("POSITION")) {
                    pos_annot_index_of_pos = i;
                }
            }

            String missingCol = null;
            if (annotation.isIntervalAnnotation()) {
                if (int_annot_index_of_chr == -1) {
                    missingCol = "Chromosome";
                } else if (int_annot_index_of_start == -1) {
                    missingCol = "Start";
                } else if (int_annot_index_of_end == -1) {
                    missingCol = "End";
                }
            } else {
                if (pos_annot_index_of_chr == -1) {
                    missingCol = "Chromosome";
                } else if (pos_annot_index_of_ref == -1) {
                    missingCol = "Ref";
                } else if (pos_annot_index_of_alt == -1) {
                    missingCol = "Alt";
                } else if (pos_annot_index_of_pos == -1) {
                    missingCol = "Position";
                }
            }

            if (missingCol != null) {
                throw new IllegalArgumentException("Couldn't locate column " + missingCol + " in annotation " + annotation.getProgram() + " (ref=" + annotation.getReferenceName() + ")");
            }
        }

        this.annotation = annotation;
        annotationHasRef = annotation.hasRef();
        annotationHasAlt = annotation.hasAlt();
        isInterval = annotation.isIntervalAnnotation();
        /*
         //annotationFormat = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID());        
         annotationHasRef = annotationFormat.hasRef();
         annotationHasAlt = annotationFormat.hasAlt();
         isInterval = annotation.isInterval();        */
    }

    private String[] getNonDefaultFields(String[] annotationLine) throws IllegalArgumentException {
        String prefix = "";
        String[] result = new String[annotation.getNumNonDefaultFields()];

        // int numNonDefaultFields = annotationFormat.getNumNonDefaultFields();        
        int numDefaultFields = annotation.getNumDefaultFields(int_annot_index_of_end != -1);

        int numNonDefaultFields = annotation.getNumNonDefaultFields();

        for (int i = 0; i < numNonDefaultFields; ++i) {
            result[i] = prefix;
            if ((numDefaultFields + i) < annotationLine.length) {
                result[i] += annotationLine[numDefaultFields + i];
            }
        }
        return result;
    }

    //Annotates the first numRecords variant records given in 'records'.  
    //Precondition: All records have the same chromosome, and are ordered by position in ascending order.
    String[][] annotateVariants(VariantWindow variantWindow) throws IOException {
        String[][] results = new String[variantWindow.getNumVariants()][];
        if (!canAnnotateThisChromosome(variantWindow.getChromosomeString())) {
            //this chromosome can't be annotated.
            return results;
        }

        

        //Annotates variants by separately seeking annotations for each variant.  Usually slow unless
        //variants are very sparse.
        if (variantWindow.getAvgDensity() <= getDensityThreshold(variantWindow.getNumVariants())) { //variant window has low density.
            int i = 0;
            for (GenomicVariant gv : variantWindow) {
                TabixReader.Iterator annotationIt = reader.query(reader.chr2tid(gv.getChrom()),
                        (int) gv.getStartPosition() - 1, //this function returns annotations AFTER this position, so we need to have the -1
                        (int) gv.getEndPosition());
                if (annotationIt == null) { //no matching annotations in this range.
                    results[i] = null;
                    continue;
                }
                while (annotationIt.hasNext()) {
                    String annotationLineStr = (String) annotationIt.next();
                    String[] annotationLine = removeNewLinesAndCarriageReturns(annotationLineStr).split(annotation.getFieldDelimiter(), -1);

                    SimpleAnnotationRecord annotationRecord = new SimpleAnnotationRecord(annotationLine);
                    if (annotationRecord.matchesVariant(gv)) {
                        //safe to break?  Here it is assumed that the same position is not duplicated in the tabix.
                        results[i] = getNonDefaultFields(annotationLine);
                        break;
                        //svr.annotate(annotationIndex, getVariantAnnotationString(annotationLine));
                    }
                }
            }

            return results;
        }//else variant window has high density:

        //Annotates variants by reading in all annotations in the region delimited by the variantWindow 
        //(only uses one seek).  Faster if variants are dense.
        TabixReader.Iterator annotationIt
                = reader.query(reader.chr2tid(variantWindow.getChromosomeString()),
                        (int) Math.max(0, variantWindow.getStartPosition() - 1), //this function returns annotations AFTER this position, so we need to have the -1
                        (int) variantWindow.getEndPosition());

        if (annotationIt == null) { //no annotations in this range.
            return results;            
        }

        int variantWindowIndex = 0;
        while (annotationIt.hasNext()) { //For each annotation in start + min(start+max_base_pair_distance_in_window, end)
            String annotationLineStr = (String) annotationIt.next();
            String[] annotationLine = removeNewLinesAndCarriageReturns(annotationLineStr).split(annotation.getFieldDelimiter(), -1);
            SimpleAnnotationRecord annotationRecord = new SimpleAnnotationRecord(annotationLine);

            ListIterator<GenomicVariant> variantWindowIt = variantWindow.listIterator(variantWindowIndex);
            while (variantWindowIt.hasNext()) {
                GenomicVariant variant = variantWindowIt.next();
                if (annotationRecord.matchesVariant(variant)) {
                    //annotate       
                    results[variantWindowIndex] = getNonDefaultFields(annotationLine);
                    //variantRecord.annotate(annotationIndex, getVariantAnnotationString( annotationLine));
                } else if (variant.getStartPosition() > annotationRecord.end) {
                    variantWindowIndex = variantWindowIt.previousIndex();
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Return whether or not this annotation contains anything for the given
     * chromosome
     *
     * @param chrom The chromosome to look for
     * @return Whether or not this annotation contains anything for the given
     * chromosome
     */
    private boolean canAnnotateThisChromosome(String chrom) {
        return reader.chr2tid(chrom) != -1;
    }

    /**
     * Remove excess stuff from the ends of lines.
     *
     * @param line a line of text, possibly with new lines and carriage returns
     * @return the line without new lines or carriage returns
     */
    private static String removeNewLinesAndCarriageReturns(String next) {
        next = next.replaceAll("\n", "");
        next = next.replaceAll("\r", "");
        return next;
    }

    /**
     * Checks if the annotation has chromosome field in the format 1 or chr1. In
     * the former case, this method returns true since we'll need to homogenize
     * the variant records, which are in the latter format.
     *
     * It is assumed that all the references in an annotation file are in the
     * same format, and not mixed.
     *
     * @param reader A Tabix reader for the annotation
     * @return Whether or not homogenization of the variant chrom field is
     * needed
     */
    private boolean checkIfHomogenizationIsNeeded(TabixReader reader) {
        for (String s : reader.getReferenceNames()) {
            if (s.contains("chr") || s.contains("contig")) {
                return true;
            }
        }
        return false;

    }

    private class SimpleAnnotationRecord {

        public String chrom;
        public int position;
        public long start;
        public long end;
        public String ref;
        public String alt;

        public SimpleAnnotationRecord(String[] line) {
            setFromLine(line);
        }

        private void setFromLine(String[] line) {
            if (isInterval) {
                setFromLineInterval(line);
            } else {
                setFromLinePosition(line);
            }
        }

        private void setFromLinePosition(String[] line) {
            chrom = line[pos_annot_index_of_chr];
            if (!chrom.toLowerCase().startsWith("chr")) {
                chrom = "chr" + VariantUtils.homogenizeSequence(chrom);
            }
            start = end = position = Integer.parseInt(line[pos_annot_index_of_pos]);
            if (annotationHasRef) {
                ref = line[pos_annot_index_of_ref];
            } else {
                ref = null;
            }
            if (annotationHasAlt) {
                alt = line[pos_annot_index_of_alt];
            } else {
                alt = null;
            }
        }

        private void setFromLineInterval(String[] line) {
            chrom = line[int_annot_index_of_chr];
            if (!chrom.toLowerCase().startsWith("chr")) {
                chrom = "chr" + VariantUtils.homogenizeSequence(chrom);
            }
            start = Integer.parseInt(line[int_annot_index_of_start]);
            end = Integer.parseInt(line[int_annot_index_of_end]);
            ref = null;
            alt = null;
        }

        @Override
        public String toString() {
            if (isInterval) {
                return "SimpleAnnotationRecord{" + "chrom=" + chrom + ", start=" + start + ", end=" + end + '}';
            } else {
                return "SimpleAnnotationRecord{" + "chrom=" + chrom + ", position=" + position + ", ref=" + ref + ", alt=" + alt + '}';
            }
        }

        private boolean matchesRef(String ref) { //doon't ever depend on ref for matching.
            return true;

            //If annotation ref is 0, then automatically assume the ref matches.
            //(i.e. we interpret the 0 as 'unspecified').            
            /*if(ref.equals("0") && !ref0_logged){
             ref0_logged = true;
             LOG.info("Reference 0 detected for annotation "+annotation.getProgram());
             }
             return (this.alt == null) || (ref.equals("0")) || (this.ref != null && this.ref.equals(ref));*/
        }

        private boolean matchesAlt(String alt) {
            return this.alt == null || (this.alt != null && this.alt.equals(alt));
        }

        private boolean intersectsPosition(String chrom, long start, long end) {
            if (this.chrom.equals(chrom)) {
                if (this.start < start) {
                    if (this.end < start) {
                        return false;
                    } else {
                        return true;
                    }
                } else if (start < this.start) { //start >= start
                    if (end < this.end) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return true;

            }
            return false;
            /*return (!isInterval && this.chrom.equals(chrom) && this.position == position)
             || (isInterval && this.start <= position && (isEndInclusive ? (this.end >= position) : (this.end > position)));*/
        }

        private boolean matchesVariant(GenomicVariant r) {
            return this.chrom.equals(r.getChrom()) && (this.start == r.getStartPosition()) && (this.end == r.getEndPosition()) && matchesAlt(r.getAlt());
            /*return (isInterval && intersectsPosition(r.chrom, r.start, r.end))
             || (!isInterval && intersectsPosition(r.chrom, r.start, r.end) && matchesRef(r.ref) && matchesAlt(r.alt));*/
        }
    }

    public void cleanup() throws IOException {
        reader.cleanup();
    }
}
