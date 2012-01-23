/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.db.admin;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.model.Chromosome;
import org.ut.biolab.medsavant.db.model.UserLevel;
import org.ut.biolab.medsavant.db.settings.Settings;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.db.util.query.SettingsQueryUtil;
import org.ut.biolab.medsavant.db.util.query.UserQueryUtil;
import org.ut.biolab.medsavant.db.util.query.api.SetupAdapter;
import org.ut.biolab.medsavant.server.SessionController;

/**
 *
 * @author mfiume
 */
public class SetupMedSavantDatabase extends java.rmi.server.UnicastRemoteObject implements SetupAdapter {
    private static final Logger LOG = Logger.getLogger(SetupMedSavantDatabase.class.getName());

    private static SetupMedSavantDatabase instance;

    public static synchronized SetupMedSavantDatabase getInstance() throws RemoteException {
        if (instance == null) {
            instance = new SetupMedSavantDatabase();
        }
        return instance;
    }

    public SetupMedSavantDatabase() throws RemoteException {}

    private static void dropTables(String sessionId) throws SQLException {

        Connection c = ConnectionController.connectPooled(sessionId);

        if (DBUtil.tableExists(sessionId, MedSavantDatabase.PatienttablemapTableSchema.getTablename())) {
            List<String> patientTables = getValuesFromField(c,MedSavantDatabase.PatienttablemapTableSchema.getTablename(), "patient_tablename");
            for (String s : patientTables) {
                DBUtil.dropTable(sessionId,s);
            }
        }

        if (DBUtil.tableExists(sessionId, MedSavantDatabase.VarianttablemapTableSchema.getTablename())) {
            List<String> variantTables = getValuesFromField(c,MedSavantDatabase.VarianttablemapTableSchema.getTablename(), "variant_tablename");
            for (String s : variantTables) {
                DBUtil.dropTable(sessionId,s);
            }
        }

        DBUtil.dropTable(sessionId,MedSavantDatabase.ServerlogTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.AnnotationTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.ReferenceTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.ProjectTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.PatienttablemapTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.VarianttablemapTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.RegionsetTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.RegionsetmembershipTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.CohortTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.CohortmembershipTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.VariantpendingupdateTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.PatienttablemapTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.ChromosomeTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.PatientformatTableSchema.getTablename());
        DBUtil.dropTable(sessionId,MedSavantDatabase.AnnotationformatTableSchema.getTablename());
    }

