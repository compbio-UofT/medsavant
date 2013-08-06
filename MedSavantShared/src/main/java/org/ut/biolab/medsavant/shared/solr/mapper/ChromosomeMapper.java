package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.common.SolrDocumentList;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.solr.SearcheableChromosome;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map chromosomes.
 */
public class ChromosomeMapper implements ResultMapper<Chromosome>{

    @Override
    public List<Chromosome> map(SolrDocumentList solrDocumentList) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<SearcheableChromosome> searcheableChromosomeList = binder.getBeans(SearcheableChromosome.class,solrDocumentList);
        return toModelList(searcheableChromosomeList);
    }

    @Override
    public List<Chromosome> map(SolrDocumentList solrDocumentList, Map<String, String> aggregateFieldMap) {
        throw new UnsupportedOperationException();
    }

    private List<Chromosome> toModelList(List<SearcheableChromosome> searcheableChromosomeList) {
        List<Chromosome> chromosomeList = new ArrayList<Chromosome>(searcheableChromosomeList.size());
        for (SearcheableChromosome searcheableChromosome : searcheableChromosomeList) {
            chromosomeList.add(searcheableChromosome.getChromosome());
        }
        return chromosomeList;
    }
}
