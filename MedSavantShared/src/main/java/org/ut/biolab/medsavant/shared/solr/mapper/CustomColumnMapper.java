package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.CustomColumn;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableCustomColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle mapping of custom columns.
 */
public class CustomColumnMapper implements ResultMapper<CustomColumn> {
    @Override
    public List<CustomColumn> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableCustomColumn> searcheableCustomColumnList = binder.getBeans(SearcheableCustomColumn.class, solrDocumentList);
        return toModelList(searcheableCustomColumnList);
    }

    private List<CustomColumn> toModelList(List<SearcheableCustomColumn> searcheableCustomColumnList) {
        List<CustomColumn> customColumnList = new ArrayList<CustomColumn>();
        for (SearcheableCustomColumn searcheableCustomColumn : searcheableCustomColumnList) {
            customColumnList.add(searcheableCustomColumn.getCustomColumn());
        }
        return customColumnList;
    }

    @Override
    public List<CustomColumn> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }
}
