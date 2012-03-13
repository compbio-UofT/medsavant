package org.ut.biolab.medsavant.db.util;

import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.connection.ConnectionDriver;

/**
 * @author mfiume
 */
public class SessionConnection {

    private Connection lastConnection;
    private String dbName;
    private final String user;
    private final String pw;
    
    private ConnectionDriver driver;

    public SessionConnection(String user, String pw, String dbname) {
        this.user = user;
        this.pw = pw;
        this.dbName = dbname;
        try {
            this.driver = new ConnectionDriver("com.mysql.jdbc.Driver", String.format("jdbc:mysql://%s:%d/%s?%s", ConnectionController.getHost(), ConnectionController.getPort(), dbname, "enableQueryTimeouts=false"), user, pw);
        } catch (Exception ex) {
            Logger.getLogger(SessionConnection.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public void disconnectAll() {
        if (lastConnection != null) {
            try {
                lastConnection.close();
            } catch (SQLException ex) {
            }
            lastConnection = null;
        }
    }

    public Connection connectPooled() throws SQLException {

        return driver.connect();
        
        /*if (lastConnection == null || lastConnection.isClosed()) {
            lastConnection = connectUnpooled(dbName,user,pw);
        }

        return lastConnection;*/
    }

    public Connection connectUnpooled(String dbname, String userName, String password) throws SQLException {
        return ConnectionController.connectOnce(ConnectionController.getHost(), ConnectionController.getPort(), dbname, userName, password);
    }

    void setDBName(String dbname) {
        this.disconnectAll();
        this.dbName = dbname;
    }

    public String getDBName() {
        return this.dbName;
    }

    public String getUser() {
        return this.user;
    }
}
