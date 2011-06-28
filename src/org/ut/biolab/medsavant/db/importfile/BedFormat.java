/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.importfile;

import java.util.TreeMap;

/**
 *
 * @author mfiume
 */
public class BedFormat extends FileFormat {

    public String getName() {
        return "BED";
    }

    public TreeMap<Integer, String> getFieldNumberToFieldNameMap() {
        TreeMap<Integer,String> map = new TreeMap<Integer,String>();
        map.put(0, "Name");
        map.put(1, "Chromosome");
        map.put(3, "Start");
        map.put(4, "End");
        return map;
    }

    public TreeMap<Integer, Class> getFieldNumberToClassMap() {
        TreeMap<Integer,Class> map = new TreeMap<Integer,Class>();
        map.put(0, String.class);
        map.put(1, String.class);
        map.put(3, Integer.class);
        map.put(4, Integer.class);
        return map;
    }
    
    
    
}
