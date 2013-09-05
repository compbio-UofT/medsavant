package org.ut.biolab.medsavant.server.db.util.ice;

import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.ConnectionPool;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.DBSettings;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.util.PersistenceEngine;
import org.ut.biolab.medsavant.server.db.variants.ImportUpdateManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationLogManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Annotation;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Implements PersistenceEngine operations for the ICE backend.
 */
public class ICEPersistenceEngine implements PersistenceEngine {

    private static final Log LOG = LogFactory.getLog(ICEPersistenceEngine.class);

    @Override
    public boolean fieldExists(String sid, String tableName, String fieldName) throws SQLException, SessionExpiredException {
        ResultSet rs = ConnectionController.executeQuery(sid, "SHOW COLUMNS IN " + tableName);

        while (rs.next()) {
            if (rs.getString(1).equals(fieldName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public DbTable importTable(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);

        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberOfColumns = rsMetaData.getColumnCount();

        while (rs.next()) {
            table.addColumn(rs.getString(1), DBUtils.getColumnTypeString(rs.getString(2)), DBUtils.getColumnLength(rs.getString(2)));
        }

        return table;
    }

    @Override
    public TableSchema importTableSchema(String sessionId, String tablename) throws SQLException, SessionExpiredException {
        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        DbTable table = schema.addTable(tablename);
        TableSchema ts = new TableSchema(table);

        LOG.info(String.format("Executing %s on %s...", "DESCRIBE " + tablename, sessionId));
        ResultSet rs = ConnectionController.executeQuery(sessionId, "DESCRIBE " + tablename);

        while (rs.next()) {
            table.addColumn(rs.getString(1), DBUtils.getColumnTypeString(rs.getString(2)), DBUtils.getColumnLength(rs.getString(2)));
            ts.addColumn(rs.getString(1), ColumnType.fromString(DBUtils.getColumnTypeString(rs.getString(2))), DBUtils.getColumnLength(rs.getString(2)));
        }

        return ts;
    }

    @Override
    public void dropTable(String sessID, String tableName) throws SQLException, SessionExpiredException {
        ConnectionController.executeUpdate(sessID, "DROP TABLE IF EXISTS " + tableName + ";");
    }

    /**
     * Add a new user to MedSavant.
     *
     * @param sessID the session we're logged in as
     * @param user the user to add
     * @param pass the password
     * @param level the user's level
     * @throws SQLException
     */
    @Override
    public synchronized void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            // TODO: Transactions aren't supported for MyISAM, so this has no effect.
            conn.setAutoCommit(false);

            conn.executePreparedUpdate("CREATE USER ?@'localhost' IDENTIFIED BY ?", user, new String(pass));
            grantPrivileges(sessID, user, level);
            conn.commit();
        } catch (SQLException sqlx) {
            conn.rollback();
            throw sqlx;
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            String dbName = ConnectionController.getDBName(sessID);
            LOG.info("Granting " + level + " privileges to " + name + " on " + dbName + "...");
            switch (level) {
                case ADMIN:
                    conn.executePreparedUpdate("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, CREATE USER, DELETE, DROP, FILE, GRANT OPTION, INSERT, SELECT, UPDATE ON *.* TO ?@'localhost'", name);
                    conn.executePreparedUpdate(String.format("GRANT GRANT OPTION ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT ALTER, CREATE, CREATE TEMPORARY TABLES, DELETE, DROP, INSERT, SELECT, UPDATE ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.db TO ?@'localhost'", name);
                    break;
                case USER:
                    conn.executePreparedUpdate(String.format("GRANT CREATE TEMPORARY TABLES, SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set_membership TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
                case GUEST:
                    conn.executePreparedUpdate(String.format("GRANT SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
            }
            LOG.info("... granted.");
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException {
        ConnectionController.executePreparedUpdate(sid, "DROP USER ?@'localhost'", name);
    }

    @Override
    public void registerCredentials(String sessionId, String user, String password, String dbName) throws SQLException {
        ConnectionController.registerCredentials(sessionId, user, password, dbName);
    }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws RemoteException, SQLException, SessionExpiredException {
        String sessID = SessionController.getInstance().registerNewSession(adminName, new String(rootPassword), "");

        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.createStatement().execute("DROP DATABASE IF EXISTS " + dbName);
        } finally {
            conn.close();
        }
    }

    @Override
    public String createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws RemoteException, SQLException, SessionExpiredException {
        SessionController sessController = SessionController.getInstance();
        String sessID = sessController.registerNewSession(adminName, new String(rootPassword), "");
        Connection conn = ConnectionController.connectPooled(sessID);
        conn.createStatement().execute("CREATE DATABASE " + dbName);
        conn.close();

        ConnectionController.switchDatabases(sessID, dbName); //closes all connections
        conn = ConnectionController.connectPooled(sessID);

        return sessID;
    }

    @Override
    public void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.ServerlogTableSchema.getTableName() + "` ("
                            + "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`user` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                            + "`event` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                            + "`description` blob,"
                            + "`timestamp` datetime NOT NULL,"
                            + "PRIMARY KEY (`id`)"
                            + ") ENGINE=MyISAM;");
            String[] users = UserManager.getInstance().getUserNames(sessID);
            for (String u : users) {
                conn.executePreparedUpdate(String.format("GRANT INSERT ON %s TO ?", MedSavantDatabase.ServerlogTableSchema.getTableName()), u);
            }

            conn.executeUpdate(MedSavantDatabase.RegionSetTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");
            conn.executeUpdate(MedSavantDatabase.RegionSetMembershipTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.CohortTableSchema.getTableName() + "` ("
                            + "`cohort_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`name` varchar(255) CHARACTER SET latin1 NOT NULL,"
                            + "PRIMARY KEY (`cohort_id`,`project_id`) USING BTREE"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.CohortmembershipTableSchema.getTableName() + "` ("
                            + "`cohort_id` int(11) unsigned NOT NULL,"
                            + "`patient_id` int(11) unsigned NOT NULL,"
                            + "PRIMARY KEY (`patient_id`,`cohort_id`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.ReferenceTableSchema.getTableName() + "` ("
                            + "`reference_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`name` varchar(50) COLLATE latin1_bin NOT NULL,"
                            + "`url` varchar(200) COLLATE latin1_bin DEFAULT NULL,"
                            + "PRIMARY KEY (`reference_id`), "
                            + "UNIQUE KEY `name` (`name`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.AnnotationTableSchema.getTableName() + "` ("
                            + "`annotation_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`program` varchar(100) COLLATE latin1_bin NOT NULL DEFAULT '',"
                            + "`version` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`path` varchar(500) COLLATE latin1_bin NOT NULL DEFAULT '',"
                            + "`has_ref` tinyint(1) NOT NULL,"
                            + "`has_alt` tinyint(1) NOT NULL,"
                            + "`type` int(11) unsigned NOT NULL,"
                            + "`is_end_inclusive` tinyint(1) NOT NULL,"
                            + "PRIMARY KEY (`annotation_id`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.ProjectTableSchema.getTableName() + "` "
                            + "(`project_id` int(11) unsigned NOT NULL AUTO_INCREMENT, "
                            + "`name` varchar(50) NOT NULL, "
                            + "PRIMARY KEY (`project_id`), "
                            + "UNIQUE KEY `name` (`name`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.PatienttablemapTableSchema.getTableName() + "` ("
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`patient_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                            + "PRIMARY KEY (`project_id`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.VarianttablemapTableSchema.getTableName() + "` ("
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`update_id` int(11) unsigned NOT NULL,"
                            + "`published` boolean NOT NULL,"
                            + "`variant_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                            + "`annotation_ids` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                            + "`variant_subset_tablename` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`subset_multiplier` float(10,6) DEFAULT 1,"
                            + "UNIQUE KEY `unique` (`project_id`,`reference_id`,`update_id`)"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.VariantpendingupdateTableSchema.getTableName() + "` ("
                            + "`upload_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`action` int(11) unsigned NOT NULL,"
                            + "`status` int(5) unsigned NOT NULL DEFAULT '0',"
                            + "`timestamp` datetime DEFAULT NULL,"
                            + "`user` varchar(200) DEFAULT NULL,"
                            + "PRIMARY KEY (`upload_id`) USING BTREE"
                            + ") ENGINE=MyISAM;");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.ChromosomeTableSchema.getTableName() + "` ("
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`contig_id` int(11) unsigned NOT NULL,"
                            + "`contig_name` varchar(100) COLLATE latin1_bin NOT NULL,"
                            + "`contig_length` int(11) unsigned NOT NULL,"
                            + "`centromere_pos` int(11) unsigned NOT NULL,"
                            + "PRIMARY KEY (`reference_id`,`contig_id`) USING BTREE"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.AnnotationFormatTableSchema.getTableName() + "` ("
                            + "`annotation_id` int(11) unsigned NOT NULL,"
                            + "`position` int(11) unsigned NOT NULL,"
                            + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                            + "`filterable` tinyint(1) NOT NULL,"
                            + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                            + "PRIMARY KEY (`annotation_id`,`position`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.PatientformatTableSchema.getTableName() + "` ("
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`position` int(11) unsigned NOT NULL,"
                            + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                            + "`filterable` tinyint(1) NOT NULL,"
                            + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                            + "PRIMARY KEY (`project_id`,`position`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.VariantformatTableSchema.getTableName() + "` ("
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`update_id` int(11) unsigned NOT NULL,"
                            + "`position` int(11) unsigned NOT NULL,"
                            + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                            + "`filterable` tinyint(1) NOT NULL,"
                            + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                            + "PRIMARY KEY (`project_id`,`reference_id`,`update_id`,`position`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            conn.executeUpdate(
                    "CREATE TABLE  `default_patient` ("
                            + "`patient_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                            + "`family_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`hospital_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`idbiomom` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`idbiodad` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                            + "`gender` int(11) unsigned DEFAULT NULL,"
                            + "`affected` int(1) unsigned DEFAULT NULL,"
                            + "`dna_ids` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                            + "`bam_url` varchar(5000) COLLATE latin1_bin DEFAULT NULL,"
                            + "`phenotypes` varchar(10000) COLLATE latin1_bin DEFAULT NULL,"
                            + "PRIMARY KEY (`patient_id`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            String createVariantStatement;
            if (MedSavantServerEngine.USE_INFINIDB_ENGINE) {

                createVariantStatement = "CREATE TABLE  default_variant "
                        + "( upload_id  INTEGER, "
                        + "file_id  INTEGER, "
                        + "variant_id  INTEGER, "
                        + "dna_id  varchar(100) ,"
                        + "chrom  varchar(5), "
                        + "position  INTEGER, "
                        + "dbsnp_id  varchar(45)  ,"
                        + "ref  varchar(30)  , "
                        + "alt  varchar(30)  , "
                        + "qual  float(10,0) , filter  varchar(500)  , "
                        + "variant_type  varchar(10)  , "
                        + "zygosity  varchar(20)  , "
                        + "gt  varchar(10)  ,"
                        + "custom_info  varchar(8000)  ) ENGINE=INFINIDB;";
            } else {
                createVariantStatement =
                        "CREATE TABLE  `default_variant` ("
                                + "`upload_id` int(11),"
                                + "`file_id` int(11),"
                                + "`variant_id` int(11),"
                                + "`dna_id` varchar(100) COLLATE latin1_bin NOT NULL,"
                                + "`chrom` varchar(30) COLLATE latin1_bin NOT NULL DEFAULT '',"
                                + "`position` int(11),"
                                + "`dbsnp_id` varchar(45) COLLATE latin1_bin DEFAULT NULL,"
                                + "`ref` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                                + "`alt` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                                + "`qual` float(10,0) DEFAULT NULL,"
                                + "`filter` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                                + "`variant_type` varchar(10) COLLATE latin1_bin DEFAULT NULL,"
                                + "`zygosity` varchar(20) COLLATE latin1_bin DEFAULT NULL,"
                                + "`gt` varchar(10) COLLATE latin1_bin DEFAULT NULL,"
                                + "`custom_info` varchar(10000) COLLATE latin1_bin DEFAULT NULL"
                                + ") ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;";
            }
            //System.out.println(createVariantStatement);
            conn.executeUpdate(createVariantStatement);

            conn.executeUpdate(MedSavantDatabase.GeneSetTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");
            conn.executeUpdate(MedSavantDatabase.OntologyTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");
            conn.executeUpdate(MedSavantDatabase.OntologyInfoTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.SettingsTableSchema.getTableName() + "` ("
                            + "`setting_key` varchar(100) COLLATE latin1_bin NOT NULL,"
                            + "`setting_value` varchar(300) COLLATE latin1_bin NOT NULL,"
                            + "PRIMARY KEY (`setting_key`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            conn.executeUpdate(MedSavantDatabase.VariantTagTableSchema.getCreateQuery() + " ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

            conn.executeUpdate(
                    "CREATE TABLE `" + MedSavantDatabase.VariantStarredTableSchema.getTableName() + "` ("
                            + "`project_id` int(11) unsigned NOT NULL,"
                            + "`reference_id` int(11) unsigned NOT NULL,"
                            + "`upload_id` int(11) NOT NULL,"
                            + "`file_id` int(11) NOT NULL,"
                            + "`variant_id` int(11) NOT NULL,"
                            + "`user` varchar(200) COLLATE latin1_bin NOT NULL,"
                            + "`description` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                            + "`timestamp` datetime NOT NULL"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

            conn.executeUpdate(
                    "CREATE TABLE  `" + MedSavantDatabase.VariantFileTableSchema.getTableName() + "` ("
                            + "`upload_id` int(11) NOT NULL,"
                            + "`file_id` int(11) NOT NULL,"
                            + "`file_name` varchar(500) COLLATE latin1_bin NOT NULL,"
                            + "UNIQUE KEY `unique` (`upload_id`,`file_id`)"
                            + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");
        } finally {
            conn.close();
        }
    }

    @Override
    public void testConnection(String sessID) throws SQLException, SessionExpiredException {
        Connection conn = null;
        try {
            conn = ConnectionController.connectPooled(sessID);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public void initializePooledConnection(ConnectionPool pool) throws SQLException {
        Connection c = null;
        try {
            c = pool.getConnection();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) throws RemoteException, SessionExpiredException, SQLException {
        // Create basic fields.
        String tableName = DBSettings.getVariantTableName(projID, refID, updID);
        if (sub) {
            tableName += "_subset";
        }
        TableSchema variantSchema = new TableSchema(MedSavantDatabase.schema, tableName, BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        for (CustomField f: ProjectManager.getInstance().getCustomVariantFields(sessID, projID, refID, updID)) {
            variantSchema.addColumn(f);
        }

        String s = "";
        for (DbColumn c : variantSchema.getColumns()) {
            s += c.getColumnNameSQL() + " ";
        }
        LOG.info("Creating variant table " + tableName + " with fields " + s);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            for (int ann: annIDs) {
                AnnotationFormat annFmt = AnnotationManager.getInstance().getAnnotationFormat(sessID, ann);
                for (CustomField f: annFmt.getCustomFields()) {
                    variantSchema.addColumn(f);
                }
            }
            String updateString;
            if (MedSavantServerEngine.USE_INFINIDB_ENGINE) {

                updateString = variantSchema.getCreateQuery() + " ENGINE=INFINIDB;";
            } else {
                updateString = variantSchema.getCreateQuery() + " ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;";
            }
            //System.out.println(updateString);
            conn.executeUpdate(updateString);
        } finally {
            conn.close();
        }
        return tableName;
    }

    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn((sub ? MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME : MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME)));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), OrderObject.Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            return rs.getString(1);
        } else {
            return null;
        }
    }

    @Override
    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME),
                table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME),
                table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), OrderObject.Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            return new Object[]{rs.getString(1), rs.getString(2), rs.getFloat(3)};
        } else {
            return null;
        }
    }

    @Override
    public int doImport(String sessionID, int projectID, int referenceID, boolean publishUponCompletion, File[] vcfFiles, boolean includeHomozygousReferenceCalls, String[][] tags) throws Exception {

        return ImportUpdateManager.doImport(sessionID, projectID, referenceID, publishUponCompletion, vcfFiles, includeHomozygousReferenceCalls, tags, true);
    }

    @Override
    public void annotateAndUploadTSVFiles(String sessionID, int updateID, int projectID, int referenceID, int[] annotationIDs, CustomField[] customFields, File[] tsvFiles, File workingDir) throws Exception {
        LOG.info("Annotating TSV files, working directory is " + workingDir.getAbsolutePath());

        ProjectManager.getInstance().setCustomVariantFields(sessionID, projectID, referenceID, updateID, customFields);

        //TODO: shouldn't these (tablename and tablenamesub) functions consider the customfields?
        String tableName = ProjectManager.getInstance().createVariantTable(
                sessionID,
                projectID,
                referenceID,
                updateID,
                annotationIDs,
                true);

        String tableNameSubset = ProjectManager.getInstance().createVariantTable(
                sessionID,
                projectID,
                referenceID,
                updateID,
                annotationIDs,
                false,
                true);

        boolean needsToUnlock = ImportUpdateManager.acquireLock(sessionID);

        try {
            //get annotation information
            Annotation[] annotations = ImportUpdateManager.getAnnotationsFromIDs(annotationIDs, sessionID);

            File[] splitTSVFiles = ImportUpdateManager.splitFilesByDNAAndFileID(tsvFiles, ImportUpdateManager.createSubdir(workingDir, "split"));
            File[] annotatedTSVFiles = ImportUpdateManager.annotateTSVFiles(sessionID, splitTSVFiles, annotations, customFields, ImportUpdateManager.createSubdir(workingDir, "annotate"));
            ImportUpdateManager.uploadTSVFiles(sessionID, annotatedTSVFiles, tableName, tableNameSubset, ImportUpdateManager.createSubdir(workingDir, "subset"));
            ImportUpdateManager.registerTable(sessionID, projectID, referenceID, updateID, tableName, tableNameSubset, annotationIDs);
            ImportUpdateManager.dropTablesPriorToUpdateID(sessionID, projectID, referenceID, updateID);
            ImportUpdateManager.setAnnotationStatus(sessionID, updateID, AnnotationLog.Status.PENDING);

        } catch (Exception e) {
            AnnotationLogManager.getInstance().setAnnotationLogStatus(sessionID, updateID, AnnotationLog.Status.ERROR);
            throw e;
        } finally {
            ImportUpdateManager.releaseLock(sessionID, needsToUnlock);
        }
    }


}
