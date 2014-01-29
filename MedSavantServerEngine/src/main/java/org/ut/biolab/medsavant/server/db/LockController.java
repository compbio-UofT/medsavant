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
package org.ut.biolab.medsavant.server.db;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

/**
 * Governs the locking of the database for background tasks which alter the
 * database.
 *
 * @author mfiume
 */
public class LockController {

    private static Log LOG = LogFactory.getLog(LockController.class);
    
    private static LockController instance;
    private final HashMap<Integer, ReentrantLock> locks;

    private LockController() {
        locks = new HashMap<Integer, ReentrantLock>();
    }

    public static LockController getInstance() {
        if (instance == null) {
            instance = new LockController();
        }
        return instance;
    }

    /**
     * Obtain a lock. If another thread already owns the lock, an exception will
     * be thrown.
     *
     * @param projectID The project ID to lock
     * @throws LockException
     */
    public synchronized void requestLock(int projectID) throws LockException {

        LOG.info("Server lock requested");
        
        ReentrantLock lock = locks.get(projectID);

        // create the lock if one doesn't exist for the project
        if (lock == null) {
            lock = new ReentrantLock();
            locks.put(projectID, lock);
        }

        // lock it down 
        // (if this thread owns the lock, call lock again which will increase the hold count)
        if (!lock.isLocked() || lock.isHeldByCurrentThread()) {
            lock.lock();
            LOG.info(String.format("Server locked - hold count %d",lock.getHoldCount()));
        } else {
            throw new LockException("Database is locked for changes");
        }
    }

    /**
     * Release the lock unforcibly.
     *
     * @param projectID The project ID to lock
     * @throws org.ut.biolab.medsavant.shared.model.LockException
     */
    public synchronized void releaseLock(int projectID) throws LockException {
        releaseLock(projectID, false);
    }

    /**
     * Release the lock. If force is false, only the thread which requested the
     * lock may unlock. However, in the case where that thread no longer exists,
     * it is necessary to force the unlock.
     *
     * @param projectID The project ID to unlock.
     * @param force Whether to force the unlock.
     * @throws org.ut.biolab.medsavant.shared.model.LockException
     */
    public synchronized void releaseLock(int projectID, boolean force) throws LockException {

        LOG.info("Server unlock requested");
        
        ReentrantLock lock = locks.get(projectID);

        // no lock exists for this project
        if (lock == null) {
            throw new LockException("No lock exists");
        }

        if (force) {
            while (lock.isLocked()) {
                lock.unlock();
            }
            LOG.info(String.format("Server forcibly unlocked - hold count %d",lock.getHoldCount()));
            
        // unlock it, or decrement the hold count
        } else if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            LOG.info(String.format("Server unlocked - hold count %d",lock.getHoldCount()));
        // not allowed to lock
        } else {
            throw new LockException("Database could not be unlocked");
        }
    }

    /**
     * Check if the project is locked
     *
     * @param projectID The project ID to check.
     * @return Whether the project is locked.
     */
    public boolean isLocked(int projectID) {
        ReentrantLock l = locks.get(projectID);

        // l will be null if no one has requested a lock for the project before
        if (l == null) {
            return false;
        }

        return l.isLocked();
    }

}
