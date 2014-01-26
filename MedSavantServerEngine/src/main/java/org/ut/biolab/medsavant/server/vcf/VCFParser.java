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
package org.ut.biolab.medsavant.server.vcf;

import com.google.code.externalsorting.ExternalSort;
import java.io.*;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;

import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume
 */
public class VCFParser {

    private static final Log LOG = LogFactory.getLog(VCFParser.class);
    private static final String HEADER_CHARS = "#";
    private static final String COMMENT_CHARS = "##";
    private static final Pattern VCF_FORMAT_REGEX = Pattern.compile("^##fileformat=VCFv([\\d+.]+)");
    private static final int VCF_START_INDEX = 1;
    private static final int VCF_ID_INDEX = 2;
    private static final int VCF_REF_INDEX = 3;
    private static final int VCF_ALT_INDEX = 4;
    private static final int VCF_QUALITY_INDEX = 5;
    private static final int VCF_FILTER_INDEX = 6;
    private static final int VCF_INFO_INDEX = 7;
    private static final int VCF_FORMAT_INDEX = 8;
    private static final int VCF_SAMPLE_START_INDEX = 9;
    private static final Pattern VCF_BADREF_REGEX = Pattern.compile("[^ACGTacgt]");
    private static final Pattern VCF_SNP_REGEX = Pattern.compile("^[ACGTacgt]");
    private static final Pattern VCF_BADALT_REGEX = VCF_BADREF_REGEX;
    private static final Pattern VCF_ALT_OLD_1000G_REGEX = Pattern.compile("^<.+>$");

    //numbers or dots delimited by pipes or slashes.
    //private static final Pattern VCF_GT_REGEX = Pattern.compile("([\\d.\\.])([/|]([\\d.\\.]))*");
    private static final Pattern VCF_GT_REGEX = Pattern.compile("([\\d.])(([/|])([\\d.]))*");
    private int lineNumber = 0;
    private int numInvalidRef = 0;
    private int numInvalidAlt = 0;
    private int numSnp = 0;
    private int numTi = 0; //mismatched base pairs in SNPs
    private int numTv = 0; //matches base pairs in SNPs
    private int numIndels = 0;
    private int numSnp1 = 0;
    private int numTi1 = 0; //mismatched base pairs in SNPs
    private int numTv1 = 0; //matches base pairs in SNPs
    private int numIndels1 = 0;
    private int numInvalidGT = 0;
    private int numHom = 0;
    private int numHet = 0;

    private String sessID;
    private File vcfFile;

    public VCFParser(String sessID, File vcfFile) {
        this.sessID = sessID;
        this.vcfFile = vcfFile;
    }

    public int getNumInvalidRef() {
        return numInvalidRef;
    }

    public int getNumInvalidAlt() {
        return numInvalidAlt;
    }

    public int getNumSnp() {
        return numSnp;
    }

    public int getNumTi() {
        return numTi;
    }

    public int getNumTv() {
        return numTv;
    }

    public int getNumIndels() {
        return numIndels;
    }

    public int getNumSnp1() {
        return numSnp1;
    }

    public int getNumTi1() {
        return numTi1;
    }

    public int getNumTv1() {
        return numTv1;
    }

    public int getNumIndels1() {
        return numIndels1;
    }

    public int getNumInvalidGT() {
        return numInvalidGT;
    }

    public int getNumHom() {
        return numHom;
    }

    public int getNumHet() {
        return numHet;
    }

    public int parseVariantsFromReader(BufferedReader r, File outfile, int updateId, int fileId) throws IOException {
        return parseVariantsFromReader(r, outfile, updateId, fileId, false);
    }

    private Map<String, BufferedWriter> chromOutOfOrderFileMap = new HashMap<String, BufferedWriter>();

    private void writeOutOfOrderLine(String chrom, String[] line, String prefix) throws IOException {
        BufferedWriter handle = chromOutOfOrderFileMap.get(chrom);
        if (handle == null) {
            handle = new BufferedWriter(new FileWriter(prefix + "_" + chrom, true));
            chromOutOfOrderFileMap.put(chrom, handle);
        }

    }

