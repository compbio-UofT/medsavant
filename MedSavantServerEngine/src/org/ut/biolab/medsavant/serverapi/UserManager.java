/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.serverapi;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.connection.PooledConnection;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author mfiume
 */
public class UserManager extends MedSavantServerUnicastRemoteObject implements UserManagerAdapter {

    private static final Log LOG = LogFactory.getLog(UserManager.class);

    private static UserManager instance;

    public static synchronized UserManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() throws RemoteException {
    }

    @Override
    public String[] getUserNames(String sessID) throws SQLException {

        List<String> results = new ArrayList<String>();
        ResultSet rs = ConnectionController.executePreparedQuery(sessID, "SELECT DISTINCT user FROM mysql.user");
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results.toArray(new String[0]);
    }

    @Override
    public boolean userExists(String sessID, String user) throws SQLException {
        return ConnectionController.executePreparedQuery(sessID, "SELECT user FROM mysql.user WHERE user=?;", user).next();
    }

    /**
     * Add a new user to MedSavant.
     *
     * @param sessID the session we're logged in as
     * @param user the user to add
     * @param pass the password
     * @param level the user's level
     * @throws SQLException 
     */
    @Override
    public synchronized void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            // TODO: Transactions aren't supported for MyISAM, so this has no effect.
            conn.setAutoCommit(false);

            conn.executePreparedUpdate("CREATE USER ?@'localhost' IDENTIFIED BY ?", user, new String(pass));
            grantPrivileges(sessID, user, level);
            conn.commit();
        } catch (SQLException sqlx) {
            conn.rollback();
            throw sqlx;
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            String dbName = ConnectionController.getDBName(sessID);
            LOG.info("Granting " + level + " privileges to " + name + " on " + dbName + "...");
            switch (level) {
                case ADMIN:
                    conn.executePreparedUpdate("GRANT CREATE, CREATE TEMPORARY TABLES, CREATE USER, DELETE, DROP, FILE, GRANT OPTION, INSERT, SELECT, UPDATE ON *.* TO ?@'localhost'", name);
                    conn.executePreparedUpdate(String.format("GRANT GRANT OPTION ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT CREATE, CREATE TEMPORARY TABLES, DELETE, DROP, INSERT, SELECT, UPDATE ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.db TO ?@'localhost'", name);
                    break;
                case USER:
                    conn.executePreparedUpdate(String.format("GRANT CREATE TEMPORARY TABLES, SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.region_set_membership TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
                case GUEST:
                    conn.executePreparedUpdate(String.format("GRANT SELECT ON %s.* TO ?@'localhost'", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?@'localhost'", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?@'localhost'", name);
                    break;
            }
            conn.executePreparedUpdate(String.format("GRANT INSERT on %s.server_log TO ?@'localhost'", dbName), name);
            LOG.info("... granted.");
        } finally {
            conn.close();
        }
    }

    @Override
    public UserLevel getUserLevel(String sessID, String name) throws SQLException {
        if (userExists(sessID, name)) {
            // If the user can create other users, they're assumed to be admin.
            PooledConnection conn = ConnectionController.connectPooled(sessID);
            try {
                ResultSet rs = conn.executePreparedQuery("SELECT Create_user_priv FROM mysql.user WHERE user=?", name);
                if (rs.next()) {
                    if (rs.getString(1).equals("Y")) {
                        return UserLevel.ADMIN;
                    }
                }
                rs = conn.executePreparedQuery("SELECT Create_tmp_table_priv FROM mysql.db WHERE user=?", name);
                if (rs.next()) {
                    if (rs.getString(1).equals("Y")) {
                        return UserLevel.USER;
                    }
                }
            } finally {
                conn.close();
            }
            return UserLevel.GUEST;
        }
        return UserLevel.NONE;
    }

    @Override
    public void removeUser(String sid, String name) throws SQLException {
        ConnectionController.executePreparedUpdate(sid, "DROP USER ?@'localhost'", name);
    }
}
