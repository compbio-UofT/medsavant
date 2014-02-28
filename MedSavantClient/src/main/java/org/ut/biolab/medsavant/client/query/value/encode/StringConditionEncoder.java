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
package org.ut.biolab.medsavant.client.query.value.encode;

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
    private static final int MAX_CHARACTERS = 90;

    public static String getDescription(List<String> chosenValues, List<String> values) {

        if (chosenValues == null) { return "unknown"; }

        String s;
        if (chosenValues.isEmpty()) {
            s = "none";
        } else if (chosenValues.size() == 1 && (chosenValues.get(0).length() < MAX_CHARACTERS)) {            
            s = chosenValues.get(0);
        } else if (chosenValues.size() == 2){
            s = "either " + chosenValues.get(0) + " or " + chosenValues.get(1);
            if(s.length() > MAX_CHARACTERS){
                s = "any of " + chosenValues.size();
            }
        } else if (values != null && chosenValues.size() == values.size()) {
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
