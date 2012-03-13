/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.connection;

import java.sql.*;
import java.util.*;

/**
 *
 * @author Andrew
 */
public class ConnectionDriver implements Driver {

    public static final String URL_PREFIX = "jdbc:jdc:";
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 0;
    private ConnectionPool pool;

    public ConnectionDriver(String driver, String url, String user, String password) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        DriverManager.registerDriver(this);
        Class.forName(driver).newInstance();
        pool = new ConnectionPool(url, user, password);
    }
    
    public Connection connect() throws SQLException {
        return connect(null, null);
    }

    public Connection connect(String url, Properties props) throws SQLException {
        //if(!url.startsWith(URL_PREFIX)) {
        //     return null;
        //}
        return pool.getConnection();
    }

    public boolean acceptsURL(String url) {
        return true;
        //return url.startsWith(URL_PREFIX);
    }

    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    public DriverPropertyInfo[] getPropertyInfo(String str, Properties props) {
        return new DriverPropertyInfo[0];
    }

    public boolean jdbcCompliant() {
        return false;
    }
}
