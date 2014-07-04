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
package org.ut.biolab.medsavant.shared.util;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.sf.samtools.SAMRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.medsavant.api.common.storage.ColumnType;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;

/**
 * Various utility methods and constants of general usefulness.
 *
 * @author mfiume, tarkvara
 */
public class MiscUtils {

    private static final Log LOG = LogFactory.getLog(MiscUtils.class);
    public static final boolean MAC;
    public static final boolean WINDOWS;
    public static final boolean LINUX;
    public static final String UNSAVED_MARK = " *";
    /**
     * OS-specific constant for determining menu-options. Either CTRL_MASK or
     * META_MASK.
     */
    public static final int MENU_MASK;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        MAC = os.startsWith("mac");
        WINDOWS = os.startsWith("windows");
        LINUX = os.contains("linux");
        MENU_MASK = MAC ? InputEvent.META_MASK : InputEvent.CTRL_MASK;
    }

    public static boolean doesIntersect(long s_start, long s_end, long t_start, long t_end) {
        return (t_start <= s_end) && (t_end >= s_start);
    }

    public static Condition getIntersectCondition(long s_start, long s_end, DbColumn t_start, DbColumn t_end) {
        return ComboCondition.and(
                new BinaryCondition(BinaryCondition.Op.LESS_THAN_OR_EQUAL_TO, t_start, s_end),
                new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, t_end, s_start));
    }

    /**
     * [[ Miscellaneous Functions ]]
     */
    /**
     * Format an integer to a string (adding commas)
     *
     * @param num The number to format
     * @return A formatted string
     */
    public static String numToString(double num) {
        return numToString(num, 0);
    }

    public static String numToStringWithOrder(long count) {
        if (count < 1000) {
            return "" + count;
        }
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "KMGTPE".charAt(exp - 1));
    }

    public static String numToString(double num, int significantdigits) {
        String formatString = "###,###";

        if (significantdigits > 0) {
            formatString += ".";
            for (int i = 0; i < significantdigits; i++) {
                formatString += "#";
            }
        }

        DecimalFormat df = new DecimalFormat(formatString);
        return df.format(num);
    }

    /**
     * Get a string representation of the the current time
     *
     * @return A string representing the current time
     */
    public static String now() {
        Calendar cal = Calendar.getInstance();
        return DateFormat.getTimeInstance().format(cal.getTime());
    }

    public static String join(Collection<? extends Object> strs, String separator) {
        StringBuilder result = null;
        for (Object o : strs) {
            if (result == null) {
                result = new StringBuilder();
            } else {
                result.append(separator);
            }
            result.append(o);
        }
        return result.toString();
    }

    /**
     * Remove the specified character from the given string.
     *
     * @param s The string from which to remove the character
     * @param c The character to remove from the string
     * @return The string with the character removed
     */
    public static String removeChar(String s, char c) {
        String r = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != c) {
                r += s.charAt(i);
            }
        }
        return r;
    }

    public static String getFilenameFromPath(String path) {
        int lastSlashIndex = path.lastIndexOf(System.getProperty("file.separator"));
        if (lastSlashIndex == -1) {
            lastSlashIndex = path.lastIndexOf("/");
        }
        return path.substring(lastSlashIndex + 1, path.length());
    }

    /**
     * Extract the base file-name (removing the extension) from a path.
     */
    public static String getBaseName(String path) {
        String name = new File(path).getName();
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == name.length() - 1) {
            return name;
        } else {
            return name.substring(0, dotIndex);
        }
    }

    /**
     * Extract the extension from the given path.
     *
     * @param path The path from which to extract the extension
     * @return The extension of the file at the given path
     */
    public static String getExtension(String path) {
        int dotIndex = path.lastIndexOf(".");

        if (dotIndex == -1 || dotIndex == path.length() - 1) {
            return "";
        } else {
            return path.substring(dotIndex + 1);
        }
    }

    /**
     * Extract the file extension from the given URL.
     *
     * @param url The URL from which to extract the extension
     * @return The extension of the URL
     */
    public static String getExtension(URL url) {
        return getExtension(url.toString());
    }

    /**
     * Extract the file-name portion of a URI.
     *
     * @param uri the URI to be processed
     * @return the file-name portion of the URI
     */
    public static String getFileName(URI uri) {
        String path = uri.toString();
        int lastSlashIndex = path.lastIndexOf("/");
        return path.substring(lastSlashIndex + 1, path.length());
    }

    public static String getTemporaryDirectory() {
        String tmpDir;
        if (MAC || LINUX) {
            tmpDir = System.getenv("TMPDIR");
            if (tmpDir != null) {
                return tmpDir;
            } else {
                return "/tmp/savant";
            }
        } else {
            if ((tmpDir = System.getenv("TEMP")) != null) {
                return tmpDir;
            } else if ((tmpDir = System.getenv("TMP")) != null) {
                return tmpDir;
            } else {
                return System.getProperty("user.dir");
            }
        }
    }

    public static List<String> set2List(Set<String> set) {
        List<String> l = new ArrayList<String>();
        for (String s : set) {
            l.add(s);
        }
        Collections.sort(l);
        return l;
    }

    /**
     * Sometimes Throwable.getMessage() returns a useless string (e.g. "null"
     * for a NullPointerException). Return a string which is more meaningful to
     * the end-user.
     */
    public static String getMessage(Throwable t) {
        if (t instanceof NullPointerException) {
            return "Null pointer exception";
        } else if (t instanceof FileNotFoundException) {
            return String.format("File %s not found", t.getMessage());
        } else if (t instanceof ArrayIndexOutOfBoundsException) {
            return "Array index out of bounds";
        } else {
            // Occasional
            String result = t.getMessage();
            if (result == null) {
                result = t.getClass().getSimpleName();
            }
            return result;
        }
    }

    public static String getStackTrace(Throwable t) {
        Writer result = new StringWriter();
        t.printStackTrace(new PrintWriter(result));
        return result.toString();
    }


    /*
     * Return string without sequence title (chr, contig)
     *
     * moved to private static member in GenomicVariantFactory
     */
   /* public static String homogenizeSequence(String s) {
        String result = s;
        if (result.contains("chr")) {
            result = result.replaceAll("chr", "");
        }
        if (result.contains("Chr")) {
            result = result.replaceAll("Chr", "");
        }
        if (result.contains("contig")) {
            result = result.replaceAll("contig", "");
        }
        if (result.contains("Contig")) {
            result = result.replaceAll("Contig", "");
        }
        return result;
    }*/

    public static double roundToSignificantDigits(double num, int n) {
        if (num == 0) {
            return 0;
        } else if (n == 0) {
            return Math.round(num);
        }

        String s = num + "";
        int index = s.indexOf(".");
        while (n >= s.length() - index) {
            s = s + "0";
        }
        return Double.parseDouble(s.substring(0, index + n + 1));
    }

    public static String getSophisticatedByteString(long bytes) {
        if (bytes < 1000) {
            return bytes + " KB";
        } else if (bytes < 1000000000) {
            return roundToSignificantDigits(((double) bytes / 1000000), 1) + " MB";
        } else {
            return roundToSignificantDigits(((double) bytes / 1000000000), 2) + " GB";
        }
    }

    /**
     * If u is a file:// URI, return the absolute path. If it's a network URI,
     * leave it unchanged.
     *
     * @param u the URI to be neatened
     * @return a canonical string representing the URI.
     */
    public static String getNeatPathFromURI(URI u) {
        if (u == null) {
            return "";
        }
        if ("file".equals(u.getScheme())) {
            return (new File(u)).getAbsolutePath();
        }
        return u.toString();
    }

    /**
     * Invoke the given runnable on the AWT event thread.
     *
     * @param r the action to be invoked
     */
    public static void invokeLaterIfNecessary(Runnable r) {
        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    public static String reverseString(String str) {
        int strlen = str.length();
        char[] result = new char[strlen];
        for (int i = 1; i <= strlen; i++) {
            result[strlen - i] = str.charAt(i - 1);
        }
        return new String(result);
    }

    /**
     * If rec1 is likely a mate of rec2, return true.
     *
     * @param rec1 first record
     * @param rec2 second record
     * @param extraCheck if true, equality check is insufficient to avoid
     * self-mating; check positions as well
     */
    public static boolean isMate(SAMRecord rec1, SAMRecord rec2, boolean extraCheck) {

        // If rec1 and rec2 came from the same source (e.g. the same call to getRecords),
        // an equality test is sufficient to avoid mating with ourselves.
        if (rec1 == rec2) {
            return false;
        }
        String name1 = rec1.getReadName();
        String name2 = rec2.getReadName();
        int len1 = name1.length();
        int len2 = name2.length();

        if (extraCheck) {
            // Check if names equal and coordinates match as expected.
            if (name1.equals(name2) && rec1.getMateAlignmentStart() == rec2.getAlignmentStart() && rec1.getAlignmentStart() == rec2.getMateAlignmentStart()) {
                return true;
            }
        } else {
            // Check if names equal.
            if (name1.equals(name2)) {
                return true;
            }
        }

        //list of possible suffices...may grow over time.
        String[][] suffices = {{"\\1", "\\2"}, {"_F", "_R"}, {"_F3", "_R3"}};

        //check suffices
        for (String[] pair : suffices) {
            int len = pair[0].length(); //assumes both suffices of same length
            if (name1.substring(0, len1 - len).equals(name2.substring(0, len2 - len))
                    && ((name1.substring(len1 - len).equals(pair[0]) && name2.substring(len2 - len).equals(pair[1]))
                    || (name1.substring(len1 - len).equals(pair[1]) && name2.substring(len2 - len).equals(pair[0])))) {
                return true;
            }
        }

        //not mates
        return false;
    }

    /**
     * Blend two colours, in the given proportions. Resulting alpha is always
     * 1.0.
     *
     * @param col1 the first colour
     * @param col2 the second colour
     * @param weight1 the weight given to col1 (from 0.0-1.0)
     */
    public static Color blend(Color col1, Color col2, float weight1) {

        float weight2 = (1.0F - weight1) / 255;
        weight1 /= 255;

        // This constructor expects values from 0.0F to 1.0F, so weights have to be scaled appropriately.
        return new Color(col1.getRed() * weight1 + col2.getRed() * weight2, col1.getGreen() * weight1 + col2.getGreen() * weight2, col1.getBlue() * weight1 + col2.getBlue() * weight2);
    }

    /**
     * Utility method to create a polygonal path from a list of coordinates
     *
     * @param coords a sequence of x,y coordinates (should be an even number and
     * at least 4)
     */
    public static Path2D.Double createPolygon(double... coords) {
        if (coords.length < 4 || (coords.length & 1) != 0) {
            throw new IllegalArgumentException("Invalid coordinates for createPolygon");
        }

        Path2D.Double result = new Path2D.Double(Path2D.WIND_NON_ZERO, coords.length / 2);
        result.moveTo(coords[0], coords[1]);
        for (int i = 2; i < coords.length; i += 2) {
            result.moveTo(coords[i], coords[i + 1]);
        }
        result.closePath();
        return result;
    }

    public static String getTagValue(Element e, String tag) {
        NodeList nlList = e.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }

    public static Set<String> getTagValues(Element e, String tag) {
        NodeList nlList = e.getElementsByTagName(tag);
        Set<String> result = new HashSet<String>();
        for (int i = 0; i < nlList.getLength(); i++) {
            Node nValue = (Node) nlList.item(i).getChildNodes().item(0);
            result.add(nValue.getNodeValue());
        }
        return result;
    }

    /*
     * Break up string with html line breaks for use in dialogs, etc.
     */
    public static String addBreaksToString(String original, int maxCharsPerLine) {

        String current = original;
        String result = "";

        while (current.length() > 0) {

            if (current.length() <= maxCharsPerLine) {
                result += current;
                break;
            }

            int index = current.substring(0, Math.min(current.length(), maxCharsPerLine)).lastIndexOf(" ");
            if (index == -1) {
                index = Math.min(current.length(), maxCharsPerLine);
            }
            index = Math.min(index + 1, current.length());

            result += current.substring(0, index) + "<BR>";
            current = current.substring(index);
        }

        return result;
    }

    public static double generateBins(CustomField field, Range r, boolean isLogScaleX) {

        //log scale
        if (isLogScaleX) {
            return 10;

            //percent fields
        } else if ((field.getColumnType() == ColumnType.DECIMAL || field.getColumnType() == ColumnType.FLOAT) && r.getMax() - r.getMin() <= 1 && r.getMax() <= 1) {

            return 0.05;

            //boolean fields
        } else if ((field.getColumnType() == ColumnType.INTEGER && field.getColumnLength() == 1) || field.getColumnType() == ColumnType.BOOLEAN) {

            return 1;

            //other fields
        } else {

            int min = (int) (r.getMin() - Math.abs(r.getMin() % (int) Math.pow(10, getNumDigits((int) (r.getMax() - r.getMin())) - 1)));
            int step1 = (int) Math.ceil((r.getMax() - min) / 25.0);
            int step2 = (int) Math.pow(10, getNumDigits(step1));
            int step = step2;
            while (step * 0.5 > step1) {
                step *= 0.5;
            }
            step = Math.max(step, 1);

            return step;
        }
    }

    public static int getNumDigits(int x) {
        x = Math.abs(x);
        int digits = 1;
        while (Math.pow(10, digits) < x) {
            digits++;
        }
        return digits;
    }

    public static String doubleToString(double d, int sigDigs) {
        String s = Double.toString(d);
        if (Math.abs(d) < 10) {
            int pos = s.indexOf(".");
            if (pos != -1) {
                s = s.substring(0, Math.min(pos + 3, s.length()));
            }
        }
        return s;
    }

    public static Object parseStringValueAs(Class c, String value) {

        if (c == String.class) {
            return value;
        }
        if (c == Long.class) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                return null;
            }
        }

        if (c == Float.class) {
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                return null;
            }
        }

        throw new UnsupportedOperationException("Parser doesn't deal with objects of type " + c);
    }

    /**
     * Unforunately List.toArray can't directly convert a List<Integer> to an
     * int[]. Apache Lang Commons has a utility functioin for this, so if we
     * ever decide to add Apache Lang Commons, we can eliminate this function
     * here.
     */
    public static int[] toIntArray(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * Extravt the file name from a pull path
     *
     * @param fullName The full path
     * @return the file name
     */
    public static String extractFileName(String fullName) {
        Pattern p = Pattern.compile(".*?([^\\\\/]+)$");
        Matcher m = p.matcher(fullName);

        return (m.find()) ? m.group(1) : "";
    }

    public static void deleteDirectory(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteDirectory(c);
            }
        }

        boolean b = f.delete();
        LOG.info("Deleting " + f.getAbsolutePath() + "... " + (b ? "SUCCESS" : "FAILED"));
    }

    public static String pluralize(int num, String singular, String plural) {
        return num == 1 ? singular : plural;
    }
}