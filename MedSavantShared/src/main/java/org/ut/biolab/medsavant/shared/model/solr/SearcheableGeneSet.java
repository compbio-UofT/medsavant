package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.GeneSet;

/**
 * Adapter class from GeneSet
 */
public class SearcheableGeneSet {


    private GeneSet geneSet;

    private String genome;
    private String type;
    private int size;

    public SearcheableGeneSet() { }

    public SearcheableGeneSet(GeneSet geneSet) {
        this.geneSet = geneSet;
    }

    public GeneSet getGeneSet() {
        geneSet = new GeneSet(genome, type, size);
        return geneSet;
    }

    @Field("genome")
    public void setGenome(String genome) {
        this.genome = genome;
    }

    @Field("type")
    public void setType(String type) {
        this.type = type;
    }

    @Field("size")
    public void setSize(int size) {
        this.size = size;
    }

    public String getGenome() {
        return genome;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }
}
