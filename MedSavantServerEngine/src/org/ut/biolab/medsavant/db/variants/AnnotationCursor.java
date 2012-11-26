package org.ut.biolab.medsavant.db.variants;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broad.tabix.TabixReader;
import org.ut.biolab.medsavant.db.variants.BatchVariantAnnotator.SimpleVariantRecord;
import org.ut.biolab.medsavant.model.Annotation;
import org.ut.biolab.medsavant.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.util.MiscUtils;

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
    // the number of fields (not including standard ones like chr, pos, ref, alt)
    private final int numNonDefaultFields;
    // a delimiter for cases where multiple annotations can be provided for a single variant
    private final char MULTI_ANNOTATION_DELIM = ',';
    // the annotation to apply
    private final Annotation annotation;

    /**
     * A file reader and cursor to be used to help in the annotation process
     *
     * @param sid The Session ID that requested the annotation
     * @param annotation The annotation that this instance refers to
     * @throws IOException
     * @throws SQLException
     */
    public AnnotationCursor(String sid, Annotation annotation) throws IOException, SQLException {
        reader = new TabixReader(annotation.getDataPath());

        this.annotation = annotation;
        annotationHasRef = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).hasRef();
        annotationHasAlt = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).hasAlt();
        isInterval = annotation.isInterval();
        numNonDefaultFields = AnnotationManager.getInstance().getAnnotationFormat(sid, annotation.getID()).getNumNonDefaultFields();

        // log
        LOG.info("Setting up Annotation Cursor for " + annotation);
        LOG.info("  has reference:\t" + annotationHasRef);
        LOG.info("  has alternate:\t" + annotationHasAlt);
        LOG.info("  is interval:\t" + isInterval);

        String references = "";
        for (String s : reader.getReferenceNames()) {
            references += ", " + s;
        }
        LOG.info("  references " + references.substring(2, references.length()));

    }
    SimpleVariantRecord lastVariantAnnotated;
    SimpleAnnotationRecord lastAnnotationConsidered;
    String[] lastResult;

    /**
     * Retrieve the annotation for this variant, if any. If an annotation does
     * not exist, an empty string array of size getNumNonDefaultFields will be
     * returned.
     *
     * @param r The genomic variant to annotate
     * @return An array of annotations
     */
    public String[] annotateVariant(SimpleVariantRecord r) throws IOException {

        // short circuit when there are gaps between the variant and the next
        // annotation, return a blank annotation
        if (lastAnnotationConsidered != null
                && r.chrom.equals(lastAnnotationConsidered.chrom)
                && r.position < lastAnnotationConsidered.position) {
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
                    r.position - 1, // the function returns matches AFTER
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
                String[] annotationLine = removeNewLinesAndCarriageReturns(annotationLineString).split("\t");
                SimpleAnnotationRecord annotationRecord = new SimpleAnnotationRecord(annotationLine);

                // save this annotation
                lastAnnotationConsidered = annotationRecord;

                //LOG.info("Comparing " + annotation + " to " + r);

                // does this annotation intersect the variant position
                if (annotationRecord.intersectsPosition(r.chrom, r.position)) {

                    // keep track of intersections
                    numberIntersectingThisVariant++;

                    // if there is a match, add it to the results
                    if (annotationRecord.matchesVariant(r)) {

                        String prefix = "";

                        // add a delimiter for >1st match
                        //if (numberMatchingThisVariant != 0) {
                        //    prefix = MULTI_ANNOTATION_DELIM + " ";
                        //}

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
     * @return The annotation this cursor works for
     */
    Annotation getAnnotation() {
        return annotation;
    }

    private class SimpleAnnotationRecord {

        private static final int POS_ANNOT_INDEX_OF_CHR = 0;
        private static final int POS_ANNOT_INDEX_OF_POS = 1;
        private static final int POS_ANNOT_INDEX_OF_REF = 2;
        private static final int POS_ANNOT_INDEX_OF_ALT = 3;
        private static final int INT_ANNOT_INDEX_OF_CHR = 0;
        private static final int INT_ANNOT_INDEX_OF_START = 1;
        private static final int INT_ANNOT_INDEX_OF_END = 2;
        public String chrom;
        public int position;
        public int start;
        public int end;
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
            chrom = line[POS_ANNOT_INDEX_OF_CHR];
            position = Integer.parseInt(line[POS_ANNOT_INDEX_OF_POS]);
            if (annotationHasRef) {
                ref = line[POS_ANNOT_INDEX_OF_REF];
            } else {
                ref = null;
            }
            if (annotationHasAlt) {
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

        @Override
        public String toString() {
            if (isInterval) {
                return "SimpleAnnotationRecord{" + "chrom=" + chrom + ", start=" + start + ", end=" + end + '}';
            } else {
                return "SimpleAnnotationRecord{" + "chrom=" + chrom + ", position=" + position + ", ref=" + ref + ", alt=" + alt + '}';
            }
        }

        public boolean matchesRef(String ref) {
            return this.ref != null && this.ref.equals(ref);
        }

        public boolean matchesAlt(String alt) {
            return this.alt != null && this.alt.equals(alt);
        }

        private boolean intersectsPosition(String chrom, int position) {
            return (!isInterval && this.chrom.equals(chrom) && this.position == position)
                    || (isInterval && this.start <= position && this.end > position);
        }

        private boolean matchesVariant(SimpleVariantRecord r) {
            return (isInterval && intersectsPosition(r.chrom, r.position))
                    || (!isInterval && intersectsPosition(r.chrom, r.position) && matchesRef(r.ref) && matchesAlt(r.alt));
        }
    }
}
