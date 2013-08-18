package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of GeneSets.
 */
public class GeneSetSolrService extends AbstractSolrService<GeneSet> {

    @Override
    protected String getName() {
        return Entity.GENE_SET;
    }
}
