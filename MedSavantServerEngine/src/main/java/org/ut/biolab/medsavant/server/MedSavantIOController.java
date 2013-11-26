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
package org.ut.biolab.medsavant.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * IOjobs submitted through this class respect MAX_IO_JOBS -- there can never
 * be more than MAX_IO_JOBS threads with a running IOJob. If the limit is
 * exceeded, the other IOJobs will wait until an IO permit becomes available.
 * The 'doIO' method of an IOJob is executed a maximum of
 * DEFAULT_NUM_BLOCKS_PER_PERMIT before the permit is temporarily released to
 * other scheduled IO jobs. This default can be overridden with an argument to
 * requestIO.
 */
public class MedSavantIOController {

    private static final Log LOG = LogFactory.getLog(MedSavantIOController.class);
    private static final int TIME_PER_PERMIT = 10000; //0.5s, for testing.
    private static Semaphore IOSem = new Semaphore(MedSavantServerEngine.MAX_IO_JOBS, true);
    private static final Timer timer = new Timer();
    private static final List<Permit> outStandingPermits;

    // INNER CLASSES
    private static class PermitRenewerTask extends TimerTask {

        @Override
        public synchronized void run() {
            for (Permit p : outStandingPermits) {
                p.invalidate();
            }
        }
    };
    
    private static class Permit {

        private boolean valid;
        private String jobName;

        private Permit(){}
        
        public static Permit getPermit(IOJob job) throws InterruptedException{
            Permit p = new Permit();
            p.jobName = job.getName();            
            synchronized (outStandingPermits) {
                outStandingPermits.add(p);                
            }
            IOSem.acquire();
            p.valid = true;

            //LOG.info("DEBUG: " + Thread.currentThread().getId() + ": " + p.jobName + " STARTED and ACTIVE");

            return p;
        }

        public void invalidate() {
            this.valid = false;
        }

        public void renew() throws InterruptedException {
            if (!this.valid) {
                IOSem.release();
                //LOG.info("DEBUG: " + Thread.currentThread().getId() + ": " + jobName + " INACTIVE");
                IOSem.acquire();
                this.valid = true;
                //LOG.info("DEBUG: " + Thread.currentThread().getId() + ": " + jobName + " ACTIVE");
            }
        }

        public void release() {
            IOSem.release();
            synchronized (outStandingPermits) {
                outStandingPermits.remove(this);
            }
            //LOG.info("DEBUG: " + Thread.currentThread().getId() + ": " + jobName + " FINISHED and INACTIVE");
        }
    };

    //INITIALIZATION AND METHODS, MedSavantIOController 
    
    static {
        timer.schedule(new PermitRenewerTask(), TIME_PER_PERMIT, TIME_PER_PERMIT);
        outStandingPermits = new ArrayList<Permit>();
    }
    

    public static void requestIO(IOJob job) throws IOException, InterruptedException {
        Permit permit = Permit.getPermit(job);       
        try{
            while (job.continueIO()) {
                permit.renew();
                job.doIO();                
            }
        }finally{
            permit.release();
        }
    }
 
}