    /**
     * Like parseVariantsFromReader, but use a pre-parsed header object (useful
     * when reusing a header)
     *
     * @param r A reader for .vcf file being parsed
     * @param header The VCF header object to use. If header == null, the first
     * header line in the file will be used
     * @param outputLinesLimit The number of lines to write to output before
     * returning
     * @param outfile The temporary output file, with variants parsed 1 per
     * position per individual
     * @param updateId The updateId to prepend to each line
     * @param fileId The fileId to prepend to each line
     * @return number of lines written to outfile
     * @throws IOException
     */
    public int parseVariantsFromReader(BufferedReader r, File outfile, int updateId, int fileId, boolean includeHomoRef) throws IOException {

        VCFHeader header = null;

        String nextLineString;
        int numRecords = 0;

        long previousPosition = -1;
        String previousChrom = "";

        final String outOfOrderFilename = outfile.getAbsolutePath() + "_ooo";
        BufferedWriter out = new BufferedWriter(new FileWriter(outfile, true));
        BufferedWriter outOfOrderHandle = null;

        int variantId = 0;
        int numLinesWritten = 0;

        while (true) {

            if ((nextLineString = r.readLine()) == null) {
                LOG.info("Reader returned null after " + numLinesWritten + " lines.");
                break;
            }
            lineNumber++;
            nextLineString = nextLineString.trim();

            //skip blank lines.
            if (nextLineString.length() == 0) {
                continue;
            }

            if (numRecords % 100000 == 0 && numRecords != 0) {
                LOG.info("Processed " + numRecords + " lines (" + numLinesWritten + " variants) so far...");
            }

            String[] nextLine = nextLineString.split("\t");

            if (nextLine[0].startsWith(COMMENT_CHARS)) {
                //Check for VCF vesion 4.
                Matcher vcf_format_matcher = VCF_FORMAT_REGEX.matcher(nextLineString);
                if (vcf_format_matcher.find()) {
                    String ver = vcf_format_matcher.group(1);
                    if (Float.parseFloat(ver) < 4.0) {
                        throw new IllegalArgumentException("VCF version (" + ver + ") is older than version 4");
                    }
                }
            } else if (nextLine[0].startsWith(HEADER_CHARS)) {
                // header line
                header = parseHeader(nextLine);
            } else {

                if (header == null) {
                    throw new IOException("Cannot parse headless VCF file");
                }

                // a data line
                List<VariantRecord> records = null;
                try {
                    records = parseRecord(nextLine, header);
                    if (records == null) {
                        continue;
                    }
                } catch (Exception ex) {
                    LOG.error("Erroneous line: " + nextLineString);
                    throw new IOException(ex);
                }
                //add records to tdf
                for (VariantRecord v : records) {
                    if (includeHomoRef || v.getZygosity() != Zygosity.HomoRef) {
                        if (previousChrom.equals(v.getChrom()) && v.getStartPosition() < previousPosition) {
                            if (outOfOrderHandle == null) {
                                outOfOrderHandle = new BufferedWriter(new FileWriter(outOfOrderFilename, true));
                            }

                            outOfOrderHandle.write(v.toTabString(updateId, fileId, variantId));
                            outOfOrderHandle.write("\r\n");
                            //out of order!
                        } else {
                            out.write(v.toTabString(updateId, fileId, variantId));
                            out.write("\r\n");
                        }
                        numLinesWritten++;
                        variantId++;
                        previousPosition = v.getStartPosition();
                        previousChrom = v.getChrom();
                    }
                }
                numRecords++;
            }//end else
        }
        out.close();

        if (outOfOrderHandle != null) {
            outOfOrderHandle.close();
            LOG.info("Some variants were out of order, sorting and merging...");
            //sort the out of order file.            
            mergeUnsortedTDFIntoSortedTDF(outOfOrderFilename, outfile);
        }

        return numLinesWritten;
    }

    private final static int EXTERNALSORT_MAX_TMPFILES = 128;
    private final static Charset EXTERNALSORT_CHARSET = Charset.defaultCharset();

    private final static int TDF_INDEX_OF_CHROM = 4;
    private final static int TDF_INDEX_OF_STARTPOS = 5;

