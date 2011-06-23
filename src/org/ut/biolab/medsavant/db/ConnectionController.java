/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.view.util.DialogUtil;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    
    private static Connection connection;
    
    
    public static void disconnect() {
        try {
            if (connection != null) 
                connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        connection = null;
    }

    public static Connection connect() throws NonFatalDatabaseException {

        boolean createNewConnection = false;

        try {
            createNewConnection = connection == null || connection.isClosed();
        } catch (SQLException ex) {
            createNewConnection = true;
        }

        if (createNewConnection) {
            try {
              Class.forName(SettingsController.getInstance().getDBDriver());
              System.out.println("Connecting to DB host: " + SettingsController.getInstance().getDBHost());
              System.out.println("Connecting to DB url: " + SettingsController.getInstance().getDBURL());
              connection = DriverManager.getConnection(SettingsController.getInstance().getDBURL(),LoginController.getUsername(),LoginController.getPassword());
              System.out.println("Connection successful");
            }
            catch (Exception e)
            {
                if (e.getMessage().startsWith("Access denied")) {
                    LoginController.logout();
                    throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_ACCESS_DENIED,LoginController.getUsername());
                } else if (e.getMessage().startsWith("Communications link failure") || e.getMessage().startsWith("Unknown database")) {
                    LoginController.logout();
                    throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_DB_CONNECTION_FAILURE,LoginController.getUsername());
                } else {
                    System.out.println("Message: " + e.getMessage());
                    //e.printStackTrace();
                    LoginController.logout();
                    throw new NonFatalDatabaseException(NonFatalDatabaseException.ExceptionType.TYPE_UNKNOWN,LoginController.getUsername());
                }
            }
        }

        return connection;
    }

}
