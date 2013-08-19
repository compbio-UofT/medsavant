package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Handle the indexing of OntologyTerm objects.
 */
public class OntologyTermSolrService extends AbstractSolrService<Ontology>{

    @Override
    protected String getName() {
        return Entity.MEDSAVANT;
    }
}
