package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of GeneralLog objects
 */
public class GeneralLogService extends AbstractSolrService<GeneralLog> {

    @Override
    protected String getName() {
        return Entity.GENERAL_LOG;
    }
}
