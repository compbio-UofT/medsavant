package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle indexing of AnnotationFormat entities.
 */
public class AnnotationFormatSolrService extends AbstractSolrService<AnnotationFormat> {

    @Override
    protected String getName() {
        return Entity.ANNOTATION_FORMAT;
    }
}
