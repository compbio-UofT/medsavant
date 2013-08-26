package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.VariantTag;

/**
 * Adapter class for mapping Solr documents to VariantTag objects.
 */
public class SearcheableVariantTag {

    VariantTag tag;

    public SearcheableVariantTag() { }

    public SearcheableVariantTag(VariantTag tag) {
        this.tag = tag;
    }

    @Field("key")
    public void setKey(String key) {
        this.tag.setKey(key);
    }

    @Field("value")
    public void setValue(String value) {
        this.tag.setValue(value);
    }

    public VariantTag getTag() {
        return tag;
    }

    public String getKey() {
        return tag.getKey();
    }

    public String getValue() {
        return tag.getValue();
    }
}
