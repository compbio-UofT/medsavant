package org.ut.biolab.medsavant.client.view.genetics.family;

import java.util.HashSet;
import java.util.Set;
import org.broad.igv.feature.genome.Genome;
import org.ut.biolab.medsavant.shared.util.MiscUtils;

/**
 *
 * @author mfiume
 */
public class SimpleFamilyMattersVariant implements Comparable {

    public final String chr;
    public final long pos;
    public final String ref;
    public final String alt;
    public final String type;
    private Set<SimpleFamilyMattersGene> genes;
    private final String homogenizedChr;

    public SimpleFamilyMattersVariant(String chr, long pos, String ref, String alt, String type) {
        this.chr = chr;
        this.homogenizedChr = MiscUtils.homogenizeSequence(chr);
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.type = type;
        this.genes = new HashSet<SimpleFamilyMattersGene>();
    }

    public Set<SimpleFamilyMattersGene> getGenes() {
        return genes;
    }

    public void addGene(SimpleFamilyMattersGene gene) {
        this.genes.add(gene);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.chr != null ? this.chr.hashCode() : 0);
        hash = 83 * hash + (int) (this.pos ^ (this.pos >>> 32));
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
        final SimpleFamilyMattersVariant other = (SimpleFamilyMattersVariant) obj;
        if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
            return false;
        }
        if (this.pos != other.pos) {
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

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SimpleFamilyMattersVariant)) {
            return -1;
        }
        SimpleFamilyMattersVariant other = (SimpleFamilyMattersVariant) o;
        boolean chromCompare = this.homogenizedChr.equals(other.homogenizedChr);
        if (!chromCompare) {
            return (this.homogenizedChr).compareTo(other.homogenizedChr);
        }

        int posCompare = ((Long) this.pos).compareTo(other.pos);
        if (posCompare != 0) {
            return posCompare;
        }

        return (this.alt.compareTo(other.alt));
    }

    @Override
    public String toString() {
        return "SimpleFamilyMattersVariant{" + "chr=" + chr + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", type=" + type + '}';
    }
}
