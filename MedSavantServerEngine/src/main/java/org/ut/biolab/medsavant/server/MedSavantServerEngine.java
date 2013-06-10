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
package org.ut.biolab.medsavant.server;

import org.ut.biolab.medsavant.server.serverapi.ReferenceManager;
import org.ut.biolab.medsavant.server.serverapi.LogManager;
import org.ut.biolab.medsavant.server.serverapi.NotificationManager;
import org.ut.biolab.medsavant.server.serverapi.GeneSetManager;
import org.ut.biolab.medsavant.server.serverapi.PatientManager;
import org.ut.biolab.medsavant.server.serverapi.UserManager;
import org.ut.biolab.medsavant.server.serverapi.CohortManager;
import org.ut.biolab.medsavant.server.serverapi.ProjectManager;
import org.ut.biolab.medsavant.server.serverapi.AnnotationManager;
import org.ut.biolab.medsavant.server.serverapi.NetworkManager;
import org.ut.biolab.medsavant.server.serverapi.RegionSetManager;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

import gnu.getopt.Getopt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.ut.biolab.medsavant.server.db.ConnectionController;

import org.ut.biolab.medsavant.server.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;

/**
 *
 * @author mfiume
 */
public class MedSavantServerEngine extends MedSavantServerUnicastRemoteObject implements MedSavantServerRegistry {

    public static boolean USE_INFINIDB_ENGINE = false;
    int listenOnPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    public MedSavantServerEngine(String databaseHost, int databasePort, String rootUserName, String password) throws RemoteException, SQLException, SessionExpiredException {

        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get inet address.");
        }

        listenOnPort = MedSavantServerUnicastRemoteObject.getListenPort();

        if (!performPreemptiveSystemCheck()) {
            System.out.println("System check FAILED, see errors above");
            System.exit(1);
        }

        System.out.println("Server Information:");
        System.out.println(
                "  SERVER ADDRESS: " + thisAddress + "\n"
                + "  LISTENING ON PORT: " + listenOnPort + "\n");
                //+ "  EXPORTING ON PORT: " + MedSavantServerUnicastRemoteObject.getExportPort());
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(listenOnPort);

            //TODO: get these from the user

            ConnectionController.setHost(databaseHost);
            ConnectionController.setPort(databasePort);

            System.out.println();
            System.out.println("Database Information:");

            System.out.println(
                    "  DATABASE ADDRESS: " + databaseHost + "\n"
                    + "  DATABASE PORT: " + databasePort);

            System.out.println("  DATABASE USER: " + rootUserName);
            if (password == null) {
                System.out.print("  PASSWORD FOR " + rootUserName + ": ");
                System.out.flush();
                char[] pass = System.console().readPassword();
                password = new String(pass);
            } else {
                System.out.print("  PASSWORD: " + password);
            }

            System.out.println();

            System.out.print("Connecting to database ... ");
            try {
                ConnectionController.connectOnce(databaseHost, databasePort, "", rootUserName, password);
            } catch (SQLException ex) {
                System.out.println("FAILED");
                throw ex;
            }
            System.out.println("OK");

            bindAdapters(registry);

            System.out.println("\nServer initialized, waiting for incoming connections...");

