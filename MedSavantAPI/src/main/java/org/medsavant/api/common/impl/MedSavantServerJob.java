/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.medsavant.api.common.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import org.medsavant.api.common.JobProgressMonitor;
import org.medsavant.api.common.ScheduleStatus;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_LONGJOB;
import static org.medsavant.api.common.ScheduleStatus.SCHEDULED_AS_SHORTJOB;
import org.medsavant.api.common.impl.JobProgressMonitorImpl;

public abstract class MedSavantServerJob implements Callable<Void> {

    private static Map<String, List<JobProgressMonitor>> rootJobs;
    private static final Object rootJobLock = new Object();
    private final JobProgressMonitor jobProgress;
    private final JobProgressMonitor parentJobProgressMonitor;
    private static final Object jobExpiryLock = new Object();
    private static final List<MedSavantServerJob> jobsToExpire = new LinkedList<MedSavantServerJob>();
    private static final long EXPIRATION_CHECK_INTERVAL = 120000l; //2 mins.
    private static final long JOB_EXPIRATION_TIME = 300000l; //5 mins

    private long expiryTime = 0;

    //private MedSavantServerJob parentJob;
    private void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setScheduleStatus(ScheduleStatus status) {
        jobProgress.setStatus(status);
    }

    public static List<JobProgressMonitor> getJobProgressesForUser(String userId) {
        if (rootJobs == null) {
            return null;
        }
        return rootJobs.get(userId);
    }

    public ScheduleStatus getScheduleStatus() {
        return jobProgress.getStatus();
    }

    public MedSavantServerJob(String userId, String jobName, JobProgressMonitor parentMonitor) {
        jobProgress = new JobProgressMonitorImpl(userId, jobName);
        this.parentJobProgressMonitor = parentMonitor;
        if (parentMonitor != null) {
            parentMonitor.addChildJobMonitor(jobProgress);
        } else {
            if (rootJobs == null) {
                rootJobs = new HashMap<String, List<JobProgressMonitor>>();
            }
            synchronized (rootJobLock) {
                List<JobProgressMonitor> myRootJobs = rootJobs.get(userId);
                if (myRootJobs == null) {
                    myRootJobs = new LinkedList<JobProgressMonitor>();
                }
                myRootJobs.add(jobProgress);
                rootJobs.put(userId, myRootJobs);
            }
        }
    }

    public JobProgressMonitor getJobProgressMonitor() {
        return jobProgress;
    }

    private static void initTimer() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (jobExpiryLock) {
                    ListIterator<MedSavantServerJob> jobIterator = jobsToExpire.listIterator();
                    while (jobIterator.hasNext()) {
                        MedSavantServerJob job = jobIterator.next();
                        if (job.expiryTime <= System.currentTimeMillis() && !job.hasChildren()) {
                            job.expireImmediately();
                            jobIterator.remove();
                        }
                    }
                }
            }

        }, EXPIRATION_CHECK_INTERVAL, EXPIRATION_CHECK_INTERVAL);
    }

    static {
        initTimer();
    }

    private void expireImmediately() {
        //does job have a parent?        
        if (parentJobProgressMonitor != null) {
            parentJobProgressMonitor.removeChildJobMonitor(jobProgress);
        } else {
            synchronized (rootJobLock) {
                List<JobProgressMonitor> myJobs = rootJobs.get(jobProgress.getUserName());
                if (myJobs != null && !myJobs.isEmpty()) {
                    int i = myJobs.indexOf(jobProgress);
                    if (i >= 0) {
                        myJobs.remove(i);
                    }
                }
            }
        }
        jobProgress.clearChildJobMonitors();
    }

    private void expire() {
        synchronized (jobExpiryLock) {
            setExpiryTime(System.currentTimeMillis() + JOB_EXPIRATION_TIME);
            jobProgress.setMessage(jobProgress.getMessage() + " (Message will expire in ~" + (JOB_EXPIRATION_TIME / 1000) + " sec)");
            jobsToExpire.add(this);
        }
    }

    @Override
    public final Void call() throws Exception {
        try {
            if (getScheduleStatus() == SCHEDULED_AS_LONGJOB) {
                setScheduleStatus(ScheduleStatus.RUNNING_AS_LONGJOB);
            } else if (getScheduleStatus() == SCHEDULED_AS_SHORTJOB) {
                setScheduleStatus(ScheduleStatus.RUNNING_AS_SHORTJOB);
            } else {
                throw new IllegalArgumentException("MedSavantJob cannot run in this state: " + getScheduleStatus());
            }
            if (run()) { //note run should BLOCK.
                setScheduleStatus(ScheduleStatus.FINISHED);
            } else {
                setScheduleStatus(ScheduleStatus.CANCELLED);
            }
            return null;
        } catch (Exception ex) {
            jobProgress.setMessage("Aborted due to error: " + ex.getMessage());
            ex.printStackTrace();
            setScheduleStatus(ScheduleStatus.CANCELLED);
            throw (ex);
        } finally {
            expire();
        }
    }

    public boolean hasChildren() {
        return jobProgress.hasChildJobMonitors();
    }

    public abstract boolean run() throws Exception;

}
