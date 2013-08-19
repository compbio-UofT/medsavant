package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.User;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of User objects.
 */
public class UserSolrService extends AbstractSolrService<User> {

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
