/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
