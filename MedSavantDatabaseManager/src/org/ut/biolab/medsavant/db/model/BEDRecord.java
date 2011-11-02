/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class BEDRecord implements Comparable {
    
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

    public int compareTo(Object o) {
        if(o.getClass() != this.getClass()) return -1;
        BEDRecord other = (BEDRecord)o;
        
                if (!this.getName().equals(other.getName())) { return this.getName().compareTo(other.getName()); }
                else if (!this.getChrom().equals(other.getChrom())) { return this.getChrom().compareTo(other.getChrom()); }
        else if (this.getStart() != other.getStart()) { return ((Integer) this.getStart()).compareTo((Integer) other.getStart()); }
        else { return ((Integer) this.getEnd()).compareTo((Integer) other.getEnd()); }
        
    }
            
}
