/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mfiume
 */
public class Util {

    private static Random numGen = new Random();

    public static Color getRandomColor() {
        return new Color(numGen.nextInt(256), numGen.nextInt(256), numGen.nextInt(256));
    }

    public static boolean isQuantatitiveClass(Class c) {
        if (c == Integer.class || c == Long.class || c == Short.class || c == Double.class || c == Float.class) { return true; }
        return false;
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

    public static String getListFilterToString(String filtername, List<String> acceptableValues) {
        String s = filtername + " = (";
        for (String v : acceptableValues) {
            s += v + ",";
        }
        if (!acceptableValues.isEmpty()) {
            s = s.substring(0,s.length()-1);
        }
        s += ")";
        return s;
    }

    public static List<Boolean> getTrueList(int size) {
        List<Boolean> results = new ArrayList<Boolean>();
        for (int i = 0; i < size; i++) {
            results.add(true);
        }
        return results;
    }
}
