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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ut.biolab.medsavant.shared.serverapi.CustomTablesAdapter;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NoRouteToHostException;
import javax.net.ssl.SSLHandshakeException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.in;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.RegionSetManagerAdapter;

public class MedSavantServlet extends HttpServlet implements MedSavantServerRegistry {

    private static final Gson gson; //does not maintain state, can be static.
    private static final String JSON_PARAM_NAME = "json";
    private static final Log LOG = LogFactory.getLog(MedSavantServlet.class);
    public static CohortManagerAdapter CohortManager;
    public static PatientManagerAdapter PatientManager;
    public static CustomTablesAdapter CustomTablesManager;
    public static AnnotationManagerAdapter AnnotationManagerAdapter;
    public static GeneSetManagerAdapter GeneSetManager;
    public static LogManagerAdapter LogManager;
    public static NetworkManagerAdapter NetworkManager;
    public static OntologyManagerAdapter OntologyManager;
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
    private static final Object managerLock = new Object();
    private static boolean initialized = false;

    private static final String medSavantServerHost;
    private static final int medSavantServerPort;
    private static final String username;
    private static final String password;
    private static final String db;
    //Debug variable, for the test method.  Don't use for other purposes. 
    //(doesn't work for multiple users)
    private static Object lastReturnVal;
    private static String sessionId = null;
    private static final int RENEW_RETRY_TIME = 10000;
    private static int UPLOAD_BUFFER_SIZE = 4096;

    static {
        String host=null;
        String uname = null;
        String pass = null;
        String dbase = null;
        int p = -1;
        try {
            InitialContext initialcontext = new InitialContext();
            String ConfigFileLocation = (String) initialcontext.lookup("java:comp/env/MedSavantConfigFile");
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ConfigFileLocation);
            Properties props = new Properties();
            props.load(in);
            in.close();
            
            host = props.getProperty("host", "");
            uname = props.getProperty("username", "");
            pass = props.getProperty("password", "");
            dbase = props.getProperty("db", "");
            
            String portStr = props.getProperty("port", "-1");
            if (portStr == null) {
                LOG.error("No port specified in configuration, cannot continue");
                System.exit(1);
                
            }
            p = Integer.parseInt(portStr);
            if (p <= 0) {
                LOG.error("Illegal port specified in configuration: " + portStr + ", cannot continue.");
                System.exit(1);
            }
                      
            if(uname.length() < 1){
                LOG.error("No username specified in configuration file, cannot continue.");
                System.exit(1);
            }
            if(pass.length() < 1){
                LOG.error("No password specified in configuration file, cannot continue.");
                System.exit(1);
            }
            if(dbase.length() < 1){
                LOG.error("No database specified in configuration file, cannot continue.");
                System.exit(1);
            }
            if(host.length() < 1){
                LOG.error("No host specified in configuration file, cannot continue.");
                System.exit(1);
            }            
        } catch (IOException iex) {
            LOG.error("IO Exception reading config file, cannot continue: "+iex);
            System.exit(1);
        } catch (NamingException ne) {
            LOG.error("Exception while loading config file, cannot continue: "+ne);
            System.exit(1);
        }
        medSavantServerHost = host;
        medSavantServerPort = p;
        username = uname;
        password = pass;
        db = dbase;
        
