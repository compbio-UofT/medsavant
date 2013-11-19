/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.ut.biolab.medsavant.shared.util.ModificationType;

public class CacheController extends Controller<ModificationType> {

    private static CacheController instance;
    private final Object timerLock;
    //Minimum amount of time to wait between firing update events, in ms.
    private static long minimumWaitInterval = 500l;
    //Amount of time to wait between firing server updates.
    private static long periodic_update_interval = 900000l; //15mins    
    //Map for each modification type to a scheduled time for an update.
    private Map<ModificationType, Long> timers;
    private Timer timer;
    private EventFiringTask eventFiringTask;

    private CacheController() {
        timers = new EnumMap<ModificationType, Long>(ModificationType.class);
        for (ModificationType mt : ModificationType.values()) {
            timers.put(mt, System.currentTimeMillis() + periodic_update_interval);
        }

        timerLock = new Object();
        timer = new Timer();
        eventFiringTask = new EventFiringTask();
        timer.scheduleAtFixedRate(eventFiringTask, minimumWaitInterval, minimumWaitInterval);
    }

    public static void setMinimumWaitInterval(long ms) {
        minimumWaitInterval = ms;
    }

    public static long getMinimumWaitInterval() {
        return minimumWaitInterval;
    }

    public static CacheController getInstance() {
        if (instance == null) {
            instance = new CacheController();
        }
        return instance;
    }
    private Set<Long> threadIds = new HashSet<Long>();

    /**
     * Executes the given runnable in a background thread, disabling the cache until
     * the thread is complete.  At that time, cache expiration events are fired corresponding
     * to the given types.          
     */
    public void blockUpdate(final Runnable r, final ModificationType[] types) {
        Thread t = new Thread() {
            @Override
            public void run() {
                threadIds.add(getId());
                r.run();
                threadIds.remove(getId());
                for(ModificationType t : types){
                    expire(t);
                }
            }
        };
        t.start();
    }

    public void expire(ModificationType t, Long threadId){
        if(threadId ==null || !threadIds.contains(threadId)){
            timers.put(t, System.currentTimeMillis() + minimumWaitInterval);
        }
    }

    //Send a notification to all listeners that a modifiation has occurred, after
    //waiting for minimumWaitInterval ms.  Calling this method multiple times with
    //the same modifier type will reset the timer to wait another minimumWaitInterval 
    //ms before firing the event. 
    public void expire(ModificationType t) {
        synchronized (timerLock) {
            expire(t, null);            
        }
    }

    private class EventFiringTask extends TimerTask {

        private Map<ModificationType, MedSavantWorker<Void>> threadMap = new EnumMap<ModificationType, MedSavantWorker<Void>>(ModificationType.class);

        @Override
        public void run() {
            synchronized (timerLock) {
                for (Map.Entry<ModificationType, Long> e : timers.entrySet()) {
                    final ModificationType type = e.getKey();
                    final Long scheduledTime = e.getValue();
                    if (scheduledTime == null) {
                        continue;
                    }
                    final Long currentTime = System.currentTimeMillis();
                    if (currentTime >= scheduledTime) {
                        //Time to fire.                        
                        MedSavantWorker<Void> worker = threadMap.get(type);
                        if (worker != null && !worker.isDone() && !worker.isCancelled()) {
                            //Worker is already executing, do not fire events until previous 
                            //thread is done (after which there is a delay of minimumWaitInterval ms).
                            timers.put(type, currentTime - 1);
                        } else {
                            //event firing happens in a background thread so that other Apps, etc. don't lock up 
                            //the timer thread.
                            worker = new MedSavantWorker<Void>("CACHE " + type) {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    fireEvent(type);
                                    return null;
                                }

                                @Override
                                protected void showSuccess(Void result) {
                                    
                                }
                            };
                            threadMap.put(type, worker);
                            worker.execute();
                        }
                        timers.put(type, currentTime + periodic_update_interval);
                    }
                }
            }
        }
    }
}
