package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Setting;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of Setting entities.
 */
public class SettingSolrService extends AbstractSolrService<Setting>{

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
