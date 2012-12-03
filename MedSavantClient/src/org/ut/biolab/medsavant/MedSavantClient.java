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
package org.ut.biolab.medsavant;

import org.ut.biolab.medsavant.shared.serverapi.CustomTablesAdapter;
import org.ut.biolab.medsavant.shared.serverapi.RegionSetManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.OntologyManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.NetworkManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.SessionManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.UserManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.CohortManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.shared.serverapi.GeneSetManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.NotificationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.DBUtilsAdapter;
import org.ut.biolab.medsavant.shared.serverapi.ReferenceManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.PatientManagerAdapter;
import java.rmi.*;
import java.rmi.registry.*;
import java.awt.Insets;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.jidesoft.plaf.LookAndFeelFactory;
import gnu.getopt.Getopt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.controller.SettingsController;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;

public class MedSavantClient implements MedSavantServerRegistry {

    private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
    public static CustomTablesAdapter CustomTablesManager;
    public static AnnotationManagerAdapter AnnotationManagerAdapter;
    public static CohortManagerAdapter CohortManager;
    public static GeneSetManagerAdapter GeneSetManager;
    public static LogManagerAdapter LogManager;
    public static NetworkManagerAdapter NetworkManager;
    public static OntologyManagerAdapter OntologyManager;
    public static PatientManagerAdapter PatientManager;
    public static ProjectManagerAdapter ProjectManager;
    public static UserManagerAdapter UserManager;
    public static SessionManagerAdapter SessionManager;
    public static SettingsManagerAdapter SettingsManager;
    public static RegionSetManagerAdapter RegionSetManager;
    public static ReferenceManagerAdapter ReferenceManager;
    public static DBUtilsAdapter DBUtils;
    public static SetupAdapter SetupManager;
    public static VariantManagerAdapter VariantManager;
    public static NotificationManagerAdapter NotificationManager;
    public static boolean initialized = false;
    private static MedSavantFrame frame;

    static public void main(String args[]) {

        verifyJIDE();
        setLAF();




        //required for FORGE plugin
        //NativeInterface.open();

        SettingsController.getInstance();

        Getopt g = new Getopt("MedSavant", args, "h:p:d:u:w:");
        int c;

        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    String host = g.getOptarg();
                    SettingsController.getInstance().setServerAddress(host);
                    break;
                case 'p':
                    int port = Integer.parseInt(g.getOptarg());
                    SettingsController.getInstance().setServerPort(port + "");
                    break;
                case 'd':
                    String dbname = g.getOptarg();
                    SettingsController.getInstance().setDBName(dbname);
                    break;
                case 'u':
                    String username = g.getOptarg();
                    SettingsController.getInstance().setUsername(username);
                    break;
                case 'w':
                    String password = g.getOptarg();
                    SettingsController.getInstance().setPassword(password);
                    break;
                case '?':
                    break; // getopt() already printed an error
                default:
                    System.out.print("getopt() returned " + c + "\n");
            }
        }


        frame = MedSavantFrame.getInstance();
        frame.setExtendedState(MedSavantFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        LOG.info("MedSavant booted.");

        //required for FORGE plugin
        //NativeInterface.runEventPump();
    }

    public static void initializeRegistry(String serverAddress, String serverPort) throws RemoteException, NotBoundException {

        if (initialized) {
            return;
        }

        Registry registry;
        LOG.debug("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");
        registry = LocateRegistry.getRegistry(serverAddress, (new Integer(serverPort)).intValue());
        LOG.debug("Connected");

        // look up the remote object
        LOG.debug("Retrieving adapters...");
        setAdaptersFromRegistry(registry);
        LOG.debug("Done");
    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException {

        AnnotationManagerAdapter = (AnnotationManagerAdapter) registry.lookup(ANNOTATION_MANAGER);
        CohortManager = (CohortManagerAdapter) (registry.lookup(COHORT_MANAGER));
        LogManager = (LogManagerAdapter) registry.lookup(LOG_MANAGER);
        NetworkManager = (NetworkManagerAdapter) registry.lookup(NETWORK_MANAGER);
        OntologyManager = (OntologyManagerAdapter) registry.lookup(ONTOLOGY_MANAGER);
        PatientManager = (PatientManagerAdapter) registry.lookup(PATIENT_MANAGER);
        ProjectManager = (ProjectManagerAdapter) registry.lookup(PROJECT_MANAGER);
        GeneSetManager = (GeneSetManagerAdapter) registry.lookup(GENE_SET_MANAGER);
        ReferenceManager = (ReferenceManagerAdapter) registry.lookup(REFERENCE_MANAGER);
        RegionSetManager = (RegionSetManagerAdapter) registry.lookup(REGION_SET_MANAGER);
        SessionManager = (SessionManagerAdapter) registry.lookup(SESSION_MANAGER);
        SettingsManager = (SettingsManagerAdapter) registry.lookup(SETTINGS_MANAGER);
        UserManager = (UserManagerAdapter) registry.lookup(USER_MANAGER);
        VariantManager = (VariantManagerAdapter) registry.lookup(VARIANT_MANAGER);
        DBUtils = (DBUtilsAdapter) registry.lookup(DB_UTIL_MANAGER);
        SetupManager = (SetupAdapter) registry.lookup(SETUP_MANAGER);
        CustomTablesManager = (CustomTablesAdapter) registry.lookup(CUSTOM_TABLES_MANAGER);
        NotificationManager = (NotificationManagerAdapter) registry.lookup(NOTIFICATION_MANAGER);
    }

    private static void setLAF() {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            if (MiscUtils.WINDOWS) {
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);
            }
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

            //tooltips
            UIManager.put("ToolTip.background", new ColorUIResource(255, 255, 255));
            ToolTipManager.sharedInstance().setDismissDelay(8000);
            ToolTipManager.sharedInstance().setInitialDelay(500);

        } catch (Exception x) {
            LOG.error("Unable to install look & feel.", x);
        }

    }

    private static void verifyJIDE() {
        com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
    }
}
