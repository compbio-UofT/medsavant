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

    public SearcheableReference() {  };

    public SearcheableReference(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Field("id")
    public void setId(int id) {
        this.id = id;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    public Reference getReference() {
        this.reference = new Reference(id,name);
        return reference;
    }

    public int getId() {
        return reference.getID();
    }

    public String getName() {
        return reference.getName();
    }
}
