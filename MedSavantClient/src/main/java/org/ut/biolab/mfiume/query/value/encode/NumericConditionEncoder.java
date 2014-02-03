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
package org.ut.biolab.mfiume.query.value.encode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NumericConditionEncoder {
    private static final Log LOG = LogFactory.getLog(NumericConditionEncoder.class);
      public static String ENCODING_NULL = "<NULL>";
    public static String ENCODING_NOTNULL = "<NOTNULL>";
    static DecimalFormat format;
    static DecimalFormatSymbols symbols;
    static char sep;

    static {
        DecimalFormat f = (DecimalFormat) DecimalFormat.getInstance();
        symbols = f.getDecimalFormatSymbols();
        sep = symbols.getDecimalSeparator();
        format = new DecimalFormat("#" + sep + "##");
    }
    /**
     * Serialization
     */
    private static String DELIM = ";";

    public static double[] unencodeConditions(String s) {
        String[] arr = s.split(DELIM);
        double[] values = new double[2];
        for (int i = 0; i < 2; i++) {
            values[i] = Double.parseDouble(arr[i]);
        }
        return values;
    }

    /*
    public static String encodeNull(){
        return ENCODING_NULL;
    }

    public static String encodeNotNull(){
        return ENCODING_NOTNULL;
    }
    */

    public static boolean encodesNull(String encoding){
        if (encoding == null) { return false; }
        String[] arr = encoding.split(DELIM);
        boolean includeNull = Boolean.parseBoolean(arr[2]);
        return includeNull;
    }

    /*public static boolean encodesNotNull(String encoding){
        return encoding.equals(ENCODING_NOTNULL);
    }*/


    public static String encodeConditions(double low, double high, boolean includeNull) {
        String lowString = Double.toString(low);
        String highString = Double.toString(high);
        //System.out.println("Encoding " + low + " " + high + "conditions as " + lowString + DELIM + highString + DELIM + includeNull);
        return lowString + DELIM + highString + DELIM + includeNull;
    }

    public static String getDescription(double[] ds) {

        double low = ds[0];
        double high = ds[1];

        String s;
        if (low == high) {
            s = ViewUtil.numToString(low);
        } else {
            s = ViewUtil.numToString(low) + " - " + ViewUtil.numToString(high);
        }
        return s;
    }

    public static String getDescription(double low, double high, double min, double max, boolean includeNull) {

        String s;

        if (low == min && high == max) {
            s = "anything";
        } else if (low == min && high != max) {
            s = "&#60; " + ViewUtil.numToString(high); // less than
        } else if (low != min && high == max) {
            s = "&#62; " + ViewUtil.numToString(low); // greater than
        } else if (low == high) {
            s = ViewUtil.numToString(low);
        } else {
            s = ViewUtil.numToString(low) + " - " + ViewUtil.numToString(high);
        }

        if (includeNull) {
            s += " or is missing";
        }

        return s;
    }
}
