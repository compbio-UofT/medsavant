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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

/**
 *
 * @author mfiume
 */
public class LockControllerTest {
    
    public LockControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testLock() {

        // initially unlocked
        assertFalse(LockController.getInstance().isLocked(0));
        
        boolean didFail = false;
        
        // request lock
        try {
            LockController.getInstance().requestLock(0);
        } catch (LockException e) {
            didFail = true;
        }
        assertFalse(didFail);
        
        // now locked
        assertTrue(LockController.getInstance().isLocked(0));

        // unlock
        didFail = false;
        try {
            LockController.getInstance().releaseLock(0);
        } catch (LockException e) {
            didFail = true;
        }
        assertFalse(didFail);
        
        // now unlocked
        assertFalse(LockController.getInstance().isLocked(0));
        
        // request 2 locks
        try {
            LockController.getInstance().requestLock(0);
            LockController.getInstance().requestLock(0);
        } catch (LockException e) {
            didFail = true;
        }
        assertFalse(didFail);
    
        // unlock once
        didFail = false;
        try {
            LockController.getInstance().releaseLock(0);
        } catch (LockException e) {
            didFail = true;
        }
        assertFalse(didFail);
        
        // still locked
        assertTrue(LockController.getInstance().isLocked(0));
        
        // unlock once more
        didFail = false;
        try {
            LockController.getInstance().releaseLock(0);
        } catch (LockException e) {
            didFail = true;
        }
        assertFalse(didFail);
        
        // now unlocked
        assertFalse(LockController.getInstance().isLocked(0));
        
    }
    
    
}
