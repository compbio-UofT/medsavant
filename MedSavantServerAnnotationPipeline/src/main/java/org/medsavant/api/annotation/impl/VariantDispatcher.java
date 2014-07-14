package org.medsavant.api.annotation.impl;

import com.google.code.externalsorting.ExternalSort;
import java.io.*;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.annotation.VariantAnnotator;
import org.medsavant.api.annotation.VariantWindow;
import org.medsavant.api.annotation.VariantWindowException;
import org.medsavant.api.common.GenomicVariant;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.common.MedSavantSession;
import org.medsavant.api.common.Reference;
import org.medsavant.api.common.VariantType;
import org.medsavant.api.common.Zygosity;
import org.medsavant.api.common.impl.GenomicVariantFactory;
import org.medsavant.api.common.impl.MedSavantServerJob;
import org.medsavant.api.common.storage.MedSavantFile;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.medsavant.api.variantstorage.GenomicVariantRecord;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;
import org.medsavant.api.variantstorage.impl.GenomicVariantRecordImpl;

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

public class VariantDispatcher {

    private static final int FILE_INDEX_OF_CHROM = 0;
    private static final int FILE_INDEX_OF_POS = FILE_INDEX_OF_CHROM + 1;
    private static final int FILE_INDEX_OF_DBSNPID = FILE_INDEX_OF_POS + 1;
    private static final int FILE_INDEX_OF_REF = FILE_INDEX_OF_DBSNPID + 1;
    private static final int FILE_INDEX_OF_ALT = FILE_INDEX_OF_REF + 1;
    private static final int FILE_INDEX_OF_QUAL = FILE_INDEX_OF_ALT + 1;
    private static final int FILE_INDEX_OF_FILTER = FILE_INDEX_OF_QUAL + 1;
    private static final int FILE_INDEX_OF_INFO = FILE_INDEX_OF_FILTER + 1;

    private static final Log LOG = LogFactory.getLog(VariantDispatcher.class);
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

    private static final String nullString = ".";

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

    private static final long MAX_WARNINGS = 1000;
    private long warningsEmitted = 0;

    private final static int EXTERNALSORT_MAX_TMPFILES = 1024;
    private final static Charset EXTERNALSORT_CHARSET = Charset.defaultCharset();

    private final static int TDF_INDEX_OF_CHROM = 4;
    private final static int TDF_INDEX_OF_STARTPOS = 5;

    private int numVariants = 0;
    private String sessID;
    private MedSavantFile vcfFile;

    private JobProgressMonitor jobProgressMonitor;
    private MedSavantServerContext serverContext;

    private boolean includeHomozygousRefs = false;

    private int blockSize = 10000; //can be overridden with configuration

    private MedSavantVariantStorageEngine storageEngine;

    //These variables control how bad refs are reported or handled.
    private static final boolean TOOLONG_REFS_GIVE_WARNING = true;
    private static final boolean UNRECOGNIZED_REFS_GIVE_WARNING = true; //only matters if above is true.
    private static final boolean TOOLONG_ALTS_GIVE_WARNING = true;
    private static final boolean UNRECOGNIZED_ALTS_GIVE_WARNING = true;

    private class VCFHeader {

        private static final int NUM_MANDATORY_FIELDS = 8;
        private List<String> genotypeLabels;

        public VCFHeader() {
            genotypeLabels = new ArrayList<String>();
        }

        public int getNumMandatoryFields() {
            return NUM_MANDATORY_FIELDS;
        }

        void addGenotypeLabel(String label) {
            genotypeLabels.add(label);
        }

        public String getGenotypeLabelForIndex(int index) {

            if (!containsGenotypeInformation()) {
                return null;
            }

            int adjustedIndex = index - getNumMandatoryFields() + 1; // +1 for "format" column
            if (adjustedIndex < 0 || adjustedIndex >= genotypeLabels.size()) {
                return null;
            } else {
                return genotypeLabels.get(adjustedIndex);
            }
        }

        public List<String> getGenotypeLabels() {
            return genotypeLabels;
        }

        @Override
        public String toString() {

            String s = "";
            for (String l : genotypeLabels) {
                s += "label:" + l + "|";
            }

            return "VCFHeader{" + "genotypeLabels=" + s + '}';
        }

        public boolean containsGenotypeInformation() {
            return !this.genotypeLabels.isEmpty();
        }
    }

