package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableOntologyTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map OntologyTerm.
 */
public class OntologyTermMapper implements ResultMapper<OntologyTerm> {

    @Override
    public List<OntologyTerm> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableOntologyTerm> searcheableOntologyTermList = binder.getBeans(SearcheableOntologyTerm.class, solrDocumentList);
        return toModelList(searcheableOntologyTermList);
    }

    @Override
    public List<OntologyTerm> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<OntologyTerm> toModelList(List<SearcheableOntologyTerm> searcheableOntologyTermList) {
        List<OntologyTerm> ontologyTermList = new ArrayList<OntologyTerm>();
        for (SearcheableOntologyTerm searcheableOntologyTerm : searcheableOntologyTermList) {
            ontologyTermList.add(searcheableOntologyTerm.getTerm());
        }
        return ontologyTermList;
    }
}
