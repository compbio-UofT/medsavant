/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server;

import gnu.getopt.Getopt;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ChromosomeQueryUtil;
import org.ut.biolab.medsavant.db.util.query.CohortQueryUtil;
import org.ut.biolab.medsavant.db.util.query.LogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.PatientQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.db.util.query.QueryUtil;
import org.ut.biolab.medsavant.db.util.query.ReferenceQueryUtil;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ServerLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.SettingsQueryUtil;
import org.ut.biolab.medsavant.db.util.query.UserQueryUtil;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.variants.update.VariantManager;
import org.ut.biolab.medsavant.server.api.MedSavantServerRegistry;

/**
 *
 * @author mfiume
 */
public class MedSavantServerEngine extends java.rmi.server.UnicastRemoteObject {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    public MedSavantServerEngine(String host, int port, String rootuser) throws RemoteException, SQLException {
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get inet address.");
        }
        thisPort = 3232;  // this port(registry’s port)

        System.out.println("== MedSavant Server Engine ==");

        System.out.println(
                "SERVER ADDRESS: " + thisAddress + "\n"
                + "SERVER PORT: " + thisPort);
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(thisPort);

            //TODO: get these from the user

            ConnectionController.setHost(host);
            ConnectionController.setPort(port);

            System.out.println(
                    "DATABASE ADDRESS: " + host + "\n"
                    + "DATABASE PORT: " + port);

            System.out.println("DATABASE USER: " + rootuser);
            System.out.print("PASSWORD FOR " + rootuser + ": ");
            System.out.flush();

            char[] pass = System.console().readPassword();

            System.out.println();

            System.out.print("Connecting to database ... ");
            try {
                ConnectionController.connectOnce(host,port,"",rootuser,new String(pass));
            } catch (SQLException ex) {
                System.out.println("FAILED");
                
                throw ex;
            }
            System.out.println("OK");

            bindAdapters(registry);

            System.out.println("\nServer initialized, waiting for incoming connections...");

        } catch (RemoteException e) {
            throw e;
        }
    }

    static public void main(String args[]) {
        try {

            Getopt g = new Getopt("MedSavantServerEngine", args, "h:p:u:");
            //
            int c;
            String arg;


            String user = "root";
            String host = "localhost";
            int port = 5029;

            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 'h':
                        host = g.getOptarg();
                        break;
                    case 'p':
                        port = Integer.parseInt(g.getOptarg());
                        break;
                    case 'u':
                        user = g.getOptarg();
                        break;
                    case '?':
                        break; // getopt() already printed an error
                    default:
                        System.out.print("getopt() returned " + c + "\n");
                }
            }

            MedSavantServerEngine s = new MedSavantServerEngine(host,port,user);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void bindAdapters(Registry registry) throws RemoteException {

        System.out.print("Initializing server registry ... ");
        System.out.flush();

        registry.rebind(MedSavantServerRegistry.Registry_UploadVariantsAdapter, VariantManager.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_FileTransferAdapter, FileServer.getInstance());

        registry.rebind(MedSavantServerRegistry.Registry_SessionAdapter, SessionController.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_CustomTablesAdapter, CustomTables.getInstance());

        registry.rebind(MedSavantServerRegistry.Registry_AnnotationLogQueryUtilAdapter, AnnotationLogQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_AnnotationQueryUtilAdapter, AnnotationQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_ChromosomeQueryUtilAdapter, ChromosomeQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_CohortQueryUtilAdapter, CohortQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_LogQueryUtilAdapter, LogQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_PatientQueryUtilAdapter, PatientQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_ProjectQueryUtilAdapter, ProjectQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_QueryUtilAdapter, QueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_ReferenceQueryUtilAdapter, ReferenceQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_RegionQueryUtilAdapter, RegionQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_ServerLogQueryUtilAdapter, ServerLogQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_SettingsQueryUtilAdapter, SettingsQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_UserQueryUtilAdapter, UserQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_VariantQueryUtilAdapter, VariantQueryUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_DBUtilAdapter, DBUtil.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_SetupAdapter, SetupMedSavantDatabase.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_CustomTablesAdapter, CustomTables.getInstance());

        System.out.println("OK");
    }
}
