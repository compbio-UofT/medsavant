package org.ut.biolab.medsavant.db.util;

import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mfiume
 */
class SessionConnection {

    private Connection lastConnection;
    private String dbName;
    private final String user;
    private final String pw;

    public SessionConnection(String user, String pw, String dbname) {
        this.user = user;
        this.pw = pw;
        this.dbName = dbname;
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

        if (lastConnection == null || lastConnection.isClosed()) {
            lastConnection = connectUnpooled(dbName,user,pw);
        }

        return lastConnection;
    }

    public Connection connectUnpooled(String dbname, String userName, String password) throws SQLException {
        return ConnectionController.connectOnce(ConnectionController.getHost(), ConnectionController.getPort(), dbname, userName, password);
    }

    void setDBName(String dbname) {
        this.disconnectAll();
        this.dbName = dbname;
    }

    String getDBName() {
        return this.dbName;
    }

    String getUser() {
        return this.user;
    }
}
