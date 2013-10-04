/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.medsavant.annotation.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.broad.tabix.TabixWriter;
import org.ut.biolab.medsavant.shared.util.IOUtils;

/**
 * @author mfiume
 */
public class MedSavantAnnotationFormatter
{
    private static final String idf_ext = ".idf";

    private static final String tsv_ext = ".tsv";

    private static final String txt_ext = ".txt";

    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    private static final char headerChar = '#';

    private static final String delim = "\t";

    private static enum TXT_FORMATS
    {
        F1("(?:[0-9]{1,2}|X|Y)\t\\d++\t[ACGTN]++\t[ACGTN0-9]++\t[0-9.]++\t[.;\\d\\w]++", new String[] {"Chr", "Start",
            "Ref", "Obs", "Freq", "Name"}),

        F2("(?:[0-9]{1,2}|X|Y)\t\\d++\t\\d++\t[ACGT]\t[ACGT]\t[-\\d.E]++\t(?:\\w|NA)", new String[] {"Chr", "Start",
            "End", "Ref", "Obs", "Freq", "C"}),

        F3("(?:[0-9]{1,2}|X|Y)\t\\d++\t\\d++\t[ACGT]*+\t[ACGT]++\t[\\d.]++(?:\t[\\w*]){2}", new String[] {"Chr",
            "Start", "End", "Ref", "Obs", "Freq", "O1", "O2"}),

        F4("(?:[0-9]{1,2}|X|Y|M)\t\\d++\t\\d++\t[-ACGT]++\t[-ACGT]++\t[\\d.]++", new String[] {"Chr", "Start", "End",
            "Ref", "Obs", "Freq"}),

        F5("(?:[0-9]{1,2}|X|Y)\t\\d++\t\\d++\t[-ACGTR0-9]*+\t[-A-Y]*+\t[^\t]++", new String[] {"Chr", "Start", "End",
            "Ref", "Obs", "Comment"}),

        F6("[0-9]++\tchr[^\t]++\t\\d++\t\\d++\tlod=\\d++\t\\d++", new String[] {"Bin", "Chr", "Start", "End", "LOD",
            "N"}),

        F7("[0-9]++\tchr[^\t]++\t\\d++\t\\d++\tchr[^\t]++\t0\t[+-]\tchr[^\t]++(\t\\d++){5}(\tN/A){4}"
            + "\t[^\t]++(\t\\d++){8}(\t[-\\d.e]++){4}", new String[] {"Bin", "Chr", "Start", "End", "OtherRef", "0",
            "Strand", "OrthoChr", "OrthoStart", "OrthoEnd", "Pos?", "N0", "1000", "NA", "NA", "NA", "NA", "AlignFile",
            "N1", "N2", "N3", "N4", "N5", "N6", "N7", "N8", "F1", "F2", "F3", "F4"}),

        F8("chr[^\t]++\t\\d++\t\\d++\t[^\t]++\t1000\t[+-](\t\\d++){4}(?:\t[\\d,]++){2}\t(?:PF\\d{5};)?", new String[] {
            "Chr", "Start", "End", "Name", "1000", "Strand", "Start2", "End2", "N1", "N2", "Array1", "Array2", "Pfam"}),

        F9(
            "\\d++\tN[MR]_\\d++\tchr[^\t]++\t[+-](?:\t\\d++){5}(?:\t[\\d,]++){2}\t0\t[^\t]++(?:\t(unk|cmpl|incmpl)){2}\t[\\d,-]++",
            new String[] {"N1", "Idx", "Chr", "Strand", "Start", "End", "Start2?", "End2?", "N2", "Array1", "Array2",
                "0", "Name", "Status1", "Status2", "Array3"}),

        F10(
            "[0-9]++\tchr[^\t]++\t\\d++\t\\d++\trs\\d++\t0\t[+-](\t[^\t]++){3}\t"
                + "(genomic|cDNA|unknown)\t(single|insertion|deletion|in-del|mnp|mixed|named|microsatellite|het)\t(unknown|by-[\\d\\w-]++|,)++"
                + "(\t[\\d.]++){2}\t"
                + "((unknown|near-gene-\\d++|splice-\\d++|untranslated-\\d++|ncRNA|intron|coding-synon|missense|frameshift|nonsense|cds-indel|stop-loss|intron),?)++\t"
                + "(between|exact|range|rangeDeletion|rangeInsertion|rangeSubstitution|fuzzy)"
                + "(\t\\d*+\t[^\t]*+){3}(\t[\\d.,]*+){2}\t[^\t]*+", new String[] {"Bin", "Chr", "Start", "End",
                "RefSNP", "Score", "Strand", "Allele1", "Allele2", "Observed", "DNAType", "ChangeType", "Source", "F1",
                "F2", "Effect", "Location", "N1", "Obs", "N2", "Dataset", "N3", "C", "Array1", "Array2", "Obs"}),