    private void dispatchBlockToAnnotators(final MedSavantSession session,
            int updId,
            MedSavantFile originalFile,
            List<String> dnaIds,
            final VariantWindow variantWindow,
            final Reference reference,
            List<VariantAnnotator> stageOneAnnotators) throws VariantWindowException {

        List<MedSavantServerJob> jobs = new ArrayList<MedSavantServerJob>(stageOneAnnotators.size());
        final List<List<String[]>> results
                = new ArrayList<List<String[]>>(stageOneAnnotators.size());

        int i = 0;
        for (final VariantAnnotator ann : stageOneAnnotators) {
            final int index = i;
            MedSavantServerJob msj = new MedSavantServerJob(session.getUser().getUsername(), "foo", jobProgressMonitor) {
                @Override
                public boolean run() throws Exception {
                    getJobProgressMonitor().setMessage("Annotating block with " + ann.getComponentName());
                    List<String[]> annotations = ann.annotate(session, jobProgressMonitor, variantWindow, reference);
                    results.add(index, annotations);
                    getJobProgressMonitor().setMessage("Done");
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

            };
            jobs.add(msj);
            ++i;
        }

        //Block until all annotators are finished.  Can optimize later to avoid blocking here and improve throughput.      
        try {
            //To avoid deadlock, we don't thread annotation.  (VariantDispatch is already threaded)
            for (MedSavantServerJob job : jobs) {
                serverContext.getExecutionService().runJobInCurrentThread(job);
            }
            //MedSavantServerEngine.getInstance().submitLongJobsAndWait(jobs);
        } catch (InterruptedException ie) {
            LOG.error("Interrupted while processing block");
            throw new VariantWindowException("Error: interrupted while annotating variant window on chromosome " + variantWindow.getChromosomeString() + ", " + variantWindow.getStartPosition() + "-" + variantWindow.getEndPosition(), ie);

        } catch (Exception ex) {
            LOG.error("Exception occurred while processing job: ", ex);
            throw new VariantWindowException("Error annotating variant window on chromosome " + variantWindow.getChromosomeString() + ", " + variantWindow.getStartPosition() + "-" + variantWindow.getEndPosition());
        }

        int j = 0;
        //dispatchBlockToAnnotators(session, updId, originalFile, dnaIds, block, stageOneAnnotators);
        List<GenomicVariantRecord> genomicVariantRecords = new ArrayList<GenomicVariantRecord>(variantWindow.getNumVariants());
        //create the genomicvariantRecords.
        for (GenomicVariant gv : variantWindow) {
            String dnaId = dnaIds.get(j);
            GenomicVariantRecord gvr = new GenomicVariantRecordImpl(gv, updId, originalFile, dnaId);

            int k = 0;
            for (VariantAnnotator va : stageOneAnnotators) {
                gvr.addAnnotationValues(va.getAnnotation(), results.get(k++));
            }

            genomicVariantRecords.add(gvr);
            ++j;
        }

        storageEngine.addVariants(genomicVariantRecords, updId);

    }

    private void dispatchToAnnotators(MedSavantSession session, int updId, MedSavantFile originalFile, File inputFile, List<VariantAnnotator> stageOneAnnotators, Reference reference) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(inputFile));

            String line;
            int bs = 0;
            VariantWindow variantWindow = new VariantWindowImpl(blockSize);
            List<String> dnaIds = new ArrayList<String>(blockSize);

