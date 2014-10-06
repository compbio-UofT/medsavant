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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.NoRouteToHostException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLHandshakeException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.exception.LockException;
import org.ut.biolab.medsavant.shared.serverapi.AnnotationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.CohortManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.CustomTablesAdapter;
import org.ut.biolab.medsavant.shared.serverapi.DBUtilsAdapter;
import org.ut.biolab.medsavant.shared.serverapi.GeneSetManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.MedSavantServerRegistry;
import org.ut.biolab.medsavant.shared.serverapi.NetworkManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.NotificationManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.OntologyManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.PatientManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.ReferenceManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.RegionSetManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.SessionManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.SetupAdapter;
import org.ut.biolab.medsavant.shared.serverapi.UserManagerAdapter;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.healthmarketscience.sqlbuilder.Condition;
import java.io.File;
import java.io.FileInputStream;

public class MedSavantServlet extends HttpServlet implements MedSavantServerRegistry {

    private static final int UPLOAD_BUFFER_SIZE = 4096;

    private static final long serialVersionUID = -77006512859078222L;

    private static final String JSON_PARAM_NAME = "json";

    private static final Log LOG = LogFactory.getLog(MedSavantServlet.class);

    private CohortManagerAdapter cohortManager;

    private PatientManagerAdapter patientManager;

    private CustomTablesAdapter customTablesManager;

    private AnnotationManagerAdapter annotationManager;

    private GeneSetManagerAdapter geneSetManager;

    @SuppressWarnings("unused")
    private LogManagerAdapter logManager;

    private NetworkManagerAdapter networkManager;

    private OntologyManagerAdapter ontologyManager;

    private ProjectManagerAdapter projectManager;

    private UserManagerAdapter userManager;

    private SessionManagerAdapter sessionManager;

    private SettingsManagerAdapter settingsManager;

    private RegionSetManagerAdapter regionSetManager;

    private ReferenceManagerAdapter referenceManager;

    private DBUtilsAdapter dbUtils;

    private SetupAdapter setupManager;

    private VariantManagerAdapter variantManager;

    private NotificationManagerAdapter notificationManager;

    @SuppressWarnings("unused")
    private JSONUtilitiesAdapter jsonUtilities;

    private static boolean initialized = false;

    private Gson gson;

    private String medSavantServerHost;

    private int medSavantServerPort;

    private String username;

    private String password;

    private String db;

    private int maxSimultaneousUploads;

    private Semaphore uploadSem;

    private Session session;

    public CohortManagerAdapter getCohortManager() {
        return cohortManager;
    }

    public PatientManagerAdapter getPatientManager() {
        return patientManager;
    }

    public CustomTablesAdapter getCustomTablesManager() {
        return customTablesManager;
    }

    public AnnotationManagerAdapter getAnnotationManager() {
        return annotationManager;
    }

    public GeneSetManagerAdapter getGeneSetManager() {
        return geneSetManager;
    }

    public NetworkManagerAdapter getNetworkManager() {
        return networkManager;
    }

    public OntologyManagerAdapter getOntologyManager() {
        return ontologyManager;
    }

    public ProjectManagerAdapter getProjectManager() {
        return projectManager;
    }

    public UserManagerAdapter getUserManager() {
        return userManager;
    }

    public SessionManagerAdapter getSessionManager() {
        return sessionManager;
    }

    public SettingsManagerAdapter getSettingsManager() {
        return settingsManager;
    }

    public RegionSetManagerAdapter getRegionSetManager() {
        return regionSetManager;
    }

    public ReferenceManagerAdapter getReferenceManager() {
        return referenceManager;
    }

    public DBUtilsAdapter getDbUtils() {
        return dbUtils;
    }

    public SetupAdapter getSetupManager() {
        return setupManager;
    }

    public VariantManagerAdapter getVariantManager() {
        return variantManager;
    }

    public NotificationManagerAdapter getNotificationManager() {
        return notificationManager;
    }

