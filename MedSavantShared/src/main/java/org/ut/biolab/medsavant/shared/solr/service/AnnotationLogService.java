package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of AnnotationLog.
 */
public class AnnotationLogService extends AbstractSolrService<AnnotationLog>{

    @Override
    protected String getName() {
        return Entity.ANNOTATION_LOG;
    }
}