    private static void createTables(String sessionId) throws SQLException {

        Connection c = ConnectionController.connectPooled(sessionId);

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ServerlogTableSchema.getTablename() + "` ("
                  + "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                  + "`user` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                  + "`event` varchar(50) COLLATE latin1_bin DEFAULT NULL,"
                  + "`description` blob,"
                  + "`timestamp` datetime NOT NULL,"
                  + "PRIMARY KEY (`id`)"
                + ") ENGINE=MyISAM;"
                );

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.RegionsetTableSchema.getTablename() + "` ("
                + "`region_set_id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(255) CHARACTER SET latin1 NOT NULL,"
                + "PRIMARY KEY (`region_set_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.RegionsetmembershipTableSchema.getTablename() + "` ("
                + "`region_set_id` int(11) NOT NULL,"
                + "`genome_id` int(11) NOT NULL,"
                + "`chrom` varchar(255) COLLATE latin1_bin NOT NULL,"
                + "`start` int(11) NOT NULL,"
                + "`end` int(11) NOT NULL,"
                + "`description` varchar(255) COLLATE latin1_bin NOT NULL"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.CohortTableSchema.getTablename() + "` ("
                + "`cohort_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`name` varchar(255) CHARACTER SET latin1 NOT NULL,"
                + "PRIMARY KEY (`cohort_id`,`project_id`) USING BTREE"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.CohortmembershipTableSchema.getTablename() + "` ("
                + "`cohort_id` int(11) unsigned NOT NULL,"
                + "`patient_id` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`patient_id`,`cohort_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ReferenceTableSchema.getTablename() + "` ("
                + "`reference_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`name` varchar(50) COLLATE latin1_bin NOT NULL,"
                + "`url` varchar(200) COLLATE latin1_bin DEFAULT NULL,"
                + "PRIMARY KEY (`reference_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.AnnotationTableSchema.getTablename() + "` ("
                + "`annotation_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`program` varchar(100) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`version` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`path` varchar(500) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`has_ref` tinyint(1) NOT NULL,"
                + "`has_alt` tinyint(1) NOT NULL,"
                + "`type` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`annotation_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.ProjectTableSchema.getTablename() + "` "
                + "(`project_id` int(11) unsigned NOT NULL AUTO_INCREMENT, "
                + "`name` varchar(50) NOT NULL, "
                + "PRIMARY KEY (`project_id`), "
                + "UNIQUE KEY `name` (`name`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.PatienttablemapTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`patient_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`project_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.VarianttablemapTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`update_id` int(11) unsigned NOT NULL,"
                + "`published` boolean NOT NULL,"
                + "`variant_tablename` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`annotation_ids` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "UNIQUE KEY `unique` (`project_id`,`reference_id`,`update_id`)"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.VariantpendingupdateTableSchema.getTablename() + "` ("
                + "`upload_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`action` int(11) unsigned NOT NULL,"
                + "`status` int(5) unsigned NOT NULL DEFAULT '0',"
                + "`timestamp` datetime DEFAULT NULL,"
                + "`user` varchar(200) DEFAULT NULL,"
                + "PRIMARY KEY (`upload_id`) USING BTREE"
                + ") ENGINE=MyISAM;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.ChromosomeTableSchema.getTablename() + "` ("
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`contig_id` int(11) unsigned NOT NULL,"
                + "`contig_name` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`contig_length` int(11) unsigned NOT NULL,"
                + "`centromere_pos` int(11) unsigned NOT NULL,"
                + "PRIMARY KEY (`reference_id`,`contig_id`) USING BTREE"
                +") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.AnnotationformatTableSchema.getTablename() + "` ("
                + "`annotation_id` int(11) unsigned NOT NULL,"
                + "`position` int(11) unsigned NOT NULL,"
                + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                + "`filterable` tinyint(1) NOT NULL,"
                + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`annotation_id`,`position`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.PatientformatTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`position` int(11) unsigned NOT NULL,"
                + "`column_name` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`column_type` varchar(45) COLLATE latin1_bin NOT NULL,"
                + "`filterable` tinyint(1) NOT NULL,"
                + "`alias` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`description` varchar(500) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`project_id`,`position`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.VariantformatTableSchema.getTablename() + "` ("
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

        c.createStatement().execute(
                "CREATE TABLE  `default_patient` ("
                + "`patient_id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`family_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`hospital_id` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`idbiomom` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`idbiodad` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`gender` int(11) unsigned DEFAULT NULL,"
                + "`affected` int(11) unsigned DEFAULT NULL,"
                + "`dna_ids` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                + "`bam_url` varchar(5000) COLLATE latin1_bin DEFAULT NULL,"
                + "PRIMARY KEY (`patient_id`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE  `default_variant` ("
                + "`upload_id` int(11) NOT NULL,"
                + "`file_id` int(11) NOT NULL,"
                + "`variant_id` int(11) NOT NULL,"
                + "`dna_id` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`chrom` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`position` int(11) NOT NULL,"
                + "`dbsnp_id` varchar(45) COLLATE latin1_bin DEFAULT NULL,"
                + "`ref` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`alt` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`qual` float(10,0) DEFAULT NULL,"
                + "`filter` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`custom_info` varchar(500) COLLATE latin1_bin DEFAULT NULL"
                + ") ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE  `" + MedSavantDatabase.SettingsTableSchema.getTablename() + "` ("
                + "`setting_key` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`setting_value` varchar(300) COLLATE latin1_bin NOT NULL,"
                + "PRIMARY KEY (`setting_key`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");

        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.VarianttagTableSchema.getTablename() + "` ("
                  + "`upload_id` int(11) NOT NULL,"
                  + "`tagkey` varchar(500) COLLATE latin1_bin NOT NULL,"
                  + "`tagvalue` varchar(1000) COLLATE latin1_bin NOT NULL DEFAULT ''"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin"
                );
        
        c.createStatement().execute(
                "CREATE TABLE `" + MedSavantDatabase.VariantStarredTableSchema.getTablename() + "` ("
                + "`project_id` int(11) unsigned NOT NULL,"
                + "`reference_id` int(11) unsigned NOT NULL,"
                + "`upload_id` int(11) NOT NULL,"
                + "`file_id` int(11) NOT NULL,"
                + "`variant_id` int(11) NOT NULL,"
                + "`user` varchar(200) COLLATE latin1_bin NOT NULL,"
                + "`description` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`timestamp` datetime NOT NULL,"
                + "UNIQUE KEY `unique` (`project_id`,`reference_id`,`upload_id`,`file_id`,`variant_id`,`user`)"
                + ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COLLATE=latin1_bin");

    }

    /**
     * Create a <i>root</i> user if MySQL does not already have one.
     * @param c database connection
     * @param password a character array, supposedly for security's sake
     * @throws SQLException
     */
    private static void addRootUser(String sid, Connection c, char[] password) throws SQLException, RemoteException {
        if (!UserQueryUtil.getInstance().userExists(sid, "root")) {
            UserQueryUtil.getInstance().addUser(sid, "root", password, UserLevel.ADMIN);
        }
    }

    public void createDatabase(String dbHost, int port, String dbname, String adminName, char[] rootPassword, String versionString) throws SQLException, RemoteException {

        String sessionId = SessionController.getInstance().registerNewSession(adminName, new String(rootPassword), "");

        Connection c = ConnectionController.connectPooled(sessionId);

        createDatabase(c,dbname);

        ConnectionController.switchDatabases(sessionId,dbname);

        dropTables(sessionId);
        createTables(sessionId);
        addRootUser(sessionId,c, rootPassword);
        addDefaultReferenceGenomes(sessionId);
        addDbSettings(sessionId,versionString);

        for (String user: UserQueryUtil.getInstance().getUserNames(sessionId)) {
            UserQueryUtil.getInstance().grantPrivileges(sessionId,user, UserQueryUtil.getInstance().getUserLevel(sessionId, user));
        }
    }

    private static void addDbSettings(String sid, String versionString) throws SQLException, RemoteException {
        SettingsQueryUtil.getInstance().addSetting(sid, Settings.KEY_CLIENT_VERSION, versionString);
        SettingsQueryUtil.getInstance().addSetting(sid, Settings.KEY_DB_LOCK, Boolean.toString(false));
    }

    private static List<String> getValuesFromField(Connection c,String tablename, String fieldname) throws SQLException {
        String q = "SELECT `" + fieldname + "` FROM `" + tablename + "`";
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        return results;
    }

    private static void createDatabase(Connection c, String dbname) throws SQLException {

        System.out.println("CREATE DATABASE " + dbname);

        //TODO: should check if the db exists already
        c.createStatement().execute("CREATE DATABASE " + dbname);
    }

    private static void addDefaultReferenceGenomes(String sessionId) throws SQLException, RemoteException {
        ReferenceQueryUtil.getInstance().addReference(sessionId,"hg17", Chromosome.getHg17Chromosomes());
        ReferenceQueryUtil.getInstance().addReference(sessionId,"hg18", Chromosome.getHg18Chromosomes(), "http://savantbrowser.com/data/hg18/hg18.fa.savant");
        ReferenceQueryUtil.getInstance().addReference(sessionId,"hg19", Chromosome.getHg19Chromosomes(), "http://savantbrowser.com/data/hg19/hg19.fa.savant");
    }
}
