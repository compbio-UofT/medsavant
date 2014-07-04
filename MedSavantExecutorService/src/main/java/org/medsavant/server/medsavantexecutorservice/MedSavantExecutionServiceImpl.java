/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.server.medsavantexecutorservice;

import java.util.Dictionary;
import org.medsavant.api.executionservice.MedSavantExecutionService;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.medsavant.api.common.InvalidConfigurationException;
import org.medsavant.api.common.MedSavantServerContext;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_LONGJOB;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_SHORTJOB;
import org.medsavant.api.common.impl.MedSavantServerJob;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 *
 * @author jim
 */
public class MedSavantExecutionServiceImpl implements MedSavantExecutionService, ManagedService {

    private static final Log LOG = LogFactory.getLog(MedSavantExecutionServiceImpl.class);
    private ExecutorService longThreadPool;
    private ExecutorService shortThreadPool;

    //Setup some defaults.  These can be overridden with the appropriate setters.
    private int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    private int blockingQueueSize = 100;
    private int maxThreadKeepAliveTime = 86400;//in seconds
    private int maxThreadTerminationTime = 3600;

    void initThreadPools() {
        //Long thread pool runs a maximum of maxThreads simultaneous threads, and queues a maximum of 
        //blockQueueSize threads before blocking.        
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(blockingQueueSize);
        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        longThreadPool = new ThreadPoolExecutor(maxThreads, maxThreads, maxThreadKeepAliveTime, TimeUnit.SECONDS, blockingQueue, rejectedExecutionHandler);
        shortThreadPool = Executors.newCachedThreadPool();
    }

    public String getComponentID() {
        return MedSavantExecutionService.class.getCanonicalName();
    }

    public String getComponentName() {
        return "MedSavantServer Thread Execution Service";
    }

    public void updated(Dictionary config) throws ConfigurationException {
        if (config == null) {
            //default config file goes into /etc folder of karaf with name org.medsavant.server.medsavantexecutorservice
            //get default config
        }
        System.out.println("Configuration change to execution service detected");
        LOG.info("Configuration change to execution service detected.");

        try {
            configure(config);
        } catch (InvalidConfigurationException ex) {
            String msg = "Invalid configuration detected for MedSavantExecutionService.  New configuration not applied!";
            LOG.error(msg, ex);
            throw new ConfigurationException(null, msg);
        }
    }

    public void configure(Dictionary dict) throws InvalidConfigurationException {
        if (dict != null) {
            while (dict.keys().hasMoreElements()) {
                Object key = dict.keys().nextElement();
                Object val = dict.get(key);
                if (key instanceof String) {
                    configure((String) key, val);
                }
            }
        }
    }

    private int getInt(Object o) throws IllegalArgumentException {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            String s = (String) o;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid Number format", nfe);
            }
        } else {
            throw new IllegalArgumentException("Illegal type");
        }
    }

    public void configure(String key, Object val) throws InvalidConfigurationException {
        try {
            if (key.equalsIgnoreCase("max-threads")) {
                this.maxThreads = getInt(val);
            } else if (key.equalsIgnoreCase("thread-keepalive-time")) {
                this.maxThreadKeepAliveTime = getInt(val);
            } else if (key.equalsIgnoreCase("queue-size")) {
                this.blockingQueueSize = getInt(val);
            } else if (key.equalsIgnoreCase("thread-termination-waittime")) {
                this.maxThreadTerminationTime = getInt(val);
            }
        } catch (IllegalArgumentException iae) {
            throw new InvalidConfigurationException("Unexpected value for configuration option " + key);
        }
    }

    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException {
        //No server context is necessary.
    }

    public void setMaxRunningLongJobs(int m) {
        this.maxThreads = m;
    }

    public void setMaxQueuedLongJobs(int m) {
        this.blockingQueueSize = m;
    }

    public void setMaxThreadKeepAliveTime(int seconds) {
        this.maxThreadKeepAliveTime = seconds;
    }

    @Override
    public int getMaxQueuedLongJobs() {
        return blockingQueueSize;
    }

    @Override
    public int getMaxRunningLongJobs() {
        return maxThreads;
    }

    @Override
    public Void runJobInCurrentThread(MedSavantServerJob msj) throws Exception {
        msj.setScheduleStatus(SCHEDULED_AS_SHORTJOB);
        return msj.call();
    }

    @Override
    public Future submitShortJob(MedSavantServerJob msj) {
        msj.setScheduleStatus(SCHEDULED_AS_SHORTJOB);
        return shortThreadPool.submit(msj);
    }

    @Override
    public Future submitLongJob(MedSavantServerJob msj) {
        msj.setScheduleStatus(SCHEDULED_AS_LONGJOB);
        return longThreadPool.submit(msj);
    }

    @Override
    public List<Future<Void>> submitShortJobs(List<MedSavantServerJob> msjs) throws InterruptedException {
        for (MedSavantServerJob j : msjs) {
            j.setScheduleStatus(SCHEDULED_AS_SHORTJOB);
        }
        return shortThreadPool.invokeAll(msjs);
    }

    @Override
    public List<Future<Void>> submitLongJobs(List<MedSavantServerJob> msjs) throws InterruptedException {
        for (MedSavantServerJob j : msjs) {
            j.setScheduleStatus(SCHEDULED_AS_LONGJOB);
        }
        return longThreadPool.invokeAll(msjs);
    }

    @Override
    public void shutdown() throws InterruptedException {
        try {
            shortThreadPool.shutdownNow();
            shortThreadPool.awaitTermination(maxThreads, TimeUnit.DAYS);
            longThreadPool.shutdownNow();
            longThreadPool.awaitTermination(maxThreads, TimeUnit.DAYS);
        } catch (InterruptedException ie) {
            LOG.error("Interrupted while waiting for threads to terminate.  Some threads may still be running!");
            throw ie;
        }
    }
}
