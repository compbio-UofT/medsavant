package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of Annotation objects.
 */
public class AnnotationSolrService extends AbstractSolrService<Annotation> {

    @Override
    protected String getName() {
        return Entity.ANNOTATION;
    }
}
