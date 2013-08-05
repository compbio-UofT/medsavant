package org.ut.biolab.medsavant.shared.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Handle the creation of EntityManager instances.
 */
public class EntityManagerFactory {

    private static final Log LOG = LogFactory.getLog(EntityManagerFactory.class);

    private static String ENTITY_MANAGER_CLASS;
    private static EntityManager entityManager;

    static {
        ENTITY_MANAGER_CLASS = "org.ut.biolab.medsavant.shared.persistence.solr.SolrEntityManager";
    }

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = createEntityManager();
        }
        return entityManager;
    }

    private static EntityManager createEntityManager() {

        EntityManager eManager = null;

        Class c = null;
        try {
            c = Class.forName(ENTITY_MANAGER_CLASS);
            eManager = (EntityManager) c.newInstance();
        } catch (ClassNotFoundException e) {
            LOG.error("Error initializing QueryManager of type " + ENTITY_MANAGER_CLASS);
        } catch (InstantiationException e) {
            LOG.error("Error initializing QueryManager of type " + ENTITY_MANAGER_CLASS);
        } catch (IllegalAccessException e) {
            LOG.error("Error initializing QueryManager of type " + ENTITY_MANAGER_CLASS);
        }
        return eManager;
    }



}
