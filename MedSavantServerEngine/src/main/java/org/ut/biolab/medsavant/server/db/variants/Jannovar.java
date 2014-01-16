package org.ut.biolab.medsavant.server.db.variants;

import jannovar.annotation.AnnotationList;
import jannovar.common.Constants;
import jannovar.common.Constants.Release;
import jannovar.exception.AnnotationException;
import jannovar.exception.FileDownloadException;
import jannovar.exception.InvalidAttributException;
import jannovar.exception.JannovarException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.FastaParser;
import jannovar.io.GFFparser;
import jannovar.io.RefSeqFastaParser;
import jannovar.io.SerializationManager;
import jannovar.io.TranscriptDataDownloader;
import jannovar.io.VCFLine;
import jannovar.io.VCFReader;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 *
 * @author mfiume
 */
class Jannovar {

    private static final Log LOG = LogFactory.getLog(Jannovar.class);

    private static SerializationManager sManager = new SerializationManager();
    private static HashMap<Byte, Chromosome> chromosomeMap;
    private static String dirPath;
    private static ArrayList<TranscriptModel> transcriptModelList = null;
    private static final String serializationFileName = "refseq_hg19.ser";

    /**
     * The main entry point to this class
     *
     * @param vcfFiles An array of VCF files to be annoted with Jannovar
     * @return An array of files that have been annotated with Jannovar
     * @throws JannovarException
     */
    public static File[] annotateVCFFiles(File[] vcfFiles) throws JannovarException, IOException {

        initialize();

        File[] jvFiles = new File[vcfFiles.length];

        int counter = 0;

        // annotate each file
        for (File file : vcfFiles) {
            LOG.info("Annotating " + file.getAbsolutePath() + " with Jannovar");
            jvFiles[counter++] = annotateVCFWithJannovar(file);
            LOG.info("Done annotating " + file.getAbsolutePath() + " with Jannovar");
        }

        return jvFiles;
    }

    private static String getJannovarDirectoryPath() {
        return new File(DirectorySettings.getCacheDirectory().getPath(), "jannovar").getAbsolutePath();
    }

