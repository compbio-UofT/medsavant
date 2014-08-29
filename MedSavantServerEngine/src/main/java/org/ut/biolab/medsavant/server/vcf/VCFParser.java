/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.server.vcf;

import com.google.code.externalsorting.ExternalSort;
import java.io.*;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.samtools.util.BlockCompressedInputStream;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.MedSavantServerJobProgress;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.IOUtils;

import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.VariantType;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume, rammar
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
    private static final Pattern VCF_BADREF_REGEX = Pattern.compile("[^ACGTNacgtn]");
    private static final Pattern VCF_SNP_REGEX = Pattern.compile("^[ACGTNacgtn]");
    private static final Pattern VCF_BADALT_REGEX = Pattern.compile("[^ACGTNacgtn:\\d\\[\\]]");
    private static final Pattern VCF_ALT_OLD_1000G_REGEX = Pattern.compile("^<.+>$");

    private static final int LINES_PER_PROGRESSREPORT = 50000;
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

    private int numVariants = 0;
    private String sessID;
    private File vcfFile;

    private MedSavantServerJobProgress jobProgress;

    public VCFParser(String sessID, File vcfFile, MedSavantServerJobProgress jobProgress) {
        this.sessID = sessID;
        this.vcfFile = vcfFile;
        this.jobProgress = jobProgress;
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

   // private Map<String, BufferedWriter> chromOutOfOrderFileMap = new HashMap<String, BufferedWriter>();
/*
     private void writeOutOfOrderLine(String chrom, String[] line, String prefix) throws IOException {
     BufferedWriter handle = chromOutOfOrderFileMap.get(chrom);
     if (handle == null) {
     handle = new BufferedWriter(new FileWriter(prefix + "_" + chrom, true));
     chromOutOfOrderFileMap.put(chrom, handle);
     }

     }
     */
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

        final String outOfOrderFilename = outfile.getAbsolutePath() + "_ooo";
        BufferedWriter outOfOrderHandle = new BufferedWriter(new FileWriter(outOfOrderFilename, true));

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

            String s = "Processed " + numRecords + " lines (" + numLinesWritten + " variants) so far...";
            jobProgress.setMessage(vcfFile.getName() + " - " + s);
            if (numRecords % 100000 == 0 && numRecords != 0) {
                LOG.info(s);
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
                        outOfOrderHandle.write(v.toTabString(updateId, fileId, variantId));
                        outOfOrderHandle.write("\r\n");
                        numLinesWritten++;
                        variantId++;
                    }
                }
                numRecords++;
            }//end else
        }

        outOfOrderHandle.close();
        jobProgress.setMessage("Sorting variants...");
        LOG.info("sorting out of order handle");
        sortTDF(outOfOrderFilename, outfile);

        return numLinesWritten;
    }

    private final static int EXTERNALSORT_MAX_TMPFILES = 1024;
    private final static Charset EXTERNALSORT_CHARSET = Charset.defaultCharset();

    private final static int TDF_INDEX_OF_CHROM = 4;
    private final static int TDF_INDEX_OF_STARTPOS = 5;

    //Uses ExternalSort so that it can be used with very large files.    
    static void sortTDF(String unsortedTDF, File sortedTDF) throws IOException {
        final boolean eliminateDuplicateRows = false;
        final int numHeaderLinesToExcludeFromSort = 0;
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
        File uf = new File(unsortedTDF);
        BufferedReader fbr = new BufferedReader(new InputStreamReader(
                new FileInputStream(uf), EXTERNALSORT_CHARSET));

        //Use 0.3 * (1/numThreads) * total memory allocated to Java of memory for sorting.
        long maxMem = (long) (0.3 * Runtime.getRuntime().maxMemory() / (double) MedSavantServerEngine.getMaxThreads());

        //...unless that amount of memory would exceed 50% of the available memory, in which case cap
        //memory use at 50% of the available memory.        
        long availMem = ExternalSort.estimateAvailableMemory();
        if (0.50 * availMem < maxMem) {
            maxMem = (long) 0.5 * availMem;
            LOG.info("WARNING: Memory is low for sorting, sorting with reduced memory of " + (maxMem >> 20) + " M");
        } else {
            LOG.info("Sorting using " + (maxMem >> 20) + "M of memory");
        }

        List<File> batch = ExternalSort.sortInBatch(fbr, uf.length(), comparator, EXTERNALSORT_MAX_TMPFILES,
                maxMem, EXTERNALSORT_CHARSET, new File(sortedTDF.getParent()),
                eliminateDuplicateRows,
                numHeaderLinesToExcludeFromSort,
                useGzipForTmpFiles);

        /*
         List<File> batch = ExternalSort.sortInBatch(
         new File(unsortedTDF),
         comparator,
         EXTERNALSORT_MAX_TMPFILES,
         EXTERNALSORT_CHARSET,
         new File(sortedTDF.getParent()),
         eliminateDuplicateRows,
         numHeaderLinesToExcludeFromSort,
         useGzipForTmpFiles);
         */
        //Merge the temporary files with each other
        //batch.add(sortedTDF);
        String finalOutputFileName = sortedTDF.getCanonicalPath();
        File outputFile = new File(finalOutputFileName + "_MERGED");

        ExternalSort.mergeSortedFiles(batch, outputFile, comparator, EXTERNALSORT_CHARSET,
                eliminateDuplicateRows, false, useGzipForTmpFiles);

        if (!IOUtils.moveFile(outputFile, sortedTDF)) {
            throw new IOException("Can't rename merged file " + outputFile.getCanonicalPath() + " to " + sortedTDF.getCanonicalPath());
        } else {
            LOG.info("Outputted sorted TDF file to " + sortedTDF);
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

    private static final long MAX_WARNINGS = 1000;
    private long warningsEmitted = 0;

    private void vcf_warning(String msg) {
        if (warningsEmitted < MAX_WARNINGS) {
            String warning = vcfFile.getName() + ": WARNING (line " + lineNumber + "): " + msg;
            messageToUser(LogManagerAdapter.LogType.WARNING, warning);
        } else if (warningsEmitted == MAX_WARNINGS) {
            String warning = vcfFile.getName() + ": Further warnings have been truncated.";
            messageToUser(LogManagerAdapter.LogType.WARNING, warning);
        }
        warningsEmitted++;
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

            int altNumber = 0;
            for (String alt : allAlt) { //process each alternative allele        
                altNumber++;
                if (badRef) {
                    ++numInvalidRef;
                }

                //handle multi-allelic variants by deleting the common portion of substring.
                boolean complex = (alt.contains("[") || alt.contains("]"));
                boolean snp = false;
                VariantType variantType = VariantType.Unknown;
                long newStart = start;
                String newRef;
                String newAlt;
                long newEnd;

                if (complex) { //complex rearrangement
                    newRef = ref;
                    newAlt = alt;
                    newEnd = newStart;
                    if (newRef.length() == 1) {
                        variantType = VariantType.Complex;
                    } else {
                        vcf_warning("Unrecognized complex rearrangement detected (ref length expected to be 1, found ref=" + newRef + ".  Storing anyway.");
                    }
                } else {

                    String prefix = StringUtils.getCommonPrefix(new String[]{ref, alt});
                    newRef = ref.substring(prefix.length());
                    newAlt = alt.substring(prefix.length());
                    newStart += prefix.length();
                    newEnd = newStart;

                    //If the ALT sequence is too long, we can't store it, and truncate it down to 10 characters.
                    if (newAlt.length() > BasicVariantColumns.ALT.getColumnLength()) {
                        if (TOOLONG_ALTS_GIVE_WARNING) {
                            vcf_warning("Skipping alternate allele with too many characters (maximum is "
                                    + BasicVariantColumns.ALT.getColumnLength() + ", " + newAlt.length()
                                    + " detected).  MedSavant does not yet support sequences of this size.  Storing first 10 characters");
                        }
                        newAlt = newAlt.substring(0, Math.min(10, BasicVariantColumns.ALT.getColumnLength()));
                        ++numInvalidAlt;
                        //   continue;
                    }

                    if (newRef.length() == newAlt.length() && VCF_SNP_REGEX.matcher(newRef).matches() && VCF_SNP_REGEX.matcher(newAlt).matches()) {
                        snp = true;
                        ++numSnp;
                        //annovar counts base pair mismatches (e.g. AG, GA, CT, TC)
                        if ((newRef.equals("A") && newAlt.equals("G"))
                                || (newRef.equals("G") && newAlt.equals("A"))
                                || (newRef.equals("C") && newAlt.equals("T"))
                                || (newRef.equals("T") && newAlt.equals("C"))) {
                            ++numTi;
                        } else {
                            ++numTv;
                        }

                    } else {
                        ++numIndels;
                    }

                    ///??
                    if (newAlt.equals("<DEL>")) { //old 1000G vcf files have this.
                        newEnd = newStart + newRef.length() - 1;
                        newAlt = "-";
                        variantType = VariantType.Deletion;
                    } else if (newAlt.equals(".")) {
                        newAlt = newRef;
                        newEnd = newStart + newRef.length() - 1;
                        variantType = VariantType.HomoRef;
                    } else if (snp) {   //SNP
                        newEnd = newStart + newRef.length() - 1;
                        variantType = VariantType.SNP;
                    } else if (!badRef && newRef.length() >= newAlt.length()) { //deletion or block substitution
                        String head = newRef.substring(0, newAlt.length());
                        if (head.equals(newAlt)) {
                            newEnd = newStart + newRef.length() - 1;
                            newStart = newStart + head.length();
                            newRef = newRef.substring(newAlt.length());
                            newAlt = "-";
                            variantType = VariantType.Deletion;
                        } else {
                            newEnd = newStart + newRef.length() - 1;
                            variantType = VariantType.InDel;
                        }
                    } else if (VCF_BADALT_REGEX.matcher(newAlt).find()) {
                        if (UNRECOGNIZED_ALTS_GIVE_WARNING) {
                            vcf_warning("Unrecognized ALT allele detected (ACGTN]:[ expected, found " + alt + ").  Storing anyway.");
                        }
                        ++numInvalidAlt;
                    } else if (!badRef) {// if(ref.length() < alt.length()){ //insertion or block substitution
                    /*    String head = newAlt.substring(0, newRef.length());
                         if (head.equals(newRef)) {
                         newEnd = newStart + newRef.length() - 1;
                         newStart = newStart + newRef.length() - 1;
                         newAlt = newAlt.substring(newRef.length());
                         newRef = "-";
                         variantType = VariantType.Insertion;
                         */
                        if (newRef.length() == 0) { //insertion
                            newRef = "-";
                            newStart--;
                            newEnd--;
                            variantType = VariantType.Insertion;
                        } else {
                            newEnd = newStart + newRef.length() - 1;
                            variantType = VariantType.InDel;
                        }
                    }
                }//end else.

                VariantRecord variantRecordTemplate = new VariantRecord(line, newStart, newEnd, newRef, newAlt, altNumber, variantType);

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
                        try {
                            sampleVariantRecord.setZygosity(calculateZygosity(sampleVariantRecord));
                        } catch (IllegalArgumentException iex) {
                            vcf_warning("SKIPPED VARIANT. " + iex.getMessage());
                            continue;
                        }
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
                        sampleInfo = sampleInfo.replace(";", ",");
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

        if ((lineNumber % LINES_PER_PROGRESSREPORT) == 0) {
            messageToUser(LogManagerAdapter.LogType.INFO, vcfFile.getName() + ": Loaded " + lineNumber + " variants...");
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

    private static Zygosity calculateZygosity(VariantRecord vr) throws IllegalArgumentException {
        boolean homoRef = (vr.getRef().equals(vr.getAlt()));

        String gt = vr.getGenotype();
        String[] split = gt.split("/|\\\\|\\|"); // splits on / or \ or |
        if (split.length < 2 || split[0] == null || split[1] == null || split[0].length() == 0 || split[1].length() == 0) {
            throw new IllegalArgumentException("Invalid genotype field: " + gt);
        }

        try {
            if (split[0].equals(".") || split[1].equals(".")) {
                if (homoRef) {
                    return Zygosity.HomoRef;
                }
                return Zygosity.Missing;
            }
            int a = Integer.parseInt(split[0]);
            int b = Integer.parseInt(split[1]);
            if (a == 0 && b == 0) {
                return Zygosity.HomoRef;
            } else if (!homoRef) {
                if (a == b) {
                    return Zygosity.HomoAlt;
                } else if (a == 0 || b == 0) {
                    return Zygosity.Hetero;
                } else {
                    return Zygosity.HeteroTriallelic;
                }
            } else {
                throw new IllegalArgumentException("Ref and Alt field are equal or Alt=., indicicating HomoRef variant, but genotype (" + gt + ") is invalid or indicates differently.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Genotype " + gt);
        }
    }
}
