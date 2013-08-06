package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle the indexing of Reference objects.
 */
public class ReferenceSolrService extends AbstractSolrService<Reference> {

    @Override
    protected String getName() {
        return Entity.REFERENCE;
    }
}
