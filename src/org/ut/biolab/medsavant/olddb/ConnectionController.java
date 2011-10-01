/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.olddb;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.log.ClientLogger;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    private static int POOL_SIZE = 10;
    private static Connection[] connections;
    private static Random random;

    public static void disconnectAll() {
        if(connections == null) return;
        for (Connection c : connections) {
            try {
                if (c != null)
                    c.close();
            } catch (SQLException ex) {
                Logger.getLogger(ConnectionController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static Connection connect() throws NonFatalDatabaseException {

        if (connections == null) {
            random = new Random();
            connections = new Connection[POOL_SIZE];
            //createConnections();
        }

        int conNum = random.nextInt(POOL_SIZE);

        boolean createNewConnection = false;

        Connection connection = connections[conNum];

        try {
            createNewConnection = connection == null || connection.isClosed();
        } catch (SQLException ex) {
            createNewConnection = true;
        }

        if (createNewConnection) {
            connection = connectOnce();
            connections[conNum] = connection;
        }

        return connection;
    }

    private static void createConnections() throws NonFatalDatabaseException {
        connections = new Connection[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            connections[i] = connectOnce();
        }
    }

    private static Connection connectOnce() throws NonFatalDatabaseException {

        Connection c;
        try {
            Class.forName(SettingsController.getInstance().getDBDriver());
            c = DriverManager.getConnection(SettingsController.getInstance().getDBURL(), LoginController.getUsername(), LoginController.getPassword());
            return c;
        } catch (Exception e) {
            if (e.getMessage().startsWith("Access denied")) {
                LoginController.logout();
                throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_ACCESS_DENIED, LoginController.getUsername());
            } else if (e.getMessage().startsWith("Communications link failure") || e.getMessage().startsWith("Unknown database")) {
                LoginController.logout();
                throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_DB_CONNECTION_FAILURE, LoginController.getUsername());
            } else {
                ClientLogger.log(e.getMessage(),Level.SEVERE);
                //e.printStackTrace();
                LoginController.logout();
                throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_UNKNOWN, LoginController.getUsername());
            }
        }
    }
}
