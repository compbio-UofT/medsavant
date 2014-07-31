/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.db.admin;

import org.ut.biolab.medsavant.server.serverapi.ReferenceManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.server.serverapi.SessionManager;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFileTableSchema;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.schema;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.shared.util.VersionSettings;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 *
 * @author mfiume
 */
public class SetupMedSavantDatabase extends MedSavantServerUnicastRemoteObject implements SetupAdapter {

    private static final Log LOG = LogFactory.getLog(SetupMedSavantDatabase.class);

    //public static final boolean ENGINE_INFINIDB = false;
    private static SetupMedSavantDatabase instance;
    private static final String BRIGHTHOUSE_ENGINE = "BRIGHTHOUSE";
    private static final String MYISAM_ENGINE = "MyISAM";
    //private static final String VARIANT_FILE_IBTABLE_SUFFIX = "_ib";

    public static synchronized SetupMedSavantDatabase getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SetupMedSavantDatabase();
        }
        return instance;
    }

    private SetupMedSavantDatabase() throws RemoteException {
    }

    @Override
    public void createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws IOException, SQLException, RemoteException, SessionExpiredException, Exception {

        SessionManager sessController = SessionManager.getInstance();
        String sessID = sessController.registerNewSession(adminName, new String(rootPassword), "");

        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.createStatement().execute("CREATE DATABASE " + dbName);
        } finally {
            conn.close();
        }

        ConnectionController.switchDatabases(sessID, dbName); //closes all connections
        conn = ConnectionController.connectPooled(sessID);

        UserManager userMgr = UserManager.getInstance();

        createTables(sessID);

        try {
            addRootUser(sessID, conn, rootPassword);
            addDefaultReferenceGenomes(sessID);
            addDBSettings(sessID, VersionSettings.getVersionString());
            // Grant the admin user privileges first so that they can give grants to everybody else.
            userMgr.grantPrivileges(sessID, adminName, UserLevel.ADMIN);

            populateGenes(sessID);

            // Grant permissions to everybody else.
            for (String user : userMgr.getUserNames(sessID)) {
                if (!user.equals(adminName)) {
                    userMgr.grantPrivileges(sessID, user, userMgr.getUserLevel(sessID, user));
                }
            }

        } finally {
            conn.close();
        }

        // We populate the ontology tables on a separate thread because it can take a very long time, and users aren't going to be
        // looking at ontologies any time soon.  The initial session has no associated database, so we need to reregister with
        // our newly created database.
        sessController.unregisterSession(sessID);
        sessID = sessController.registerNewSession(adminName, new String(rootPassword), dbName);
        OntologyManager.getInstance().populate(sessID);
    }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws SQLException, RemoteException, SessionExpiredException, Exception {

        String sessID = SessionManager.getInstance().registerNewSession(adminName, new String(rootPassword), "");

        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.createStatement().execute("DROP DATABASE IF EXISTS " + dbName);
        } finally {
            conn.close();
        }
    }

    private void createTables(String sessID) throws IOException, SQLException, RemoteException, SessionExpiredException {

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
            Set<String> users = UserManager.getInstance().getAllUserNames(sessID);
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
                    + "`tags` varchar(500) COLLATE latin1_bin NOT NULL,"
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
                    + "PRIMARY KEY (`patient_id`), "
                    + "UNIQUE KEY `hospital_id` (`hospital_id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

            /*
             String q = 
             "CREATE TABLE "+MedSavantDatabase.UserCommentTableSchema.getTableName()+"("
             + "	project_id INTEGER,"
             + "	reference_id INTEGER,"
             + "	chrom varchar(5),"
             + "	start_position integer,"
             + "	end_position integer,"
             + "	ref varchar(255),"
             + "	alt varchar(255),	"
             + "	ontology_id integer, "
             + "	user varchar(200),"
             + "	is_approved boolean not null default false,"
             + "	is_included boolean not null default false,"
             + "	is_pending_review boolean not null default false,"
             + "	creation_date DATE,"
             + "	last_modified TIMESTAMP,"
             + "	variant_comment text,"
             + "	primary key(chrom, start_position, end_position, ref, alt),"
             + "	FOREIGN KEY(ontology_id) REFERENCES ontology(id) ON UPDATE CASCADE ON DELETE RESTRICT," //foreign keys ignored for now.
             + "	FOREIGN KEY(project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE,"
             + "	FOREIGN KEY(reference_id) REFERENCES reference(reference_id) ON UPDATE RESTRICT ON DELETE RESTRICT"
             + ")ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Disease-specific comments, diseases are ontology terms.'"
             ;
             */
            String q = "CREATE TABLE " + MedSavantDatabase.UserRoleTableSchema.getTableName() + "("
                    + " user_role_id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + " role_name varchar(64) not null, "
                    + " role_description text "
                    + ")ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='User role definitions'";
            LOG.info(q);
            conn.executeUpdate(q);

            q = "CREATE TABLE " + MedSavantDatabase.UserRoleAssignmentTableSchema.getTableName() + "("
                    + " user VARCHAR(300) NOT NULL, "
                    + " fk_user_role_id INTEGER NOT NULL, "
                    + " PRIMARY KEY(user, fk_user_role_id), "
                    + " FOREIGN KEY(fk_user_role_id) REFERENCES " + MedSavantDatabase.UserRoleTableSchema.getTableName() + "(" + MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ID + ") ON UPDATE CASCADE ON DELETE CASCADE "
                    + ")ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Assignments of users to roles.'";
            LOG.info(q);
            conn.executeUpdate(q);

            q = "CREATE TABLE " + MedSavantDatabase.UserCommentGroupTableSchema.getTableName() + "("
                    + " user_comment_group_id	INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "	project_id INTEGER,"
                    + "	reference_id INTEGER,"
                    + "	chrom varchar(5),"
                    + "	start_position integer,"
                    + "	end_position integer,"
                    + "	ref varchar(255),"
                    + "	alt varchar(255),	"
                    + "	UNIQUE(project_id, reference_id, chrom, start_position, end_position, ref, alt),"
                    + "	FOREIGN KEY(project_id) REFERENCES " + MedSavantDatabase.ProjectTableSchema.getTableName() + "(" + MedSavantDatabase.ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID + ") ON UPDATE CASCADE ON DELETE CASCADE,"
                    + "	FOREIGN KEY(reference_id) REFERENCES " + MedSavantDatabase.ReferenceTableSchema.getTableName() + "(" + MedSavantDatabase.ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID + ") ON UPDATE RESTRICT ON DELETE RESTRICT,"
                    + " KEY(chrom) "
                    + ")ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Disease-specific comments, diseases are ontology terms.'";

            LOG.info(q);
            conn.executeUpdate(q);

            q = "CREATE TABLE " + MedSavantDatabase.UserCommentTableSchema.getTableName() + "("
                    + " user_comment_id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + " fk_user_comment_group_id INTEGER NOT NULL, "
                    + " fk_parent_user_comment_id INTEGER, "
                    + "	user varchar(200),"
                    + " ontology varchar(10), "
                    + "	ontology_id varchar(30), "
                    + "	is_approved boolean not null default false,"
                    + "	is_included boolean not null default false,"                    
                    + " is_deleted boolean not null default false,"
                    + "	creation_date DATE,"
                    + "	last_modified TIMESTAMP,"
                    + "	variant_comment text,"
                    + " FOREIGN KEY(ontology) REFERENCES " + MedSavantDatabase.OntologyTableSchema.getTableName() + "(" + MedSavantDatabase.OntologyColumns.ONTOLOGY.getColumnName() + ") ON UPDATE CASCADE ON DELETE RESTRICT," //foreign keys ignored for now.
                    + "	FOREIGN KEY(ontology_id) REFERENCES " + MedSavantDatabase.OntologyTableSchema.getTableName() + "(" + MedSavantDatabase.OntologyColumns.ID.getColumnName() + ") ON UPDATE CASCADE ON DELETE RESTRICT," //foreign keys ignored for now.
                    + "	FOREIGN KEY(fk_user_comment_group_id) REFERENCES " + MedSavantDatabase.UserCommentGroupTableSchema.getTableName() + "(" + MedSavantDatabase.UserCommentGroupTableSchema.COLUMNNAME_OF_USER_COMMENT_GROUP_ID + ") ON UPDATE RESTRICT ON DELETE RESTRICT,"
                    + "	FOREIGN KEY(fk_parent_user_comment_id) REFERENCES " + MedSavantDatabase.UserCommentTableSchema.getTableName() + "(" + MedSavantDatabase.UserCommentTableSchema.COLUMNNAME_OF_USER_COMMENT_ID + ") ON UPDATE RESTRICT ON DELETE RESTRICT"
                    + ")ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin";
            LOG.info(q);
            conn.executeUpdate(q);

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
                createVariantStatement
                        = "CREATE TABLE  `default_variant` ("
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
                    + "`project_id` int(11) NOT NULL,"
                    + "`reference_id` int(11) NOT NULL,"
                    + "`file_name` varchar(500) COLLATE latin1_bin NOT NULL,"
                    + "UNIQUE KEY `unique` (`upload_id`,`file_id`)"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

            makeVariantFileTable(sessID, false);

        } finally {
            conn.close();
        }
    }

    public static String getVariantFileIBTableName() {
        TableSchema table = MedSavantDatabase.VariantFileIBTableSchema;
        return table.getTableName();
    }

    public static synchronized TableSchema makeTemporaryVariantFileIBTable(String sid) throws IOException, SQLException, SessionExpiredException {
        int i = 0;
        String tableName;
        final String suffixPrefix = "_ib_tmp";
        String suffix;
        do {
            suffix = suffixPrefix + i;
            tableName = VariantFileTableSchema.TABLE_NAME_PREFIX + suffix;
            i++;
        } while (DBUtils.tableExists(sid, tableName));

        makeVariantFileTable(sid, true, tableName, BRIGHTHOUSE_ENGINE);
        return new MedSavantDatabase.VariantFileTableSchema(schema, suffix);

    }

    public static void makeVariantFileIBTable(String sid) throws IOException, SQLException, SessionExpiredException {
        makeVariantFileTable(sid, true);
    }

    public static void makeVariantFileTable(String sid, boolean brighthouse) throws IOException, SQLException, SessionExpiredException {
        String tableName;
        String engine;

        if (brighthouse) {
            tableName = getVariantFileIBTableName();
            engine = BRIGHTHOUSE_ENGINE;
        } else {
            tableName = MedSavantDatabase.VariantFileTableSchema.getTableName();
            engine = MYISAM_ENGINE;
        }
        makeVariantFileTable(sid, brighthouse, tableName, engine);
    }

    private static void makeVariantFileTable(String sid, boolean brighthouse, String tableName, String engine) throws IOException, SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VariantFileTableSchema;

        String extras = "";
        if (!brighthouse) {
            extras = ",UNIQUE(upload_id, file_id), UNIQUE(file_id)";
        }
        ConnectionController.executeUpdate(sid, "DROP TABLE IF EXISTS " + tableName);
        String query = "CREATE TABLE  `" + tableName + "` ("
                + "`upload_id` int(11) NOT NULL,"
                + "`file_id` int(11) NOT NULL " + (brighthouse ? "" : "AUTO_INCREMENT") + ","
                + "`project_id` int(11) NOT NULL,"
                + "`reference_id` int(11) NOT NULL,"
                + "`file_name` varchar(500) COLLATE latin1_bin NOT NULL"
                + extras
                + ") ENGINE=" + engine + " DEFAULT CHARSET=latin1 COLLATE=latin1_bin";

        LOG.info(query);
        ConnectionController.executeUpdate(sid, query);

        if (brighthouse) {
            DBUtils.copyTable(sid, MedSavantDatabase.VariantFileTableSchema.getTableName(), getVariantFileIBTableName());
        }
    }

    /**
     * Create a <i>root</i> user if MySQL does not already have one.
     *
     * @param c database connection
     * @param password a character array, supposedly for security's sake
     * @throws SQLException
     */
    private void addRootUser(String sid, Connection c, char[] password) throws SQLException, RemoteException, SessionExpiredException {
        if (!UserManager.getInstance().userExists(sid, "root")) {
            UserManager.getInstance().addUser(sid, "root", password, UserLevel.ADMIN);
        }
    }

    private static void addDefaultReferenceGenomes(String sessionId) throws SQLException, RemoteException, SessionExpiredException {
        ReferenceManager.getInstance().addReference(sessionId, "hg17", Chromosome.getHG17Chromosomes(), null);
        ReferenceManager.getInstance().addReference(sessionId, "hg18", Chromosome.getHG18Chromosomes(), "http://savantbrowser.com/data/hg18/hg18.fa.savant");
        ReferenceManager.getInstance().addReference(sessionId, "hg19", Chromosome.getHG19Chromosomes(), "http://savantbrowser.com/data/hg19/hg19.fa.savant");
    }

    private static void addDBSettings(String sid, String versionString) throws SQLException, RemoteException, SessionExpiredException {
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_SERVER_VERSION, versionString);
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_DB_LOCK, Boolean.toString(false));
    }

    private static void populateGenes(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        TabixTableLoader loader = new TabixTableLoader(MedSavantDatabase.GeneSetTableSchema.getTable());

        try {
            // bin	name	chrom	strand	txStart	txEnd	cdsStart	cdsEnd	exonCount	exonStarts	exonEnds	score	name2	cdsStartStat	cdsEndStat	exonFrames
            loader.loadGenes(sessID, WebResources.REFGENE_HG18_URL.toURI(), "hg18", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
            loader.loadGenes(sessID, WebResources.REFGENE_HG19_URL.toURI(), "hg19", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
            //refGene.txt.gz
            //loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://genomesavant.com/data/hg19/hg19.refGene.gz").toURI(), "hg19", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
        } catch (IOException iox) {
            throw new RemoteException("Error populating gene tables.", iox);
        } catch (URISyntaxException ignored) {
        }
    }
}
