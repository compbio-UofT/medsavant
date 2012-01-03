
package org.ut.biolab.medsavant;

import java.rmi.*;
import java.rmi.registry.*;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jidesoft.plaf.LookAndFeelFactory;
import org.ut.biolab.medsavant.controller.LoginController;

import org.ut.biolab.medsavant.db.util.query.api.AnnotationLogQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.AnnotationQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.ChromosomeQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.CohortQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.LogQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.PatientQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.ProjectQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.QueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.ReferenceQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.RegionQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.ServerLogQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.SettingsQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.UserQueryUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.VariantQueryUtilAdapter;
import org.ut.biolab.medsavant.server.api.MedSavantServerRegistry;
import org.ut.biolab.medsavant.server.api.SessionAdapter;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.db.util.query.api.DBUtilAdapter;
import org.ut.biolab.medsavant.db.util.query.api.SetupAdapter;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.MainFrame;


public class MedSavantClient {

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
    //public static SessionAdapter SessionAdapter;
    
    //public static String sessionId;
    
    private static MainFrame frame;
    
    static public void main(String args[]) {

        System.setProperty("java.security.policy", "client.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
        /*try {
            // get the “registry”
            registry = LocateRegistry.getRegistry(
                    serverAddress,
                    (new Integer(serverPort)).intValue());

            // look up the remote object

            setAdaptersFromRegistry(registry);

            // call the remote method

            sessionId = SessionAdapter.registerNewSession("root", "", "tgp");

            System.out.println("server>" + sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }*/
        
        try {    
            initializeRegistry();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        verifyJIDE();
        setLAF();
        SettingsController.getInstance();
        frame = MainFrame.getInstance();
        frame.setExtendedState(MainFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        ClientLogger.log(Main.class, "MedSavant booted");
        
    }
    
    public static void initializeRegistry() throws RemoteException, NotBoundException {
        
        Registry registry;
        String serverAddress = "localhost";
        String serverPort = "3232";
        
        // get the “registry”
        registry = LocateRegistry.getRegistry(
                serverAddress,
                (new Integer(serverPort)).intValue());

        // look up the remote object
        setAdaptersFromRegistry(registry);

        // call the remote method
        //sessionId = SessionAdapter.registerNewSession("root", "", "tgp");

        //System.out.println("server>" + sessionId);
    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException {
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
    }
    
    private static void setLAF() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0)); 
            LookAndFeelFactory.installJideExtension(LookAndFeelFactory.XERTO_STYLE_WITHOUT_MENU);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void verifyJIDE() {
        com.jidesoft.utils.Lm.verifyLicense("Marc Fiume", "Savant Genome Browser", "1BimsQGmP.vjmoMbfkPdyh0gs3bl3932");
    }
}