        LOG.info("Configured with:");
        LOG.info("Host = "+host);
        LOG.info("Port = "+p);
        LOG.info("Username = "+uname);
        LOG.info("Database = "+db);       
    }

    private class SimplifiedCondition {

        private int projectId;
        private int refId;
        private String type;
        private String method;
        private String[] args;

        private Condition getCondition() throws JsonParseException {
            try {
                if (args.length < 1) {
                    throw new JsonParseException("No arguments given for SimplifiedCondition with type " + type + " and method " + method);
                }

                //this should really be cached...
                TableSchema tableSchema = VariantManager.getCustomTableSchema(getSessionId(), projectId, refId);

                DbColumn col = tableSchema.getDBColumn(args[0]);
                if (type.equals("BinaryCondition")) {
                    if (method.equals("lessThan")) {
                        return BinaryCondition.lessThan(col, args[1], Boolean.parseBoolean(args[2]));
                    } else if (method.equals("greaterThan")) {
                        return BinaryCondition.greaterThan(col, args[1], Boolean.parseBoolean(args[2]));
                    } else if (method.equals("equalTo")) {
                        return BinaryCondition.equalTo(col, args[1]);
                    } else if (method.equals("notEqualTo")) {
                        return BinaryCondition.notEqualTo(col, args[1]);
                    } else if (method.equals("like")) {
                        return BinaryCondition.like(col, args[1]);
                    } else if (method.equals("notLike")) {
                        return BinaryCondition.notLike(col, args[1]);
                    }
                    throw new JsonParseException("Unrecognized method " + method + " for simplified condition " + type);
                } else if (type.equals("UnaryCondition")) {
                    if (method.equals("isNull")) {
                        return UnaryCondition.isNull(col);
                    } else if (method.equals("isNotNull")) {
                        return UnaryCondition.isNotNull(col);
                    } else if (method.equals("exists")) {
                        return UnaryCondition.exists(col);
                    } else if (method.equals("unique")) {
                        return UnaryCondition.unique(col);
                    }
                    throw new JsonParseException("Unrecognized method " + method + " for simplified condition " + type);
                }
                throw new JsonParseException("Unrecognized simplified condition type " + type);
            } catch (ArrayIndexOutOfBoundsException ai) {
                throw new JsonParseException("Invalid arguments specified for SimplifiedCondition of type" + type + ", method " + method + ", and args=" + args);
            } catch (SQLException ex) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + ex);
            } catch (RemoteException re) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + re);
            } catch (SessionExpiredException se) {
                throw new JsonParseException("Couldn't fetch variant table schema: " + se);
            }
        }
    }

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();

        //Handle the condition type.
        gsonBuilder.registerTypeAdapter(Condition.class, new JsonDeserializer<Condition>() {
            @Override
            public Condition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
                return gson.fromJson(je, SimplifiedCondition.class).getCondition();
            }
        });
        gson = gsonBuilder.create();
    }

    private static synchronized boolean renewSession() {
        try {
            sessionId = null;
            sessionId = SessionManager.registerNewSession(username, password, db);
            LOG.info("Renewed new session with id " + sessionId);
        } catch (Exception e) {
            //can't recover from this.
            LOG.error("Exception while registering session, retrying in " + RENEW_RETRY_TIME + " ms: " + e);
            sessionId = null;
            return false;
        }

        return true;
    }

    private static synchronized String getSessionId() {
        try {
            if (sessionId == null) {
                while (!renewSession()) {
                    Thread.sleep(RENEW_RETRY_TIME);
                }
            }
            return sessionId;
        } catch (InterruptedException iex) {
            LOG.error("CRITICAL: Thread interrupted while trying to get session id");
        }
        return null;
    }

    private static void test() {
        //A few simple tests.        

        String js = json_invoke("ProjectManager", "getProjectNames", "[\"\"]");
        System.out.println("JS: " + js + "\n");
        String firstProject = ((String[]) lastReturnVal)[0];

        js = json_invoke("ProjectManager", "getProjectID", "[\"" + firstProject + "\"]");
        System.out.println("JS: " + js + "\n");

        int projId = ((Integer) lastReturnVal);

        js = json_invoke("CohortManager", "getCohorts", "[\"" + projId + "\"]");
        System.out.println("JS: " + js + "\n");

        int cohortId = 1;
        js = json_invoke("CohortManager", "getIndividualsInCohort", "[\"" + projId + "\", \"" + cohortId + "\"]");
        System.out.println("JS: " + js + "\n");

        js = json_invoke("PatientManager", "getPatientFields", "[\"" + projId + "\"]");
        System.out.println("JS: " + js + "\n");

    }

    public static String json_invoke(String adapter, String method, String jsonStr) throws IllegalArgumentException {

        adapter = adapter + "Adapter";

        Field selectedAdapter = null;
        for (Field f : MedSavantServlet.class.getFields()) {
            if (f.getType().getSimpleName().equalsIgnoreCase(adapter)) {
                selectedAdapter = f;
            }
        }

        if (selectedAdapter == null) {
            throw new IllegalArgumentException("The adapter " + adapter + " does not exist");
        }

        JsonParser parser = new JsonParser();
        JsonArray gArray = parser.parse(jsonStr).getAsJsonArray();
        JsonElement jse = parser.parse(jsonStr);
        JsonArray jsonArray;

        if (jse.isJsonArray()) {
            jsonArray = jse.getAsJsonArray();
        } else {
            throw new IllegalArgumentException("The json method arguments are not an array");
        }

        Method selectedMethod = null;

        for (Method m : selectedAdapter.getType().getMethods()) {
            if (m.getName().equalsIgnoreCase(method) && m.getGenericParameterTypes().length == (jsonArray.size() + 1)) {
                selectedMethod = m;
            }
        }

        if (selectedMethod == null) {
            throw new IllegalArgumentException("The method " + method + " in adapter " + adapter + " with " + jsonArray.size() + " arguments does not exist");
        }

        int i = 0;
        Object[] methodArgs = new Object[selectedMethod.getParameterTypes().length];

        try {
            for (Type t : selectedMethod.getGenericParameterTypes()) {
                System.out.println("Field " + i + " is " + t.toString() + " for method " + selectedMethod.toString());
                methodArgs[i] = (i > 0) ? gson.fromJson(gArray.get(i - 1), t) : getSessionId();
                ++i;
            }
        } catch (JsonParseException je) {
            LOG.error(je);
        }

        while (true) {
            try {
                Object selectedAdapterInstance = selectedAdapter.get(null);
                if (selectedAdapterInstance == null) {
                    throw new NullPointerException("Requested adapter " + selectedAdapter.getName() + " was not initialized.");
                }
                //Method invocation
                Object returnVal = selectedMethod.invoke(selectedAdapterInstance, methodArgs);
                lastReturnVal = returnVal;
                if (returnVal == null) {
                    return null;
                } else {
                    return gson.toJson(returnVal, selectedMethod.getReturnType());
                }
            } catch (IllegalAccessException iae) {
                throw new IllegalArgumentException("Couldn't execute method with given arguments: " + iae.getMessage());
            } catch (InvocationTargetException ite) {
                if (ite.getCause() instanceof SessionExpiredException) {
                    //session expired.  renew and try again.
                    renewSession();
                } else {
                    throw new IllegalArgumentException("Couldn't execute method with given arguments, " + ite.getCause());
                }
            }
        }
    }

    @Override
    public void init() {
        LOG.info("MedSavant JSON Client/Server booted.");
        try {
            initializeRegistry(medSavantServerHost, Integer.toString(medSavantServerPort));
        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
        }
    }

    public static void initializeRegistry(String serverAddress, String serverPort) throws RemoteException, NotBoundException, NoRouteToHostException, ConnectIOException {

        if (initialized) {
            return;
        }

        int port = (new Integer(serverPort)).intValue();

        Registry registry;

        LOG.debug("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");

        try {
            registry = LocateRegistry.getRegistry(serverAddress, port, new SslRMIClientSocketFactory());
            LOG.debug("Retrieving adapters...");
            setAdaptersFromRegistry(registry);
            LOG.info("Connected with SSL/TLS Encryption");
            initialized = true;
        } catch (ConnectIOException ex) {
            if (ex.getCause() instanceof SSLHandshakeException) {
                registry = LocateRegistry.getRegistry(serverAddress, port);
                LOG.debug("Retrieving adapters...");
                setAdaptersFromRegistry(registry);
                LOG.info("Connected without SSL/TLS encryption");
            }
        }
        LOG.debug("Done");

    }

    private static void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException, NoRouteToHostException, ConnectIOException {
        CustomTablesAdapter CustomTablesManager;
        AnnotationManagerAdapter AnnotationManagerAdapter;
        CohortManagerAdapter CohortManager;
        GeneSetManagerAdapter GeneSetManager;
        LogManagerAdapter LogManager;
        NetworkManagerAdapter NetworkManager;
        OntologyManagerAdapter OntologyManager;
        PatientManagerAdapter PatientManager;
        ProjectManagerAdapter ProjectManager;
        UserManagerAdapter UserManager;
        SessionManagerAdapter SessionManager;
        SettingsManagerAdapter SettingsManager;
        RegionSetManagerAdapter RegionSetManager;
        ReferenceManagerAdapter ReferenceManager;
        DBUtilsAdapter DBUtils;
        SetupAdapter SetupManager;
        VariantManagerAdapter VariantManager;
        NotificationManagerAdapter NotificationManager;

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

        if (Thread.interrupted()) {
            return;
        }

        synchronized (managerLock) {
            MedSavantServlet.CustomTablesManager = CustomTablesManager;
            MedSavantServlet.AnnotationManagerAdapter = AnnotationManagerAdapter;
            MedSavantServlet.CohortManager = CohortManager;
            MedSavantServlet.GeneSetManager = GeneSetManager;
            MedSavantServlet.LogManager = LogManager;
            MedSavantServlet.NetworkManager = NetworkManager;
            MedSavantServlet.OntologyManager = OntologyManager;
            MedSavantServlet.PatientManager = PatientManager;
            MedSavantServlet.ProjectManager = ProjectManager;
            MedSavantServlet.UserManager = UserManager;
            MedSavantServlet.SessionManager = SessionManager;
            MedSavantServlet.SettingsManager = SettingsManager;
            MedSavantServlet.RegionSetManager = RegionSetManager;
            MedSavantServlet.ReferenceManager = ReferenceManager;
            MedSavantServlet.DBUtils = DBUtils;
            MedSavantServlet.SetupManager = SetupManager;
            MedSavantServlet.VariantManager = VariantManager;
            MedSavantServlet.NotificationManager = NotificationManager;
        }
    }

    private static void setExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.info("Global exception handler caught: " + t.getName() + ": " + e);

                        if (e instanceof InvocationTargetException) {
                            e = ((InvocationTargetException) e).getCause();
                        }

                        if (e instanceof SessionExpiredException) {
                            SessionExpiredException see = (SessionExpiredException) e;
                            LOG.error("Session expired exception: " + see.toString());
                            return;
                        }
                        e.printStackTrace();
                    }
                });
    }

    private static int copyStreamToServer(InputStream inputStream, String filename, long filesize) throws IOException, InterruptedException {

        int streamID = -1;

        try {
            streamID
                    = NetworkManager.openWriterOnServer(getSessionId(), filename, filesize);
            int numBytes;
            byte[] buf = new byte[UPLOAD_BUFFER_SIZE];

            while ((numBytes = inputStream.read(buf)) != -1) {
                //System.out.println("Read " + numBytes +" bytes");                
                NetworkManager.writeToServer(getSessionId(), streamID, ArrayUtils.subarray(buf, 0, numBytes));
            }
        } finally {
            if (streamID >= 0) {
                NetworkManager.closeWriterOnServer(getSessionId(), streamID);
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return streamID;
    }

    private static Semaphore uploadSem = new Semaphore(1, true);

    private static class Upload {

        String fieldName;
        int streamId;

        public Upload(String fieldName, int streamId) {
            this.fieldName = fieldName;
            this.streamId = streamId;
        }
    }

    private static Upload[] handleUploads(FileItemIterator iter) throws FileUploadException, IOException, InterruptedException {
        List<Upload> uploads = new ArrayList<Upload>();
        try {
            if (!uploadSem.tryAcquire()) {
                throw new FileUploadException("Can't upload file: other uploads are in progress");
            }
            //uploadSem.acquire();
            FileItemStream streamToUpload = null;
            long filesize = -1;

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                //System.out.println("Got file " + name);
                if (item.isFormField()) {
                    if (name.startsWith("size_")) {
                        filesize = Long.parseLong(Streams.asString(stream));

                    }
                } else if (name.startsWith("file_")) {
                    if (streamToUpload != null) {
                        throw new IllegalArgumentException("More than one file detected -- only one file can be uploaded at a time");
                    } else {
                        streamToUpload = item;
                    }
                } else {
                    throw new IllegalArgumentException("Unrecognized file detected with field name " + name);
                }
                if (streamToUpload == null) {
                    throw new IllegalArgumentException("Can't begin upload - no files were detetected");
                }
                if (filesize == -1) {
                    //System.out.println("No filesize given for file " + name);
                }

                //Do the upload
                int streamId = copyStreamToServer(streamToUpload.openStream(), streamToUpload.getName(), filesize);
                if (streamId >= 0) {
                    uploads.add(new Upload(name, streamId));
                }
            }

        } finally {
            uploadSem.release();
        }
        return uploads.toArray(new Upload[uploads.size()]);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //format: ..../adapter/method

        String uri = req.getRequestURI();
        String[] x = uri.split("/");
        int methodIndex;
        int adapterIndex;

        if (uri.endsWith("/")) {
            methodIndex = x.length - 2;
            adapterIndex = x.length - 3;
        } else {
            methodIndex = x.length - 1;
            adapterIndex = x.length - 2;
        }

        resp.setContentType("text/x-json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        try {
            if (adapterIndex >= 0 && x[adapterIndex].equals("UploadManager") && x[methodIndex].equals("upload")) {
                if (!ServletFileUpload.isMultipartContent(req)) {
                    throw new IllegalArgumentException(gson.toJson("File upload failed: content is not multipart", String.class));
                }
                FileItemIterator iter = (new ServletFileUpload()).getItemIterator(req);
                System.out.println("Handling upload");
                Upload[] uploads = handleUploads(iter); //note this BLOCKS until upload is finished.

                resp.getWriter().print(gson.toJson(uploads, uploads.getClass()));
                resp.getWriter().close();

            } else if (methodIndex < 0 || adapterIndex < 0) {
                throw new IllegalArgumentException(gson.toJson("Malformed URL", String.class));
            } else {

                // Print parameter map to stdout
            /*
                 for (Object o : req.getParameterMap().entrySet()) {
                 Map.Entry e = (Map.Entry) o;
                 System.out.println("Key="+e.getKey());
                 for(String a : (String[])e.getValue()){
                 System.out.println("\tVal="+a);
                 }                
                 }
                 */
                String json_args = req.getParameter(JSON_PARAM_NAME);
                if (json_args == null) {
                    json_args = "[]";
                }

                String ret = json_invoke(x[adapterIndex], x[methodIndex], json_args);

                resp.getWriter().print(ret);
                resp.getWriter().close();
            }
        } catch (FileUploadException fue) {
            LOG.error(fue);
        } catch (IllegalArgumentException iae) {
            LOG.error(iae);
        } catch (InterruptedException iex) { //file upload cancelled.
            LOG.error(iex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String[] x = uri.split("/");
        int adapterIndex = x.length - 1;

        resp.setContentType("text/x-json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache");
        if (x[adapterIndex].equalsIgnoreCase("testing")) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.setHeader("Cache-Control", "no-cache");
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("/testing.html");
            resp.getWriter().print(readText(input, "UTF-8"));
        } else {
            resp.getWriter().print("Invalid");
        }
        resp.getWriter().close();
    }

    public static String readText(InputStream is, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        for (int len; (len = is.read(bytes)) > 0;) {
            baos.write(bytes, 0, len);
        }
        return new String(baos.toByteArray(), charset);
    }
}