    public String json_invoke(String adapter, String method, String jsonStr) throws IllegalArgumentException {

        adapter = adapter + "Adapter";

        Field selectedAdapter = null;
        for (Field f : MedSavantServlet.class.getDeclaredFields()) {
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
            throw new IllegalArgumentException("The method " + method + " in adapter " + adapter + " with "
                    + jsonArray.size() + " arguments does not exist");
        }

        int i = 0;
        Object[] methodArgs = new Object[selectedMethod.getParameterTypes().length];

        try {
            for (Type t : selectedMethod.getGenericParameterTypes()) {
                LOG.debug("Field " + i + " is " + t.toString() + " for method " + selectedMethod.toString());
                methodArgs[i] = (i > 0) ? gson.fromJson(gArray.get(i - 1), t) : session.getSessionId();
                ++i;
            }
        } catch (JsonParseException je) {
            LOG.error(je);
        }

        Object selectedAdapterInstance = null;
        try {
            selectedAdapterInstance = selectedAdapter.get(this);
            if (selectedAdapterInstance == null) {
                throw new NullPointerException("Requested adapter " + selectedAdapter.getName()
                        + " was not initialized.");
            }
            MethodInvocation methodInvocation
                    = new MethodInvocation(session, gson, selectedAdapterInstance, selectedMethod, methodArgs);

            return methodInvocation.invoke();
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Couldn't execute method with given arguments: " + iae.getMessage());
        } catch (LockException lex) {
            //this shouldn't happen, as locking exceptions can only be thrown by queued method invocations, which
            //are intercepted in the BlockingQueueManager.
            String msg = "Unexpected locking exception thrown in unqueued method invocation.";
            LOG.error(msg);
            throw new IllegalArgumentException(msg + ": " + lex.getMessage());
        }

    }

    @Override
    public void init() throws ServletException {
        LOG.info("MedSavant JSON Client/Server booted.");
        try {
            loadConfiguration();
            initializeRegistry(this.medSavantServerHost, Integer.toString(this.medSavantServerPort));
            session = new Session(sessionManager, username, password, db);
            GsonBuilder gsonBuilder = new GsonBuilder();

            // Handle the condition type.
            gsonBuilder.registerTypeAdapter(Condition.class, new JsonDeserializer<Condition>() {
                @Override
                public Condition deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                        throws JsonParseException {
                    return gson.fromJson(je, SimplifiedCondition.class).getCondition(session.getSessionId(), variantManager);
                }
            });
            gson = gsonBuilder.create();

        } catch (Exception ex) {            
            LOG.error(ex);
            throw new ServletException("Failed to initialize the medsavant JSON client: " + ex.getMessage());
        }
    }

    public void initializeRegistry(String serverAddress, String serverPort) throws RemoteException, NotBoundException,
            NoRouteToHostException, ConnectIOException {

        if (initialized) {
            return;
        }

        int port = (new Integer(serverPort)).intValue();

        Registry registry;

        LOG.info("Connecting to MedSavantServerEngine @ " + serverAddress + ":" + serverPort + "...");

        try {
            registry = LocateRegistry.getRegistry(serverAddress, port, new SslRMIClientSocketFactory());
            LOG.debug("Retrieving adapters...");
            setAdaptersFromRegistry(registry);
            setLocalAdapters();
            LOG.info("Connected with SSL/TLS Encryption");
            initialized = true;
        } catch (ConnectIOException ex) {
            if (ex.getCause() instanceof SSLHandshakeException) {
                registry = LocateRegistry.getRegistry(serverAddress, port);
                LOG.info("Retrieving adapters...");
                setAdaptersFromRegistry(registry);
                LOG.info("Connected without SSL/TLS encryption");
            }
        }
        LOG.info("Done");

    }

    private void setLocalAdapters() {
        this.jsonUtilities = new JSONUtilities(this.variantManager, this.annotationManager);
    }

