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
        private final int value;
        private final String key;

        public FrequencyEntry(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public int getFrequency() {
            return value;
        }

        public int compareTo(Object o) {
            if (o instanceof FrequencyEntry) {
                return this.getKey().compareTo(((FrequencyEntry) o).getKey());
            }
            return -1;
        }
    }

