package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of VariantFile
 */
public class VariantFileService extends AbstractSolrService<SimpleVariantFile>{

    @Override
    protected String getName() {
        return Entity.VARIANT_FILE;
    }
}
