package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map Reference objects.
 */
public class ReferenceMapper implements ResultMapper<Reference> {

    @Override
    public List<Reference> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableReference> searcheableReferenceList = binder.getBeans(SearcheableReference.class, solrDocumentList);
        return toModelList(searcheableReferenceList);
    }

    @Override
    public List<Reference> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<Reference> toModelList(List<SearcheableReference> searcheableReferenceList) {
        List<Reference> referenceList = new ArrayList<Reference>();
        for (SearcheableReference searcheableReference : searcheableReferenceList) {
            referenceList.add(searcheableReference.getReference());
        }
        return referenceList;
    }

}
