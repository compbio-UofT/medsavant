package org.medsavant.server.medsavantexecutorservice;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.executionservice.MedSavantExecutionService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

/**
 * Exposes an ExecutorService for use by other bundles.
 *
 * @author jim
 */
public class Activator implements BundleActivator {
    private static final Log LOG = LogFactory.getLog(Activator.class);
    private volatile ExecutorService threadPool;
    private volatile ServiceRegistration svcReg;
    private MedSavantExecutionService mes;
   
    public void start(BundleContext bundleContext) throws Exception {        
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Dictionary props = new Hashtable<String, Object>();
        props.put("servicepid", MedSavantExecutionServiceImpl.class.getName());
        this.mes = new MedSavantExecutionServiceImpl();
        this.svcReg = bundleContext.registerService(ManagedService.class.getName(), mes, props);
        LOG.info("MedSavantExecutor service started!");
    }

    public void stop(BundleContext context) throws Exception {
        LOG.info("MedSavantExecutor service is shutting down.  Sending interrupt to running threads and waiting...");
        mes.shutdown();
    }
}