    /**
     * Initialize Jannovar
     */
    private static boolean initialize() {

        File dir = new File(getJannovarDirectoryPath());

        // make the jannovar directory if needed
        if (!dir.exists()) {
            LOG.info("Creating " + Jannovar.getJannovarDirectoryPath());
            File jannovarDirectory = new File(DirectorySettings.getMedSavantDirectory().getPath(), "jannovar");
            jannovarDirectory.mkdir();
            dirPath = jannovarDirectory.getPath();
        }

        // download the serizalized files, if needed
        if (!hasSerializedFile(serializationFileName)) {
            LOG.info("Downloading Jannovar annotation files");
            try {
                downloadSerializedFile(jannovar.common.Constants.REFSEQ);
            } catch (JannovarException e) {
                dir.delete();
                LOG.error(e);
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the Jannovar serialized annotation file has been downloaded.
     */
    private static boolean hasSerializedFile(String filename) {
        File serFile = new File(Jannovar.getJannovarDirectoryPath(), filename);
        return serFile.exists();
    }

    /**
     * Download the Jannovar serialized annotation file.
     */
    private static void downloadSerializedFile(int sourceDB) throws JannovarException {
        if (sourceDB == jannovar.common.Constants.REFSEQ) {
            downloadTranscriptFiles(jannovar.common.Constants.REFSEQ);
            inputTranscriptModelDataFromRefSeq();
            serializeRefSeqData();
        } else {
            throw new JannovarException("VCFAnnotationWizard: Currently unsupported DB specified");
        }
    }

    /**
     * This function creates a {@link TranscriptDataDownloader} object in order
     * to download the required transcript data files. If the user has set the
     * proxy and proxy port via the command line, we use these to download the
     * files.
     */
    private static void downloadTranscriptFiles(int source) throws FileDownloadException {
        TranscriptDataDownloader downloader = null;
        downloader = new TranscriptDataDownloader(dirPath);
        downloader.downloadTranscriptFiles(source, Release.HG19);
    }

    /**
	 * Modified from Jannovar
     */
    private static void inputTranscriptModelDataFromRefSeq() {
		// parse GFF/GTF
		GFFparser gff = new GFFparser();
		gff.parse(dirPath + Constants.refseq_gff_hg19);
		try {
			transcriptModelList = gff.getTranscriptModelBuilder().buildTranscriptModels();
		} catch (InvalidAttributException e) {
			System.out.println("[Jannovar] Unable to input data from the Refseq files");
			e.printStackTrace();
			System.exit(1);
		}
		// add sequences
		FastaParser efp = new RefSeqFastaParser(dirPath + Constants.refseq_rna, transcriptModelList);
		int before	= transcriptModelList.size();
		transcriptModelList = efp.parse();
		int after = transcriptModelList.size();
		LOG.info(String.format("[Jannovar] removed %d (%d --> %d) transcript models w/o rna sequence",
						before-after,before, after));
    }

    /**
	 * Modified from Jannovar
     */
    public static void serializeRefSeqData() throws JannovarException {
        SerializationManager manager = new SerializationManager();
        LOG.info("[Jannovar] Serializing known gene data as " + serializationFileName);
        manager.serializeKnownGeneList(dirPath + File.separator + serializationFileName, transcriptModelList);
    }

	
    /**
     * Uses Jannovar to create a new VCF file and sends that file to server. The
     * Jannovar VCF file is subsequently removed (treated as temporary data)
     *
     * Code modified from Jannovar class.
     */
    private static File annotateVCFWithJannovar(File sourceVCF) throws JannovarException, IOException {
        chromosomeMap = Chromosome.constructChromosomeMapWithIntervalTree(
                sManager.deserializeKnownGeneList(dirPath + File.separator + serializationFileName));

        /* Annotated VCF name as determined by Jannovar. */
        String outname = sourceVCF.getName();
        int i = outname.lastIndexOf("vcf");
        if (i < 0) {
            i = outname.lastIndexOf("VCF");
        }
        if (i < 0) {
            outname = outname + ".jv.vcf";
        } else {
            outname = outname.substring(0, i) + "jv.vcf";
        }
        File outFile = new File(DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory()),outname);

        VCFReader parser = new VCFReader(sourceVCF.getAbsolutePath());
        VCFLine.setStoreVCFLines();
        try {
            parser.parseFile();
        } catch (VCFParseException e) {
            LOG.error("[Jannovar] Unable to parse VCF file");
            LOG.error(e.toString());
        }

        FileWriter fstream = new FileWriter(outFile.getAbsolutePath());
        BufferedWriter out = new BufferedWriter(fstream);

        /**
         * Write the header of the new VCF file.
         */
        ArrayList<String> lst = parser.getAnnotatedVCFHeader();
        for (String s : lst) {
            out.write(s + "\n");
        }
		
		/** Now write each of the variants. */
	    Iterator<VCFLine> iter = parser.getVCFLineIterator();
	    while(iter.hasNext()){
			VCFLine line = iter.next();
			Variant v = line.toVariant();
				try {
				annotateVCFLine(line,v,out);
			} catch (AnnotationException e) {
				System.out.println("[Jannovar] Warning: Annotation error: " + e.toString());
			}
	    }

        out.close();

        LOG.info("[Jannovar] Wrote annotated VCF file to \"" + outFile.getAbsolutePath() + "\"");

        return outFile;
    }

    /**
     * Annotate a single line of a VCF file, and output the line together with
     * the new INFO fields representing the annotations.
     *
     * Code modified from Jannovar class.
     *
     * @param line an object representing the original VCF line
     * @param v the Variant object that was parsed from the line
     * @param out A file handle to write to.
     */
    private static void annotateVCFLine(VCFLine line, Variant v, Writer out) throws IOException, AnnotationException, JannovarException {
        byte chr = v.getChromosomeAsByte();
        int pos = v.get_position();
        String ref = v.get_ref();
        String alt = v.get_alt();
        Chromosome c = chromosomeMap.get(chr);
        if (c == null) {
            String e = String.format("[Jannovar] Could not identify chromosome \"%d\"", chr);
            throw new AnnotationException(e);
        }
        AnnotationList anno = c.getAnnotationList(pos, ref, alt);
        if (anno == null) {
            String e = String.format("[Jannovar] No annotations found for variant %s", v.toString());
            throw new AnnotationException(e);
        }
        String annotation = anno.getSingleTranscriptAnnotation();
        String effect = anno.getVariantType().toString();
        String A[] = line.getOriginalVCFLine().split("\t");
        for (int i = 0; i < 7; ++i) {
            out.write(A[i] + "\t");
        }
        /* Now add the stuff to the INFO line */
        String INFO = String.format("EFFECT=%s;HGVS=%s;%s", effect, annotation, A[7]);
        out.write(INFO + "\t");
        for (int i = 8; i < A.length; ++i) {
            out.write(A[i] + "\t");
        }
        out.write("\n");
    }

}
