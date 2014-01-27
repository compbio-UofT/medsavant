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
import java.util.ArrayList;
import java.util.List;
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
    }
    SimpleVariantRecord lastVariantAnnotated;
    SimpleAnnotationRecord lastAnnotationConsidered;
    String[] lastResult;
    private final int MAX_BASEPAIR_DISTANCE_IN_WINDOW = 10000;
    private final boolean REQUIRE_EXACT_MATCH = true;

    private String[] getVariantAnnotationString(SimpleVariantRecord variant, SimpleAnnotationRecord annotation, String[] annotationLine) {
        String prefix = "";
        String[] result = new String[getNumNonDefaultFields()];

        // the number of redundant and non-redundant columns
        int numColumnsToCopy = getNumNonDefaultFields();
        int numRedundantColumns = annotationLine.length - numColumnsToCopy;

        // copy only the non-redundant columns
        for (int i = 0; i < numColumnsToCopy; i++) {
            // tricky, skip the redundant columns
            result[i] = prefix + annotationLine[numRedundantColumns + i];
        }
        return result;
    }

    //Annotates the first numRecords variant records given in 'records'.  
    //Precondition: All records have the same chromosome, and are ordered by position in ascending order.
    String[][] annotateVariants(SimpleVariantRecord[] records, long start, long end, int numRecords) {
        int numAnnots = 0;       
        long queryStart = start;
        long queryEnd = Math.min(start + MAX_BASEPAIR_DISTANCE_IN_WINDOW, end);

        String[][] results = new String[numRecords][];

        do {
            //LOG.info("\tProcessing range " + queryStart + " - " + queryEnd + " inclusive.");
            
            //find all annotations that lie within the current basepair window.
            if (!canAnnotateThisChromosome(records[0].chrom)) {
                return null;
            }

            TabixReader.Iterator it
                    = reader.query(reader.chr2tid(records[0].chrom),
                            (int) queryStart - 1, //this function returns annotations AFTER this position, so we need to have the -1
                            (int) queryEnd);
            if (it == null) {
                //LOG.info("Didn't find any annotations between " + (queryStart - 1) + " and " + queryEnd);
            } else {
                try {
                    boolean first;
                    int vi = 0;
                    int l = 0;
                    while (it.hasNext()) { //For each annotation in start + min(start+max_base_pair_distance_in_window, end)
                        String annotationLineStr = (String) it.next();
                        String[] annotationLine = removeNewLinesAndCarriageReturns(annotationLineStr).split(VariantManagerUtils.FIELD_DELIMITER, -1);
                        SimpleAnnotationRecord annotationRecord = new SimpleAnnotationRecord(annotationLine);
                        first = true;
                        while (vi < numRecords) { //for each variant                           
                            if (annotationRecord.intersectsPosition(records[vi].chrom, records[vi].start, records[vi].end)) {
                                if (!REQUIRE_EXACT_MATCH || annotationRecord.matchesVariant(records[vi])) {
                                    results[vi] = getVariantAnnotationString(records[vi], annotationRecord, annotationLine);
                                    //results.add(getVariantAnnotationString(records[vi], annotationRecord, annotationLine));
                                }
                                //If this is the first annotation that intersects a variant, we're guaranteed
                                //the future annotations don't need to consider variants to the left of the current
                                //variant index, vi.
                                if (first) {
                                    first = false;
                                    l = vi;
                                }
                            }

                            if (records[vi].start > annotationRecord.end) {
                                vi = l;
                                break;
                            }
                            vi++;
                        }
                        numAnnots++;
                    }
                    //LOG.info("\tProcessed " + numAnnots);
                } catch (IOException ioex) {
                    //TODO: Change.
                    LOG.error(ioex);
                }
            }          
            queryStart = queryEnd + 1; 
            queryEnd = Math.min(queryStart + MAX_BASEPAIR_DISTANCE_IN_WINDOW, end);
        } while (queryStart < queryEnd);

        return results;
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
            if (start == 52238) {
                LOG.info("Variant: chrom=" + chrom + " start=" + start + " end=" + end + "; Annotation=" + this.chrom + " start=" + this.start + " end=" + this.end + " ref=" + this.ref + " alt=" + this.alt);
            }
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
