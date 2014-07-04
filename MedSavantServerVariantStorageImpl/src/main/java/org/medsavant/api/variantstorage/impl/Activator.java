package org.medsavant.api.variantstorage.impl;

import org.medsavant.api.variantstorage.VariantFilterBuilder;
import org.medsavant.api.variantstorage.VariantStorageEngine;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    private BundleContext bundleContext;
    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;
        
        //create instances.
        VariantStorageEngine vseInstance = new InfobrightVariantStorageEngine();
        //VariantFilterBuilder vfbInstance = new InfobrightVariantFilterBuilder();
        
        bundleContext.registerService(VariantStorageEngine.class.getName(), vseInstance, null);
        //bundleContext.registerService(VariantFilterBuilder.class.getName(),vfbInstance, null);
        //context.registerService(VariantField.class.getName(), variantFieldInstance, null);
        
        //
        
        // TODO add activation code here
        /*
         this.bundleContext = context;
        VCFPreProcessor vpp = new JannovarVCFPreProcessor();
        vcfPreProcessorServiceRegistration = context.registerService(VCFPreProcessor.class.getName(), vpp, null);                   
        
        // m_context.getServiceReferences(
               //DictionaryService.class.getName(), "(Language=*)");
        */
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

}
