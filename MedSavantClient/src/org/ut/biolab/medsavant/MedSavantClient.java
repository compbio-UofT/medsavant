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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jidesoft.plaf.LookAndFeelFactory;
import gnu.getopt.Getopt;
import javax.swing.ToolTipManager;
import javax.swing.plaf.ColorUIResource;
import org.ut.biolab.medsavant.controller.LoginController;

import org.ut.biolab.medsavant.serverapi.AnnotationLogQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.AnnotationQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.ChromosomeQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.CohortQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.LogQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.PatientQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.ProjectQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.QueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.ReferenceQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.RegionQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.ServerLogQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.SettingsQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.UserQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.VariantQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.serverapi.SessionAdapter;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.serverapi.CustomTablesAdapter;
import org.ut.biolab.medsavant.serverapi.DBUtilAdapter;
import org.ut.biolab.medsavant.serverapi.NotificationQueryUtilAdapter;
import org.ut.biolab.medsavant.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.MedSavantFrame;


public class MedSavantClient {

    public static CustomTablesAdapter CustomTablesAdapter;
    public static AnnotationLogQueryUtilAdapter AnnotationLogQueryUtilAdapter;
    public static AnnotationQueryUtilAdapter AnnotationQueryUtilAdapter;
    public static ChromosomeQueryUtilAdapter ChromosomeQueryUtilAdapter;
    public static CohortQueryUtilAdapter CohortQueryUtilAdapter;
    public static LogQueryUtilAdapter LogQueryUtilAdapter;
    public static PatientQueryUtilAdapter PatientQueryUtilAdapter;
    public static ProjectQueryUtilAdapter ProjectQueryUtilAdapter;
    public static VariantQueryUtilAdapter VariantQueryUtilAdapter;
    public static UserQueryUtilAdapter UserQueryUtilAdapter;
    public static SettingsQueryUtilAdapter SettingsQueryUtilAdapter;
    public static ServerLogQueryUtilAdapter ServerLogQueryUtilAdapter;
    public static RegionQueryUtilAdapter RegionQueryUtilAdapter;
    public static ReferenceQueryUtilAdapter ReferenceQueryUtilAdapter;
    public static QueryUtilAdapter QueryUtilAdapter;
    public static DBUtilAdapter DBUtilAdapter;
    public static SetupAdapter SetupAdapter;
    public static VariantManagerAdapter VariantManagerAdapter;
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
        ClientLogger.log(MedSavantClient.class, "MedSavant booted");

        //required for FORGE plugin
        //NativeInterface.runEventPump();
    }

    public static void initializeRegistry(String serverAddress, String serverPort) throws RemoteException, NotBoundException {

        if(initialized) return;

        Registry registry;
        System.out.print("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");
        System.out.flush();
        registry = LocateRegistry.getRegistry(serverAddress,(new Integer(serverPort)).intValue());
        System.out.println("Connected");

        // look up the remote object
        System.out.print("Retrieving adapters...");
        System.out.flush();
        setAdaptersFromRegistry(registry);
        System.out.println("Done");
    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException {

        VariantManagerAdapter = (VariantManagerAdapter) (registry.lookup(MedSavantServerRegistry.Registry_UploadVariantsAdapter));

        LoginController.SessionAdapter = (SessionAdapter) (registry.lookup(MedSavantServerRegistry.Registry_SessionAdapter));

        AnnotationLogQueryUtilAdapter = (AnnotationLogQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_AnnotationLogQueryUtilAdapter));
        AnnotationQueryUtilAdapter = (AnnotationQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_AnnotationQueryUtilAdapter));
        ChromosomeQueryUtilAdapter = (ChromosomeQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_ChromosomeQueryUtilAdapter));
        CohortQueryUtilAdapter = (CohortQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_CohortQueryUtilAdapter));
        LogQueryUtilAdapter = (LogQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_LogQueryUtilAdapter));
        PatientQueryUtilAdapter = (PatientQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_PatientQueryUtilAdapter));
        ProjectQueryUtilAdapter = (ProjectQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_ProjectQueryUtilAdapter));
        QueryUtilAdapter = (QueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_QueryUtilAdapter));
        ReferenceQueryUtilAdapter = (ReferenceQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_ReferenceQueryUtilAdapter));
        RegionQueryUtilAdapter = (RegionQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_RegionQueryUtilAdapter));
        ServerLogQueryUtilAdapter = (ServerLogQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_ServerLogQueryUtilAdapter));
        SettingsQueryUtilAdapter = (SettingsQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_SettingsQueryUtilAdapter));
        UserQueryUtilAdapter = (UserQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_UserQueryUtilAdapter));
        VariantQueryUtilAdapter = (VariantQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_VariantQueryUtilAdapter));
        DBUtilAdapter = (DBUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_DBUtilAdapter));
        SetupAdapter = (SetupAdapter) (registry.lookup(MedSavantServerRegistry.Registry_SetupAdapter));
        CustomTablesAdapter = (CustomTablesAdapter) (registry.lookup(MedSavantServerRegistry.Registry_CustomTablesAdapter));
        NotificationQueryUtilAdapter = (NotificationQueryUtilAdapter) (registry.lookup(MedSavantServerRegistry.Registry_NotificationQueryUtilAdapter));
    }

    public static final boolean MAC;
    public static final boolean WINDOWS;
    public static final boolean LINUX;


    static {
        String os = System.getProperty("os.name").toLowerCase();
        MAC = os.startsWith("mac");
        WINDOWS = os.startsWith("windows");
        LINUX = os.contains("linux");
    }

    private static void setLAF() {
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            if(WINDOWS){
                LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);
            }
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            //tooltips
            UIManager.put("ToolTip.background", new ColorUIResource(255,255,255));
            ToolTipManager.sharedInstance().setDismissDelay(8000);
            ToolTipManager.sharedInstance().setInitialDelay(500);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MedSavantClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MedSavantClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MedSavantClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MedSavantClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void verifyJIDE() {
        com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
    }
}
