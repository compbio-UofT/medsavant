/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb.importfile;

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
        map.put(0, "Chromosome");
        map.put(1, "Start");
        map.put(2, "End");
        map.put(3, "Name");
        return map;
    }

    public TreeMap<Integer, Class> getFieldNumberToClassMap() {
        TreeMap<Integer,Class> map = new TreeMap<Integer,Class>();
        map.put(0, String.class);
        map.put(1, String.class); // todo: 1 and 2 should be ints!!
        map.put(2, String.class);
        map.put(3, String.class);
        return map;
    }
    
    
    
}
