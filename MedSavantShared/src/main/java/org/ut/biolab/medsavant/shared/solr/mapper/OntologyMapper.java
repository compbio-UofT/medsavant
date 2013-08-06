package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableOntology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map Ontology objects.
 */
public class OntologyMapper implements ResultMapper<Ontology> {

    @Override
    public List<Ontology> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableOntology> searcheableOntologyList = binder.getBeans(SearcheableOntology.class, solrDocumentList);
        return toModelList(searcheableOntologyList);
    }

    @Override
    public List<Ontology> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<Ontology> toModelList(List<SearcheableOntology> searcheableOntologyList) {
        List<Ontology> ontologyList = new ArrayList<Ontology>();
        for (SearcheableOntology searcheableOntology : searcheableOntologyList) {
            ontologyList.add(searcheableOntology.getOntology());
        }
        return ontologyList;
    }
}