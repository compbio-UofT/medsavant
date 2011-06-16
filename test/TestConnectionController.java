/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import medsavant.db.ConnectionController;
import java.sql.Connection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mfiume
 */
public class TestConnectionController {

    public TestConnectionController() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void testConnect() {
        Connection c = ConnectionController.connect();
        /**
         * Connection is successful
         */
        assertTrue(c != null);
    }

}