        F11(
            "uc[0-9]{3}[a-z]{3}\\.[1-4]\tchr[^\t]++\t[+-](\t\\d++){5}(\t(\\d++,)++){2}\t[^\t]*+\tuc[0-9]{3}[a-z]{3}\\.[1-4]",
            new String[] {"UCSC", "Chr", "Strand", "Start", "End", "Pos1", "Pos2", "N1", "Array1", "Array2",
                "UniProtID", "UCSC2"});

        // F12("uc[0-9]{3}[a-z]{3}\\.[1-4](\t[^\t]*+){6}\t[^\t]++\t(RF\\d{5})?\t(chr[^\t]++)?", new String[] {"UCSC",
        // "ID1", "ID2","ID3", "ID4", "ID5", "ID6", "Desc", "Rfam", "Chr"});

        Pattern pattern;

        String[] columns;

        TabixWriter.Conf tabixConf;

        private TXT_FORMATS(String pattern, String[] columns)
        {
            this.pattern = Pattern.compile(pattern);
            this.columns = columns;
            this.tabixConf =
                new TabixWriter.Conf(0, ArrayUtils.indexOf(columns, "Chr") + 1,
                    ArrayUtils.indexOf(columns, "Start") + 1, ArrayUtils.indexOf(columns, "End") + 1, '#', 0);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        String rootpath = "./";

        for (String child : new File(rootpath).list(new SuffixFileFilter(new String[] {idf_ext, tsv_ext, txt_ext}))) {
            System.out.println("Processing " + child);
            try {
                processFile(new File(rootpath + child));
            } catch (Exception e) {
                System.err.println("Error processing " + child);
                e.printStackTrace();
                continue;
            }
        }
    }

    public static void output(Object[] string, PrintStream os)
    {
        for (Object s : string) {
            os.print(s + "\t");
        }
        os.println();
    }

    private static void processFile(File file) throws Exception
    {
        String fileName = file.getName();

        boolean isTSV = false;
        boolean isIDF = false;
        boolean isTXT = false;

        if (fileName.endsWith(idf_ext)) {
            isIDF = true;
        } else if (fileName.endsWith(tsv_ext)) {
            isTSV = true;
        } else if (fileName.endsWith(txt_ext)) {
            isTXT = true;
        }

        String truncatedFileName = FilenameUtils.removeExtension(fileName);

        File outDir = new File(file.getParent(), truncatedFileName);
        outDir.mkdir();

        File dataFile;
        File xmlFile = new File(outDir, truncatedFileName + ".xml");

        if (isIDF) {
            dataFile = new File(outDir, truncatedFileName + ".tab");
            processIDFFile(file, dataFile, xmlFile);
        } else if (isTSV) {
            dataFile = new File(outDir, truncatedFileName + ".gz");
            processTSVFile(file, dataFile, xmlFile);
        } else if (isTXT) {
            dataFile = new File(outDir, truncatedFileName + ".gz");
            processTXTFile(file, dataFile, xmlFile);
        }

        IOUtils.zipDirectory(outDir, new File(file.getParent(), truncatedFileName + ".zip"));
    }

    private static void processTSVFile(File file, File dataFile, File xmlFile) throws ClassNotFoundException,
        IOException, SQLException, ParserConfigurationException, TransformerException, Exception
    {
        bgZipFile(file, dataFile);

        TabixWriter.Conf TSV_CONF = new TabixWriter.Conf(0, 1, 2, 3, '#', 0);
        TabixWriter w = new TabixWriter(dataFile, TSV_CONF);
        w.createIndex(dataFile);

        new File(dataFile.getAbsolutePath() + ".tbi");

        String fileName = file.getName();
        String fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf("."));

        int underscore = indexOfOrMax(fileNameNoExtension, "_");
        int hyphen = indexOfOrMax(fileNameNoExtension, "-");
        int period = indexOfOrMax(fileNameNoExtension, ".");

        fileNameNoExtension.substring(0, Math.min(underscore, Math.min(hyphen, period)));

        String reference;
        if (fileNameNoExtension.contains("GRCh_37")) {
            reference = "hg19";
        } else {
            throw new Exception("Unknown reference from " + fileNameNoExtension);
        }

        AnnotationWriter aw = new AnnotationWriter(dataFile, xmlFile);

        aw.setName(fileNameNoExtension);
        aw.setReference(reference);
        aw.setVersion("1.0");

