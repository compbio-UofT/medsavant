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

import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.serverapi.*;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.MedSavantFrame;


public class MedSavantClient implements MedSavantServerRegistry {
    private static final Log LOG = LogFactory.getLog(MedSavantClient.class);

    public static CustomTablesAdapter CustomTablesAdapter;
    public static AnnotationLogQueryUtilAdapter AnnotationLogQueryUtilAdapter;
    public static AnnotationQueryUtilAdapter AnnotationQueryUtilAdapter;
    public static ChromosomeQueryUtilAdapter ChromosomeQueryUtilAdapter;
    public static CohortQueryUtilAdapter CohortQueryUtilAdapter;
    public static GeneSetManagerAdapter GeneSetManager;
    public static LogQueryUtilAdapter LogQueryUtilAdapter;
    public static PatientQueryUtilAdapter PatientQueryUtilAdapter;
    public static ProjectQueryUtilAdapter ProjectQueryUtilAdapter;
    public static UserQueryUtilAdapter UserQueryUtilAdapter;
    public static SettingsQueryUtilAdapter SettingsQueryUtilAdapter;
    public static ServerLogQueryUtilAdapter ServerLogQueryUtilAdapter;
    public static RegionSetManagerAdapter RegionSetManager;
    public static ReferenceQueryUtilAdapter ReferenceQueryUtilAdapter;
    public static QueryUtilAdapter QueryUtilAdapter;
    public static DBUtilAdapter DBUtilAdapter;
    public static SetupAdapter SetupAdapter;
    public static VariantManagerAdapter VariantManager;
    public static NotificationQueryUtilAdapter NotificationQueryUtilAdapter;

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

        if (initialized) return;

        Registry registry;
        LOG.debug("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");
        registry = LocateRegistry.getRegistry(serverAddress,(new Integer(serverPort)).intValue());
        LOG.debug("Connected");

        // look up the remote object
        LOG.debug("Retrieving adapters...");
        setAdaptersFromRegistry(registry);
        LOG.debug("Done");
    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException {

        LoginController.SessionAdapter = (SessionAdapter)registry.lookup(Registry_SessionAdapter);

        AnnotationLogQueryUtilAdapter = (AnnotationLogQueryUtilAdapter)registry.lookup(Registry_AnnotationLogQueryUtilAdapter);
        AnnotationQueryUtilAdapter = (AnnotationQueryUtilAdapter)registry.lookup(Registry_AnnotationQueryUtilAdapter);
        ChromosomeQueryUtilAdapter = (ChromosomeQueryUtilAdapter)registry.lookup(Registry_ChromosomeQueryUtilAdapter);
        CohortQueryUtilAdapter = (CohortQueryUtilAdapter) (registry.lookup(Registry_CohortQueryUtilAdapter));
        LogQueryUtilAdapter = (LogQueryUtilAdapter)registry.lookup(Registry_LogQueryUtilAdapter);
        PatientQueryUtilAdapter = (PatientQueryUtilAdapter)registry.lookup(Registry_PatientQueryUtilAdapter);
        ProjectQueryUtilAdapter = (ProjectQueryUtilAdapter)registry.lookup(Registry_ProjectQueryUtilAdapter);
        QueryUtilAdapter = (QueryUtilAdapter)registry.lookup(Registry_QueryUtilAdapter);
        GeneSetManager = (GeneSetManagerAdapter)registry.lookup(Registry_GeneSetManager);
        ReferenceQueryUtilAdapter = (ReferenceQueryUtilAdapter)registry.lookup(Registry_ReferenceQueryUtilAdapter);
        RegionSetManager = (RegionSetManagerAdapter)registry.lookup(Registry_RegionSetManager);
        ServerLogQueryUtilAdapter = (ServerLogQueryUtilAdapter)registry.lookup(Registry_ServerLogQueryUtilAdapter);
        SettingsQueryUtilAdapter = (SettingsQueryUtilAdapter)registry.lookup(Registry_SettingsQueryUtilAdapter);
        UserQueryUtilAdapter = (UserQueryUtilAdapter)registry.lookup(Registry_UserQueryUtilAdapter);
        VariantManager = (VariantManagerAdapter)registry.lookup(Registry_VariantManager);
        DBUtilAdapter = (DBUtilAdapter)registry.lookup(Registry_DBUtilAdapter);
        SetupAdapter = (SetupAdapter)registry.lookup(Registry_SetupAdapter);
        CustomTablesAdapter = (CustomTablesAdapter)registry.lookup(Registry_CustomTablesAdapter);
        NotificationQueryUtilAdapter = (NotificationQueryUtilAdapter)registry.lookup(Registry_NotificationQueryUtilAdapter);
    }

    private static void setLAF() {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            if (MiscUtils.WINDOWS) {
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);
            }
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            //tooltips
            UIManager.put("ToolTip.background", new ColorUIResource(255,255,255));
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
