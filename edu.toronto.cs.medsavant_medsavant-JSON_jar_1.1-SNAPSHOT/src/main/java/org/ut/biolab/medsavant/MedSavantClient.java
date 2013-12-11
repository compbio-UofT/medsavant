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
import gnu.getopt.Getopt;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NoRouteToHostException;
import javax.net.ssl.SSLHandshakeException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.RegionSetManagerAdapter;

public class MedSavantClient implements MedSavantServerRegistry {

    //A bean for primitive types.
    public static class PrimitiveBean<T>{
        private T value;
        public PrimitiveBean(T value){
            this.value = value;
        }
        public T getValue(){
            return value;
        }
        public void setValue(T value){
            this.value = value;
        }
    }
    
    private static final Log LOG = LogFactory.getLog(MedSavantClient.class);
    public static CustomTablesAdapter CustomTablesManager;
    public static AnnotationManagerAdapter AnnotationManagerAdapter;
    public static CohortManagerAdapter CohortManager;
    public static GeneSetManagerAdapter GeneSetManager;
    public static LogManagerAdapter LogManager;
    public static NetworkManagerAdapter NetworkManager;
    public static OntologyManagerAdapter OntologyManager;
    public static PatientManagerAdapter PatientManager;
    public static ProjectManagerAdapter ProjectManager;
    public static UserManagerAdapter UserManager;
    public static SessionManagerAdapter SessionManager;
    public static SettingsManagerAdapter SettingsManager;
    public static RegionSetManagerAdapter RegionSetManager;
    public static ReferenceManagerAdapter ReferenceManager;
    public static DBUtilsAdapter DBUtils;
    public static SetupAdapter SetupManager;
    public static VariantManagerAdapter VariantManager; //proxy
    public static NotificationManagerAdapter NotificationManager;
    private static final Object managerLock = new Object();
    private static boolean initialized = false;

    private static String medSavantServerHost = "localhost";
    private static int medSavantServerPort = 36850;
    
    //Debug variable, for convenience.
    private static Object lastReturnVal;
    
    private static void test(){      
        //String registerNewSession(String user, String pass, String db)
        String js = json_invoke("SessionManager", "registerNewSession", "[\"root\", \"savant12\", \"indeltest\"]");        
        String sessionId = (String)lastReturnVal;
        System.out.println("JS: "+js+"\n");
        
        js = json_invoke("ProjectManager", "getProjectNames", "[\""+sessionId+"\"]");
        System.out.println("JS: "+js+"\n");
        String firstProject = ((String[])lastReturnVal)[0];
        
        js = json_invoke("ProjectManager", "getProjectID", "[\""+sessionId+"\", \""+firstProject+"\"]");
        System.out.println("JS: "+js+"\n");
        
        int projId = ((Integer)lastReturnVal);
        
        js = json_invoke("CohortManager", "getCohorts", "[\""+sessionId+"\", \""+projId+"\"]");
        System.out.println("JS: "+js+"\n");

        
        int cohortId = 1;
        js = json_invoke("CohortManager", "getIndividualsInCohort", "[\""+sessionId+"\", \""+projId+"\", \""+cohortId+"\"]");
        System.out.println("JS: "+js+"\n");     
    }
    
    /*
    medsavant.com/VariantManager/getVariants
            [{} {} {}]*/
    public static String json_invoke(String adapter, String method, String jsonStr) throws IllegalArgumentException{
        JSON json = JSONSerializer.toJSON( jsonStr );
        JSONArray jsonArray;
        if(json.isArray()){            
            jsonArray = (JSONArray)json;// JSONSerializer.toJSON( json );
        }else{
            throw new IllegalArgumentException("The json method arguments are not an array");
        }
        
        adapter = adapter+"Adapter";
        
        Field selectedAdapter = null;
        for(Field f : MedSavantClient.class.getFields()){
            if(f.getType().getSimpleName().equalsIgnoreCase(adapter)){
                selectedAdapter = f;                
            }else{
              //  System.out.println(f.getType().getSimpleName()+" != "+adapter);
            }
        }
        if(selectedAdapter == null){
            throw new IllegalArgumentException("The adapter "+adapter+" does not exist");
        }
        
        
        System.out.println("Selected adapter has class "+selectedAdapter.getType().getName());        
        Method selectedMethod = null;
        
        
        for(Method m : selectedAdapter.getType().getMethods()){
            if(m.getName().equalsIgnoreCase(method)){
                selectedMethod = m;
            }else{
                //System.out.println(method+" != "+m.getName());
            }
        }
        if(selectedMethod == null){
            throw new IllegalArgumentException("The method "+method+" in adapter "+adapter+" does not exist");
        }                              
        
        int i = 0;
        
        Object[] methodArgs = new Object[selectedMethod.getParameterTypes().length];
        for(Class t : selectedMethod.getParameterTypes()){   
            System.out.println("Parameter "+i+" should be of type "+t.getName());         
            
            if(String.class == t){
                methodArgs[i] = jsonArray.getString(i);
            }else if(Integer.class == t || int.class == t){
                methodArgs[i] = jsonArray.getInt(i);
            }else if(Double.class == t || double.class == t){
                methodArgs[i] = jsonArray.getDouble(i);
            }else if(Float.class == t || float.class == t){
                methodArgs[i] = (float)jsonArray.getDouble(i);
            }else if(Boolean.class == t || boolean.class == t){
                methodArgs[i] = jsonArray.getBoolean(i);                
            }else if(Long.class == t || long.class == t){
                methodArgs[i] = jsonArray.getLong(i);
            }else{                                    
                JSONObject jo = jsonArray.getJSONObject(i);                    
                methodArgs[i] = JSONObject.toBean(jo, t.getClass());
            }
            ++i;
            //BeanA bean = (BeanA) JSONObject.toBean( jsonObject, BeanA.class );  
        }
        try{
            Object selectedAdapterInstance = selectedAdapter.get(null);
            if(selectedAdapterInstance == null){
                throw new NullPointerException("Requested adapter "+selectedAdapter.getName()+" was not initialized.");
            }
            Object returnVal = selectedMethod.invoke(selectedAdapterInstance, methodArgs);                         
            lastReturnVal = returnVal;
            if(returnVal == null){
                System.out.println("No return val!");
                return null;
            }else{
                //Accepts JSON formatted strings, Maps, arrays, Collections, DynaBeans and JavaBeans. 
                if(returnVal instanceof Integer){
                    return JSONSerializer.toJSON(new PrimitiveBean<Integer>((Integer)returnVal)).toString();
                }else if(returnVal instanceof Long){
                    return JSONSerializer.toJSON(new PrimitiveBean<Long>((Long)returnVal)).toString();
                }else if(returnVal instanceof String){
                    return JSONSerializer.toJSON(new PrimitiveBean<String>((String)returnVal)).toString();
                }else if(returnVal instanceof Double || returnVal instanceof Float){
                    return JSONSerializer.toJSON(new PrimitiveBean<Double>((Double)returnVal)).toString();
                }else if(returnVal instanceof Boolean){
                    return JSONSerializer.toJSON(new PrimitiveBean<Boolean>((Boolean)returnVal)).toString();
                }else{
                    return JSONSerializer.toJSON(returnVal).toString();
                }
            }
            //return JSONObject.fromObject(returnVal).toString();
        }catch(IllegalAccessException iae){
            throw new IllegalArgumentException("Couldn't execute method with given arguments: "+iae.getMessage());
        }catch(InvocationTargetException ite){
            throw new IllegalArgumentException("Couldn't execute method with given arguments: "+ite.getMessage());
        }
    }
    
