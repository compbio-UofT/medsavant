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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.shared.model.GenomicRegion;

/**
 *
 * @author mfiume, rammar
 */
public class SimpleVariant {

    private final long start_pos;
    private final long end_pos;
    private final String chr;
    private final String ref;
    private final String alt;
    private final String type;

    public SimpleVariant(String chr, long start_pos, long end_pos, String ref, String alt, String type) {
        this.chr = chr;
        this.start_pos = start_pos;
        this.end_pos = end_pos;
        this.ref = ref;
        this.alt = alt;
        this.type = type;
    }

    public long getStartPosition() {
        return start_pos;
    }

    public long getEndPosition() {
        return end_pos;
    }

    public String getChromosome() {
        return chr;
    }

    public String getReference() {
        return ref;
    }

    public String getAlternate() {
        return alt;
    }

    public String getType() {
        return type;
    }
    
    public GenomicRegion getGenomicRegion(String name){
        //GenomicRegion(String name, String chrom, int start, int end)             
        return new GenomicRegion(name, chr, (int)start_pos, (int)end_pos);
    }
    
    public GenomicRegion getGenomicRegion(){
        return getGenomicRegion("Chr: "+chr+"Pos: "+start_pos+"-"+end_pos);
    }
    
    @Override
    public String toString() {
        return "SimpleVariant{" + "startpos=" + start_pos + ", endpos="+end_pos+", chr=" + chr + ", ref=" + ref + ", alt=" + alt + ", type=" + type + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (int) (this.start_pos ^ (this.start_pos >>> 32));
        hash = 37 * hash + (int) (this.end_pos ^ (this.end_pos >>> 32));
        hash = 37 * hash + (this.chr != null ? this.chr.hashCode() : 0);
        hash = 37 * hash + (this.ref != null ? this.ref.hashCode() : 0);
        hash = 37 * hash + (this.alt != null ? this.alt.hashCode() : 0);
        hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleVariant other = (SimpleVariant) obj;
        if (this.start_pos != other.start_pos) {
            return false;
        }
        if (this.end_pos != other.end_pos) {
            return false;
        }
        if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
            return false;
        }
        if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
            return false;
        }
        if ((this.alt == null) ? (other.alt != null) : !this.alt.equals(other.alt)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
