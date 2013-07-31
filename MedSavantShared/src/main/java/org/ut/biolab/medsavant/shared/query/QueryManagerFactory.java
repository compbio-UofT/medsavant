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
