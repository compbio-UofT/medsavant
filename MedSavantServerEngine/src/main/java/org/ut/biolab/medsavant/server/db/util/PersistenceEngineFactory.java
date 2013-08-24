package org.ut.biolab.medsavant.server.db.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Responsible for creating PersistenceEngine objects.
 */
public class PersistenceEngineFactory {

    private static final Log LOG = LogFactory.getLog(PersistenceEngineFactory.class);
    private static String PERSISTENCE_ENGINE_CLASS = "org.ut.biolab.medsavant.server.db.util.solr.SolrPersistenceEngine";
    private static PersistenceEngine persistenceEngine;

    public static PersistenceEngine getPersistenceEngine() {
        if (persistenceEngine == null) {
            persistenceEngine = createPersistenceEngine();
        }
        return persistenceEngine;
    }

    private static PersistenceEngine createPersistenceEngine() {
        Class c = null;
        try {
            c = Class.forName(PERSISTENCE_ENGINE_CLASS);
            persistenceEngine = (PersistenceEngine) c.newInstance();
        } catch (ClassNotFoundException e) {
            LOG.error("Bad class for persistence engine.");
        } catch (InstantiationException e) {
            LOG.error("Error instantiating persistence engine of class " + PERSISTENCE_ENGINE_CLASS);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal access on " + PERSISTENCE_ENGINE_CLASS);
        }
        return persistenceEngine;
    }
}
