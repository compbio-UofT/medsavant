package org.medsavant.annotation.impl;

import org.medsavant.api.annotation.VCFPreProcessor;
import org.medsavant.api.common.GlobalWrapper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    
    private BundleContext bundleContext;
    private ServiceRegistration vcfPreProcessorServiceRegistration;
    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;
        VCFPreProcessor vpp = new JannovarVCFPreProcessor(GlobalWrapper.getServerContext());        
        vcfPreProcessorServiceRegistration = context.registerService(VCFPreProcessor.class.getName(), vpp, null);    
        
        /**
         * Needs access to the file directory service.
         */
        
        // m_context.getServiceReferences(
               //DictionaryService.class.getName(), "(Language=*)");
        
        

    }

    public void stop(BundleContext context) throws Exception {
        vcfPreProcessorServiceRegistration.unregister();
    }

}
