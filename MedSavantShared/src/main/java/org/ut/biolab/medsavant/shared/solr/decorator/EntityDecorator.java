package org.ut.biolab.medsavant.shared.solr.decorator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrInputDocument;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Decorate Solr documents with entity discriminant.
 */
public class EntityDecorator implements SolrInputDocumentDecorator {

    private static Log LOG = LogFactory.getLog(EntityDecorator.class);

    @Override
    public SolrInputDocument decorate(SolrInputDocument document, Object payload) {
        Class clazz = (Class) payload;
        document.addField(Entity.ENTITY_DISCRIMINANT_FIELD, clazz.getName());

        LOG.debug("Decorating document with entity field " + clazz.getName());

        return document;
    }
}
