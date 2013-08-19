package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle the indexing of Gene objects.
 */
public class GeneSolrService extends AbstractSolrService<Gene> {

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
