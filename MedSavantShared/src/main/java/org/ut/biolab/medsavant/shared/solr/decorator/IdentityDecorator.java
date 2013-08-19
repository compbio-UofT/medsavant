package org.ut.biolab.medsavant.shared.solr.decorator;

import org.apache.solr.common.SolrInputDocument;

/**
 *  In case we don't want to decorate the documents at all.
 */
public class IdentityDecorator implements SolrInputDocumentDecorator {

    @Override
    public SolrInputDocument decorate(SolrInputDocument document, Object payload) {
        //ignore decorator info
        return document;
    }
}
