/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author mfiume
 */
public class BEDRecord implements Comparable<BEDRecord>, Serializable {
    
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

    @Override
    public int compareTo(BEDRecord other) {
        if (!this.getName().equals(other.getName())) { return this.getName().compareTo(other.getName()); }
        else if (!this.getChrom().equals(other.getChrom())) { return this.getChrom().compareTo(other.getChrom()); }
        else if (this.getStart() != other.getStart()) { return ((Integer) this.getStart()).compareTo((Integer) other.getStart()); }
        else { return ((Integer) this.getEnd()).compareTo((Integer) other.getEnd()); }
    }
}
