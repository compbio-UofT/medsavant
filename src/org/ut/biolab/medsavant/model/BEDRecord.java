/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class BEDRecord {
    
    String name;
    String chrom;
    int start;
    int end;

    public BEDRecord(String chrom, int start, int end, String name) {
        this.name = name;
        this.chrom = chrom;
        this.start = start;
        this.end = end;
    }
    
    public String getChrom() {
        return chrom;
    }

    public int getEnd() {
        return end;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }
    
    public Vector toVector() {
        Vector v = new Vector();
        v.add(name);
        v.add(chrom);
        v.add(start);
        v.add(end);
        return v;
    }
    
    public static List<String> getFieldNames() {
        List<String> rs = new ArrayList<String>();
        rs.add("Name");
        rs.add("Chromosome");
        rs.add("Start");
        rs.add("End");
        return rs;
    }
    
    public static List<Class> getFieldClasses() {
        List<Class> rs = new ArrayList<Class>();
        rs.add(String.class);
        rs.add(String.class);
        rs.add(Integer.class);
        rs.add(Integer.class);
        return rs;
    }

    @Override
    public String toString() {
        return "BEDRecord{" + "name=" + name + ", chrom=" + chrom + ", start=" + start + ", end=" + end + '}';
    }
            
}
