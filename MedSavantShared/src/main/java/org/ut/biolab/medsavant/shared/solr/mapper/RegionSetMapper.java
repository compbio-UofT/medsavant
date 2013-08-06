package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableRegionSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map Region sets.
 */
public class RegionSetMapper implements ResultMapper<RegionSet> {

    @Override
    public List<RegionSet> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableRegionSet> searcheableRegionSetList = binder.getBeans(SearcheableRegionSet.class, solrDocumentList);
        return toModelList(searcheableRegionSetList);
    }

    @Override
    public List<RegionSet> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<RegionSet> toModelList(List<SearcheableRegionSet> searcheableRegionSetList) {
        List<RegionSet> regionSetList = new ArrayList<RegionSet>();
        for (SearcheableRegionSet searcheableRegionSet : searcheableRegionSetList) {
            regionSetList.add(searcheableRegionSet.getRegionSet());
        }
        return regionSetList;
    }



}