    //Uses ExternalSort so that it can be used with very large files.    
    static void mergeUnsortedTDFIntoSortedTDF(String unsortedTDF, File sortedTDF) throws IOException {
        final boolean eliminateDuplicateRows = false;
        final int numHeaderLinesToExcludeFromSort = 0;
        //should stay false, due to batch.add(sortedTDF).  (can't mix gzipped
        //and non-gzipped files).
        final boolean useGzipForTmpFiles = false;

        //Sorts by chromosome, then position.
        Comparator comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] tokens1 = o1.split("\t");
                String[] tokens2 = o2.split("\t");

                String chr1 = tokens1[TDF_INDEX_OF_CHROM].toLowerCase();
                String chr2 = tokens2[TDF_INDEX_OF_CHROM].toLowerCase();

                //Removed these checks - assume chromosomes are quoted.
                /*
                 if(chr1.startsWith("\"") && chr1.endsWith("\"")){
                 chr1 = chr1.substring(1, chr1.length()-1);                    
                 }
                 if(chr2.startsWith("\"") && chr2.endsWith("\"")){
                 chr1 = chr1.substring(1, chr1.length()-1);                    
                 }*/
                chr1 = chr1.substring(1, chr1.length() - 1);
                chr2 = chr2.substring(1, chr2.length() - 1);

                if (chr1.startsWith("chr")) {
                    chr1 = chr1.substring(3);
                }
                if (chr2.startsWith("chr")) {
                    chr2 = chr2.substring(3);
                }

