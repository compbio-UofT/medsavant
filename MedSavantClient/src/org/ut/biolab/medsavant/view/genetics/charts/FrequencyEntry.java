/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

/**
 *
 * @author mfiume
 */
public class FrequencyEntry implements Comparable {
        private final long value;
        private String key;

        public FrequencyEntry(String key, long value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public long getFrequency() {
            return value;
        }

        public int compareTo(Object o) {
            if (o instanceof FrequencyEntry) {
                return this.getKey().compareTo(((FrequencyEntry) o).getKey());
            }
            return -1;
        }

    void setKey(String string) {
        this.key = string;
    }
    }

