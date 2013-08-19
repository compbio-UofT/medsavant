package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.AnnotatedColumn;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of annotation columns.
 */
public class AnnotationColumnService extends AbstractSolrService<AnnotatedColumn>{

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
