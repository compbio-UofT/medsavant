/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.db.util.query;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.db.model.UserLevel;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.query.api.UserQueryUtilAdapter;

/**
 *
 * @author mfiume
 */
public class UserQueryUtil extends java.rmi.server.UnicastRemoteObject implements UserQueryUtilAdapter {

    private static UserQueryUtil instance;

    public static UserQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new UserQueryUtil();
        }
        return instance;
    }

    public UserQueryUtil() throws RemoteException {}


    public List<String> getUserNames(String sid) throws SQLException {

        ResultSet rs = ConnectionController.executeQuery(sid,"SELECT DISTINCT user FROM mysql.user");

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }

    public boolean userExists(String userName) throws SQLException {
        ResultSet rs = ConnectionController.executeQuery("SELECT user FROM mysql.user WHERE user=?;", userName);
        return rs.next();
    }

    public synchronized void addUser(String sid, String name, char[] pass, UserLevel level) throws SQLException {
        ConnectionController.connectPooled(sid).setAutoCommit(false);

        try {
            ConnectionController.executeUpdate("CREATE USER ?@'localhost' IDENTIFIED BY ?;", name, new String(pass));
            grantPrivileges(sid,name, level);
            ConnectionController.connectPooled(sid).commit();
        } catch (SQLException sqlx) {
            ConnectionController.connectPooled(sid).rollback();
            throw sqlx;
        } finally {
            for (int i = 0; i < pass.length; i++) {
                pass[i] = 0;
            }
            ConnectionController.connectPooled(sid).setAutoCommit(true);
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    public void grantPrivileges(String sid, String name, UserLevel level) throws SQLException {
        switch (level) {
            case ADMIN:
                ConnectionController.executeUpdate(String.format("GRANT ALL ON %s.* TO ?@'localhost';", ConnectionController.getDBName(sid)), name);
                ConnectionController.executeUpdate(String.format("GRANT GRANT OPTION ON *.* TO ?@'localhost';", ConnectionController.getDBName(sid)), name);
                ConnectionController.executeUpdate(String.format("GRANT CREATE USER ON *.* TO ?@'localhost';", ConnectionController.getDBName(sid)), name);
                ConnectionController.executeUpdate("GRANT SELECT ON mysql.user TO ?@'localhost';", name);
                break;
            case USER:
                ConnectionController.executeUpdate(String.format("GRANT SELECT, CREATE TEMPORARY TABLES ON %s.* TO ?@'localhost';", ConnectionController.getDBName(sid)), name);
                break;
            case GUEST:
                ConnectionController.executeUpdate(String.format("GRANT SELECT ON %s.* TO ?@'localhost'", ConnectionController.getDBName(sid)), name);
                break;
        }
    }

    public boolean isUserAdmin(String name) throws SQLException {
        if (userExists(name)) {
            // If the user can create other users, they're assumed to be admin.
            ResultSet rs = ConnectionController.executeQuery("SELECT Create_user_priv FROM mysql.user WHERE user=?;", name);
            rs.next();
            return rs.getString(1).equals("Y");
        } else {
            return false;
        }
    }

    public UserLevel getUserLevel(String name) throws SQLException {
        if (userExists(name)) {
            // If the user can create other users, they're assumed to be admin.

            ResultSet rs = ConnectionController.executeQuery("SELECT Create_user_priv FROM mysql.user WHERE user=?;", name);
            if (rs.next()) {
                if (rs.getString(1).equals("Y")) {
                    return UserLevel.ADMIN;
                }
            }
            rs = ConnectionController.executeQuery("SELECT Create_tmp_table_priv FROM mysql.db WHERE user=?", name);
            if (rs.next()) {
                if (rs.getString(1).equals("Y")) {
                    return UserLevel.USER;
                }
            }
            return UserLevel.GUEST;
        }
        return UserLevel.NONE;
    }

    public void removeUser(String name) throws SQLException {
        ConnectionController.executeUpdate("DROP USER ?@'localhost';", name);
    }
}
