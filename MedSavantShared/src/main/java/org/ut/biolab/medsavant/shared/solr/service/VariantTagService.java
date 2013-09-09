package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of variant tags.
 */
public class VariantTagService extends AbstractSolrService<VariantTagService> {

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
