package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of Cohort objects.
 */
public class CohortService extends AbstractSolrService<Cohort>{

    @Override
    protected String getName() {
        return Entity.COHORT;
    }
}
