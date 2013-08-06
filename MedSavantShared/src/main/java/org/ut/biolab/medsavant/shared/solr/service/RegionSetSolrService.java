package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle the indexing of RegionSet objects.
 */
public class RegionSetSolrService extends AbstractSolrService<RegionSet> {

    @Override
    protected String getName() {
        return Entity.REGION_SET;
    }
}
