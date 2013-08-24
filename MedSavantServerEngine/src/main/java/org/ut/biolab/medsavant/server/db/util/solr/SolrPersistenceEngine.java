package org.ut.biolab.medsavant.server.db.util.solr;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

import java.sql.SQLException;

/**
 * Implements PersistenceEngine operations for the Solr backend.
 */
public class SolrPersistenceEngine implements PersistenceEngine {

    private static final Log LOG = LogFactory.getLog(SolrPersistenceEngine.class);

    @Override
    public boolean fieldExists(String sid, String tableName, String fieldName) {
        return false;
    }

    @Override
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException {
        throw new UnsupportedOperationException();
    }
}