    //Proxy the adapters to process annotations and fire events to the cache controller.
    private static void initProxies() {
        /*
        VariantManager = (VariantManagerAdapter) Proxy.newProxyInstance(
                VariantManager.getClass().getClassLoader(),
                new Class[]{VariantManagerAdapter.class},
                new ServerModificationInvocationHandler<VariantManagerAdapter>(VariantManager));

        CohortManager = (CohortManagerAdapter) Proxy.newProxyInstance(
                CohortManager.getClass().getClassLoader(),
                new Class[]{CohortManagerAdapter.class},
                new ServerModificationInvocationHandler<CohortManagerAdapter>(CohortManager));

        PatientManager = (PatientManagerAdapter) Proxy.newProxyInstance(
                PatientManager.getClass().getClassLoader(),
                new Class[]{PatientManagerAdapter.class},
                new ServerModificationInvocationHandler<PatientManagerAdapter>(PatientManager));

        RegionSetManager = (RegionSetManagerAdapter) Proxy.newProxyInstance(
                RegionSetManager.getClass().getClassLoader(),
                new Class[]{RegionSetManagerAdapter.class},
                new ServerModificationInvocationHandler<RegionSetManagerAdapter>(RegionSetManager));

        OntologyManager = (OntologyManagerAdapter) Proxy.newProxyInstance(
                OntologyManager.getClass().getClassLoader(),
                new Class[]{OntologyManagerAdapter.class},
                new ServerModificationInvocationHandler<OntologyManagerAdapter>(OntologyManager));
                */
    }
    

    static public void main(String args[]) {
        // Avoids "Comparison method violates its general contract" bug.
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7075600
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        setExceptionHandler();

        Getopt g = new Getopt("MedSavant", args, "h:p:");
        int c;

        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    medSavantServerHost = g.getOptarg();
                    break;
                case 'p':
                    medSavantServerPort = Integer.parseInt(g.getOptarg());
                case '?':
                    break;
                default:
                    System.out.print("getopt() returned " + c + "\n");
            }
        }

        LOG.info("MedSavant JSON Client/Server booted.");
        try{
            initializeRegistry(medSavantServerHost, Integer.toString(medSavantServerPort));
            test();
        }catch(Exception ex){
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
            MedSavantClient.CustomTablesManager = CustomTablesManager;
            MedSavantClient.AnnotationManagerAdapter = AnnotationManagerAdapter;
            MedSavantClient.CohortManager = CohortManager;
            MedSavantClient.GeneSetManager = GeneSetManager;
            MedSavantClient.LogManager = LogManager;
            MedSavantClient.NetworkManager = NetworkManager;
            MedSavantClient.OntologyManager = OntologyManager;
            MedSavantClient.PatientManager = PatientManager;
            MedSavantClient.ProjectManager = ProjectManager;
            MedSavantClient.UserManager = UserManager;
            MedSavantClient.SessionManager = SessionManager;
            MedSavantClient.SettingsManager = SettingsManager;
            MedSavantClient.RegionSetManager = RegionSetManager;
            MedSavantClient.ReferenceManager = ReferenceManager;
            MedSavantClient.DBUtils = DBUtils;
            MedSavantClient.SetupManager = SetupManager;
            MedSavantClient.VariantManager = VariantManager;
            MedSavantClient.NotificationManager = NotificationManager;

            initProxies();
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
}
