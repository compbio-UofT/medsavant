/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.server.core;

import java.rmi.RemoteException;
import java.util.Dictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

/**
 *
 * @author jim
 */
public class MedSavantServerEngineConfigurator implements ManagedService {

    private Log LOG = LogFactory.getLog(MedSavantServerEngineConfigurator.class.getName());

    @Override
    public void updated(Dictionary config) throws ConfigurationException {
        if(config == null){
            //default config file goes into /etc folder of karaf with name org.medsavant.server.core.MedSavantServerEngineConfigurator.cfg 
            //get default config
        }
        System.out.println("Server configuration change detected, restarting server.");
        LOG.info("Server configuration change detected, restarting server.");
        Object o = config.get("friendlyRestart");

        if (MedSavantServerEngine.isStarted()) {
            if (o != null && (o instanceof Boolean)) {
                MedSavantServerEngine.shutdown((Boolean) o);
            } else {
                MedSavantServerEngine.shutdown(true);
            }
        }
        try {
            MedSavantServerEngine.restart(config);
        } catch (RemoteException rex) {
            LOG.error("Couldn't start MedSavantServerEngine!");
        }

    }
}