    private void setAdaptersFromRegistry(Registry registry) throws RemoteException, NotBoundException,
            NoRouteToHostException, ConnectIOException {
        this.annotationManager = (AnnotationManagerAdapter) registry.lookup(ANNOTATION_MANAGER);
        this.cohortManager = (CohortManagerAdapter) (registry.lookup(COHORT_MANAGER));
        this.logManager = (LogManagerAdapter) registry.lookup(LOG_MANAGER);
        this.networkManager = (NetworkManagerAdapter) registry.lookup(NETWORK_MANAGER);
        this.ontologyManager = (OntologyManagerAdapter) registry.lookup(ONTOLOGY_MANAGER);
        this.patientManager = (PatientManagerAdapter) registry.lookup(PATIENT_MANAGER);
        this.projectManager = (ProjectManagerAdapter) registry.lookup(PROJECT_MANAGER);
        this.geneSetManager = (GeneSetManagerAdapter) registry.lookup(GENE_SET_MANAGER);
        this.referenceManager = (ReferenceManagerAdapter) registry.lookup(REFERENCE_MANAGER);
        this.regionSetManager = (RegionSetManagerAdapter) registry.lookup(REGION_SET_MANAGER);
        this.sessionManager = (SessionManagerAdapter) registry.lookup(SESSION_MANAGER);
        this.settingsManager = (SettingsManagerAdapter) registry.lookup(SETTINGS_MANAGER);
        this.userManager = (UserManagerAdapter) registry.lookup(USER_MANAGER);
        this.variantManager = (VariantManagerAdapter) registry.lookup(VARIANT_MANAGER);
        this.dbUtils = (DBUtilsAdapter) registry.lookup(DB_UTIL_MANAGER);
        this.setupManager = (SetupAdapter) registry.lookup(SETUP_MANAGER);
        this.customTablesManager = (CustomTablesAdapter) registry.lookup(CUSTOM_TABLES_MANAGER);
        this.notificationManager = (NotificationManagerAdapter) registry.lookup(NOTIFICATION_MANAGER);
        this.jsonUtilities = new JSONUtilities(variantManager, annotationManager);
    }

