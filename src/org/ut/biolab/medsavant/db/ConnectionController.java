/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author mfiume
 */
public class ConnectionController {

     private static String un = "root";
    private static String pw = "";
    private static String host = "localhost";
    private static String port = "5029";
    private static String dbName = "medsavant";
    private static String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
    private static String table = "annotation_variant";
    private static String driver = "com.mysql.jdbc.Driver";
    private static Connection connection;

    public static Connection connect() {

        if (connection == null) {
            try {
              Class.forName(driver);
              System.out.println("Connecting to DB host: " + host);
              Connection conn = DriverManager.getConnection(url,un,pw);
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
