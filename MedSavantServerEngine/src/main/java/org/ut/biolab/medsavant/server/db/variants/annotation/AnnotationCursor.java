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

import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broad.tabix.TabixReader;
import org.ut.biolab.medsavant.server.db.variants.annotation.BatchVariantAnnotator.SimpleVariantRecord;
import org.ut.biolab.medsavant.server.db.variants.VariantManagerUtils;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
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
    // are the endpoints inclusive (only relevant for interval annotations)
    private final boolean isEndInclusive;
    // the number of fields (not including standard ones like chr, pos, ref, alt)
    private final int numNonDefaultFields;
    // a delimiter for cases where multiple annotations can be provided for a single variant
    private final char MULTI_ANNOTATION_DELIM = ',';
    // the annotation to apply
    private final Annotation annotation;
    //Setup default column indices for required columns.  These can be overridden
    //if the file has a header
    private int pos_annot_index_of_chr = 0;
    private int pos_annot_index_of_pos = 1;
    private int pos_annot_index_of_ref = 2;
    private int pos_annot_index_of_alt = 3;
    private int int_annot_index_of_chr = 0;
    private int int_annot_index_of_start = 1;
    private int int_annot_index_of_end = 2;
    private boolean ref0_logged = false;

    /**
     * A file reader and cursor to be used to help in the annotation process
     *
     * @param sid The Session ID that requested the annotation
     * @param annotation The annotation that this instance refers to
     * @throws IOException
     * @throws SQLException
     */
    public AnnotationCursor(String sid, Annotation annotation) throws IOException, SQLException, SessionExpiredException, IllegalArgumentException {
        TabixReader headerReader = new TabixReader(annotation.getDataPath());
        String header = headerReader.readLine().trim();
        headerReader.cleanup();

        reader = new TabixReader(annotation.getDataPath());


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
                if (parts[i].equals("CHR") || parts[i].equals("CHROM")) {
                    pos_annot_index_of_chr = int_annot_index_of_chr = i;
                } else if (parts[i].equals("START")) {
                    int_annot_index_of_start = i;
                    pos_annot_index_of_pos = i;
                } else if (parts[i].equals("END")) {
                    int_annot_index_of_end = i;
                } else if (parts[i].equals("REF")) {
                    pos_annot_index_of_ref = i;
                } else if (parts[i].equals("ALT")) {
                    pos_annot_index_of_alt = i;
                } else if (parts[i].equals("POSITION")) {
                    pos_annot_index_of_pos = i;
                }
            }

            String missingCol = null;
            if (annotation.isInterval()) {
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
        annotationHasRef = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).hasRef();
        annotationHasAlt = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).hasAlt();
        isInterval = annotation.isInterval();
        isEndInclusive = annotation.isEndInclusive();
        numNonDefaultFields = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).getNumNonDefaultFields();


        String references = "";
        for (String s : reader.getReferenceNames()) {
            references += ", " + s;
        }
        // log
        /*LOG.info("Setting up Annotation Cursor for " + annotation);
         LOG.info("  has reference:\t" + annotationHasRef);
         LOG.info("  has alternate:\t" + annotationHasAlt);
         LOG.info("  is interval:\t" + isInterval);
         LOG.info("  is end inclusive:\t" + isEndInclusive);
         LOG.info("  references " + references.substring(2, references.length()));
         */

    }
    SimpleVariantRecord lastVariantAnnotated;
    SimpleAnnotationRecord lastAnnotationConsidered;
    String[] lastResult;
    private final int MAX_BASEPAIR_DISTANCE_IN_WINDOW = 10000;

    //Annotates the first numRecords variant records given in 'records'.  
    //Precondition: All records have the same chromosome, are SNPs, and are ordered by position in ascending order.
    String[][] annotateSnps(SimpleVariantRecord[] records, int numRecords) {
        long variantStart = records[0].start;
        long variantEnd = records[numRecords-1].end;
        
        do {
            //find all annotations that lie within the current basepair window.
            if (!canAnnotateThisChromosome(records[0].chrom)) {
                return null;
            }

            TabixReader.Iterator it =
                    reader.query(reader.chr2tid(records[0].chrom),
                    (int) variantStart - 1,
                    Integer.MAX_VALUE); //this function returns variants AFTER this position, so we need to have the -1

            variantStart += MAX_BASEPAIR_DISTANCE_IN_WINDOW;
            variantEnd = Math.min(variantStart + MAX_BASEPAIR_DISTANCE_IN_WINDOW, records[numRecords - 1].end);
        } while (variantStart < variantEnd);


        if (variantEnd - variantStart) {
        }
        return null;
    }

    /**
     * Retrieve the annotation for this variant, if any. If an annotation does
     * not exist, an empty string array of size getNumNonDefaultFields will be
     * returned.
     *
     * @param r The genomic variant to annotate
     * @return An array of annotations
     */
    String[] annotateVariant(SimpleVariantRecord r) throws IOException {

        //if set, an annotation must overlap and not merely intersect a variant
        //in order for that variant to be annotated.
        boolean requireExactMatch = true;

        // short circuit when there are gaps between the variant and the next
        // annotation, return a blank annotation
        if (lastAnnotationConsidered != null
                && r.chrom.equals(lastAnnotationConsidered.chrom)
                && r.end < lastAnnotationConsidered.start) {
            lastVariantAnnotated = r;
            lastResult = new String[getNumNonDefaultFields()]; // this annotation is empty
            return lastResult;
        }

        // try to reuse the previous annotation
        if (lastVariantAnnotated != null && lastVariantAnnotated.equals(r)) {
            lastVariantAnnotated = r;
            return lastResult; // resuse the last annotation
        }

        // the annotation
        String[] result = new String[getNumNonDefaultFields()];

        // check if there are annotations for this chromosome
        if (canAnnotateThisChromosome(r.chrom)) {

            // seek to the appropriate record by genetic position            
            TabixReader.Iterator it = reader.query(
                    reader.chr2tid(r.chrom),
                    (int) r.start - 1, // the function returns matches AFTER
                    // this position, so we need to have the -1
                    Integer.MAX_VALUE);

            int numberIntersectingThisVariant = 0;
            int numberMatchingThisVariant = 0;

            // loop until the annotations no longer intersect the variant
            // intersect for positional annotations imply a exact match of chrom and pos
            // intersect for interval annotations imply containment
            while (true) {

                // nothing there
                if (it == null) {
                    //LOG.warn("NULL iterator; offending variant is " + r);
                    break;
                }
                String annotationLineString = it.next();
                if (annotationLineString == null) {
                    //LOG.warn("NULL annotation; offending variant is " + r);
                    break;
                }

                // get the next annotation and parse it
                String[] annotationLine = removeNewLinesAndCarriageReturns(annotationLineString).split(VariantManagerUtils.FIELD_DELIMITER, -1);
                SimpleAnnotationRecord annotationRecord = new SimpleAnnotationRecord(annotationLine);

                // save this annotation
                lastAnnotationConsidered = annotationRecord;


                // does this annotation intersect the variant position
                if (annotationRecord.intersectsPosition(r.chrom, r.start, r.end)) {

                    // keep track of intersections
                    numberIntersectingThisVariant++;

                    // if there is a match, add it to the results
                    if (requireExactMatch && annotationRecord.matchesVariant(r)) {

                        String prefix = "";

                        // the number of redundant and non-redundant columns
                        int numColumnsToCopy = getNumNonDefaultFields();
                        int numRedundantColumns = annotationLine.length - numColumnsToCopy;

                        // copy only the non-redundant columns
                        for (int i = 0; i < numColumnsToCopy; i++) {
                            // tricky, skip the redundant columns
                            result[i] = prefix + annotationLine[numRedundantColumns + i];
                        }


                        // track the number of matches
                        numberMatchingThisVariant++;
                        break;
                    }

                    // if the next annotation doesn't intersect, break out
                } else {
                    break;
                }
            }

            // log
            //LOG.info(numberIntersectingThisVariant + " annotations intersecting variant");
            //LOG.info(numberMatchingThisVariant + " annotations matching variant");
        }

        // save the result and return
        lastVariantAnnotated = r;
        lastResult = result;
        return result;
    }

    /**
     * Get the number of fields (not including standard ones like chr, pos, ref,
     * alt)
     *
     * @return the number of non default fields
     */
    public int getNumNonDefaultFields() {
        return numNonDefaultFields;
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

    /**
     * Get the annotation that this cursor works for
     *
     * @return The annotation this cursor works for
     */
    Annotation getAnnotation() {
        return annotation;
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
                chrom = "chr" + MiscUtils.homogenizeSequence(chrom);
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
                chrom = "chr" + MiscUtils.homogenizeSequence(chrom);
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

        private boolean matchesVariant(SimpleVariantRecord r) {
            //return this.chrom.equals(r.chrom) && (this.start == r.start) && (this.end == r.end);

            return (isInterval && intersectsPosition(r.chrom, r.start, r.end))
                    || (!isInterval && intersectsPosition(r.chrom, r.start, r.end) && matchesRef(r.ref) && matchesAlt(r.alt));
        }
    }

    public void cleanup() throws IOException {
        reader.cleanup();
    }
}
