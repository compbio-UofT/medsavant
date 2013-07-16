package org.ut.biolab.medsavant.shared.solr.mapper;

import org.apache.solr.common.SolrDocumentList;

import java.util.List;

/**
 * Generic interface for mappers.
 * @param <T>       The entity class for the mapper.
 */
public interface ResultMapper<T> {

    /**
     * Convert a list of class T entities
     * @param solrDocumentList
     * @return
     */
    public List<T> map(SolrDocumentList solrDocumentList);
}
