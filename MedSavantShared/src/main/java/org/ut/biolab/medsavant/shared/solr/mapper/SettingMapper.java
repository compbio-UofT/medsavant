package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Setting;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle mapping of Setting entities.
 */
public class SettingMapper implements ResultMapper<Setting> {

    @Override
    public List<Setting> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableSetting> searcheableSettingList = binder.getBeans(SearcheableSetting.class, solrDocumentList);
        return toModelList(searcheableSettingList);
    }

    private List<Setting> toModelList(List<SearcheableSetting> searcheableSettingList) {
        List<Setting> settingList = new ArrayList<Setting>();
        for (SearcheableSetting searcheableSetting : searcheableSettingList) {
            settingList.add(searcheableSetting.getSetting());
        }
        return settingList;
    }

    @Override
    public List<Setting> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }
}