        BlockCompressedInputStream bcis = new BlockCompressedInputStream(dataFile);
        String line;
        String[] tokens;
        while ((line = bcis.readLine()) != null) {
            // header line
            if (line.charAt(0) == headerChar) {
                line = line.substring(1, line.length());
                tokens = line.split(delim, -1);
                aw.setFieldNames(tokens);

                // data line
            } else {
                tokens = line.split(delim, -1);
                aw.addLine(tokens);
            }
        }
        bcis.close();

        aw.writeXMLSummary();
    }

    private static void processTXTFile(File file, File dataFile, File xmlFile) throws Exception
    {
        if (!dataFile.exists()) {
            bgZipFile(file, dataFile);
        }

        BlockCompressedInputStream bcis = new BlockCompressedInputStream(dataFile);
        String line;
        String[] tokens;
        TXT_FORMATS matchingFormat = null;

        line = bcis.readLine();
        tokens = line.split(delim, -1);
        for (TXT_FORMATS format : TXT_FORMATS.values()) {
            if (format.pattern.matcher(line).matches()) {
                matchingFormat = format;
                break;
            }
        }
        if (matchingFormat == null) {
            System.out.println("Unknown file format for [" + file.getCanonicalPath() + "]");
            return;
        }

        TabixWriter w = new TabixWriter(dataFile, matchingFormat.tabixConf);
        w.createIndex(dataFile);

        new File(dataFile.getAbsolutePath() + ".tbi");
        String fileName = file.getName();
        String fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf("."));

        int underscore = indexOfOrMax(fileNameNoExtension, "_");
        int hyphen = indexOfOrMax(fileNameNoExtension, "-");
        int period = indexOfOrMax(fileNameNoExtension, ".");

        fileNameNoExtension.substring(0, Math.min(underscore, Math.min(hyphen, period)));

        String reference = (fileName.startsWith("hg18") ? "hg18" : "hg19");

        AnnotationWriter aw = new AnnotationWriter(dataFile, xmlFile);

        aw.setName(fileNameNoExtension);
        aw.setReference(reference);
        aw.setVersion("1.0");
        aw.setFieldNames(matchingFormat.columns);

        aw.addLine(tokens);
        while ((line = bcis.readLine()) != null) {
            tokens = line.split(delim, -1);
            aw.addLine(tokens);
        }
        bcis.close();

        aw.writeXMLSummary();
    }

    private static void processIDFFile(File file, File dataFile, File xmlFile) throws ClassNotFoundException,
        IOException, SQLException, ParserConfigurationException, TransformerException, Exception
    {

        String path = file.getAbsolutePath();

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path);

        Statement nameStatement = conn.createStatement();
        ResultSet nameResultSet = nameStatement.executeQuery("SELECT name,build FROM source");
        nameResultSet.next();
        String name = nameResultSet.getString("name");
        String build = nameResultSet.getString("build");

        String version = "0";
        String minor_version = "0";

        Statement versionStatement = conn.createStatement();
        ResultSet versionResultSet = versionStatement.executeQuery("SELECT name,value FROM meta_data");
        while (versionResultSet.next()) {
            String n = versionResultSet.getString("name");
            String v = versionResultSet.getString("value");
            if (n.equals("version")) {
                version = v;
            } else if (n.equals("minor_version")) {
                minor_version = v;
            }
        }

        Statement dataStatement = conn.createStatement();
        ResultSet dataResultSet = dataStatement.executeQuery("SELECT * FROM interval");

        AnnotationWriter aw = new AnnotationWriter(dataFile, xmlFile);

        aw.setName(name);
        aw.setReference(build);
        aw.setVersion(version + "." + minor_version);

        aw.setFieldNames(new String[] {"query_id", "start", "stop", "data"});

        aw.openWriter();

        int rowNum = 0;
        while (dataResultSet.next()) {
            rowNum++;

            if (rowNum == 2) {
                break;
            }

            dataResultSet.getBytes("data");

            aw.addLine(new String[] {"" + dataResultSet.getInt("query_id"), "" + dataResultSet.getInt("start"),
                "" + dataResultSet.getInt("stop"), new String(dataResultSet.getBytes("data"), LATIN1)});
        }

        aw.closeWriter();
        aw.writeXMLSummary();

        dataResultSet.close();
        conn.close();
    }

    private static void bgZipFile(File infile, File outFile) throws IOException
    {
        BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(outFile);
        BufferedReader br = new BufferedReader(new FileReader(infile));
        String line;
        while ((line = br.readLine()) != null) {
            line = line + "\n";
            bcos.write(line.getBytes(LATIN1));
        }
        bcos.close();
    }

    private static int indexOfOrMax(String str, String regex)
    {
        if (str.contains(regex)) {
            return str.indexOf(regex);
        } else {
            return str.length();
        }
    }
}
