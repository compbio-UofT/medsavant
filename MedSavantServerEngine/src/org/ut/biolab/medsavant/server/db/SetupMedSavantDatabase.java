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
package org.ut.biolab.medsavant.server.db;

import org.ut.biolab.medsavant.server.serverapi.ReferenceManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.util.TabixTableLoader;
import org.ut.biolab.medsavant.shared.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;

/**
 *
 * @author mfiume
 */
public class SetupMedSavantDatabase extends MedSavantServerUnicastRemoteObject implements SetupAdapter {

    public static final boolean ENGINE_INFINIDB = false;
    private static SetupMedSavantDatabase instance;

    public static synchronized SetupMedSavantDatabase getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SetupMedSavantDatabase();
        }
        return instance;
    }

    private SetupMedSavantDatabase() throws RemoteException {
    }

    @Override
    public void createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws IOException, SQLException, RemoteException {

        SessionController sessController = SessionController.getInstance();
        String sessID = sessController.registerNewSession(adminName, new String(rootPassword), "");

        Connection conn = ConnectionController.connectPooled(sessID);
        conn.createStatement().execute("CREATE DATABASE " + dbName);
        conn.close();


        ConnectionController.switchDatabases(sessID, dbName); //closes all connections
        conn = ConnectionController.connectPooled(sessID);

        UserManager userMgr = UserManager.getInstance();

        // Grant the admin user privileges first so that they can give grants to everybody else.
        userMgr.grantPrivileges(sessID, adminName, UserLevel.ADMIN);

        createTables(sessID);
        addRootUser(sessID, conn, rootPassword);
        addDefaultReferenceGenomes(sessID);
        addDBSettings(sessID, versionString);
        populateGenes(sessID);

        // Grant permissions to everybody else.
        for (String user : userMgr.getUserNames(sessID)) {
            if (!user.equals(adminName)) {
                userMgr.grantPrivileges(sessID, user, userMgr.getUserLevel(sessID, user));
            }
        }

        conn.close();

        // We populate the ontology tables on a separate thread because it can take a very long time, and users aren't going to be
        // looking at ontologies any time soon.  The initial session has no associated database, so we need to reregister with
        // our newly created database.
        sessController.unregisterSession(sessID);
        sessID = sessController.registerNewSession(adminName, new String(rootPassword), dbName);
        OntologyManager.getInstance().populate(sessID);
    }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws SQLException, RemoteException {

        String sessID = SessionController.getInstance().registerNewSession(adminName, new String(rootPassword), "");

        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.createStatement().execute("DROP DATABASE IF EXISTS " + dbName);
        } finally {
            conn.close();
        }
    }

    private void createTables(String sessID) throws SQLException, RemoteException {

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
            if (ENGINE_INFINIDB) {

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
                        + "`chrom` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT '',"
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
            System.out.println(createVariantStatement);
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

    /**
     * Create a <i>root</i> user if MySQL does not already have one.
     *
     * @param c database connection
     * @param password a character array, supposedly for security's sake
     * @throws SQLException
     */
    private void addRootUser(String sid, Connection c, char[] password) throws SQLException, RemoteException {
        if (!UserManager.getInstance().userExists(sid, "root")) {
            UserManager.getInstance().addUser(sid, "root", password, UserLevel.ADMIN);
        }
    }

    private static void addDefaultReferenceGenomes(String sessionId) throws SQLException, RemoteException {
        ReferenceManager.getInstance().addReference(sessionId, "hg17", Chromosome.getHG17Chromosomes(), null);
        ReferenceManager.getInstance().addReference(sessionId, "hg18", Chromosome.getHG18Chromosomes(), "http://savantbrowser.com/data/hg18/hg18.fa.savant");
        ReferenceManager.getInstance().addReference(sessionId, "hg19", Chromosome.getHG19Chromosomes(), "http://savantbrowser.com/data/hg19/hg19.fa.savant");
    }

    private static void addDBSettings(String sid, String versionString) throws SQLException, RemoteException {
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_CLIENT_VERSION, versionString);
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_DB_LOCK, Boolean.toString(false));
    }

    private static void populateGenes(String sessID) throws SQLException, RemoteException {
        TabixTableLoader loader = new TabixTableLoader(MedSavantDatabase.GeneSetTableSchema.getTable());

        try {
            // bin	name	chrom	strand	txStart	txEnd	cdsStart	cdsEnd	exonCount	exonStarts	exonEnds	score	name2	cdsStartStat	cdsEndStat	exonFrames
            loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://savantbrowser.com/data/hg18/hg18.refGene.gz").toURI(), "hg18", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
            loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://savantbrowser.com/data/hg19/hg19.refGene.gz").toURI(), "hg19", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
        } catch (IOException iox) {
            throw new RemoteException("Error populating gene tables.", iox);
        } catch (URISyntaxException ignored) {
        }
    }
}
