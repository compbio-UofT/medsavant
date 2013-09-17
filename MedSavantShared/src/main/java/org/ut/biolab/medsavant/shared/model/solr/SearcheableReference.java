package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Reference;

import java.util.List;

/**
 * Adapter class for mapping Solr documents to Reference objects.
 */
public class SearcheableReference {

    private Reference reference;

    public SearcheableReference() {
        reference = new Reference();
    }

    public SearcheableReference(Reference reference) {
        this.reference = reference;
    }

    @Field("id")
    public void setId(int id) {
        this.reference.setId(id);
    }

    @Field("name")
    public void setName(String name) {
        this.reference.setName(name);
    }

    @Field("url")
    public void setURL(String url) {
        this.reference.setUrl(url);
    }

    @Field("chromosome_ids")
    public void setChromosomeIds(List<String> chromosomeIds) {
        this.reference.setChromosomeIds(chromosomeIds);
    }

    public Reference getReference() {
        return reference;
    }

    public int getId() {
        return reference.getID();
    }

    public String getName() {
        return reference.getName();
    }

    public String getURL() {
        return reference.getUrl();
    }

    public List<String> getChromosomeIds() {
        return reference.getChromosomeIds();
    }
}