            EmailLogger.logByEmail("Server booted", "The MedSavant Server Engine successfully booted.");
        } catch (RemoteException e) {
            throw e;
        } catch (SessionExpiredException e) {
            throw e;
        }
    }

    static public void main(String args[]) {

        System.out.println("== MedSavant Server Engine ==\n");

        try {

            /**
             * Override with commands from the command line
             */
            Getopt g = new Getopt("MedSavantServerEngine", args, "c:l:h:p:u:e:");
            //
            int c;

            String user = "root";
            String password = null;
            String host = "localhost";
            int port = 5029;

            // print usage
            if (args.length > 0 && args[0].equals("--help")) {
                System.out.println("java -jar -Djava.rmi.server.hostname=<hostname> MedSavantServerEngine.jar { [-c CONFIG_FILE] } or { [-l RMI_PORT] [-h DATABASE_HOST] [-p DATABASE_PORT] [-u DATABASE_ROOT_USER] [-e ADMIN_EMAIL] }");
                System.out.println("\n\tCONFIG_FILE should be a file containing any number of these keys:\n"
                        + "\t\tdb-user - the database user\n"
                        + "\t\tdb-password - the database password\n"
                        + "\t\tdb-host - the database host\n"
                        + "\t\tdb-port - the database port\n"
                        + "\t\tlisten-on-port - the port on which clients will connect\n"
                        + "\t\temail - the email address to send important notifications\n"
                        + "\t\ttmp-dir - the directory to use for temporary files\n"
                        + "\t\tms-dir - the directory to use to store permanent files\n");
                return;
            }

            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 'c':
                        String configFileName = g.getOptarg();
                        System.out.println("Loading configuration from " + (new File(configFileName)).getAbsolutePath() + " ...");

                        Properties prop = new Properties();
                        try {
                            prop.load(new FileInputStream(configFileName));
                            if (prop.containsKey("db-user")) {
                                user = prop.getProperty("db-user");
                            }
                            if (prop.containsKey("db-password")) {
                                password = prop.getProperty("db-password");
                            }
                            if (prop.containsKey("db-host")) {
                                host = prop.getProperty("db-host");
                            }
                            if (prop.containsKey("db-port")) {
                                port = Integer.parseInt(prop.getProperty("db-port"));
                            }
                            if (prop.containsKey("listen-on-port")) {
                                int listenOnPort = Integer.parseInt(prop.getProperty("listen-on-port"));
                                MedSavantServerUnicastRemoteObject.setListenPort(listenOnPort);
                                //MedSavantServerUnicastRemoteObject.setExportPort(listenOnPort + 1);
                            }
                            if (prop.containsKey("email")) {
                                EmailLogger.setMailRecipient(prop.getProperty("email"));
                            }
                            if (prop.containsKey("tmp-dir")) {
                                DirectorySettings.setTmpDirectory(prop.getProperty("tmp-dir"));
                            }
                            if (prop.containsKey("ms-dir")) {
                                DirectorySettings.setMedSavantDirectory(prop.getProperty("ms-dir"));
                            }

                        } catch (Exception e) {
                            System.out.println("ERROR: Could not load properties file " + configFileName);
                        }
                        break;
                    case 'h':
                        System.out.println("Host " + g.getOptarg());
                        host = g.getOptarg();
                        break;
                    case 'p':
                        port = Integer.parseInt(g.getOptarg());
                        break;
                    case 'l':
                        int listenOnPort = Integer.parseInt(g.getOptarg());
                        MedSavantServerUnicastRemoteObject.setListenPort(listenOnPort);
                        //MedSavantServerUnicastRemoteObject.setExportPort(listenOnPort + 1);
                        break;
                    case 'u':
                        user = g.getOptarg();
                        break;
                    case 'e':
                        EmailLogger.setMailRecipient(g.getOptarg());
                        break;
                    case '?':
                        break; // getopt() already printed an error
                    default:
                        System.out.print("getopt() returned " + c + "\n");
                }
            }

            new MedSavantServerEngine(host, port, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void bindAdapters(Registry registry) throws RemoteException, SessionExpiredException {

        System.out.print("Initializing server registry ... ");
        System.out.flush();

        registry.rebind(SESSION_MANAGER, SessionController.getInstance());
        registry.rebind(CUSTOM_TABLES_MANAGER, CustomTables.getInstance());

        registry.rebind(ANNOTATION_MANAGER, AnnotationManager.getInstance());
        registry.rebind(COHORT_MANAGER, CohortManager.getInstance());
        registry.rebind(GENE_SET_MANAGER, GeneSetManager.getInstance());
        registry.rebind(LOG_MANAGER, LogManager.getInstance());
        registry.rebind(NETWORK_MANAGER, NetworkManager.getInstance());
        registry.rebind(ONTOLOGY_MANAGER, OntologyManager.getInstance());
        registry.rebind(PATIENT_MANAGER, PatientManager.getInstance());
        registry.rebind(PROJECT_MANAGER, ProjectManager.getInstance());
        registry.rebind(REFERENCE_MANAGER, ReferenceManager.getInstance());
        registry.rebind(REGION_SET_MANAGER, RegionSetManager.getInstance());
        registry.rebind(SETTINGS_MANAGER, SettingsManager.getInstance());
        registry.rebind(USER_MANAGER, UserManager.getInstance());
        registry.rebind(VARIANT_MANAGER, VariantManager.getInstance());
        registry.rebind(DB_UTIL_MANAGER, DBUtils.getInstance());
        registry.rebind(SETUP_MANAGER, SetupMedSavantDatabase.getInstance());
        registry.rebind(CUSTOM_TABLES_MANAGER, CustomTables.getInstance());
        registry.rebind(NOTIFICATION_MANAGER, NotificationManager.getInstance());

        System.out.println("OK");
    }

    private static boolean performPreemptiveSystemCheck() {

        File tmpDir = DirectorySettings.getTmpDirectory();
        File cacheDir = DirectorySettings.getCacheDirectory();
        File medsavantDir = DirectorySettings.getMedSavantDirectory();

        System.out.println("Directory information:");
        System.out.println("  TMP DIRECTORY: " + tmpDir.getAbsolutePath() + " has permissions " + permissions(tmpDir));
        System.out.println("  MEDSAVANT DIRECTORY: " + medsavantDir.getAbsolutePath() + " has permissions " + permissions(medsavantDir));
        System.out.println("  CACHE DIRECTORY: " + cacheDir.getAbsolutePath() + " has permissions " + permissions(cacheDir));
        System.out.println();

        boolean passed = true;

        if (!completelyPermissive(tmpDir)) {
            System.out.println("ERROR: " + tmpDir.getAbsolutePath() + " does not have appropriate permissions (require rwx)");
            passed = false;
        }

        if (!completelyPermissive(medsavantDir)) {
            System.out.println("ERROR: " + medsavantDir.getAbsolutePath() + " does not have appropriate permissions (require rwx)");
            passed = false;
        }
        if (!completelyPermissive(cacheDir)) {
            System.out.println("ERROR: " + cacheDir.getAbsolutePath() + " does not have appropriate permissions (require rwx)");
            passed = false;
        }
        try {
            File cacheNow = DirectorySettings.generateDateStampDirectory(cacheDir);
            if (!completelyPermissive(cacheNow)) {
                System.out.println("ERROR: Directories created inside " + cacheDir + " do not have appropriate permissions (require rwx)");
                passed = false;
            }
        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't create directory inside " + cacheDir.getAbsolutePath());
            passed = false;
        }

        try {
            File tmpNow = DirectorySettings.generateDateStampDirectory(tmpDir);
            if (!completelyPermissive(tmpNow)) {
                System.out.println("ERROR: Directories created inside " + tmpDir + " do not have appropriate permissions (require rwx)");
                passed = false;
            }
        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't create directory inside " + tmpDir.getAbsolutePath());
            passed = false;
        }

        return passed;
    }

    private static boolean completelyPermissive(File d) {
        return d.canRead() && d.canWrite() && d.canExecute();
    }

    private static String permissions(File d) {
        return (d.canRead() ? "r" : "-") + (d.canWrite() ? "w" : "-") + (d.canExecute() ? "x" : "-");
    }
}
