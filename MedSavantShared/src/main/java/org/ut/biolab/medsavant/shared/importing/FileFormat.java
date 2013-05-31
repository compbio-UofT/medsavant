/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.shared.importing;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author mfiume
 */
public abstract class FileFormat implements Serializable {

    public abstract String getName();

    public abstract TreeMap<Integer,String> getFieldNumberToFieldNameMap();

    public abstract TreeMap<Integer,Class> getFieldNumberToClassMap();

    // result is sorted
    public int[] getRequiredFieldIndexes() {

        Set<Integer> indexes = getFieldNumberToFieldNameMap().keySet();
        int[] result = new int[indexes.size()];
        int j = 0;
        for(int i : indexes){
            result[j++] = i;
        }

        return result;
    }
}
