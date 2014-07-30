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

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

import java.util.Random;

/**
 *
 * @author mfiume
 */
public class LockControllerTest {
    private Random random;

    static private LockController lockController;
    final private String db = "dummyDB";
    private int project;

    public LockControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        lockController = LockController.getInstance();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        random = new Random();
        project = random.nextInt();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void releaseLock() throws LockException {
        lockController.requestLock(db, project);
        assertTrue(lockController.isLocked(db, project));

        lockController.releaseLock(db, project);
        assertFalse(lockController.isLocked(db, project));
    }

    @Test
    public void testRequestLock() throws LockException {
        assertFalse(lockController.isLocked(db, project));

        lockController.requestLock(db, project);

        // now locked
        assertTrue(lockController.isLocked(db, project));
    }

    /**
     * Test locking multiple times on the same thread.
     */
    @Test
    public void testLockSameThreadMultipleTimes() throws LockException {
        lockController.requestLock(db, project);
        lockController.requestLock(db, project);

        lockController.releaseLock(db, project);

        // still locked
        assertTrue(lockController.isLocked(db, project));

        lockController.releaseLock(db, project);

        // now unlocked
        assertFalse(lockController.isLocked(db, project));
    }

    /**
     * Test locking multiple times on different threads
     */
    @Test(expected = LockException.class)
    public void testLockDiffThreads() throws LockException, InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    lockController.requestLock(db, project);
                } catch (LockException e) {
                    fail();
                }
            }
        };
        thread.start();
        thread.join();
        lockController.requestLock(db, project); // Should throw LockException

    }


}