                if (chr1.equals(chr2)) {
                    //assume positions are also quoted
                    long pos1 = Long.parseLong(tokens1[TDF_INDEX_OF_STARTPOS].substring(1, tokens1[TDF_INDEX_OF_STARTPOS].length() - 1));
                    long pos2 = Long.parseLong(tokens2[TDF_INDEX_OF_STARTPOS].substring(1, tokens2[TDF_INDEX_OF_STARTPOS].length() - 1));

                    if (pos1 < pos2) {
                        return -1;
                    } else if (pos1 > pos2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    
                    int c1 = NumberUtils.isDigits(chr1) ? Integer.parseInt(chr1) : (int) chr1.charAt(0);
                    int c2 = NumberUtils.isDigits(chr2) ? Integer.parseInt(chr2) : (int) chr2.charAt(0);
                    return (c1 < c2) ? -1 : ((c1 > c2) ? 1 : 0);
                }
            }
        };

        //Sort the file, producing up to EXTERNALSORT_MAX_TMPFILES temproary 
        //files.
        List<File> batch = ExternalSort.sortInBatch(
                new File(unsortedTDF),
                comparator,
                EXTERNALSORT_MAX_TMPFILES,
                EXTERNALSORT_CHARSET,
                new File(sortedTDF.getParent()),
                eliminateDuplicateRows,
                numHeaderLinesToExcludeFromSort,
                useGzipForTmpFiles);

        //Merge the temporary files with each other and with sortedTDF
        batch.add(sortedTDF);
        String finalOutputFileName = sortedTDF.getCanonicalPath();
        File outputFile = new File(finalOutputFileName + "_MERGED");

        ExternalSort.mergeSortedFiles(batch, outputFile, comparator, EXTERNALSORT_CHARSET,
                eliminateDuplicateRows, false, useGzipForTmpFiles);

        if (!outputFile.renameTo(sortedTDF)) {
            throw new IOException("Can't rename merged file " + outputFile.getCanonicalPath() + " to " + sortedTDF.getCanonicalPath());
        }
    }

    static BufferedReader openFile(File vcf) throws FileNotFoundException, IOException {
        if (vcf.getAbsolutePath().endsWith(".gz")) {
            return new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(vcf)));
        } else {
            return new BufferedReader(new FileReader(vcf));
        }
    }

    private VCFHeader parseHeader(String[] headerLine) {
        VCFHeader result = new VCFHeader();
        // has genotype information
        if (headerLine.length > VCFHeader.getNumMandatoryFields()) {
            // get the genotype labels
            for (int i = VCFHeader.getNumMandatoryFields() + 1; i < headerLine.length; i++) {
                if (headerLine[i] != null && headerLine[i].length() != 0) {
                    result.addGenotypeLabel(headerLine[i]);
                }
            }
        }

        return result;
    }

    private void messageToUser(LogManagerAdapter.LogType logtype, String msg) {
        try {
            org.ut.biolab.medsavant.server.serverapi.LogManager.getInstance().addServerLog(
                    sessID,
                    logtype,
                    msg);
            LOG.info("sessId=" + sessID + " " + msg);
        } catch (RemoteException re) {
            LOG.error(re);
            LOG.error("WARNING: Couldn't log warning due to RemoteException.  Warning: " + msg);
        } catch (SessionExpiredException see) {
            LOG.error(see);
            LOG.error("WARNING: Couldn't log warning due to SessionExpiredException.  Warning: " + msg);
        }
    }

    private void vcf_warning(String msg) {
        String warning = vcfFile.getName() + ": WARNING (line " + lineNumber + "): " + msg;
        messageToUser(LogManagerAdapter.LogType.WARNING, warning);
    }

    //These variables control how bad refs are reported or handled.
    private static final boolean TOOLONG_REFS_GIVE_WARNING = true;
    private static final boolean UNRECOGNIZED_REFS_GIVE_WARNING = true; //only matters if above is true.
    private static final boolean TOOLONG_ALTS_GIVE_WARNING = true;
    private static final boolean UNRECOGNIZED_ALTS_GIVE_WARNING = true;

    private List<VariantRecord> parseRecord(String[] line, VCFHeader h) {
        int numMandatoryFields = VCFHeader.getNumMandatoryFields();

        //remove quotes from info field, if necessary.
        if (line[VCF_INFO_INDEX].startsWith("\"") && line[VCF_INFO_INDEX].endsWith("\"")) {
            line[VCF_INFO_INDEX] = line[VCF_INFO_INDEX].substring(1, line[VCF_INFO_INDEX].length() - 1);
        }

        List<String> infos = new ArrayList<String>();
        List<String> ids;

        for (int i = numMandatoryFields; i < line.length; i++) {
            infos.add(line[i]);
        }

        if (infos.isEmpty()) {
            infos.add(".");
            ids = new ArrayList<String>();
            ids.add(".");
        } else {
            ids = h.getGenotypeLabels();
        }

        List<VariantRecord> records = new ArrayList<VariantRecord>();

        int triedIndex = 0;
        try {

            String ref = line[VCF_REF_INDEX].toUpperCase();
            String altStr = line[VCF_ALT_INDEX].toUpperCase();
            long start = 0;
            try {
                start = Long.parseLong(line[VCF_START_INDEX]);
            } catch (NumberFormatException nex) {
                vcf_warning("Invalid (non-numeric) start position detected in VCF4 file: " + line[VCF_START_INDEX]);
                return null;
            }

            if (altStr.equals(".")) { //no real variant call was made at this position
                return null;
            }

            boolean badRef = false;

            //If a ref is unrecognized or too long, we truncate it to 0 and store it anyway.
            //(We never match on ref anyway)
            if (ref.length() > BasicVariantColumns.REF.getColumnLength()) {
                if (TOOLONG_REFS_GIVE_WARNING) {
                    vcf_warning("Detected reference allele with too many characters (maximum is " + BasicVariantColumns.REF.getColumnLength() + ", " + ref.length() + " detected).  Setting ref=0");
                }
                badRef = true;
                ref = "0";
            } else if (VCF_BADREF_REGEX.matcher(ref).find()) { //Unrecognized ref                               
                /*if (ref.length() < 100) { //sometimes extremely long ref contains N nucleotides
                 vcf_warning("Invalid reference ref allele record found in VCF4 file (ACGT expected, found " + ref + ") Setting ref as 0");
                 }*/

                badRef = true;
                if (UNRECOGNIZED_REFS_GIVE_WARNING) {
                    vcf_warning("Unrecognized reference allele found in VCF4 file (ACGT expected, found " + ref + ") Storing anyway.");
                }
            }

            String[] allAlt = altStr.split(","); //there may be multiple alternative alleles

            for (String alt : allAlt) { //process each alternative allele                    
                if (badRef) {
                    ++numInvalidRef;
                }

                //If the ALT sequence is too long, we can't store it, and truncate it down to 10 characters.
                if (alt.length() > BasicVariantColumns.ALT.getColumnLength()) {
                    if (TOOLONG_ALTS_GIVE_WARNING) {
                        vcf_warning("Skipping alternate allele with too many characters (maximum is " + BasicVariantColumns.ALT.getColumnLength() + ", " + alt.length() + " detected).  MedSavant does not yet support sequences of this size.  Storing first 10 characters");
                    }
                    alt = alt.substring(0, Math.min(10, BasicVariantColumns.ALT.getColumnLength()));
                    ++numInvalidAlt;
                    //   continue;
                } else if (VCF_BADALT_REGEX.matcher(alt).find() && !VCF_ALT_OLD_1000G_REGEX.matcher(alt).find()) {//Old 1000g vcf file has <del> as alternative allele    

                    if (UNRECOGNIZED_ALTS_GIVE_WARNING) {
                        vcf_warning("Unrecognized ALT allele detected (ACGT expected, found " + alt + ").  Storing anyway.");
                    }
                    ++numInvalidAlt;
                    //continue;
                }

                boolean snp = false;
                if (ref.length() == alt.length() && VCF_SNP_REGEX.matcher(ref).find() && VCF_SNP_REGEX.matcher(alt).find()) {
                    snp = true;
                    ++numSnp;
                    //annovar counts base pair mismatches (e.g. AG, GA, CT, TC)
                    if ((ref.equals("A") && alt.equals("G"))
                            || (ref.equals("G") && alt.equals("A"))
                            || (ref.equals("C") && alt.equals("T"))
                            || (ref.equals("T") && alt.equals("C"))) {
                        ++numTi;
                    } else {
                        ++numTv;
                    }

                } else {
                    ++numIndels;
                }

                String newAlt;
                String newRef;
                long newStart;
                long newEnd;
                VariantType variantType;

                if (alt.equals("<DEL>")) { //old 1000G vcf files have this.
                    newStart = start;
                    newEnd = start + ref.length() - 1;
                    newRef = ref;
                    newAlt = "-";
                    variantType = VariantType.Deletion;
                } else if (snp) {
                    newStart = start;
                    newEnd = start + ref.length() - 1;
                    newRef = ref;
                    newAlt = alt;
                    variantType = VariantType.SNP;
                } else if (ref.length() >= alt.length()) { //deletion or block substitution
                    String head = ref.substring(0, alt.length());
                    if (head.equals(alt)) {
                        newStart = start + head.length();
                        newEnd = start + ref.length() - 1;
                        newRef = ref.substring(alt.length());
                        newAlt = "-";
                        variantType = VariantType.Deletion;
                    } else {
                        newStart = start;
                        newEnd = start + ref.length() - 1;
                        newRef = ref;
                        newAlt = alt;
                        variantType = VariantType.InDel;
                    }
                } else {// if(ref.length() < alt.length()){ //insertion or block substitution
                    String head = alt.substring(0, ref.length());
                    if (head.equals(ref)) {
                        newStart = start + ref.length() - 1;
                        newEnd = start + ref.length() - 1;
                        newRef = "-";
                        newAlt = alt.substring(ref.length());
                        variantType = VariantType.Insertion;
                    } else {
                        newStart = start;
                        newEnd = start + ref.length() - 1;
                        newRef = ref;
                        newAlt = alt;
                        variantType = VariantType.InDel;
                    }
                }

                VariantRecord variantRecordTemplate = new VariantRecord(line, newStart, newEnd, newRef, newAlt, variantType);

                int indexGT = getIndexGT(line); //index of GT in line

                for (int i = 0; i < ids.size(); i++) { //for each sample.                   
                    VariantRecord sampleVariantRecord = new VariantRecord(variantRecordTemplate);

                    if (indexGT >= 0) {
                        String chunk = line[numMandatoryFields + i + 1];
                        String gt = chunk.split(":")[indexGT];
                        Matcher gtMatcher = VCF_GT_REGEX.matcher(gt);
                        if (!gtMatcher.find() || indexGT < 0) {

                            vcf_warning("SKIPPED VARIANT. Invalid GT field (" + gt.substring(0, Math.min(10, gt.length())) + (gt.length() < 10 ? "" : "...") + ") found in VCF file. cannot determine genotype.");
                            ++numInvalidGT;
                            continue;
                        }

                        //Replaced by 'calculateZygosity'.                
                        /*
                         String a1 = gtMatcher.group(1);
                         String a2 = gtMatcher.group(2);
                         if(a1.equals(".")){ //CG VCF files have many records as ".", probably denoting unknown alleles
                         a1 = "0";
                         }
                         if(a2.equals(".")){
                         a2 = "0";
                         }

                         if(a1.equals("0") && a2.equals("0")){
                         continue; //no mutation in this sample.
                         }else if(a1.equals(a2)){
                         if(a1.equals(Integer.toString(i+1))){
                         sampleVariantRecord.setZygosity(Zygosity.HomoAlt);                            
                         ++numHom;
                         }else{
                         continue;
                         }
                         }else{
                         String x = Integer.toString(i+1);
                         if(!a1.equals(x) && !a2.equals(x)){
                         sampleVariantRecord.setZygosity(Zygosity.Hetero);
                         ++numHet;
                         }
                         }
                         */
                        sampleVariantRecord.setGenotype(gt);
                        sampleVariantRecord.setZygosity(calculateZygosity(sampleVariantRecord.getGenotype()));
                        if (sampleVariantRecord.getZygosity() == Zygosity.Hetero) {
                            ++numHet;
                        } else if (sampleVariantRecord.getZygosity() == Zygosity.HomoAlt) {
                            ++numHom;
                        }
                    }

                    if (snp) {
                        ++numSnp1;
                        //annovar counts base pair mismatches (e.g. AG, GA, CT, TC)
                        if ((ref.equals("A") && alt.equals("G"))
                                || (ref.equals("G") && alt.equals("A"))
                                || (ref.equals("C") && alt.equals("T"))
                                || (ref.equals("T") && alt.equals("C"))) {
                            ++numTi1;
                        } else {
                            ++numTv1;
                        }

                    } else {
                        ++numIndels1;
                    }

                    triedIndex = 0;

                    String id = ids.get(i);
                    sampleVariantRecord.setDnaID(id);

                    // add sample information to the INFO field on this line
                    try {
                        String format = line[VCFHeader.getNumMandatoryFields()].trim();
                        String sampleInfo = line[numMandatoryFields + i + 1];
                        sampleVariantRecord.setSampleInformation(format, sampleInfo);
                    } catch (Exception e) {
                    }

                    records.add(sampleVariantRecord);
                }
            }
        } catch (IllegalArgumentException ex) { //only thrown if chromosome was invalid
            vcf_warning(ex.getMessage());
            return null;
        } catch (Exception ex) {
            String lStr = "";
            for (int i = 0; i < line.length; i++) {
                lStr += line[i] + "\t";
            }
            LOG.info("Tried index " + triedIndex + " of line with " + line.length + " entries");
            String badString = lStr.length() > 300 ? lStr.substring(0, 299) + "..." : lStr;
            LOG.error("Error parsing line " + badString + ": " + ex.getClass() + " " + MiscUtils.getMessage(ex));
            ex.printStackTrace();
        }
        return records;
    }

    private static int getIndexGT(String[] line) {
        if (line.length >= VCFHeader.getNumMandatoryFields() + 1) {
            String[] list = line[VCFHeader.getNumMandatoryFields()].trim().split(":");
            for (int i = 0; i < list.length; i++) {
                if (list[i].equals("GT")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static Zygosity calculateZygosity(String gt) {
        String[] split = gt.split("/|\\\\|\\|"); // splits on / or \ or |
        if (split.length < 2 || split[0] == null || split[1] == null || split[0].length() == 0 || split[1].length() == 0) {
            return null;
        }

        try {
            if (split[0].equals(".") || split[1].equals(".")) {
                return Zygosity.Missing;
            }
            int a = Integer.parseInt(split[0]);
            int b = Integer.parseInt(split[1]);
            if (a == 0 && b == 0) {
                return Zygosity.HomoRef;
            } else if (a == b) {
                return Zygosity.HomoAlt;
            } else if (a == 0 || b == 0) {
                return Zygosity.Hetero;
            } else {
                return Zygosity.HeteroTriallelic;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
