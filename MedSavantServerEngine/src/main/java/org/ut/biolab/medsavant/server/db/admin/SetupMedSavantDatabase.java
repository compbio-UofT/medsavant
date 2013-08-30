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
package org.ut.biolab.medsavant.server.db.admin;

import org.ut.biolab.medsavant.server.db.util.PersistenceUtil;
import org.ut.biolab.medsavant.server.serverapi.ReferenceManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.shared.model.Chromosome;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.VersionSettings;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;

/**
 *
 * @author mfiume
 */
public class SetupMedSavantDatabase extends MedSavantServerUnicastRemoteObject implements SetupAdapter {

    //public static final boolean ENGINE_INFINIDB = false;
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
    public void createDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword, String versionString) throws IOException, SQLException, RemoteException, SessionExpiredException {

        SessionController sessController = SessionController.getInstance();

        String sessID = PersistenceUtil.createDatabase(dbHost,port,dbName,adminName,rootPassword, versionString);

        UserManager userMgr = UserManager.getInstance();

        // Grant the admin user privileges first so that they can give grants to everybody else.
        userMgr.grantPrivileges(sessID, adminName, UserLevel.ADMIN);

        createTables(sessID);
        addRootUser(sessID, null, rootPassword);
        addDefaultReferenceGenomes(sessID);
        addDBSettings(sessID, versionString);
        populateGenes(sessID);

        // Grant permissions to everybody else.
        for (String user : userMgr.getUserNames(sessID)) {
            if (!user.equals(adminName)) {
                userMgr.grantPrivileges(sessID, user, userMgr.getUserLevel(sessID, user));
            }
        }

        // We populate the ontology tables on a separate thread because it can take a very long time, and users aren't going to be
        // looking at ontologies any time soon.  The initial session has no associated database, so we need to reregister with
        // our newly created database.
        sessController.unregisterSession(sessID);
        sessID = sessController.registerNewSession(adminName, new String(rootPassword), dbName);
        OntologyManager.getInstance().populate(sessID);
    }

    @Override
    public void removeDatabase(String dbHost, int port, String dbName, String adminName, char[] rootPassword) throws SQLException, RemoteException, SessionExpiredException {
        PersistenceUtil.removeDatabase(dbHost, port, dbName, adminName, rootPassword);
    }

    private void createTables(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        PersistenceUtil.createTables(sessID);
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
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_CLIENT_VERSION, versionString);
        SettingsManager.getInstance().addSetting(sid, Settings.KEY_DB_LOCK, Boolean.toString(false));
    }

    private static void populateGenes(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        TabixTableLoader loader = new TabixTableLoader(MedSavantDatabase.GeneSetTableSchema.getTable());

        try {
            // bin	name	chrom	strand	txStart	txEnd	cdsStart	cdsEnd	exonCount	exonStarts	exonEnds	score	name2	cdsStartStat	cdsEndStat	exonFrames
            loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://genomesavant.com/data/hg18/hg18.refGene.gz").toURI(), "hg18", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
            loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://genomesavant.com/data/medsavant/hg19/refGene.txt.gz").toURI(), "hg19", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
                    //refGene.txt.gz
            //loader.loadGenes(sessID, NetworkUtils.getKnownGoodURL("http://genomesavant.com/data/hg19/hg19.refGene.gz").toURI(), "hg19", "RefSeq", null, "transcript", "chrom", null, "start", "end", "codingStart", "codingEnd", null, "exonStarts", "exonEnds", null, "name");
        } catch (IOException iox) {
            throw new RemoteException("Error populating gene tables.", iox);
        } catch (URISyntaxException ignored) {
        }
    }

    @Override
    public String getServerVersion() throws RemoteException, SessionExpiredException {
        return VersionSettings.getVersionString();
    }
}