    private void setExceptionHandler() {
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

    private int copyStreamToServer(InputStream inputStream, String filename, long filesize) throws IOException,
            InterruptedException {

        int streamID = -1;

        try {
            uploadSem.acquire();
            streamID = this.networkManager.openWriterOnServer(session.getSessionId(), filename, filesize);
            int numBytes;
            byte[] buf = new byte[UPLOAD_BUFFER_SIZE];

            while ((numBytes = inputStream.read(buf)) != -1) {
                // System.out.println("Read " + numBytes +" bytes");
                this.networkManager.writeToServer(session.getSessionId(), streamID, ArrayUtils.subarray(buf, 0, numBytes));
            }
        } finally {
            if (streamID >= 0) {
                this.networkManager.closeWriterOnServer(session.getSessionId(), streamID);
            }
            uploadSem.release();
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return streamID;
    }

    private static class Upload {

        String fieldName;
        int streamId;

        public Upload(String fieldName, int streamId) {
            this.fieldName = fieldName;
            this.streamId = streamId;
        }
    }

    private Upload[] handleUploads(FileItemIterator iter) throws FileUploadException, IOException,
            InterruptedException {
        List<Upload> uploads = new ArrayList<Upload>();

        FileItemStream streamToUpload = null;
        long filesize = -1;

        String sn = null;
        String fn = null;

        while (iter.hasNext()) {

            FileItemStream item = iter.next();
            String name = item.getFieldName();
            InputStream stream = item.openStream();
            // System.out.println("Got file " + name);
            if (item.isFormField()) {
                if (name.startsWith("size_")) {
                    sn = name.split("_")[1];
                    filesize = Long.parseLong(Streams.asString(stream));
                }
            } else if (name.startsWith("file_")) {
                streamToUpload = item;
            } else {
                throw new IllegalArgumentException("Unrecognized file detected with field name " + name);
            }
            if (streamToUpload != null) {
                // Do the upload               
                int streamId = copyStreamToServer(
                        streamToUpload.openStream(),
                        streamToUpload.getName(),
                        (sn != null && fn != null && sn.equals(fn)) ? filesize : -1);
                if (streamId >= 0) {
                    uploads.add(new Upload(name, streamId));
                }
            }

        }

        return uploads.toArray(new Upload[uploads.size()]);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // format: ..../adapter/method

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
            if (adapterIndex >= 0 && x[adapterIndex].equals("UploadManager") && x[methodIndex].startsWith("upload")) {
                if (!ServletFileUpload.isMultipartContent(req)) {
                    throw new IllegalArgumentException(gson.toJson("File upload failed: content is not multipart",
                            String.class));
                }

                FileItemIterator iter = (new ServletFileUpload()).getItemIterator(req);                
                Upload[] uploads = handleUploads(iter); // note this BLOCKS until upload is finished.
                resp.getWriter().print(gson.toJson(uploads, uploads.getClass()));
                resp.getWriter().close();
            } else if (methodIndex < 0 || adapterIndex < 0) {
                throw new IllegalArgumentException(gson.toJson("Malformed URL", String.class));
            } else {

                // Print parameter map to stdout
                /*
                 * for (Object o : req.getParameterMap().entrySet()) { Map.Entry e = (Map.Entry) o;
                 * System.out.println("Key="+e.getKey()); for(String a : (String[])e.getValue()){
                 * System.out.println("\tVal="+a); } }
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
        } catch (InterruptedException iex) { // file upload cancelled.
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
            IOUtils.copy(input, resp.getOutputStream());
            resp.getOutputStream().close();
        } else {
            resp.getWriter().print("Invalid");
            resp.getWriter().close();
        }
    }

    private InputStream getConfigInputStream() throws ServletException, IOException{
     String[] configFileLocations = getServletContext().getInitParameter("MedSavantConfigFile").split(":");
            
            File f = null;
            for(String cf : configFileLocations){
                
                if(!cf.startsWith("/")){
                    LOG.info("Looking for configuration from path relative to servlet context "+cf);                    
                    InputStream in =  getServletContext().getResourceAsStream("/"+cf);                    
                    if(in != null){
                        LOG.info("Reading configuration from /"+cf);
                        return in;
                    }
                }else{
                    f = new File(cf);
                    LOG.info("Looking for config file at: "+f.getAbsolutePath());
                    if(f.exists()){
                        break;
                    }else{
                        f = null;
                    }
                }
            }
            if(f == null){
                throw new ServletException("Can't load configuration - no config file found!");
            }
            
            LOG.info("Reading configuration from "+f.getAbsolutePath());                        
                        
            return new FileInputStream(f);                
    }
    
    private void loadConfiguration() throws ServletException {
        String host = null;
        String uname = null;
        String pass = null;
        String dbase = null;
        String maxSimultaneousUploadsStr = null;
        int p = -1;
        try {                                          
            Properties props = new Properties();            
            InputStream in = getConfigInputStream();
            props.load(in);
            in.close();

            host = props.getProperty("host", "");
            uname = props.getProperty("username", "");
            pass = props.getProperty("password", "");
            dbase = props.getProperty("db", "");
            maxSimultaneousUploadsStr = props.getProperty("max_simultaneous_uploads", "").trim();
            String portStr = props.getProperty("port", "-1").trim();
            if (StringUtils.isBlank(portStr) || !NumberUtils.isNumber(portStr)) {
                LOG.error("No port specified in configuration, cannot continue");
            }
            if (maxSimultaneousUploadsStr == null) {
                throw new ServletException("No maximum number of simultaneous uploads specified.  Cannot continue");
            }
            p = Integer.parseInt(portStr);
            if (p <= 0) {
                throw new ServletException("Illegal port specified in configuration: " + portStr + ", cannot continue.");
            }

            maxSimultaneousUploads = Integer.parseInt(maxSimultaneousUploadsStr);
            if (maxSimultaneousUploads <= 0) {
                throw new ServletException("Illegal number of maximum simultaneous uploads in configuration: " + maxSimultaneousUploadsStr + ", cannot continue.");
            }

            uploadSem = new Semaphore(maxSimultaneousUploads, true);
            if(StringUtils.isBlank(uname)){
                throw new ServletException("No username specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(pass)) {
                throw new ServletException("No password specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(dbase)) {
                throw new ServletException("No database specified in configuration file, cannot continue.");
            }
            if (StringUtils.isBlank(host)) {
                throw new ServletException("No host specified in configuration file, cannot continue.");
            }
        } catch (IOException iex) {
            throw new ServletException("IO Exception reading config file, cannot continue: " + iex.getMessage());
        }
        this.medSavantServerHost = host;
        this.medSavantServerPort = p;
        this.username = uname;
        this.password = pass;
        this.db = dbase;

        LOG.info("Configured with:");
        LOG.info("Host = " + host);
        LOG.info("Port = " + p);
        LOG.info("Username = " + uname);
        LOG.info("Database = " + this.db);
    }   
}
