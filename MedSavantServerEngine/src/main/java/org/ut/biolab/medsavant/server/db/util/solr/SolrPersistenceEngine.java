package org.ut.biolab.medsavant.server.db.util.solr;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionPool;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.shared.db.ColumnDef;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.CustomColumn;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.util.Entity;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements PersistenceEngine operations for the Solr backend.
 */
public class SolrPersistenceEngine implements PersistenceEngine {

    private static final Log LOG = LogFactory.getLog(SolrPersistenceEngine.class);

    private static QueryManager queryManager = QueryManagerFactory.getQueryManager();

    @Override
    public boolean fieldExists(String sid, String tableName, String fieldName) {
        return false;
    }

    @Override
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        return null;
    }

    @Override
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {

        String entityName;
        List<CustomField> basicFields;
        if (tablename.contains(Entity.VARIANT)) {
            entityName = Entity.VARIANT;
            basicFields = Arrays.asList(BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        } else if (tablename.contains(Entity.PATIENT)) {
            entityName = Entity.PATIENT;
            basicFields = Arrays.asList(BasicPatientColumns.REQUIRED_PATIENT_FIELDS);
        } else {
            return null;
        }

        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName");
        query.setParameter("entityName", entityName);
        List<CustomField> customFields = query.execute();
        customFields = ListUtils.union(customFields, basicFields);

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(entityName);
        TableSchema ts = new TableSchema(table);
        for (CustomField customField : customFields) {
            table.addColumn(customField.getColumnName(), customField.getColumnType().toString(), customField.getColumnLength());
            ts.addColumn(customField.getColumnName(), customField.getColumnType(), customField.getColumnLength());
        }
        return ts;
    }

    @Override
    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException { }

    @Override
    public void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException { }

    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException { }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException { }

    @Override
    public void registerCredentials(String sessionId, String user, String password, String dbName) { }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) { }

    @Override
    public String createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws RemoteException, SQLException {
        SessionController sessController = SessionController.getInstance();
        return sessController.registerNewSession(adminName, new String(rootPassword), "");
    }

    @Override
    public void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException {  }

    @Override
    public void testConnection(String sessIDs) {
        //nothing for now
    }

    @Override
    public void initializePooledConnection(ConnectionPool pool) {
        //nothing for now
    }

    @Override
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) {
        return "Variant";
    }

    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) {
        return "Variant";
    }

    @Override
    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) {
        return new Object[] { "Variant", "Variant" , 1};
    }
}
