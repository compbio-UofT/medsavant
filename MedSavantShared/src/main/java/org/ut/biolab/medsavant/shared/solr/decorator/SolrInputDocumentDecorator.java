package org.ut.biolab.medsavant.shared.solr.decorator;

import org.apache.solr.common.SolrInputDocument;

/**
 *  Interface for adding fields to Solr input documents.
 *
 *  Maybe need to rename, as to remove confusion with decorator pattern.
 */
public interface SolrInputDocumentDecorator {

    public SolrInputDocument decorate(SolrInputDocument document, Object payload);
}