            while ((line = in.readLine()) != null) {
                String[] s = line.split("\t");
                String dnaId = s[0];

                GenomicVariant gv = GenomicVariantFactory.createFromTabString(Arrays.copyOfRange(s, 1, s.length));
                variantWindow.addVariant(gv);
                dnaIds.add(dnaId);

                if (variantWindow.getNumVariants() == blockSize) {
                    dispatchBlockToAnnotators(session, updId, originalFile, dnaIds, variantWindow, reference, stageOneAnnotators);
                    variantWindow.clear();
                    dnaIds.clear();
                }
            }
            in.close();
            if (variantWindow.getNumVariants() > 0) {
                dispatchBlockToAnnotators(session, updId, originalFile, dnaIds, variantWindow, reference, stageOneAnnotators);
            }
            /*
             //all work dispatched to annotators for this file.  
        
             String[] s = in.readLine().split("\t");
             String dnaIds = s[0];
             for (VariantAnnotator ann : stageOneAnnotators) {
             ann.annotate(username, jobProgressMonitor, annotationBlock, null);
             }*/
        } catch (FileNotFoundException fnfe) {
            LOG.error("File " + inputFile + " was not found.", fnfe);
            throw new IOException("File " + inputFile + " was not found.", fnfe);
        } catch (VariantWindowException vwe) {
            throw new IOException("File " + inputFile + " resulted in errors.  Aborting file import", vwe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ie) {
                    LOG.error("IO error while closing file " + inputFile, ie);
                }
            }
        }
        //FileNotFoundException, IOException, VariantWindowException
    }

    /*
     public static void addCustomVCFFields(String infile, String outfile, CustomField[] customFields, int customInfoIndex) throws FileNotFoundException, IOException {

     //System.out.println("Adding custom VCF fields infile=" + infile + " oufile=" + outfile + " customInfoIndex=" + customInfoIndex);
     String[] infoFields = new String[customFields.length];
     Class[] infoClasses = new Class[customFields.length];
     for (int i = 0; i < customFields.length; i++) {
     infoFields[i] = customFields[i].getColumnName();
     infoClasses[i] = customFields[i].getColumnClass();
     }

     BufferedReader reader = new BufferedReader(new FileReader(infile));
     BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
     String line;
     while ((line = reader.readLine()) != null) {
     String[] split = line.split(FIELD_DELIMITER + "");
     String info = split[customInfoIndex].substring(1, split[customInfoIndex].length() - 1); //remove quotes
     writer.write(line + FIELD_DELIMITER + GenomicVariantImpl.createTabString(GenomicVariantImpl.parseInfo(info, infoFields, infoClasses)) + "\n");
     }
     reader.close();
     writer.close();
     }
     */
    private void addCustomVCFFields(MedSavantSession session, List<GenomicVariant> block) {
        //parseInfo(info, infoFields, infoClasses)

        //determine the custom fields that should be added.
        //session: sessionId, projectId
        //referenceId: ???
        //
        //customFields are got via getCustomFields(...)
        //when running VariantAnnotator on a single file, immediately before annotating.  
        /*
         if (customFields.length > 0) {
         //LOG.info("Adding " + customFields.length + " custom VCF fields");

         String customFieldFilename = workingFilePath + "_plusfields";
         filesUsed.add(customFieldFilename);
         VariantManagerUtils.addCustomVCFFields(workingFilePath, customFieldFilename, customFields, INDEX_OF_CUSTOM_INFO); //last of the default fields
         File oldFile = new File(workingFilePath);
         oldFile.delete(); //don't need the old file.
         workingFilePath = customFieldFilename;
         }
        
        
         //VariantManagerUtils.addCustomVCFFields:
         public static void addCustomVCFFields(String infile, String outfile, CustomField[] customFields, int customInfoIndex) throws FileNotFoundException, IOException {

         //System.out.println("Adding custom VCF fields infile=" + infile + " oufile=" + outfile + " customInfoIndex=" + customInfoIndex);
         String[] infoFields = new String[customFields.length];
         Class[] infoClasses = new Class[customFields.length];
         for (int i = 0; i < customFields.length; i++) {
         infoFields[i] = customFields[i].getColumnName();
         infoClasses[i] = customFields[i].getColumnClass();
         }

         BufferedReader reader = new BufferedReader(new FileReader(infile));
         BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
         String line;
         while ((line = reader.readLine()) != null) {
         String[] split = line.split(FIELD_DELIMITER + "");
         String info = split[customInfoIndex].substring(1, split[customInfoIndex].length() - 1); //remove quotes
         writer.write(line + FIELD_DELIMITER + VariantRecord.createTabString(VariantRecord.parseInfo(info, infoFields, infoClasses)) + "\n");
         }
         reader.close();
         writer.close();
         }


         */
      //CustomField[] customFields = ProjectManager.getInstance().getCustomVariantFields(sessionID, projectID, referenceID, ProjectManager.getInstance().getNewestUpdateID(sessionID, projectID, referenceID, false));  
    }

    public VariantDispatcher(MedSavantServerContext serverContext, MedSavantVariantStorageEngine storageEngine) {
        this.serverContext = serverContext;
        this.storageEngine = storageEngine;
    }

    /**
     * Reads variants from the given VCF, and dispatches blocks of variants to
     * anntoators. Collects results, and passes them to the storage engine. This
     * method BLOCKS until everything is finished.
     *
     * @param session
     * @param updId
     * @param projectId
     * @param vcfFile
     * @param jpm
     * @param annotators
     * @throws IOException
     */
    public void dispatch(MedSavantSession session, int updId, MedSavantFile vcfFile, JobProgressMonitor jpm, List<VariantAnnotator> annotators, Reference reference) throws IOException, VariantWindowException, MedSavantSecurityException {
        this.vcfFile = vcfFile;
        this.jobProgressMonitor = jpm;
        InputStream is = null;
        //create the outputFile.
        try {
            File outputFile = serverContext.getTemporaryFile(session);
            try {
                is = serverContext.getMedSavantFileDirectory().getInputStream(session, vcfFile);
            } catch (MedSavantFileDirectoryException mfde) {
                String str = "Couldn't locate VCF file "+vcfFile.getName()+" in VCF file directory.";
                LOG.error(str);
                throw new IOException(mfde);
            } 
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            parseVariantsFromReader(br, outputFile, includeHomozygousRefs);
            dispatchToAnnotators(session, updId, vcfFile, outputFile, annotators, reference);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    LOG.error("WARNING: Couldn't close input stream from file " + vcfFile.getName(), ioe);
                }
            }
        }
    }

    //Stores a genomic variant and the dna id that has that variant.
    private class PatientVariant {

        private final GenomicVariant gv;
        private final String dnaId;

        public PatientVariant(GenomicVariant gv, String dnaId) {
            this.gv = gv;
            this.dnaId = dnaId;
        }

        public GenomicVariant getGv() {
            return gv;
        }

        public String getDnaId() {
            return dnaId;
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
    private int parseVariantsFromReader(BufferedReader r, File outfile, boolean includeHomoRef) throws IOException {
        VCFHeader header = null;
        String nextLineString;
        int numRecords = 0;

        final String outOfOrderFilename = outfile.getAbsolutePath() + "_ooo";
        BufferedWriter outOfOrderHandle = new BufferedWriter(new FileWriter(outOfOrderFilename, true));

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
            jobProgressMonitor.setMessage(vcfFile.getName() + " - " + s);
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
                List<PatientVariant> records = null;
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
                for (PatientVariant v : records) {
                    if (includeHomoRef || v.getGv().getZygosity() != Zygosity.HomoRef) {
                        outOfOrderHandle.write(v.getDnaId() + "\t" + v.getGv().toTabString());
                        outOfOrderHandle.write("\r\n");
                        numLinesWritten++;
                    }
                }
                numRecords++;
            }//end else
        }

        outOfOrderHandle.close();
        jobProgressMonitor.setMessage("Sorting variants...");
        LOG.info("sorting out of order handle");
        sortTDF(outOfOrderFilename, outfile);

        return numLinesWritten;
    }

    //Uses ExternalSort so that it can be used with very large files.    
    private void sortTDF(String unsortedTDF, File sortedTDF) throws IOException {
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
        long maxMem = (long) (0.3 * Runtime.getRuntime().maxMemory() / (double) serverContext.getExecutionService().getMaxRunningLongJobs());

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

        FileUtils.moveFile(outputFile, sortedTDF);

        LOG.info("Outputted sorted TDF file to " + sortedTDF);

    }

    private VCFHeader parseHeader(String[] headerLine) {
        VCFHeader result = new VCFHeader();
        // has genotype information
        if (headerLine.length > result.getNumMandatoryFields()) {
            // get the genotype labels
            for (int i = result.getNumMandatoryFields() + 1; i < headerLine.length; i++) {
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
        if (warningsEmitted < MAX_WARNINGS) {
            String warning = vcfFile.getName() + ": WARNING (line " + lineNumber + "): " + msg;
            messageToUser(LogManagerAdapter.LogType.WARNING, warning);
        } else if (warningsEmitted == MAX_WARNINGS) {
            String warning = vcfFile.getName() + ": Further warnings have been truncated.";
            messageToUser(LogManagerAdapter.LogType.WARNING, warning);
        }
        warningsEmitted++;
    }

    private List<PatientVariant> parseRecord(String[] line, VCFHeader header) {
        int numMandatoryFields = header.getNumMandatoryFields();

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
            ids = header.getGenotypeLabels();
        }

        List<PatientVariant> records = new ArrayList<PatientVariant>();

        int triedIndex = 0;
        try {

            String ref = line[VCF_REF_INDEX].toUpperCase();
            String altStr = line[VCF_ALT_INDEX].toUpperCase();

            int start = 0;
            try {
                start = Integer.parseInt(line[VCF_START_INDEX]);
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
                int newStart = start;
                String newRef;
                String newAlt;
                int newEnd;

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

                /*
                 public GenomicVariant create(
                 String chrom,
                 int start_position,
                 int end_position,
                 String dbSNPID,
                 String ref,
                 String alt,
                 int altNumber,
                 float qual,
                 String filter,
                 String customInfo);
                 */
                /*
                 String chrom,
                 int start_position,
                 int end_position,
                 String dbSNPID,
                 String ref,
                 String alt,
                 int altNumber,
                 float qual,
                 String filter,
                 String customInfo)
                 */
                final String dbSNPID = (String) parse(String.class, line[FILE_INDEX_OF_DBSNPID]);
                final Float qual = (Float) parse(Float.class, line[FILE_INDEX_OF_QUAL]);
                final String filter = (String) parse(String.class, line[FILE_INDEX_OF_FILTER]);
                final String customInfo = (String) parse(String.class, line[FILE_INDEX_OF_INFO]);
                final String chrom = (String) parse(String.class, line[FILE_INDEX_OF_CHROM]);

                GenomicVariant variantTemplate
                        = GenomicVariantFactory.create(chrom, newStart, newEnd, dbSNPID, newRef, newAlt, altNumber, qual, filter, customInfo);

                //variantRecordTemplate.create(line, newStart, newEnd, newRef, newAlt, altNumber, variantType);
                int indexGT = getIndexGT(header, line); //index of GT in line

                for (int i = 0; i < ids.size(); i++) { //for each sample. 
                    GenomicVariant sampleVariant = GenomicVariantFactory.createFrom(variantTemplate);
                    //GenomicVariantImpl sampleVariant = new GenomicVariantImpl(variantRecordTemplate);

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
                         sampleVariant.setZygosity(Zygosity.HomoAlt);                            
                         ++numHom;
                         }else{
                         continue;
                         }
                         }else{
                         String x = Integer.toString(i+1);
                         if(!a1.equals(x) && !a2.equals(x)){
                         sampleVariant.setZygosity(Zygosity.Hetero);
                         ++numHet;
                         }
                         }
                         */
                        sampleVariant.setGenotype(gt);
                        try {
                            sampleVariant.setZygosity(calculateZygosity(sampleVariant));
                        } catch (IllegalArgumentException iex) {
                            vcf_warning("SKIPPED VARIANT. " + iex.getMessage());
                            continue;
                        }
                        if (sampleVariant.getZygosity() == Zygosity.Hetero) {
                            ++numHet;
                        } else if (sampleVariant.getZygosity() == Zygosity.HomoAlt) {
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

                    // add sample information to the INFO field on this line
                    try {
                        String format = line[header.getNumMandatoryFields()].trim();
                        String sampleInfo = line[numMandatoryFields + i + 1];
                        sampleInfo = sampleInfo.replace(";", ",");
                        sampleVariant.setSampleInformation(format, sampleInfo);
                    } catch (Exception e) {
                    }

                    String id = ids.get(i);
                    records.add(new PatientVariant(sampleVariant, id));

                    //                   records.add(sampleVariant);
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

    private static int getIndexGT(VCFHeader header, String[] line) {
        if (line.length >= header.getNumMandatoryFields() + 1) {
            String[] list = line[header.getNumMandatoryFields()].trim().split(":");
            for (int i = 0; i < list.length; i++) {
                if (list[i].equals("GT")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Object parse(Class c, String value) {

        if (c == String.class) {
            if (value.equals(nullString)) {
                return "";
            }
            return value;
        }

        if (value.equals(nullString)) {
            return null;
        }

        if (c == Long.class) {
            try {
                return NumberUtils.isDigits(value) ? Long.parseLong(value) : null;
            } catch (Exception e) {
                return null;
            }
        }

        if (c == Float.class) {
            try {
                return NumberUtils.isNumber(value) ? Float.parseFloat(value) : null;
            } catch (Exception e) {
                return null;
            }
        }

        //if flag exists, set to true
        if (c == Boolean.class) {
            return true;
        }

        if (c == Integer.class) {
            try {
                return NumberUtils.isDigits(value) ? Integer.parseInt(value) : null;
            } catch (Exception e) {
                return null;
            }
        }

        throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
    }

    private static Zygosity calculateZygosity(GenomicVariant vr) throws IllegalArgumentException {
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
