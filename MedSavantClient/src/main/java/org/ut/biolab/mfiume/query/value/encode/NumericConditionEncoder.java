package org.ut.biolab.mfiume.query.value.encode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NumericConditionEncoder {

    /**
     * Serialization
     */
    private static String DELIM = ";";

    public static double[] unencodeConditions(String s) {
        String[] arr = s.split(DELIM);
        double[] values = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            values[i] = Double.parseDouble(arr[i]);
        }
        return values;
    }

    public static String encodeConditions(double low, double high) {        
        return Double.toString(low)+DELIM+Double.toString(high);        
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

    public static String getDescription(double low, double high, double min, double max) {

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

        return s;
    }
}
