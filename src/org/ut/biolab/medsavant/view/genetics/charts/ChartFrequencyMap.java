/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mfiume
 */
public class ChartFrequencyMap {

    public static class FrequencyEntry implements Comparable {
        private final int value;
        private final String key;

        public FrequencyEntry(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }

        public int compareTo(Object o) {
            if (o instanceof FrequencyEntry) {
                return this.getKey().compareTo(((FrequencyEntry) o).getKey());
            }
            return -1;
        }
    }
    
    private List<FrequencyEntry> entries;

    public ChartFrequencyMap() {
        this.entries = new ArrayList<FrequencyEntry>();
    }
    
    public void addEntry(String key, int value) {
        this.entries.add(new FrequencyEntry(key,value));
    }
    
    public List<FrequencyEntry> getEntries() {
        return entries;
    }
    
    public void addAll(Map<String, Integer> map) {
        for (String s : map.keySet()) {
            addEntry(s,map.get(s));
        }
    }
    
}
