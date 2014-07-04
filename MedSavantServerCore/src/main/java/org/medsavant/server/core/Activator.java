package org.medsavant.server.core;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

public class Activator implements BundleActivator {

    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;

        @SuppressWarnings("UseOfObsoleteCollectionType")
        Dictionary props = new Hashtable<String, Object>();
        
        props.put("service.pid", MedSavantServerEngineConfigurator.class.getName());        

        this.serviceRegistration
                = bundleContext.registerService(ManagedService.class.getName(), new MedSavantServerEngineConfigurator(), props);

        //MedSavantServerEngine.initialize(prop);
        System.out.println("Bundle started!");
    }

    public void stop(BundleContext context) throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }        
    }

}
