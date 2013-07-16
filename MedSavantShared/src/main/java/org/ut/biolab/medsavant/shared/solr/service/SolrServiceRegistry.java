package org.ut.biolab.medsavant.shared.solr.service;

import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

/**
 * Keep track of references of Solr services.
 */
public class SolrServiceRegistry {

    private static AbstractSolrService variantService;

    /**
     * Get the appropriate solr service based on the entity string.
     * @param entityName            The entity name.
     * @return                      An instance of a solr service.
     */
    public static AbstractSolrService getService(String entityName) throws InitializationException {

        AbstractSolrService service = null;

        if ("variant".equals(entityName)) {
            if (variantService == null) {
                variantService =  new VariantService();
                variantService.initialize();
            }
            return variantService;
        }

        return null;
    }
}
