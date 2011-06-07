/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

    
    private static Connection connection;

    public static Connection connect() {

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
              connection = DriverManager.getConnection(DBSettings.DB_URL,DBSettings.DB_USER_NAME,DBSettings.DB_PASSWORD);
              System.out.println("Connection successful");
            }
            catch (Exception e)
            {
              System.err.println(e.getMessage());
            }
        }

        return connection;
    }

}
