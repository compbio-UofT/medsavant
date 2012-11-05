package org.ut.biolab.medsavant.view.genetics.variantinfo;

/**
 *
 * @author mfiume
 */
public class SimpleVariant {

    long pos;
    String chr;
    String ref;
    String alt;
    String type;

    public SimpleVariant(String chr, long pos, String ref, String alt, String type) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.type = type;
    }

    @Override
    public String toString() {
        return "SimpleVariant{" + "pos=" + pos + ", chr=" + chr + ", ref=" + ref + ", alt=" + alt + ", type=" + type + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (int) (this.pos ^ (this.pos >>> 32));
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
        if (this.pos != other.pos) {
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
