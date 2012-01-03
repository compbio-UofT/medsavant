/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.server;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.ut.biolab.medsavant.db.admin.Setup;
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
import org.ut.biolab.medsavant.server.api.MedSavantServerRegistry;

/**
 *
 * @author mfiume
 */
public class MedSavantServerEngine extends java.rmi.server.UnicastRemoteObject {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    public MedSavantServerEngine() throws RemoteException {
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("can't get inet address.");
        }
        thisPort = 3232;  // this port(registry’s port)
        System.out.println("this address=" + thisAddress + ",port=" + thisPort);
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(thisPort);

            //TODO: get these from the user
            ConnectionController.setHost("localhost");
            ConnectionController.setPort(5029);

            bindAdapters(registry);


        } catch (RemoteException e) {
            throw e;
        }
    }
    
    static public void main(String args[]) {
        try {
            MedSavantServerEngine s = new MedSavantServerEngine();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void bindAdapters(Registry registry) throws RemoteException {

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
        registry.rebind(MedSavantServerRegistry.Registry_SetupAdapter, Setup.getInstance());
        registry.rebind(MedSavantServerRegistry.Registry_CustomTablesAdapter, CustomTables.getInstance());
    }
}
