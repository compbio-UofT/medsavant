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
package org.ut.biolab.medsavant.server;

import gnu.getopt.Getopt;
import org.ut.biolab.medsavant.server.serverapi.SessionManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Dictionary;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.common.GlobalWrapper;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.variantstorage.MedSavantVariantStorageEngine;
import org.ut.biolab.medsavant.server.db.ConnectionController;

import org.ut.biolab.medsavant.server.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.serverapi.VariantManager;
import org.ut.biolab.medsavant.server.log.EmailLogger;
import org.ut.biolab.medsavant.server.mail.Mail;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.server.phasing.BEAGLEWrapper;
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.VersionSettings;

/**
 * * @author mfiume
 */
public class MedSavantServerEngine implements MedSavantServerRegistry {

    private static MedSavantServerEngine instance = null;
    private static final Log LOG = LogFactory.getLog(MedSavantServerEngine.class);
    //ssl/tls off by default.
    private boolean require_ssltls = false;
    private boolean require_client_auth = false;

    //Maximum number of IO-heavy jobs that can be run simultaneously.
    //(see MedSavantIOScheduler)  If > MAX_THREADS, this has no effect.
    private final int MAX_IO_JOBS = Integer.MAX_VALUE;

    private final boolean USE_INFINIDB_ENGINE = false;

    int listenOnPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    private String host;
    private int port;
    private String rootName;
    private String pass;

    private static final int SLEEP_DELAY_BETWEEN_UNEXPORT_REQUESTS = 60000;

    private int connectPort = 36800;
    private int exportPort = 36801;   
    
    public MedSavantServerContext getServerContext(){                
        return GlobalWrapper.getServerContext();
    }
    
    public MedSavantVariantStorageEngine getStorageEngine(){
        return GlobalWrapper.getVariantStorageEngine();
    }
   
    public boolean useInfiniDB() {
        return USE_INFINIDB_ENGINE;
    }

    public int getListenPort() {
        return connectPort;
    }

    public int getExportPort() {
        return exportPort;
    }

    public void setListenPort(int port) {
        connectPort = port;
        exportPort = port + 1;
    }

