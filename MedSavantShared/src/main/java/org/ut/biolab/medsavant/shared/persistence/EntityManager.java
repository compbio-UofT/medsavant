package org.ut.biolab.medsavant.shared.persistence;

import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

import java.util.List;

/**
 * Entity Manager
 *
 * Deals with inserting data into the database.
 */
public interface EntityManager {

    /**
     * Persist an entity into the data store.
     * @param entity        The entity
     */
    public void persist(Object entity) throws InitializationException;

    /**
     * Persist a list of entities to the data store.
     * @param entities      List of entities.
     */
    public <T> void persistAll(List<T> entities) throws InitializationException;


}
