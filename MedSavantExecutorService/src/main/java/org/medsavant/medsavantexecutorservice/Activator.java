package org.medsavant.medsavantexecutorservice;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Exposes an ExecutorService for use by other bundles.   
 * 
 * @author jim
 */
public class Activator implements BundleActivator {        
    private volatile ExecutorService threadPool;
    private volatile ServiceRegistration svcReg;
    
    /*
     this.bundleContext = context;

        @SuppressWarnings("UseOfObsoleteCollectionType")
        Dictionary props = new Hashtable<String, Object>();
        
        props.put("service.pid", MedSavantServerEngineConfigurator.class.getName());        

        this.serviceRegistration
                = bundleContext.registerService(ManagedService.class.getName(), new MedSavantServerEngineConfigurator(), props);

        //MedSavantServerEngine.initialize(prop);
        System.out.println("Bundle started!");
    */
    public void start(BundleContext context) throws Exception {
       
       
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

}
