package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableGene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  Handle mapping of genes.
 */
public class GeneMapper implements ResultMapper<Gene> {

    @Override
    public List<Gene> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableGene> searcheableGenes = binder.getBeans(SearcheableGene.class, solrDocumentList);
        return toModelList(searcheableGenes);
    }

    @Override
    public List<Gene> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    public List<Gene> toModelList(List<SearcheableGene> searcheableGeneList) {
        List<Gene> geneList = new ArrayList<Gene>();
        for (SearcheableGene searcheableGene : searcheableGeneList) {
            geneList.add(searcheableGene.getGene());
        }
        return geneList;
    }
}
