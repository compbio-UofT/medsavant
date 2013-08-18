package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableGeneSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handle mapping of gene sets.
 */
public class GeneSetMapper implements ResultMapper<GeneSet> {

    @Override
    public List<GeneSet> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableGeneSet> searcheableGeneSetList = binder.getBeans(SearcheableGeneSet.class, solrDocumentList);
        return toModelList(searcheableGeneSetList);
    }

    @Override
    public List<GeneSet> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<GeneSet> toModelList(List<SearcheableGeneSet> searcheableGeneSetList) {
        List<GeneSet> geneSetList = new ArrayList<GeneSet>();
        for (SearcheableGeneSet searcheableGeneSet : searcheableGeneSetList) {
            geneSetList.add(searcheableGeneSet.getGeneSet());
        }
        return geneSetList;
    }
}
