/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import medsavant.exception.AccessDeniedDatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.ut.biolab.medsavant.controller.LoginController;
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

    public static Connection connect() throws AccessDeniedDatabaseException {

        boolean createNewConnection = false;

        try {
            createNewConnection = connection == null || connection.isClosed();
        } catch (SQLException ex) {
            createNewConnection = true;
        }

        if (createNewConnection) {
            try {
              Class.forName(DBSettings.DRIVER);
              System.out.println("Connecting to DB host: " + DBSettings.DB_HOST);
              connection = DriverManager.getConnection(DBSettings.DB_URL,LoginController.getUsername(),LoginController.getPassword());
              //connection = DriverManager.getConnection(DBSettings.DB_URL,DBSettings.DB_USER_NAME,DBSettings.DB_PASSWORD);
              System.out.println("Connection successful");
            }
            catch (Exception e)
            {
                if (e.getMessage().startsWith("Access denied")) {
                    LoginController.logout();
                    DialogUtil.displayErrorMessage("Access denied for user: " + LoginController.getUsername(), e);
                    throw new AccessDeniedDatabaseException(LoginController.getUsername());
                } else {
                    e.printStackTrace();
                }
            }
        }

        return connection;
    }

}
