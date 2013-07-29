package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of ProjectDetails objects
 */
public class ProjectDetailsService extends AbstractSolrService<ProjectDetails> {

    @Override
    protected String getName() {
        return Entity.PROJECT;
    }
}
