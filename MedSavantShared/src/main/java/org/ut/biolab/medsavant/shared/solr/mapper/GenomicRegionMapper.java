package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableGenomicRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle mapping of GenomicRegions
 */
public class GenomicRegionMapper implements ResultMapper<GenomicRegion>{

    @Override
    public List<GenomicRegion> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableGenomicRegion> searcheableGenomicRegionList = binder.getBeans(SearcheableGenomicRegion.class, solrDocumentList);
        return toModelList(searcheableGenomicRegionList);
    }

    @Override
    public List<GenomicRegion> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<GenomicRegion> toModelList(List<SearcheableGenomicRegion> searcheableGenomicRegionList) {
        List<GenomicRegion> genomicRegionList = new ArrayList<GenomicRegion>();
        for (SearcheableGenomicRegion searcheableGenomicRegion : searcheableGenomicRegionList) {
            genomicRegionList.add(searcheableGenomicRegion.getRegion());
        }
        return genomicRegionList;
    }
}
