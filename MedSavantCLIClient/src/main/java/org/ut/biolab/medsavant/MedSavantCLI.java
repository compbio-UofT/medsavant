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
package org.ut.biolab.medsavant;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.DownloadEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.*;

import javax.net.ssl.SSLHandshakeException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.NoRouteToHostException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public class MedSavantCLI implements MedSavantServerRegistry {

    private static final int UPLOAD_BUFFER_SIZE = 4096;

    private static final long serialVersionUID = -77006512859078222L;

    private static final String JSON_PARAM_NAME = "json";

    private static final Log LOG = LogFactory.getLog(MedSavantCLI.class);

    private CohortManagerAdapter cohortManager;

    private PatientManagerAdapter patientManager;

    private CustomTablesAdapter customTablesManager;

    private AnnotationManagerAdapter annotationManager;

    private GeneSetManagerAdapter geneSetManager;

    @SuppressWarnings("unused")
    private LogManagerAdapter logManager;

    private NetworkManagerAdapter networkManager;

    private OntologyManagerAdapter ontologyManager;

    private ProjectManagerAdapter projectManager;

    private UserManagerAdapter userManager;

    private SessionManagerAdapter sessionManager;

    private SettingsManagerAdapter settingsManager;

    private RegionSetManagerAdapter regionSetManager;

    private ReferenceManagerAdapter referenceManager;

    private DBUtilsAdapter dbUtils;

    private SetupAdapter setupManager;

    private VariantManagerAdapter variantManager;

    private NotificationManagerAdapter notificationManager;

    private static boolean initialized = false;

    private String medSavantServerHost;

    private int medSavantServerPort;

    private String username;

    private String password;

    private String db;

    private int maxSimultaneousUploads;

    private Semaphore uploadSem;

    public CohortManagerAdapter getCohortManager() {
        return cohortManager;
    }

    public PatientManagerAdapter getPatientManager() {
        return patientManager;
    }

    public CustomTablesAdapter getCustomTablesManager() {
        return customTablesManager;
    }

    public AnnotationManagerAdapter getAnnotationManager() {
        return annotationManager;
    }

    public GeneSetManagerAdapter getGeneSetManager() {
        return geneSetManager;
    }

    public NetworkManagerAdapter getNetworkManager() {
        return networkManager;
    }

    public OntologyManagerAdapter getOntologyManager() {
        return ontologyManager;
    }

    public ProjectManagerAdapter getProjectManager() {
        return projectManager;
    }

    public UserManagerAdapter getUserManager() {
        return userManager;
    }

    public SessionManagerAdapter getSessionManager() {
        return sessionManager;
    }

    public SettingsManagerAdapter getSettingsManager() {
        return settingsManager;
    }

    public RegionSetManagerAdapter getRegionSetManager() {
        return regionSetManager;
    }

    public ReferenceManagerAdapter getReferenceManager() {
        return referenceManager;
    }

    public DBUtilsAdapter getDbUtils() {
        return dbUtils;
    }

    public SetupAdapter getSetupManager() {
        return setupManager;
    }

    public VariantManagerAdapter getVariantManager() {
        return variantManager;
    }

    public NotificationManagerAdapter getNotificationManager() {
        return notificationManager;
    }

    public MedSavantCLI(String[] args) {
        LOG.info("MedSavant CLI starting...");
        try {
            loadConfiguration();
            initializeRegistry(this.medSavantServerHost, Integer.toString(this.medSavantServerPort));
//            session = new Session(sessionManager, username, password, db);
            copyFile("");
            processFile("");

        } catch (Exception ex) {
            LOG.error(ex);
        }
    }

    private void processFile(String s) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (ProjectController.getInstance().promptForUnpublished()) {
                        String sessionID = LoginController.getSessionID();
                        int projectID = ProjectController.getInstance().getCurrentProjectID();

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                AppDirectory.getTaskManager().showMessageForTask(instance,
                                        "<html>Variants have been uploaded and are now being processed.<br/>"
                                                + "You may view progress in the Server Log in the Task Manager<br/><br/>"
                                                + "You may log out or continue doing work.</html>");
                                notification.close();
                            }

                        });
                        variantManager.uploadVariants(
                                sessionID,
                                transferIDs,
                                projectID,
                                ReferenceController.getInstance().getCurrentReferenceID(),
                                new String[][]{},
                                ProjectController.getInstance().getContainsRefCalls(sessionID, projectID),
                                emailPlaceholder.getText(),
                                true,
                                annovarCheckbox.isSelected(),
                                phasingCheckbox.isSelected());

                        succeeded();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOG.error("Error: ", ex);
                    instance.addLog("Error: " + ex.getMessage());
                    instance.setStatus(TaskStatus.ERROR);
                    AppDirectory.getTaskManager().showErrorForTask(instance, ex);
                }
            }

            private void succeeded() {
                LOG.info("Upload succeeded");

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        LOG.info("Upload succeeded");
                        AppDirectory.getTaskManager().showMessageForTask(instance,
                                "<html>Variants have completed being imported.<br/>"
                                        + "As a result, you must login again.</html>");
                        MedSavantFrame.getInstance().requestLogoutAndRestart();
                    }

                });

            }

        });

        t.start();
    }

    private void copyFile(String file) throws ExecutionException, InterruptedException {
        ClientNetworkUtils.copyFileToServer(new File(file), new Listener<DownloadEvent>() {

            @Override
            public void handleEvent(DownloadEvent event) {
                switch (event.getType()) {
                    case PROGRESS:
                        //handle progress
                        break;
                }
            }
        }).get();
    }

    public void initializeRegistry(String serverAddress, String serverPort) throws RemoteException, NotBoundException,
            NoRouteToHostException, ConnectIOException {

        if (initialized) {
            return;
        }

        int port = (new Integer(serverPort)).intValue();

        Registry registry;

        LOG.info("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");

        try {
            registry = LocateRegistry.getRegistry(serverAddress, port, new SslRMIClientSocketFactory());
            LOG.debug("Retrieving adapters...");
            setAdaptersFromRegistry(registry);
            LOG.info("Connected with SSL/TLS Encryption");
            initialized = true;
        } catch (ConnectIOException ex) {
            if (ex.getCause() instanceof SSLHandshakeException) {
                registry = LocateRegistry.getRegistry(serverAddress, port);
                LOG.info("Retrieving adapters...");
                setAdaptersFromRegistry(registry);
                LOG.info("Connected without SSL/TLS encryption");
            }
        }
        LOG.info("Done");

    }

    private void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException,
            NoRouteToHostException, ConnectIOException {
        this.annotationManager = (AnnotationManagerAdapter) registry.lookup(ANNOTATION_MANAGER);
        this.cohortManager = (CohortManagerAdapter) (registry.lookup(COHORT_MANAGER));
        this.logManager = (LogManagerAdapter) registry.lookup(LOG_MANAGER);
        this.networkManager = (NetworkManagerAdapter) registry.lookup(NETWORK_MANAGER);
        this.ontologyManager = (OntologyManagerAdapter) registry.lookup(ONTOLOGY_MANAGER);
        this.patientManager = (PatientManagerAdapter) registry.lookup(PATIENT_MANAGER);
        this.projectManager = (ProjectManagerAdapter) registry.lookup(PROJECT_MANAGER);
        this.geneSetManager = (GeneSetManagerAdapter) registry.lookup(GENE_SET_MANAGER);
        this.referenceManager = (ReferenceManagerAdapter) registry.lookup(REFERENCE_MANAGER);
        this.regionSetManager = (RegionSetManagerAdapter) registry.lookup(REGION_SET_MANAGER);
        this.sessionManager = (SessionManagerAdapter) registry.lookup(SESSION_MANAGER);
        this.settingsManager = (SettingsManagerAdapter) registry.lookup(SETTINGS_MANAGER);
        this.userManager = (UserManagerAdapter) registry.lookup(USER_MANAGER);
        this.variantManager = (VariantManagerAdapter) registry.lookup(VARIANT_MANAGER);
        this.dbUtils = (DBUtilsAdapter) registry.lookup(DB_UTIL_MANAGER);
        this.setupManager = (SetupAdapter) registry.lookup(SETUP_MANAGER);
        this.customTablesManager = (CustomTablesAdapter) registry.lookup(CUSTOM_TABLES_MANAGER);
        this.notificationManager = (NotificationManagerAdapter) registry.lookup(NOTIFICATION_MANAGER);
    }

    private InputStream getConfigInputStream() throws Exception, IOException{

        File f = null;
        String cf = ""; // TODO: get config file

        if(!cf.startsWith("/")){
            LOG.info("Looking for configuration from path relative to servlet context "+cf);
            InputStream in =  MedSavantCLI.class.getResourceAsStream("/" + cf); //TODO: should be relative to current dir
            if(in != null){
                LOG.info("Reading configuration from /"+cf);
                return in;
            }
        }else{
            f = new File(cf);
            LOG.info("Looking for config file at: "+f.getAbsolutePath());
            if(!f.exists()){
                f = null;
            }
        }

        if(f == null){
            throw new Exception("Can't load configuration - no config file found!");
        }

        LOG.info("Reading configuration from "+f.getAbsolutePath());

        return new FileInputStream(f);
    }
    private void loadConfiguration() throws Exception {
        String host = null;
        String uname = null;
        String pass = null;
        String dbase = null;
        String maxSimultaneousUploadsStr = null;
        int p = -1;
        try {                                          
            Properties props = new Properties();
            InputStream in = getConfigInputStream();
            props.load(in);
            in.close();

            host = props.getProperty("host", "");
            uname = props.getProperty("username", "");
            pass = props.getProperty("password", "");
            dbase = props.getProperty("db", "");
            maxSimultaneousUploadsStr = props.getProperty("max_simultaneous_uploads", "").trim();
            String portStr = props.getProperty("port", "-1").trim();
            if (StringUtils.isBlank(portStr) || !NumberUtils.isNumber(portStr)) {
                LOG.error("No port specified in configuration, cannot continue");
            }
            if (maxSimultaneousUploadsStr == null) {
                throw new Exception("No maximum number of simultaneous uploads specified.  Cannot continue");
            }
            p = Integer.parseInt(portStr);
            if (p <= 0) {
                throw new Exception("Illegal port specified in configuration: " + portStr + ", cannot continue.");
            }

            maxSimultaneousUploads = Integer.parseInt(maxSimultaneousUploadsStr);
            if (maxSimultaneousUploads <= 0) {
                throw new Exception("Illegal number of maximum simultaneous uploads in configuration: " + maxSimultaneousUploadsStr + ", cannot continue.");
            }

            uploadSem = new Semaphore(maxSimultaneousUploads, true);
            if(StringUtils.isBlank(uname)){
                throw new Exception("No username specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(pass)) {
                throw new Exception("No password specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(dbase)) {
                throw new Exception("No database specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(host)) {
                throw new Exception("No host specified in configuration file, cannot continue.");
            }
        } catch (IOException iex) {
            throw new Exception("IO Exception reading config file, cannot continue: " + iex.getMessage());
        }
        this.medSavantServerHost = host;
        this.medSavantServerPort = p;
        this.username = uname;
        this.password = pass;
        this.db = dbase;

        LOG.info("Configured with:");
        LOG.info("Host = " + host);
        LOG.info("Port = " + p);
        LOG.info("Username = " + uname);
        LOG.info("Database = " + this.db);
    }

    public static void main(String[] args) {
        MedSavantCLI cli = new MedSavantCLI(args);
    }
}
