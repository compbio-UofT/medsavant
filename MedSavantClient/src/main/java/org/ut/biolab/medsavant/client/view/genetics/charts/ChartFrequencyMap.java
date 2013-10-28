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
        if(!key.isEmpty()){
            this.entries.add(new FrequencyEntry(key, value));
        }
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
