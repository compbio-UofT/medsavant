package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle the indexing of GenomicRegion objects.
 */
public class GenomicRegionSolrService extends AbstractSolrService<GenomicRegion> {

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
