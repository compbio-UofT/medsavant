package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Reference;

/**
 * Adapter class for mapping Solr documents to Reference objects.
 */
public class SearcheableReference {

    private Reference reference;

    private int id;
    private String name;
    private String url;

    public SearcheableReference() {  };

    public SearcheableReference(int id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    @Field("id")
    public void setId(int id) {
        this.id = id;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("url")
    public void setURL(String url) {
        this.url = url;
    }

    public Reference getReference() {
        this.reference = new Reference(id,name, url);
        return reference;
    }

    public int getId() {
        return reference.getID();
    }

    public String getName() {
        return reference.getName();
    }

    public String getUrl() {
        return url;
    }
}
