/*
 *    Copyright 2009-2010 University of Toronto
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
package org.ut.biolab.medsavant.model;

import fiume.vcf.VariantRecord;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class VariantRecordModel {

    public static final int INDEX_OF_SAMPLEID = 0;
    public static final int INDEX_OF_CALLDETAILS = INDEX_OF_SAMPLEID + 1;
    public static final int INDEX_OF_CHROM = INDEX_OF_CALLDETAILS + 1;
    public static final int INDEX_OF_POS = INDEX_OF_CHROM + 1;
    public static final int INDEX_OF_ID = INDEX_OF_POS + 1;
    public static final int INDEX_OF_REF = INDEX_OF_ID + 1;
    public static final int INDEX_OF_ALT = INDEX_OF_REF + 1;
    public static final int INDEX_OF_QUAL = INDEX_OF_ALT + 1;
    public static final int INDEX_OF_FILTER = INDEX_OF_QUAL + 1;
    public static final int INDEX_OF_INFO = INDEX_OF_FILTER + 1;
    public static final int INDEX_OF_FORMAT = INDEX_OF_INFO + 1;
    private static final Class CLASS_OF_SAMPLEID = String.class;
    private static final Class CLASS_OF_CALLDETAILS = String.class;
    private static final Class CLASS_OF_CHROM = VariantRecord.CLASS_OF_CHROM;
    private static final Class CLASS_OF_POS = VariantRecord.CLASS_OF_POS;
    private static final Class CLASS_OF_ID = VariantRecord.CLASS_OF_ID;
    private static final Class CLASS_OF_REF = VariantRecord.CLASS_OF_REF;
    private static final Class CLASS_OF_ALT = VariantRecord.CLASS_OF_ALT;
    private static final Class CLASS_OF_QUAL = VariantRecord.CLASS_OF_QUAL;
    private static final Class CLASS_OF_FILTER = VariantRecord.CLASS_OF_FILTER;
    private static final Class CLASS_OF_INFO = VariantRecord.CLASS_OF_INFO;
    private static final Class CLASS_OF_FORMAT = VariantRecord.CLASS_OF_FORMAT;
    private static final int NUM_FIELDS = INDEX_OF_FORMAT + 1; // index of the last field + 1

    public static int getNumberOfFields() {
        return NUM_FIELDS;
    }

    public static Vector convertToVector(VariantRecord r) {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add(r.getSampleID());
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add(r.getCallDetails());
                    break;
                case INDEX_OF_CHROM:
                    v.add(r.getChrom());
                    break;
                case INDEX_OF_POS:
                    v.add(r.getPos());
                    break;
                case INDEX_OF_ID:
                    v.add(r.getId());
                    break;
                case INDEX_OF_REF:
                    v.add(r.getRef());
                    break;
                case INDEX_OF_ALT:
                    v.add(r.getAlt());
                    break;
                case INDEX_OF_QUAL:
                    v.add(r.getQual());
                    break;
                case INDEX_OF_FILTER:
                    v.add(r.getFilter());
                    break;
                case INDEX_OF_INFO:
                    v.add(r.getInfo());
                    break;
                case INDEX_OF_FORMAT:
                    v.add(r.getFormat());
                    break;
            }
        }
        return v;
    }

    public static Vector getFieldClasses() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add(CLASS_OF_SAMPLEID);
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add(CLASS_OF_CALLDETAILS);
                    break;
                case INDEX_OF_CHROM:
                    v.add(CLASS_OF_CHROM);
                    break;
                case INDEX_OF_POS:
                    v.add(CLASS_OF_POS);
                    break;
                case INDEX_OF_ID:
                    v.add(CLASS_OF_ID);
                    break;
                case INDEX_OF_REF:
                    v.add(CLASS_OF_REF);
                    break;
                case INDEX_OF_ALT:
                    v.add(CLASS_OF_ALT);
                    break;
                case INDEX_OF_QUAL:
                    v.add(CLASS_OF_QUAL);
                    break;
                case INDEX_OF_FILTER:
                    v.add(CLASS_OF_FILTER);
                    break;
                case INDEX_OF_INFO:
                    v.add(CLASS_OF_INFO);
                    break;
                case INDEX_OF_FORMAT:
                    v.add(CLASS_OF_FORMAT);
                    break;
            }
        }
        return v;
    }

    public static Vector getFieldNames() {
        Vector v = new Vector();
        for (int i = 0; i < NUM_FIELDS; i++) {
            switch (i) {
                case INDEX_OF_SAMPLEID:
                    v.add("Sample");
                    break;
                case INDEX_OF_CALLDETAILS:
                    v.add("Details");
                    break;
                case INDEX_OF_CHROM:
                    v.add("Chromosome");
                    break;
                case INDEX_OF_POS:
                    v.add("Position");
                    break;
                case INDEX_OF_ID:
                    v.add("ID");
                    break;
                case INDEX_OF_REF:
                    v.add("Reference");
                    break;
                case INDEX_OF_ALT:
                    v.add("Alternate");
                    break;
                case INDEX_OF_QUAL:
                    v.add("Quality");
                    break;
                case INDEX_OF_FILTER:
                    v.add("Filter");
                    break;
                case INDEX_OF_INFO:
                    v.add("Information");
                    break;
                case INDEX_OF_FORMAT:
                    v.add("Format");
                    break;
            }
        }
        return v;
    }

    public static String getFieldNameForIndex(int index) {
        switch (index) {
            case INDEX_OF_SAMPLEID:
                return "Sample";
            case INDEX_OF_CALLDETAILS:
                return "Details";
            case INDEX_OF_CHROM:
                return "Chromosome";
            case INDEX_OF_POS:
                return "Position";
            case INDEX_OF_ID:
                return "ID";
            case INDEX_OF_REF:
                return "Reference";
            case INDEX_OF_ALT:
                return "Alternate";
            case INDEX_OF_QUAL:
                return "Quality";
            case INDEX_OF_FILTER:
                return "Filter";
            case INDEX_OF_INFO:
                return "Information";
            case INDEX_OF_FORMAT:
                return "Format";
            default:
                return null;
        }
    }

    public static int getIndexOfField(String fieldName) {
        return getFieldNames().indexOf(fieldName);
    }

    public static Class getFieldClass(int index) {
        return (Class) getFieldClasses().get(index);
    }

    public static Object getValueOfFieldAtIndex(int keyIndex, VariantRecord r) {
        switch (keyIndex) {
            case INDEX_OF_ALT:
                return r.getAlt();
            case INDEX_OF_CALLDETAILS:
                return r.getCallDetails();
            case INDEX_OF_CHROM:
                return r.getChrom();
            case INDEX_OF_FILTER:
                return r.getFilter();
            case INDEX_OF_FORMAT:
                return r.getFormat();
            case INDEX_OF_ID:
                return r.getId();
            case INDEX_OF_POS:
                return r.getPos();
            case INDEX_OF_QUAL:
                return r.getQual();
            case INDEX_OF_REF:
                return r.getRef();
            case INDEX_OF_SAMPLEID:
                return r.getSampleID();
        }

        return null;
    }

    /*
    private static final Class sc = String.class;
    private static final Class ic = Integer.class;
    private static final Class lc = Long.class;
    private static final Class dc = Double.class;
    private static final Class bc = Boolean.class;
     * 
     */

    /*
    private static Class[] sequenceColumnClasses = { sc };
    private static String[] sequenceColumnNames = { "Sequence" };

    private static Class[] pointColumnClasses = { sc, lc, sc };
    private static String[] pointColumnNames = { "Reference", "Position", "Description" };

    private static Class[] intervalColumnClasses = { sc, lc, lc, sc };
    private static String[] intervalColumnNames = { "Reference", "From", "To", "Description" };

    private static Class[] bamColumnClasses = {   sc,     sc,         ic,         ic,         bc,         bc,         ic,     sc,     sc,         ic,         bc,             ic,                   sc};
    private static String[] bamColumnNames =  {   "Name", "Sequence", "Length",   "Position", "First",    "Strand",   "MQ",   "BQs",  "CIGAR",    "MatePos",  "MateStrand",   "InferredInsertSize", "Attributes" };

    private static Class[] bedColumnClasses = { sc,         lc,     lc,     sc,     dc,      sc,        ic,         ic,         sc,         ic,             sc,         sc  };
    private static String[] bedColumnNames = {"Reference", "Start", "End",  "Name", "Score", "Strand", "ThickStart", "ThickEnd", "ItemRGB", "BlockCount", "BlockSizes", "BlockStarts"};

    private static Class[] continuousColumnClasses = { sc, lc, dc };
    private static String[] continuousColumnNames = { "Reference", "Position", "Value" };
     *
     */
    /*
    public static Vector getDataForTrack(TrackAdapter t) {

    Vector result = new Vector();
    Vector s;

    if (t.getDataInRange() == null) { return result; }

    int id = 1;

    switch(t.getDataSource().getDataFormat()) {
    case SEQUENCE_FASTA:
    for (Record r : t.getDataInRange()) {
    s = new Vector();
    s.add(id++);
    s.add(new String(((SequenceRecord) r).getSequence()));
    result.add(s);
    }
    break;
    case INTERVAL_BAM:
    for (Record r : t.getDataInRange()) {
    BAMIntervalRecord b = (BAMIntervalRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getSamRecord().getReadName());
    s.add(b.getSamRecord().getReadString());
    s.add(b.getSamRecord().getReadLength());
    s.add(b.getSamRecord().getAlignmentStart());
    s.add(b.getSamRecord().getFirstOfPairFlag());
    s.add(!b.getSamRecord().getReadNegativeStrandFlag());
    s.add(b.getSamRecord().getMappingQuality());
    s.add(b.getSamRecord().getBaseQualityString());
    s.add(b.getSamRecord().getCigar());
    s.add(b.getSamRecord().getMateAlignmentStart());
    s.add(!b.getSamRecord().getMateNegativeStrandFlag());
    s.add(b.getSamRecord().getInferredInsertSize());
    String atts = "";
    if (b.getSamRecord().getAttributes().size() > 0) {
    for (SAMTagAndValue v : b.getSamRecord().getAttributes()) {
    atts += v.tag + ":" + v.value + "; ";
    }
    atts.substring(0,atts.length()-2);
    }
    s.add(atts);
    result.add(s);
    }
    break;
    case POINT_GENERIC:
    for (Record r : t.getDataInRange()) {
    GenericPointRecord b = (GenericPointRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getReference());
    s.add(b.getPoint().getPosition());
    s.add(b.getDescription());
    result.add(s);
    }
    break;
    case INTERVAL_GENERIC:
    for (Record r : t.getDataInRange()) {
    GenericIntervalRecord b = (GenericIntervalRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getReference());
    s.add(b.getInterval().getStart());
    s.add(b.getInterval().getEnd());
    s.add(b.getDescription());
    result.add(s);
    }
    break;
    case INTERVAL_BED:
    for (Record r : t.getDataInRange()) {
    BEDIntervalRecord b = (BEDIntervalRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getReference());
    s.add(b.getInterval().getStart());
    s.add(b.getInterval().getEnd());
    s.add(b.getName());
    s.add(b.getScore());
    s.add(b.getStrand());
    s.add(b.getThickStart());
    s.add(b.getThickEnd());
    s.add(b.getItemRGB());
    int numBlocks = b.getBlocks().size();
    s.add(numBlocks);
    String sizes = "";
    String starts = "";
    String[] sizearr = new String[numBlocks];
    String[] startarr = new String[numBlocks];
    List<Block> blocks = b.getBlocks();
    for (int i = 0; i < numBlocks; i++) {
    sizearr[i] = blocks.get(i).getSize() + "";
    startarr[i] = blocks.get(i).getPosition() + "";
    }
    s.add(StringUtils.join(sizearr, ", "));
    s.add(StringUtils.join(startarr, ", "));
    result.add(s);
    }
    break;
    case CONTINUOUS_GENERIC:
    for (Record r : t.getDataInRange()) {
    GenericContinuousRecord b = (GenericContinuousRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getReference());
    s.add(b.getPosition());
    s.add(b.getValue());
    result.add(s);
    }
    break;
    case TABIX:
    for (Record r : t.getDataInRange()) {
    TabixIntervalRecord b = (TabixIntervalRecord) r;
    s = new Vector();
    s.add(id++);
    s.add(b.getReference());
    s.add(b.getInterval().getStart());
    s.add(b.getInterval().getEnd()+1); // tabix intervals are not end-inclusive; our intervals are
    for (String str : b.getOtherValues()) {
    s.add(str);
    }
    result.add(s);
    }
    break;
    default:
    throw new UnsupportedOperationException("");
    }

    return result;
    }
     * 
     */

    /*
    public static Vector getColumnNamesForTrack(TrackAdapter t) {
    Vector result = new Vector();
    result.add("No.");
    switch(t.getDataSource().getDataFormat()) {
    case SEQUENCE_FASTA:
    result.addAll(Arrays.asList(sequenceColumnNames));
    break;
    case INTERVAL_BAM:
    result.addAll(Arrays.asList(bamColumnNames));
    break;
    case INTERVAL_BED:
    result.addAll(Arrays.asList(bedColumnNames));
    break;
    case INTERVAL_GENERIC:
    result.addAll(Arrays.asList(intervalColumnNames));
    break;
    case CONTINUOUS_GENERIC:
    result.addAll(Arrays.asList(continuousColumnNames));
    break;
    case POINT_GENERIC:
    result.addAll(Arrays.asList(pointColumnNames));
    break;
    case TABIX:
    String[] tabixColumnNames = null;
    if (t.getDataInRange().size() > 0) {
    TabixIntervalRecord r = ((TabixIntervalRecord) t.getDataInRange().get(0));
    int numfields = ((TabixIntervalRecord) t.getDataInRange().get(0)).getOtherValues().size();
    tabixColumnNames = new String[3+numfields];
    for (int i = 0; i < numfields; i++) {
    tabixColumnNames[i+3] = "Field" + (i+3+1);
    }
    } else {
    tabixColumnNames = new String[3];
    }
    tabixColumnNames[0] = "Reference";
    tabixColumnNames[1] = "Start";
    tabixColumnNames[2] = "End";
    result.addAll(Arrays.asList(tabixColumnNames));
    break;
    default:
    throw new UnsupportedOperationException(t.getDataSource().getDataFormat() + " is not supported");
    }
    return result;
    }

    public static Vector getColumnClassesForTrack(TrackAdapter t) {
    Vector result = new Vector();
    result.add(Integer.class);

    switch(t.getDataSource().getDataFormat()) {
    case SEQUENCE_FASTA:
    result.addAll(Arrays.asList(sequenceColumnClasses));
    break;
    case INTERVAL_BAM:
    result.addAll(Arrays.asList(bamColumnClasses));
    break;
    case INTERVAL_BED:
    result.addAll(Arrays.asList(bedColumnClasses));
    break;
    case INTERVAL_GENERIC:
    result.addAll(Arrays.asList(intervalColumnClasses));
    break;
    case CONTINUOUS_GENERIC:
    result.addAll(Arrays.asList(continuousColumnClasses));
    break;
    case POINT_GENERIC:
    result.addAll(Arrays.asList(pointColumnClasses));
    break;
    case TABIX:
    Class[] tabixColumnClasses = null;
    if (t.getDataInRange().size() > 0) {
    TabixIntervalRecord r = ((TabixIntervalRecord) t.getDataInRange().get(0));
    int numfields = ((TabixIntervalRecord) t.getDataInRange().get(0)).getOtherValues().size();
    tabixColumnClasses = new Class[3+numfields];
    for (int i = 0; i < numfields; i++) {
    tabixColumnClasses[i+3] = sc;
    }
    } else {
    tabixColumnClasses = new Class[3];
    }
    tabixColumnClasses[0] = sc;
    tabixColumnClasses[1] = lc;;
    tabixColumnClasses[2] = lc;
    result.addAll(Arrays.asList(tabixColumnClasses));
    break;
    default:
    throw new UnsupportedOperationException("");
    }
    return result;
    }
     * 
     */
}
