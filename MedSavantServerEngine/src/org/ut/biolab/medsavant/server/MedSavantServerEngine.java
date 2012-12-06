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
import org.ut.biolab.medsavant.server.serverapi.SettingsManager;
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
import java.util.Scanner;

import org.ut.biolab.medsavant.server.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.server.db.connection.ConnectionController;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.server.ontology.OntologyManager;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.shared.util.MiscUtils;


/**
 *
 * @author mfiume
 */
public class MedSavantServerEngine extends MedSavantServerUnicastRemoteObject implements MedSavantServerRegistry {

    int listenOnPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.


    public MedSavantServerEngine(String databaseHost, int databasePort, String rootUserName) throws RemoteException, SQLException {

        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get inet address.");
        }

        listenOnPort = MedSavantServerUnicastRemoteObject.getListenPort();

        System.out.println("== MedSavant Server Engine ==\n");

        System.out.println("> Server Information:");
        System.out.println(
                "SERVER ADDRESS: " + thisAddress + "\n"
                + "LISTENING ON PORT: " + listenOnPort + "\n"
                + "EXPORTING ON PORT: " + MedSavantServerUnicastRemoteObject.getExportPort());
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(listenOnPort);

            //TODO: get these from the user

            ConnectionController.setHost(databaseHost);
            ConnectionController.setPort(databasePort);

            System.out.println();
            System.out.println("> Database Information:");

            System.out.println(
                    "DATABASE ADDRESS: " + databaseHost + "\n"
                    + "DATABASE PORT: " + databasePort);

            System.out.println("DATABASE USER: " + rootUserName);
            System.out.print("PASSWORD FOR " + rootUserName + ": ");
            System.out.flush();

            boolean retrieveFromConsole = System.console() != null;
            
            char[] pass;
            if (retrieveFromConsole) {
                pass = System.console().readPassword();
            } else {
                Scanner sc = new Scanner(System.in);
                String passStr = sc.nextLine();
                pass = passStr.toCharArray();
            }
            
            System.out.println();

            System.out.print("Connecting to database ... ");
            try {
                ConnectionController.connectOnce(databaseHost,databasePort,"",rootUserName,new String(pass));
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

            Getopt g = new Getopt("MedSavantServerEngine", args, "l:h:p:u:");
            //
            int c;

            String user = "root";
            String host = "localhost";
            int port = 5029;

            // print usage
            if (args.length > 0 && args[0].equals("--help")) {
                System.out.println("java -jar -Djava.rmi.server.hostname=<hostname> MedSavantServerEngine.jar [-l RMI_PORT] [-h DATABASE_HOST] [-p DATABASE_PORT] [-u DATABASE_ROOT_USER]");
                return;
            }


            while ((c = g.getopt()) != -1) {
                switch (c) {
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
                        MedSavantServerUnicastRemoteObject.setExportPort(listenOnPort+1);
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



            new MedSavantServerEngine(host,port,user);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void bindAdapters(Registry registry) throws RemoteException {

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
}
