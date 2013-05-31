package org.ut.biolab.mfiume.query.value.encode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class StringConditionEncoder {

    /**
     * Serialization
     */
    private static String DELIM = ";";
    //private static final String ENCODING_ALL = "<anything>";
    //private static final String ENCODING_NONE = "<nothing>";
    public static String ENCODING_NULL = "<NULL>";
    public static String ENCODING_NOTNULL = "<NOTNULL>";

    public static List<String> unencodeConditions(String s) {
        if (s.isEmpty()) { /* || s.equals(ENCODING_NONE)) {*/
            return new ArrayList<String>();
        }
        String[] arr = s.split(DELIM);
        return Arrays.asList(arr);
    }

    public static String encodeConditions(List<String> values) {/*, List<String> from) {*/
        /*if (values.size() == from.size()) {
         return ENCODING_ALL;
         } else if (values.isEmpty()) {
         return ENCODING_NONE;
         }*/
        StringBuilder result = new StringBuilder();
        for (Object string : values) {
            result.append(string);
            result.append(DELIM);
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    /*public static boolean encodesNone(String encoding) {
     return encoding.equals(ENCODING_NONE);
     }

     public static boolean encodesAll(String encoding) {
     return encoding.equals(ENCODING_ALL);
     }*/
    public static String getDescription(List<String> chosenValues) {
        String s;
        if (chosenValues.isEmpty()) {
            s = "none";
        } else if (chosenValues.size() == 1) {
            s = chosenValues.get(0);
        } else if (chosenValues.size() == 2) {
            s = "either " + chosenValues.get(0) + " or " + chosenValues.get(1);
        } else {
            s = "any of " + chosenValues.size();
        }
        return s;
    }

    public static String getDescription(List<String> chosenValues, List<String> values) {
        String s;
        if (chosenValues.isEmpty()) {
            s = "none";
        } else if (chosenValues.size() == 1) {
            s = chosenValues.get(0);
        } else if (chosenValues.size() == 2) {
            s = "either " + chosenValues.get(0) + " or " + chosenValues.get(1);
        } else if (chosenValues.size() == values.size()) {
            s = "anything";
        } else {
            s = "any of " + chosenValues.size();
        }
        return s;
    }

    public static boolean encodesNull(String encoding) {
        if (encoding == null) { return false; }
        return encoding.equals(ENCODING_NULL);
    }

    public static boolean encodesNotNull(String encoding) {
        if (encoding == null) { return false; }
        return encoding.equals(ENCODING_NOTNULL);
    }
}
