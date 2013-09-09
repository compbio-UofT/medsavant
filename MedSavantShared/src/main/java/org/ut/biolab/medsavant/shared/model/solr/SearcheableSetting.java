package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.Setting;

/**
 *  Adapter class for mapping settings.
 */
public class SearcheableSetting {

    private Setting setting;

    public SearcheableSetting() { }

    public SearcheableSetting(Setting setting) {
        this.setting = setting;
    }

    @Field("key")
    public void setKey(String key) {
        this.setting.setKey(key);
    }

    @Field("value")
    public void setValue(String value) {
        this.setting.setValue(value);
    }

    public String getKey() {
        return setting.getKey();
    }

    public String getValue() {
        return setting.getValue();
    }

    public Setting getSetting() {
        return setting;
    }
}
