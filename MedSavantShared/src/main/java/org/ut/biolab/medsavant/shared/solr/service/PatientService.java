package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.model.Patient;
import org.ut.biolab.medsavant.shared.util.Entity;

/**
 * Manager indexing of Patient objects.
 */
public class PatientService extends AbstractSolrService<Patient> {

    @Override
    protected String getName() {
        return Entity.PATIENT;
    }

}
