package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manage indexing of chromosome service.
 */
public class ChromosomeSolrService extends AbstractSolrService<Chromosome> {

    @Override
    protected String getName() {
        return Entity.CHROMOSOME;
    }
}
