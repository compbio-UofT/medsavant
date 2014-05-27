/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.app.mendelclinic.model;

import java.util.HashSet;
import java.util.Set;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class MendelVariant implements Comparable {

    public final String chr;
    public final long start_pos;
    public final long end_pos;
    public final String ref;
    public final String alt;
    public final String type;
    private Set<MendelGene> genes;
    private final String homogenizedChr;
    

    public MendelVariant(String chr, long start_pos, long end_pos, String ref, String alt, String type) {
        this.chr = chr;
        this.homogenizedChr = MiscUtils.homogenizeSequence(chr);
        this.start_pos = start_pos;
        this.end_pos = end_pos;
        this.ref = ref;
        this.alt = alt;
        this.type = type;
        this.genes = new HashSet<MendelGene>();
    }

    public Set<MendelGene> getGenes() {
        return genes;
    }

    public void addGene(MendelGene gene) {
        this.genes.add(gene);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.chr != null ? this.chr.hashCode() : 0);
        hash = 83 * hash + (int) (this.start_pos ^ (this.start_pos >>> 32));
        hash = 83 * hash + (int) (this.end_pos ^ (this.end_pos >>> 32));
        hash = 83 * hash + (this.ref != null ? this.ref.hashCode() : 0);
        hash = 83 * hash + (this.alt != null ? this.alt.hashCode() : 0);
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
        final MendelVariant other = (MendelVariant) obj;
        if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
            return false;
        }
        if (this.start_pos != other.start_pos || this.end_pos != other.end_pos) {
            return false;
        }
        
        if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
            return false;
        }
        if ((this.alt == null) ? (other.alt != null) : !this.alt.equals(other.alt)) {
            return false;
        }
        return true;
    }

    //orders by chrom, start, end, alt.
    @Override
    public int compareTo(Object o) {
        if (!(o instanceof MendelVariant)) {
            return -1;
        }
        MendelVariant other = (MendelVariant) o;
        boolean chromCompare = this.homogenizedChr.equals(other.homogenizedChr);
        if (!chromCompare) {
            return (this.homogenizedChr).compareTo(other.homogenizedChr);
        }

        int posCompare = ((Long) this.start_pos).compareTo(other.start_pos);
        if (posCompare != 0) {
            return posCompare;
        }

        int endCompare = ((Long) this.end_pos).compareTo(other.end_pos);
        if (endCompare != 0) {
            return endCompare;
        }
        return (this.alt.compareTo(other.alt));
    }

    @Override
    public String toString() {
        return "SimpleFamilyMattersVariant{" + "chr=" + chr + ", start_pos=" + start_pos + ", end_pos="+end_pos+" ref=" + ref + ", alt=" + alt + ", type=" + type + '}';
    }
}
