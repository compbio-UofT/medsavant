/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.jidesoft.chart.model.ChartCategory;

import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;


/**
 *
 * @author mfiume
 */
public class ChartFrequencyMap {

    private List<FrequencyEntry> entries;
    private List<FrequencyEntry> originalEntries;

    public ChartFrequencyMap() {
        this.entries = new ArrayList<FrequencyEntry>();
    }

    public void addEntry(String key, long value) {
        this.entries.add(new FrequencyEntry(key, value));
    }

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
        if (originalEntries == null) {
            originalEntries = new ArrayList<FrequencyEntry>();
            originalEntries.addAll(entries);
        }
        Collections.sort(entries, new Comparator<FrequencyEntry>() {
            @Override
            public int compare(FrequencyEntry t, FrequencyEntry t1) {
                long diff = t.getFrequency() - t1.getFrequency();
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                }
                return 0;
            }
        });
        Collections.reverse(entries);
    }

    public void undoSortNumerically() {
        if (originalEntries != null) {
            entries = originalEntries;
            originalEntries = null;
        }
    }

    public void sortKaryotypically() {
        Collections.sort(entries, new Comparator<FrequencyEntry>() {
            private ChromosomeComparator comparator = new ChromosomeComparator();

            @Override
            public int compare(FrequencyEntry t, FrequencyEntry t1) {
                return comparator.compare(t.getKey(), t1.getKey());
            }
        });
    }

    /**
     * LogY flag indicates that the log of each value will later be taken. This
     * should be used for "show original frequencies" in order to get an accurate
     * difference (as it is stacked on top of current frequencies). Do not use
     * otherwise.
     */
    static ChartFrequencyMap subtract(ChartFrequencyMap map1, ChartFrequencyMap map2, boolean logY) {
        ChartFrequencyMap result = new ChartFrequencyMap();
        for (FrequencyEntry fe1 : map1.entries) {
            FrequencyEntry fe2 = map2.getEntry(fe1.getKey());
            if (fe2 == null) {
                result.addEntry(fe1.getKey(), fe1.getFrequency());
            } else if (logY) {
                result.addEntry(fe1.getKey(), (long)(Math.pow(10, Math.log10(fe1.getFrequency()) - Math.log10(fe2.getFrequency()))));
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
}
