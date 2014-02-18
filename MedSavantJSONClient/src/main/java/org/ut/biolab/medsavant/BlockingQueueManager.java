package org.ut.biolab.medsavant;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

/**
 * Maintains a thread to dequeue items from the method invocation queue and 
 * invoke them.  If the queue is empty, the thread will block.  If the queue has
 * an item but the MedSavant server is locked, the item will be dequeued and 
 * the thread will sleep for POLL_INTERVAL milliseconds before trying again. 
 * It will keep trying indefinitely.
 * 
 * If the queue processing thread is not already alive, it is started when
 * an item is enqueued.
 *  
 */
public class BlockingQueueManager {
   
    private static final BlockingQueue<MethodInvocation> queue = new LinkedBlockingQueue<MethodInvocation>();
    private static Thread queueProcessor = null;    
    private static final long POLL_INTERVAL = 30000;
    private static final Log LOG = LogFactory.getLog(BlockingQueueManager.class);

    private static class QueueProcessor extends Thread{
        @Override
            public void run() {
                MethodInvocation mi = null;
                while (true) {
                    try {
                        mi = queue.take(); //blocks until something available.
                    } catch (InterruptedException ie) {
                        LOG.error("Queue interrupted while waiting for work");
                        return;
                    }

                    boolean locked = false;
                    do {
                        try {                            
                            mi.invoke(true);
                        } catch (LockException le) {                                                                                 
                            //Database is locked (e.g. someone else is modifying
                            //the database).  Try again in POLL_INTERVAL ms.
                            locked = true;   
                            try{
                                Thread.sleep(POLL_INTERVAL);
                            } catch(InterruptedException iex){
                                LOG.error("Queue interrupted while sleeping.  Waiting job will be pushed back.");
                                queue.add(mi);
                                return;
                            }
                        }
                    } while (locked);                    
                }            
            }            
    };
    
    public static synchronized void enqueue(MethodInvocation mi) {  
        queue.add(mi);                
        if(queueProcessor == null || !queueProcessor.isAlive()){
            LOG.info("No active queue to execute method "+mi.getName()+" - starting new thread");
            queueProcessor = new QueueProcessor();
            queueProcessor.start();            
        }
    }
}
