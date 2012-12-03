package org.ut.biolab.medsavant.client.view.genetics.family;

import org.broad.igv.feature.genome.Genome;

/**
 *
 * @author mfiume
 */
public class SimpleFamilyMattersGene implements Comparable {

    public final String chr;
    public final long start;
    public final long end;
    public final String name;
    //private Set<SimpleVariant> variants;

    public SimpleFamilyMattersGene(String chr, long start, long end, String name) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.name = name;
        //variants = new HashSet<SimpleVariant>();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.chr != null ? this.chr.hashCode() : 0);
        hash = 17 * hash + (int) (this.start ^ (this.start >>> 32));
        hash = 17 * hash + (int) (this.end ^ (this.end >>> 32));
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
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
        final SimpleFamilyMattersGene other = (SimpleFamilyMattersGene) obj;
        if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /*
     public void addVariant(SimpleVariant v) {
     variants.add(v);
     }
     */

    /*private Set<SimpleVariant> getVariants() {
     return variants;
     }
     */
    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SimpleFamilyMattersGene)) {
            return -1;
        }
        SimpleFamilyMattersGene other = (SimpleFamilyMattersGene) o;
        int chromCompare = (new Genome.ChromosomeComparator()).compare(this.chr, other.chr);
        if (chromCompare != 0) {
            return chromCompare;
        }
        return ((Long) this.start).compareTo(other.start);
    }
}
