/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import com.jidesoft.chart.model.ChartCategory;
import com.jidesoft.range.CategoryRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author mfiume
 */
public class ChartFrequencyMap {

    static ChartFrequencyMap subtract(ChartFrequencyMap map1, ChartFrequencyMap map2) {
        ChartFrequencyMap result = new ChartFrequencyMap();
        for (FrequencyEntry fe1 : map1.entries) {
            FrequencyEntry fe2 = map2.getEntry(fe1.getKey());
            if (fe2 == null) {
                result.addEntry(fe1.getKey(), fe1.getFrequency());
            } else {
                result.addEntry(fe1.getKey(), fe1.getFrequency() - fe2.getFrequency());
            }
        }
        return result;
    }

    List<ChartCategory> getCategories() {

        List<ChartCategory> map = new ArrayList<ChartCategory>();
        for (FrequencyEntry fe : getEntries()) {
            ChartCategory cat = new ChartCategory<String>(fe.getKey());
            map.add(cat);
        }
        return map;
    }

    long getMax() {
        long max = Integer.MIN_VALUE;
        for (FrequencyEntry fe : this.getEntries()) {
            if (fe.getFrequency() > max) {
                max = fe.getFrequency();
            }
        }
        return max;
    }

    FrequencyEntry getEntry(String key) {
        for (FrequencyEntry fe : entries) {
            if (fe.getKey().equals(key)) {
                return fe;
            }
        }
        return null;
    }

    private static class NumericComparator implements Comparator {

        public int compare(Object o1, Object o2) {

            if (o1 instanceof FrequencyEntry && o2 instanceof FrequencyEntry) {
                long f1 = ((FrequencyEntry) o1).getFrequency();
                long f2 = ((FrequencyEntry) o2).getFrequency();

                if (f1 > f2) {
                    return 1;
                } else if (f1 < f2) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        }
    }

    private static class ChromosomeComparator implements Comparator {

        /**
         * @param chr1
         * @param chr2
         * @return
         */
        public int compare(Object o1, Object o2) {

            String chr1 = ((FrequencyEntry) o1).getKey();
            String chr2 = ((FrequencyEntry) o2).getKey();

            try {

                // Special rule -- put the mitochondria at the end
                if (chr1.equals("chrM") || chr1.equals("MT")) {
                    return 1;
                } else if (chr2.equals("chrM") || chr2.equals("MT")) {
                    return -1;
                }

                // Find the first digit
                int idx1 = findDigitIndex(chr1);
                int idx2 = findDigitIndex(chr2);
                if (idx1 == idx2) {
                    String alpha1 = idx1 == -1 ? chr1 : chr1.substring(0, idx1);
                    String alpha2 = idx2 == -1 ? chr2 : chr2.substring(0, idx2);
                    int alphaCmp = alpha1.compareTo(alpha2);
                    if (alphaCmp != 0) {
                        return alphaCmp;
                    } else {
                        int dig1 = Integer.parseInt(chr1.substring(idx1));
                        int dig2 = Integer.parseInt(chr2.substring(idx2));
                        return dig1 - dig2;
                    }
                } else if (idx1 == -1) {
                    return 1;

                } else if (idx2 == -1) {
                    return -1;
                }
                return idx1 - idx2;
            } catch (Exception numberFormatException) {
                return 0;
            }

        }

        int findDigitIndex(String chr) {

            int n = chr.length() - 1;
            if (!Character.isDigit(chr.charAt(n))) {
                return -1;
            }

            for (int i = n - 1; i > 0; i--) {
                if (!Character.isDigit(chr.charAt(i))) {
                    return i + 1;
                }
            }
            return 0;
        }
    }
    private List<FrequencyEntry> entries;

    public ChartFrequencyMap() {
        this.entries = new ArrayList<FrequencyEntry>();
    }

    public void addEntry(String key, long value) {
        this.entries.add(new FrequencyEntry(key, value));
    }

    /*
    public void removeEntry(String key) {
    FrequencyEntry toremove = null;
    for (FrequencyEntry fe : this.entries) {
    if (fe.getKey().equals(key)) {
    toremove = fe;
    break;
    }
    }
    this.entries.remove(toremove);
    }
     *
     */
    public List<FrequencyEntry> getEntries() {
        return entries;
    }

    public void addAll(Map<String, Integer> map) {
        for (String s : map.keySet()) {
            addEntry(s, map.get(s));
        }
    }

    public void sort() {
        Collections.sort(entries);
    }

    public void sortNumerically() {
        Collections.sort(entries, new NumericComparator());
        Collections.reverse(entries);
    }

    public void sortKaryotypically() {
        Collections.sort(entries, new ChromosomeComparator());
    }
}
