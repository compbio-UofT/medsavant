
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.ut.biolab.medsavant.server.api.RemoteFileServer;
import org.ut.biolab.medsavant.server.api.SessionAdapter;

public class MedSavantClient {

    private static AnnotationLogQueryUtilAdapter AnnotationLogQueryUtilAdapter;
    private static AnnotationQueryUtilAdapter AnnotationQueryUtilAdapter;
    private static ChromosomeQueryUtilAdapter ChromosomeQueryUtilAdapter;
    private static CohortQueryUtilAdapter CohortQueryUtilAdapter;
    private static LogQueryUtilAdapter LogQueryUtilAdapter;
    private static PatientQueryUtilAdapter PatientQueryUtilAdapter;
    private static ProjectQueryUtilAdapter ProjectQueryUtilAdapter;
    private static VariantQueryUtilAdapter VariantQueryUtilAdapter;
    private static UserQueryUtilAdapter UserQueryUtilAdapter;
    private static SettingsQueryUtilAdapter SettingsQueryUtilAdapter;
    private static ServerLogQueryUtilAdapter ServerLogQueryUtilAdapter;
    private static RegionQueryUtilAdapter RegionQueryUtilAdapter;
    private static ReferenceQueryUtilAdapter ReferenceQueryUtilAdapter;
    private static QueryUtilAdapter QueryUtilAdapter;
    private static SessionAdapter SessionAdapter;
    private static RemoteFileServer RemoteFileServer;

    static public void main(String args[]) {

        /*
        System.setProperty("java.security.policy", "client.policy");
        if (System.getSecurityManager() == null) {
        System.setSecurityManager(new RMISecurityManager());
        }
         *
         */


        Registry registry;
        String serverAddress = "localhost";
        String serverPort = "3232";

        try {
            // get the “registry”
            registry = LocateRegistry.getRegistry(
                    serverAddress,
                    (new Integer(serverPort)).intValue());

            // look up the remote object

            setAdaptersFromRegistry(registry);

            // call the remote method

            String sessionId = SessionAdapter.registerNewSession("root", "", "tgp");

            System.out.println("server>" + sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException {

        RemoteFileServer = (RemoteFileServer) (registry.lookup(MedSavantServerRegistry.Registry_FileTransferAdapter));
        SessionAdapter = (SessionAdapter) (registry.lookup(MedSavantServerRegistry.Registry_SessionAdapter));
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


        try {
            // grab the file name from the commandline
            String fileName = "/Users/mfiume/Desktop/data.xml";
            // get a handle to the remote service to which we want to send the file
            System.out.println("Sending file " + fileName);

            // setup the remote input stream.  note, the client here is actually
            // acting as an RMI server (very confusing, i know).  this code sets up an
            // RMI server in the client, which the RemoteFileServer will then
            // interact with to get the file data.
            SimpleRemoteInputStream istream = new SimpleRemoteInputStream(
                    new FileInputStream(fileName));

            try {
                try {
                    // call the remote method on the server.  the server will actually
                    // interact with the RMI "server" we started above to retrieve the
                    // file data
                    RemoteFileServer.sendFile(istream.export());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                // always make a best attempt to shutdown RemoteInputStream
                istream.close();
            }

            System.out.println("Finished sending file " + fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MedSavantClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
