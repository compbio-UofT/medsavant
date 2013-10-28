/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.medsavant.annotation.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.samtools.util.BlockCompressedOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.broad.tabix.TabixReader;
import org.broad.tabix.TabixWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AnnotationFormatter {

    //listed in order of prescedence.
    public enum ColumnType {

        UNDEFINED, TINYINT, SMALLINT, MEDIUMINT, INTEGER, BIGINT, DECIMAL, VARCHAR;
    }
    private static final Charset LATIN1 = Charset.forName("ISO-8859-1");
    private static final String COLTITLE_CHROM = "CHR";
    private static final String COLTITLE_START = "START";
    private static final String COLTITLE_POSITION = "POSITION";
    private static final String COLTITLE_END = "END";
    private static final String COLTITLE_REF = "REF";
    private static final String COLTITLE_ALT = "ALT";
    private static final int MAX_COLNAME_LENGTH = 14; //maximum allowed length of a database column name.
    private final static String DEFAULT_REF_GENOME = "Unknown"; //If ref genome is not given, assume this value.
    private final static String DEFAULT_PROGRAM = "Unknown";
    private final static String DEFAULT_VERSION = "1.0.0";
    private final static boolean DEFAULT_INTERVAL = true; //by default, assume type is 'interval'.
    //Constraints
    private static Pattern REGEXP_INTERVAL = Pattern.compile("interval|position");
    private static Pattern REGEXP_VERSION = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)?");
    private static Pattern REGEXP_PROGRAM = Pattern.compile(".+");
    private static Pattern REGEXP_REFERENCE = Pattern.compile(".+");
    private static Pattern REGEXP_ALIAS = Pattern.compile(".+");
    private static Pattern REGEXP_BOOLEAN = Pattern.compile("true|false|y|n|0|1|yes|no");
    private static Pattern REGEXP_DESCRIPTION = Pattern.compile(".*"); //allow description to be blank.
    //If the number of characters in a string column matches or exceeds this number, then calculate
    //the number of characters, N, to use in the varchar as the minimum power of 2 (minus 1) that satisfies
    //(max length in tabix / N) > VARCHAR_RANGE_THRESH.
    private static final int VARCHAR_AUTO_THRESH = 2;
    private static final double VARCHAR_RANGE_THRESH = 0.85;
    private static final String TABIX_COMMENT_CHAR = "#";
    private static final String TABIX_GZ_EXTENSION = "gz";
    private static final String TABIX_INDEX_EXTENSION = "tbi";
    private static int MAX_DECIMAL_DIGITS = 65; //maximum number of digits allowed by 'DECIMAL' data type.
    private static int[] TINY_INT_RANGE = new int[]{-128, 127};
    private static int[] SMALL_INT_RANGE = new int[]{-32768, 32767};
    private static int[] MEDIUM_INT_RANGE = new int[]{-8388608, 8388607};
    private static int[] INT_RANGE = new int[]{-2147483648, 2147483647};
    private Column[] columnTypes;
    private static Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");
    private static Pattern DECIMAL_PATTERN = Pattern.compile("");
    private int numExtraFields;
    private boolean hasRef;
    private boolean hasAlt;
    private String version;
    private Boolean interval;
    private String program;
    private String refgenome;
    private String coltitle_chrom;
    private String coltitle_start;
    private String coltitle_end;
    private String coltitle_ref;
    private String coltitle_alt;
    private String outputTabixFile;
    private String[] mainColumnTitles;

    private class Field {

        String colName;
        String colType;
        boolean filterable;
        String alias;
        String description;

        @Override
        public String toString() {
            String s = "[" + colName + "]\t[" + colType + "]\t[" + filterable + "]\t[" + alias + "]\t[" + description + "]";
            return s;
        }

        private String getColumnName() {
            String s = alias.toLowerCase(); //keep column names as lower case.
            String cn = "";
            char lastChar = 0;
            for (int i = 0; i < alias.length(); ++i) {
                char c = s.charAt(i);
                if (c == lastChar && Character.isSpaceChar(c)) { //skip multiple spaces
                    continue;
                }
                lastChar = c;
                if (Character.isLetterOrDigit(c)) {
                    cn = cn + c;
                } else if (Character.isSpaceChar(c)) {
                    cn = cn + "_";
                }
            }

            //truncate string to MAX_COLNAME_LENGTH characters
            if (cn.length() > MAX_COLNAME_LENGTH) {
                cn = cn.substring(0, MAX_COLNAME_LENGTH);

                //If last char is _ after truncation, then just drop the _.
                if (cn.charAt(cn.length() - 1) == '_') {
                    return cn.substring(0, cn.length() - 1);
                }
            }
            return cn;
        }

        public Field(boolean filterable, String alias, String description) {

            this.filterable = filterable;
            this.alias = alias;
            this.description = description;
            this.colName = getColumnName();

        }

        public Field setColumn(Column colType) {
            switch (colType.ct) {
                case BIGINT:
                    //MedSavant doesn't currently support 8 bit integers, so we'll make it a DECIMAL
                    this.colType = "DECIMAL(" + colType.digitlen + ", " + 0 + ")";
                    break;

                case MEDIUMINT:
                case INTEGER:
                case SMALLINT:
                    //Medsavant doesn't currently support medium or small ints.  Make them INTEGER.
                    this.colType = "INTEGER";
                    break;

                case TINYINT:
                    //Medsavant only supports integers, but will auto-convert the 'tinyint' type to integer,
                    //so we'll use tinyint.
                    this.colType = colType.ct.toString();
                    break;

                case DECIMAL:
                    this.colType = "DECIMAL(" + colType.digitlen + ", " + colType.afterDec + ")";
                    break;

                case VARCHAR:
                    int j = colType.strlen;
                    int numChars = colType.strlen;
                    if (j >= VARCHAR_AUTO_THRESH) {
                        numChars = 0;
                        while (j > 0) {
                            j = j >> 1;
                            numChars = (numChars << 1) + 1;
                        }
                        if (((colType.strlen) / (double) numChars) > VARCHAR_RANGE_THRESH) {
                            numChars = (numChars << 1) + 1;
                        }
                    }

                    this.colType = "VARCHAR(" + numChars + ")";
                    break;

                default:
                    System.err.println("No column type detected for column " + alias);
                    System.exit(1);
                    break;

            }
            return this;
        }
    }

    private class Column {

        ColumnType ct;
        int digitlen;
        int strlen;
        int afterDec;

        public Column() {
            this.ct = ColumnType.UNDEFINED;
            this.digitlen = 0;
            this.strlen = 0;
            this.afterDec = 0;
        }

        public void setType(ColumnType nt, int strlen, int digitlen, int afterDec) {
            if (this.ct.ordinal() < nt.ordinal()) {
                this.ct = nt;

            }
            this.strlen = Math.max(strlen, this.strlen);

            if (this.digitlen < digitlen) {
                this.digitlen = digitlen;
                this.afterDec = afterDec;
            }
        }

        public void setType(ColumnType nt, int strlen, int digitlen) {
            setType(nt, strlen, digitlen, 0);
        }

        public void setType(ColumnType nt, int strlen) {
            setType(nt, strlen, 0, 0);
        }
    }
    private Field[] extraFields;
    private Map<String, Boolean> alias2filterableMap = new HashMap<String, Boolean>();
    private Map<String, String> alias2descriptionMap = new HashMap<String, String>();

    private static boolean getBoolean(String s) throws IllegalArgumentException {
        if (!REGEXP_BOOLEAN.matcher(s).matches()) {
            throw new IllegalArgumentException("ERROR: Invalid boolean value " + s + ", expected true or false, y or n, yes or no, 0 or 1.");
        }
        return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equals("1");
    }

    private static void bgZipFile(File infile, File outFile) throws IOException {
        BlockCompressedOutputStream bcos = new BlockCompressedOutputStream(outFile);
        BufferedReader br = new BufferedReader(new FileReader(infile));
        String line;
        while ((line = br.readLine()) != null) {
            line = line + "\n";
            bcos.write(line.getBytes(LATIN1));
        }
        bcos.close();
    }

    private int[] getHeaderMappings(String[] header) throws IllegalArgumentException {
        int[] mappings = {-1, -1, -1, -1, -1};
        numExtraFields = header.length;
        mainColumnTitles = new String[mappings.length];
        for (int i = 0; i < header.length; ++i) {
            if (header[i].equalsIgnoreCase(this.coltitle_chrom)) {
                mappings[0] = i;
                numExtraFields--;
                mainColumnTitles[0] = COLTITLE_CHROM;
            } else if (header[i].equalsIgnoreCase(this.coltitle_start)) {
                mappings[1] = i;
                numExtraFields--;
                mainColumnTitles[1] = COLTITLE_START;
            } else if (header[i].equalsIgnoreCase(this.coltitle_end) && interval) {
                mappings[2] = i;
                numExtraFields--;
                mainColumnTitles[2] = COLTITLE_END;
            } else if (header[i].equalsIgnoreCase(this.coltitle_ref)) {
                mappings[interval ? 3 : 2] = i;
                numExtraFields--;
                mainColumnTitles[interval ? 3 : 2] = COLTITLE_REF;
            } else if (header[i].equalsIgnoreCase(this.coltitle_alt)) {
                mappings[interval ? 4 : 3] = i;
                numExtraFields--;
                mainColumnTitles[interval ? 4 : 3] = COLTITLE_ALT;
            }
        }

        if (mappings[0] == -1) {
            throw new IllegalArgumentException("ERROR: Couldn't locate chromosome column called " + this.coltitle_chrom + " in input tabix file\nColumn Titles Found: " + StringUtils.join(header, "; "));
        }
        if (mappings[1] == -1) {
            throw new IllegalArgumentException("ERROR: Couldn't locate start/position column called " + this.coltitle_start + " in input tabix file\nColumn Titles Found: " + StringUtils.join(header, "; "));
        }

        if (interval && mappings[2] == -1) {
            throw new IllegalArgumentException("ERROR: Couldn't locate 'end' column called " + this.coltitle_end + " in input tabix file\nColumn Titles Found: " + StringUtils.join(header, "; "));
        }

        if (hasRef && mappings[interval ? 3 : 2] == -1) {
            throw new IllegalArgumentException("ERROR: Couldn't locate Reference column called " + this.coltitle_ref + " in input tabix file\nColumn Titles Found: " + StringUtils.join(header, "; "));
        }
        if (hasAlt && mappings[interval ? 4 : 3] == -1) {
            throw new IllegalArgumentException("ERROR: Couldn't locate Alternate column called " + this.coltitle_alt + " in input tabix file\nColumn Titles Found: " + StringUtils.join(header, "; "));
        }

        if (outputTabixFile == null) {
            for (int i = 0; i < mappings.length; ++i) {
                if (mappings[i] != i && mappings[i] != -1) {
                    throw new IllegalArgumentException("ERROR: Input Tabix file has columns in wrong order, use -fixTabix to write a new tabix file.");
                }
            }
        }
        return mappings;
    }

    private void outputLine(BufferedWriter out, int[] headerMappings, String[] data) throws IOException {
        out.write(data[headerMappings[0]]);
        for (int i = 1; i < headerMappings.length; ++i) {
            if (headerMappings[i] != -1) {
                out.write("\t" + data[headerMappings[i]]);
            }
        }
        for (int i = 0; i < data.length; ++i) {
            if (!ArrayUtils.contains(headerMappings, i)) {
                out.write("\t" + data[i]);
            }
        }
        out.write("\n");
    }

    public String getValue(Pattern constraint, Element e, String a) throws IllegalArgumentException {


        if (!e.hasAttribute(a)) {
            return null;
        }
        String val = e.getAttribute(a);
        return getValue(constraint, a, val);

    }

    public static String getValue(Pattern constraint, String attribute, String val) throws IllegalArgumentException {
        if (constraint.matcher(val).matches()) {
            return val;
        } else {
            throw new IllegalArgumentException("ERROR: Invalid value " + val + " for attribute " + attribute);
        }
    }

    public void readXML(String xmlFormatFile) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlFormatFile);
        doc.getDocumentElement().normalize();

        String v = getValue(REGEXP_BOOLEAN, doc.getDocumentElement(), "hasref");
        if (v != null) {
            hasRef = getBoolean(v);
        }

        v = getValue(REGEXP_BOOLEAN, doc.getDocumentElement(), "hasalt");
        if (v != null) {
            hasAlt = getBoolean(v);
        }

        if (doc.getDocumentElement().hasAttribute("type")) {
            v = getValue(REGEXP_INTERVAL, doc.getDocumentElement(), "type");
            if (v != null) {
                this.interval = v.equalsIgnoreCase("interval");
            }
        }

        v = getValue(REGEXP_VERSION, doc.getDocumentElement(), "version");
        if (v != null) {
            version = v;
        }

        v = getValue(REGEXP_PROGRAM, doc.getDocumentElement(), "program");
        if (v != null) {
            program = v;
        }

        v = getValue(REGEXP_REFERENCE, doc.getDocumentElement(), "reference");
        if (v != null) {
            refgenome = v;
        }

        String alias = "?";
        try {
            NodeList fields = doc.getElementsByTagName("field");
            extraFields = new Field[fields.getLength()];
            for (int i = 0; i < fields.getLength(); i++) {
                alias = "?";
                Element field = (Element) (fields.item(i));
                alias = getValue(REGEXP_ALIAS, field, "alias");
                if (alias != null) {
                    v = getValue(REGEXP_DESCRIPTION, field, "description");
                    if (v != null) {
                        alias2descriptionMap.put(alias.toLowerCase(), v);
                    }

                    v = getValue(REGEXP_BOOLEAN, field, "filterable");
                    if (v != null) {
                        alias2filterableMap.put(alias.toLowerCase(), getBoolean(v));
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ex.getMessage() + ", for field with alias '" + alias + "'");
        }
    }

    public void printXML(String filename) throws IOException {

        String type = "position";
        if (this.interval) {
            type = "interval";
        }
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
        s += "<annotation program=\"" + program + "\" version=\"" + version + "\" reference=\"" + refgenome + "\" type=\"" + type + "\" hasref=\"" + hasRef + "\" hasalt=\"" + hasAlt + "\">\n";

        for (int i = 0; i < extraFields.length; ++i) {

            s += "\t<field name=\"" + extraFields[i].colName + "\" type=\"" + extraFields[i].colType + "\" filterable=\"" + extraFields[i].filterable + "\" alias=\"" + extraFields[i].alias + "\" description=\"" + extraFields[i].description + "\" />\n";
        }
        s += "</annotation>\n";

        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write(s);
        out.close();
        System.out.println("Wrote " + filename);
    }

    public void readTabix(String filename) throws Exception {
        readTabix(filename, null);
    }

    public void readTabix(String filename, String[] header) throws Exception {
        TabixReader tr = new TabixReader(filename);

        if (header == null) {
            String fl = tr.readLine().trim();
            if (fl.startsWith(TABIX_COMMENT_CHAR)) {
                fl = fl.substring(1);
            }
            header = fl.split("\t");
        }

        int[] headerMappings = getHeaderMappings(header);

        File tsv = null;
        BufferedWriter out = null;
        if (outputTabixFile != null) { //If user specified -fixTabix, write an output file.
            tsv = new File(outputTabixFile);
            //Write header, using the headerMappings to determine which columns of the input tabix
            //correspond to the first N columns.
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsv), "utf-8"));

            //Replace column titles in tabix file with default names if the column corresponds to a main field.
            //(e.g. chromosome, ref, alt, start, or end)
            String[] newHeader = ArrayUtils.clone(header);
            int j = 0;
            while (j < headerMappings.length && headerMappings[j] > -1) {
                newHeader[headerMappings[j]] = mainColumnTitles[j];
                j++;
            }

            if (!newHeader[headerMappings[0]].startsWith(TABIX_COMMENT_CHAR)) {
                newHeader[headerMappings[0]] = TABIX_COMMENT_CHAR + newHeader[headerMappings[0]];
                outputLine(out, headerMappings, newHeader);
            }
        }

        String s;
        columnTypes = new Column[header.length];
        for (int i = 0; i < header.length; ++i) {
            columnTypes[i] = new Column();
        }

        while (true) {
            s = tr.readLine();
            if (s == null) {
                break;
            }

            String[] vals = s.trim().split("\t");

            if (vals.length > header.length) {
                throw new IOException("Error in file " + filename + ", more columns than column titles");
            }

            if (outputTabixFile != null) {
                outputLine(out, headerMappings, vals);
            }

            for (int i = 0; i < vals.length; ++i) {
                String str = vals[i].trim();
                if (str.length() < 1) {
                    continue; //skip whitespace.
                }

                String nosignStr = str.replace("-", "").replace("+", "");

                if (INTEGER_PATTERN.matcher(str).matches()) {
                    //tinyint?
                    try {
                        Long x = Long.parseLong(str);
                        if (x >= TINY_INT_RANGE[0] && x <= TINY_INT_RANGE[1]) {
                            columnTypes[i].setType(ColumnType.TINYINT, str.length(), nosignStr.length());
                        } else if (x >= SMALL_INT_RANGE[0] && x <= SMALL_INT_RANGE[1]) {
                            columnTypes[i].setType(ColumnType.SMALLINT, str.length(), nosignStr.length());
                        } else if (x >= MEDIUM_INT_RANGE[0] && x <= MEDIUM_INT_RANGE[1]) {
                            columnTypes[i].setType(ColumnType.MEDIUMINT, str.length(), nosignStr.length());
                        } else if (x >= INT_RANGE[0] && x <= INT_RANGE[1]) {
                            columnTypes[i].setType(ColumnType.INTEGER, str.length(), nosignStr.length());
                        } else {
                            //Long.parseLong should throw an exception if number is too big to fit in a BIGINT...
                            columnTypes[i].setType(ColumnType.BIGINT, str.length(), nosignStr.length());
                        }
                    } catch (NumberFormatException ex) {
                        if (nosignStr.length() > MAX_DECIMAL_DIGITS) {
                            columnTypes[i].setType(ColumnType.VARCHAR, str.length());
                        } else {
                            columnTypes[i].setType(ColumnType.DECIMAL, str.length(), nosignStr.length(), 0);
                        }
                    }
                } else if (DECIMAL_PATTERN.matcher(str).matches()) {
                    if (nosignStr.length() > MAX_DECIMAL_DIGITS) {
                        columnTypes[i].setType(ColumnType.VARCHAR, str.length());
                    } else {
                        String[] d = nosignStr.split(".");
                        int ad = 0;
                        if (d.length == 2) {
                            ad = d[1].length();
                        }

                        columnTypes[i].setType(ColumnType.DECIMAL, str.length(), nosignStr.length(), ad);
                    }
                } else {
                    columnTypes[i].setType(ColumnType.VARCHAR, str.length());
                }
            }
        }

        extraFields = new Field[numExtraFields];
        int j = 0;
        for (int i = 0; i < header.length; ++i) {
            Boolean filterable = alias2filterableMap.get(header[i].toLowerCase());
            if (filterable == null) {
                filterable = false;
            }

            String desc = alias2descriptionMap.get(header[i].toLowerCase());
            if (desc == null) {
                desc = "";
            }

            if (!ArrayUtils.contains(headerMappings, i)) {
                extraFields[j++] = (new Field(filterable, header[i], desc)).setColumn(columnTypes[i]);
            }

        }

        if (outputTabixFile != null) {
            out.close();

            File outputFile = new File(tsv.getPath() + "." + TABIX_GZ_EXTENSION);
            bgZipFile(tsv, outputFile);
            System.out.println("Wrote fixed tabix file " + outputFile.getPath());
            tsv.deleteOnExit();
            TabixWriter tw = new TabixWriter(outputFile, new TabixWriter.Conf(0, 1, 2, 3, '#', 0));
            tw.createIndex(outputFile);
            System.out.println("Wrote fixed tabix index file " + outputFile.getPath() + "." + TABIX_INDEX_EXTENSION);

        }

    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("USAGE: AnnotationFormatter <input tabix file> <output XML file> {-inputXML <path>, -fixTabix <output filename>, -o option, ...}");
            System.out.println("options:\n version=X.X.X, type=interval|position, program=<program name>, refgenome=<refgenome name>,\n");
            System.out.println("\tchr=<Name of Chromosome column>, position=<Name of 'position' column>, start=<Name of 'start' column>, end=<Name of 'end' column>, ref=<name of 'ref' column>, alt=<name of 'alt' column>\n");
            System.exit(1);
        }
        System.out.print("Running with args: ");
        for (String arg : args) {
            System.out.print(" " + arg);
        }
        System.out.println();
        String attribute = "";
        String[] header = null;
        try {
            AnnotationFormatter af = new AnnotationFormatter();

            if (args.length > 2) {
                for (int i = 2; i < args.length; ++i) {
                    if (args[i].equalsIgnoreCase("-inputXML")) {
                        i++;
                        File f = new File(args[i]);
                        if (!f.exists()) {
                            System.err.println("Bad filename for option 'inputXML' -- file does not exist.");
                            System.exit(1);
                        }
                        if (!f.canRead()) {
                            System.err.println("Bad filename for option 'inputXML' -- cannot read from file (no permission?)");
                            System.exit(1);
                        }
                        af.readXML(args[i]);
                    } else if (args[i].equalsIgnoreCase("-fixTabix")) {
                        i++;
                        af.outputTabixFile = args[i];
                    } else if (args[i].equalsIgnoreCase("-header")) {
                        i++;
                        header = args[i].split(",");
                    } else if (args[i].equals("-o") || args[i].equals("-O")) {
                        i++;
                        if (i >= args.length) {
                            System.err.println("Bad argument -- expected an option after -o");
                            System.exit(1);
                        }
                        String[] pair = args[i].split("=");
                        if (pair.length != 2) {
                            System.err.println("Bad argument -- malformed option after -o");
                            System.exit(1);
                        }
                        attribute = pair[0];
                        if (pair[0].equalsIgnoreCase("version")) {
                            af.version = getValue(REGEXP_VERSION, attribute, pair[1]);
                        } else if (pair[0].equalsIgnoreCase("type")) {
                            af.interval = getValue(REGEXP_INTERVAL, attribute, pair[1]).equalsIgnoreCase("interval");
                        } else if (pair[0].equalsIgnoreCase("program")) {
                            af.program = getValue(REGEXP_PROGRAM, attribute, pair[1]);
                        } else if (pair[0].equalsIgnoreCase("refgenome")) {
                            af.refgenome = getValue(REGEXP_REFERENCE, attribute, pair[1]);
                        } else if (pair[0].equalsIgnoreCase("chr")) {
                            af.coltitle_chrom = pair[1];
                        } else if (pair[0].equalsIgnoreCase("ref")) {
                            af.coltitle_ref = pair[1];
                            af.hasRef = true;
                        } else if (pair[0].equalsIgnoreCase("alt")) {
                            af.coltitle_alt = pair[1];
                            af.hasAlt = true;
                        } else if (pair[0].equalsIgnoreCase("start") || pair[0].equalsIgnoreCase("position")) {
                            af.coltitle_start = pair[1];
                        } else if (pair[0].equalsIgnoreCase("end")) {
                            af.coltitle_end = pair[1];
                        }
                    }
                }
            }

            if (af.refgenome == null || af.refgenome.length() < 1) {
                System.err.println("WARNING: No Reference genome specified -- setting to '" + DEFAULT_REF_GENOME + "'");
                af.refgenome = DEFAULT_REF_GENOME;
            }
            if (af.program == null || af.program.length() < 1) {
                System.err.println("WARNING: No program specified -- setting to '" + DEFAULT_PROGRAM + "'");
                af.program = DEFAULT_PROGRAM;
            }
            if (af.version == null || af.version.length() < 1) {
                System.err.println("WARNING: No version specified -- setting to '" + DEFAULT_VERSION + "'");
                af.version = DEFAULT_VERSION;
            }
            if (af.interval == null) {
                System.err.println("WARNING: No interval specified -- setting to '" + DEFAULT_INTERVAL + "'");
                af.interval = DEFAULT_INTERVAL;
            }

            if (af.coltitle_chrom == null) {
                System.err.println("WARNING: No Chromosome column title specified, assuming it's called " + COLTITLE_CHROM);
                af.coltitle_chrom = COLTITLE_CHROM;
            }

            if (af.coltitle_start == null) {
                if (af.interval) {
                    System.err.println("WARNING: No 'start' column title specified, assuming it's called " + COLTITLE_START);
                    af.coltitle_start = COLTITLE_START;
                } else {
                    System.err.println("WARNING: No 'position' column title specified, assuming it's called " + COLTITLE_POSITION);
                    af.coltitle_start = COLTITLE_POSITION;
                }
            }

            if (af.coltitle_end == null && af.interval) {
                System.err.println("WARNING: No 'end' column title specified, assuming it's called " + COLTITLE_END);
                af.coltitle_end = COLTITLE_END;
            }

            if (af.hasRef && af.coltitle_ref == null) {
                System.err.println("WARNING: Reference column was indicated as present, but no name given.  Assuming it's called " + COLTITLE_REF);
                af.coltitle_ref = COLTITLE_REF;
            }

            if (af.hasAlt && af.coltitle_alt == null) {
                System.err.println("WARNING: Alternate column was indicated as present, but no name given.  Assuming it's called " + COLTITLE_ALT);
                af.coltitle_alt = COLTITLE_ALT;
            }


            af.readTabix(args[0],header);
            af.printXML(args[1]);
        } catch (IllegalArgumentException iae) {
            if (attribute.equalsIgnoreCase("inputxml")) {
                System.err.println("ERROR: Problem with file " + attribute);
            }
            System.err.println(iae.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }
}
