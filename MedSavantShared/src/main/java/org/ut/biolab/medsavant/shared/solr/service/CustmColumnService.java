package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.CustomColumn;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of custom columns.
 */
public class CustmColumnService extends AbstractSolrService<CustomColumn>{

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
