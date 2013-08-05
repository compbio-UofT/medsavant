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
package org.ut.biolab.medsavant.shared.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Handle the creation of QueryManager instances.
 */
public class QueryManagerFactory {

    private static final Log LOG = LogFactory.getLog(QueryManagerFactory.class);

    private static String QUERY_MANAGER_CLASS;
    private static QueryManager queryManager;

    static {
        //ToDo read from a file
        QUERY_MANAGER_CLASS = "org.ut.biolab.medsavant.shared.query.solr.SolrQueryManager";
    }

    public static QueryManager getQueryManager() {
        if (queryManager == null) {
            queryManager = createQueryManger();
        }
        return queryManager;
    }

    private static QueryManager createQueryManger() {

        QueryManager qManager = null;

        try {
            Class c = Class.forName(QUERY_MANAGER_CLASS);
            qManager = (QueryManager) c.newInstance();
        } catch (ClassNotFoundException e) {
            LOG.error("Error initializing QueryManager of type " + QUERY_MANAGER_CLASS);
        } catch (InstantiationException e) {
            LOG.error("Error initializing QueryManager of type " + QUERY_MANAGER_CLASS);
        } catch (IllegalAccessException e) {
            LOG.error("Error initializing QueryManager of type " + QUERY_MANAGER_CLASS);
        }

        return qManager;
    }

}
