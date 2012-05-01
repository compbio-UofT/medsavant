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

import gnu.getopt.Getopt;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

import org.ut.biolab.medsavant.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.serverapi.*;
import org.ut.biolab.medsavant.db.variants.update.VariantManager;
import org.ut.biolab.medsavant.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author mfiume
 */
public class MedSavantServerEngine extends MedSavantServerUnicastRemoteObject implements MedSavantServerRegistry {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    public MedSavantServerEngine(String host, int port, String rootuser) throws RemoteException, SQLException {
        super();

        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get inet address.");
        }
        thisPort = super.getPort();  // this port(registry’s port)

        System.out.println("== MedSavant Server Engine ==\n");

        System.out.println("> Server Information:");
        System.out.println(
                "SERVER ADDRESS: " + thisAddress + "\n"
                + "SERVER PORT: " + thisPort);
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(thisPort);

            //TODO: get these from the user

            ConnectionController.setHost(host);
            ConnectionController.setPort(port);

            System.out.println();
            System.out.println("> Database Information:");

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

        registry.rebind(Registry_UploadVariantsAdapter, VariantManager.getInstance());

        registry.rebind(Registry_SessionAdapter, SessionController.getInstance());
        registry.rebind(Registry_CustomTablesAdapter, CustomTables.getInstance());

        registry.rebind(Registry_AnnotationLogQueryUtilAdapter, AnnotationLogQueryUtil.getInstance());
        registry.rebind(Registry_AnnotationQueryUtilAdapter, AnnotationQueryUtil.getInstance());
        registry.rebind(Registry_ChromosomeQueryUtilAdapter, ChromosomeQueryUtil.getInstance());
        registry.rebind(Registry_CohortQueryUtilAdapter, CohortQueryUtil.getInstance());
        registry.rebind(Registry_GeneSetAdapter, GeneSetManager.getInstance());
        registry.rebind(Registry_LogQueryUtilAdapter, LogQueryUtil.getInstance());
        registry.rebind(Registry_PatientQueryUtilAdapter, PatientQueryUtil.getInstance());
        registry.rebind(Registry_ProjectQueryUtilAdapter, ProjectQueryUtil.getInstance());
        registry.rebind(Registry_QueryUtilAdapter, QueryUtil.getInstance());
        registry.rebind(Registry_ReferenceQueryUtilAdapter, ReferenceQueryUtil.getInstance());
        registry.rebind(Registry_RegionQueryUtilAdapter, RegionQueryUtil.getInstance());
        registry.rebind(Registry_ServerLogQueryUtilAdapter, ServerLogQueryUtil.getInstance());
        registry.rebind(Registry_SettingsQueryUtilAdapter, SettingsQueryUtil.getInstance());
        registry.rebind(Registry_UserQueryUtilAdapter, UserQueryUtil.getInstance());
        registry.rebind(Registry_VariantQueryUtilAdapter, VariantQueryUtil.getInstance());
        registry.rebind(Registry_DBUtilAdapter, DBUtil.getInstance());
        registry.rebind(Registry_SetupAdapter, SetupMedSavantDatabase.getInstance());
        registry.rebind(Registry_CustomTablesAdapter, CustomTables.getInstance());
        registry.rebind(Registry_NotificationQueryUtilAdapter, NotificationQueryUtil.getInstance());

        System.out.println("OK");
    }
}