    public static MedSavantServerEngine getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MedSavantServerEngine is not started!");
        }
        return instance;
    }

    public static boolean isStarted() {
        return instance != null;
    }

    public static void shutdown(boolean friendly) {
        if (instance == null) {
            return;
        }

        try {
            GlobalWrapper.getServerContext().getExecutionService().shutdown();

            String[] names = instance.registry.list();
            String errname = null;
            try {
                errname = "";
                for (String name : names) {
                    Remote r = instance.registry.lookup(name);
                    int requests = 0;
                    while (!UnicastRemoteObject.unexportObject(r, !friendly)) {
                        requests++;
                        LOG.info("Shutdown attempt " + requests + " - Cannot unexport " + name + " during shutdown -- Server Engine is busy.");
                        try {
                            Thread.sleep(SLEEP_DELAY_BETWEEN_UNEXPORT_REQUESTS);
                        } catch (InterruptedException ie) {

                        }
                    }
                    instance.registry.unbind(name);
                    errname = name;
                }
            } catch (RemoteException re) {
                LOG.error("Couldn't unbind " + errname, re);
            } catch (NotBoundException ne) {
                LOG.error("Couldn't unbind " + errname + ", not bound?", ne);
            }
        } catch (RemoteException rex) {
            LOG.error("Couldn't get registry names during shutdown", rex);
        } catch (InterruptedException ex) {
            LOG.error("Interruped while waiting for execution service to shutdown");
        }
        ConnectionController.reapAll();
        instance = null;

        System.gc();
    }

    /*
     public Void runJobInCurrentThread(MedSavantServerJob msj) throws Exception {
     msj.setScheduleStatus(SCHEDULED_AS_SHORTJOB);
     return msj.call();
     }
     */
    public boolean isClientAuthRequired() {
        return require_client_auth;
    }

    public boolean isTLSRequired() {
        return require_ssltls;
    }

    public RMIServerSocketFactory getDefaultServerSocketFactory() {
        return isTLSRequired() ? new SslRMIServerSocketFactory(null, null, require_client_auth) : RMISocketFactory.getSocketFactory();
    }

    public RMIClientSocketFactory getDefaultClientSocketFactory() {
        return isTLSRequired() ? new SslRMIClientSocketFactory() : RMISocketFactory.getSocketFactory();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getRootName() {
        return rootName;
    }

    public String getPass() {
        return pass;
    }

    private void initialize(String databaseHost, int databasePort, String rootUserName, String password) throws RemoteException, SQLException, SessionExpiredException {
        host = databaseHost;
        port = databasePort;
        rootName = rootUserName;
        pass = password;
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get inet address.");
        }

        listenOnPort = getListenPort();

        if (!performPreemptiveSystemCheck()) {
            System.out.println("System check FAILED, see errors above");
            System.exit(1);
        }

        System.out.println("Server Information:");
        System.out.println(
                "  SERVER VERSION: " + VersionSettings.getVersionString() + "\n"
                + "  SERVER ADDRESS: " + thisAddress + "\n"
                + "  LISTENING ON PORT: " + listenOnPort + "\n"
                + "  EXPORT PORT: " + getExportPort() + "\n"
                + "  MAX JOBS: " + GlobalWrapper.getServerContext().getExecutionService().getMaxRunningLongJobs() + "\n");
        //+ " MAX IO THREADS: " + MAX_IO_JOBS + "\n");

        MedSavantIOController.setMaxIOJobs(MAX_IO_JOBS);
        //+ "  EXPORTING ON PORT: " + MedSavantServerUnicastRemoteObject.getExportPort());
        try {
            // create the registry and bind the name and object.
            if (isTLSRequired()) {
                System.out.println("SSL/TLS Encryption is enabled, Client authentication is " + (isClientAuthRequired() ? "required." : "NOT required."));
            } else {
                System.out.println("SSL/TLS Encryption is NOT enabled");
                //registry = LocateRegistry.createRegistry(listenOnPort);
            }
            registry = LocateRegistry.createRegistry(listenOnPort, getDefaultClientSocketFactory(), getDefaultServerSocketFactory());

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
            }
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

    private static Properties getProperties(Dictionary dict) {
        Properties prop = new Properties();
        while (dict.keys().hasMoreElements()) {
            Object e = dict.keys().nextElement();
            prop.put(e, dict.get(e));
        }
        return prop;
    }

    private MedSavantServerEngine() throws RemoteException {
        System.out.println("== MedSavant Server Engine ==\n");
    }

    static public void main(String args[]) {
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
                        + "\t\tms-dir - the directory to use to store permanent files\n"
                        + "\t\tencryption - indicate whether encryption should be disabled ('disabled'), enabled without requiring a client certificate ('no_client_auth'), or enabled with requirement for a client certificate ('with_client_auth')\n"
                        + "\t\tkeyStore - full path to the key store\n"
                        + "\t\tkeyStorePass - password for the key store\n"
                        + "\t\tmax-threads - maximum number of allowed ");
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
                            restart(prop);
                        } catch (Exception e) {
                            System.err.println("ERROR: Could not load properties file " + configFileName + ", " + e);
                        }
                        break;
                    default:
                        System.err.println("Unrecognized argument!");
                        System.exit(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Exiting with exception", e);
            System.exit(1);
        }
    }

    public static void restart(Properties prop) throws RemoteException {
        instance = new MedSavantServerEngine();
        String user = "root";
        String password = null;
        String host = "localhost";
        int port = 5029;

        if (prop.containsKey("db-user")) {
            user = (String) prop.get("db-user");
        }
        if (prop.containsKey("db-password")) {
            password = prop.getProperty("db-password");
        }
        /* max-threads and blocking-queue size are separately configured as part of the executor service component.
        if (prop.containsKey("max-threads")) {
            instance.maxThreads = Integer.parseInt(prop.getProperty("max-threads"));
        }
        if (prop.containsKey("blocking-queue-size")) {
            instance.blockingQueueSize = Integer.parseInt(prop.getProperty("blocking-queue-size"));
        }
        */
        if (prop.containsKey("db-host")) {
            host = prop.getProperty("db-host");
        }
        if (prop.containsKey("db-port")) {
            port = Integer.parseInt(prop.getProperty("db-port"));
        }
        if (prop.containsKey("server-hostname")) {
            String serverHost = prop.getProperty("server-hostname");
            System.setProperty("java.rmi.server.hostname", serverHost);
        }
        if (prop.containsKey("listen-on-port")) {
            int listenOnPort = Integer.parseInt(prop.getProperty("listen-on-port"));
            instance.setListenPort(listenOnPort);
            //MedSavantServerUnicastRemoteObject.setExportPort(listenOnPort + 1);
        }
        if (prop.containsKey("mail-un") && prop.containsKey("mail-pw") && prop.containsKey("smtp-server") && prop.containsKey("mail-port")) {
            Mail.setMailCredentials(prop.getProperty("mail-un"), prop.getProperty("mail-pw"), prop.getProperty("smtp-server"), Integer.parseInt(prop.getProperty("mail-port")));
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
        if (prop.containsKey("encryption")) {
            String p = prop.getProperty("encryption");
            if (p.equalsIgnoreCase("disabled")) {
                instance.require_ssltls = false;
                instance.require_client_auth = false;
            } else if (p.equalsIgnoreCase("no_client_auth")) {
                instance.require_ssltls = true;
                instance.require_client_auth = false;
            } else if (p.equalsIgnoreCase("with_client_auth")) {
                instance.require_ssltls = true;
                instance.require_client_auth = true;
            } else {
                throw new IllegalArgumentException("Uncrecognized value for property 'encryption': " + p);
            }
            if (instance.require_ssltls) {
                if (prop.containsKey("keyStore")) {
                    System.setProperty("javax.net.ssl.keyStore", prop.getProperty("keyStore"));
                } else {
                    System.err.println("WARNING: No keyStore specified in configuration");
                }

                if (prop.containsKey("keyStorePass")) {
                    System.setProperty("javax.net.ssl.keyStorePassword", prop.getProperty("keyStorePass"));
                } else {
                    throw new IllegalArgumentException("ERROR: No keyStore password specified in configuration");
                }
            }
        }
        //instance.initThreadPools();
        try {
            instance.initialize(host, port, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Exiting with exception", e);
            System.exit(1);
        }
    }

    public static void restart(Dictionary dict) throws RemoteException {
        Properties prop = getProperties(dict);
        restart(prop);
    }

    private void bindAdapters(Registry registry) throws RemoteException, SessionExpiredException {

        System.out.print("Initializing server registry ... ");
        System.out.flush();

        registry.rebind(SESSION_MANAGER, SessionManager.getInstance());
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

    private boolean performPreemptiveSystemCheck() {

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
            cacheNow.delete();
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
            tmpNow.delete();
        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't create directory inside " + tmpDir.getAbsolutePath());
            passed = false;
        }

        try {
            BEAGLEWrapper.install(medsavantDir);
        } catch (IOException iex) {
            LOG.error(iex);
            passed = false;
        }
        return passed;
    }

    private boolean completelyPermissive(File d) {
        return d.canRead() && d.canWrite() && d.canExecute();
    }

    private String permissions(File d) {
        return (d.canRead() ? "r" : "-") + (d.canWrite() ? "w" : "-") + (d.canExecute() ? "x" : "-");
    }
}
