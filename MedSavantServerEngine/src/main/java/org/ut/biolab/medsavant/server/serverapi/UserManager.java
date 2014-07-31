/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.serverapi;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import org.ut.biolab.medsavant.shared.format.UserRole;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.exception.UnauthorizedException;
import org.ut.biolab.medsavant.shared.serverapi.UserManagerAdapter;

/**
 *
 * @author mfiume
 */
public class UserManager extends MedSavantServerUnicastRemoteObject implements UserManagerAdapter {

    private static final Log LOG = LogFactory.getLog(UserManager.class);

    private static final String DATABASE_USER_KEY_PREFIX = "_dbuser_";
    private static UserManager instance;

    public static synchronized UserManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() throws RemoteException, SessionExpiredException {
    }

    private boolean checkAdmin(String sessID) throws SecurityException, RemoteException, SessionExpiredException, SQLException {
        String thisUser = SessionManager.getInstance().getUserForSession(sessID);
        String thisDatabase = SessionManager.getInstance().getDatabaseForSession(sessID);
        if (!isAdmin(sessID)) {
            String err = "Cannot add role to user.  This requires " + thisUser + " to have administrative privileges";
            LOG.error(err);
            throw new SecurityException(err);
        }

        if (!isUserOfThisDatabase(sessID)) {
            String err = "Cannot add role to user.  The current user " + thisUser + " is not a user of " + thisDatabase;
            LOG.error(err);
            throw new SecurityException(err);
        }
        return true;
    }
    
    private Set<UserRole> getRolesForUser(String sessID, String user) throws RemoteException, SQLException, SessionExpiredException {
        String database = SessionManager.getInstance().getDatabaseForSession(sessID);
        TableSchema roleTable = MedSavantDatabase.UserRoleTableSchema;
        TableSchema roleATable = MedSavantDatabase.UserRoleAssignmentTableSchema;

        SelectQuery sq = new SelectQuery();
        sq.addColumns(
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ID),
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLENAME),
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLE_DESCRIPTION)
        );
        Condition joinCondition = BinaryCondition.equalTo(roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ID), roleATable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_ROLE_ID));
        sq.addJoin(SelectQuery.JoinType.INNER, roleTable.getTable(), roleATable.getTable(), joinCondition);
        sq.addCondition(BinaryCondition.equalTo(roleATable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_USERNAME), user));

        ResultSet rs = null;
        try {
            rs = ConnectionController.executeQuery(sessID, sq.toString());
            Set<UserRole> roleSet = new HashSet<UserRole>();
            while (rs.next()) {
                int roleId = rs.getInt(1);
                String roleName = rs.getString(2);
                String roleDescription = rs.getString(3);
                roleSet.add(new UserRole(roleId, roleName, roleDescription, database));
            }
            return roleSet;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public boolean checkRole(String sessID, UserRole role) throws RemoteException, SQLException, SessionExpiredException {
        Set<UserRole> assignedRoles
                = getRolesForUser(sessID, SessionManager.getInstance().getUserForSession(sessID));
        return assignedRoles.contains(role);
    }

    //verifies the user is assigned the given role.
    public boolean checkAllRoles(String sessID, Set<UserRole> roles) throws RemoteException, SQLException, SessionExpiredException {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Can't check empty role");
        }

        Set<UserRole> assignedRoles
                = getRolesForUser(sessID, SessionManager.getInstance().getUserForSession(sessID));
        return !assignedRoles.isEmpty() && assignedRoles.containsAll(roles);
    }

    public boolean checkAnyRole(String sessID, Set<UserRole> roles) throws RemoteException, SQLException, SessionExpiredException {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Can't check empty role");
        }
        Set<UserRole> assignedRoles
                = getRolesForUser(sessID, SessionManager.getInstance().getUserForSession(sessID));

        if (!assignedRoles.isEmpty()) {
            for (UserRole role : roles) {
                if (assignedRoles.contains(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<UserRole> getAllRoles(String sessID) throws RemoteException, SQLException, SecurityException, SessionExpiredException {
        String thisDatabase = SessionManager.getInstance().getDatabaseForSession(sessID);
        TableSchema roleTable = MedSavantDatabase.UserRoleTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(roleTable.getTable());
        sq.addColumns(
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ID),
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLENAME),
                roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLE_DESCRIPTION)
        );
        sq.setIsDistinct(true);
        ResultSet rs = null;
        try {
            rs = ConnectionController.executeQuery(sessID, sq.toString());
            Set<UserRole> roleSet = new TreeSet<UserRole>();
            while (rs.next()) {
                int roleId = rs.getInt(1);
                String roleName = rs.getString(2);
                String roleDescription = rs.getString(3);
                roleSet.add(new UserRole(roleId, roleName, roleDescription, thisDatabase));
            }
            return roleSet;
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
    
    @Override 
    public UserRole getRoleByName(String sessID, String roleName) throws RemoteException, SessionExpiredException, SQLException{
        String thisDatabase = SessionManager.getInstance().getDatabaseForSession(sessID);
        TableSchema roleTable = MedSavantDatabase.UserRoleTableSchema;
        SelectQuery sq = new SelectQuery();
        sq.addFromTable(roleTable.getTable());
        sq.addAllColumns();
        sq.addCondition(BinaryCondition.equalTo(roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLENAME), roleName));
        ResultSet rs = null;
        try{
            rs = ConnectionController.executeQuery(sessID, sq.toString());
            if(rs.next()){
                int roleId = rs.getInt(1);
                String name = rs.getString(2);
                String roleDescription = rs.getString(3);                
                return new UserRole(roleId, name, roleDescription, thisDatabase);                
            }
            return null;            
        }finally{
            if(rs != null){
                rs.close();
            }
        }                
    }

    @Override
    public UserRole addRole(String sessID, String roleName, String roleDescription) throws RemoteException, SessionExpiredException, SQLException, SecurityException {        
        String thisDatabase = SessionManager.getInstance().getDatabaseForSession(sessID);
        checkAdmin(sessID);

        //Check if role already exists, and if so, return it.
        Set<UserRole> roles = getAllRoles(sessID);
        for (UserRole r : roles) {
            if (r.getDatabase().equals(thisDatabase) && r.getRoleName().equals(roleName)) {
                return r;
            }
        }

        TableSchema roleTable = MedSavantDatabase.UserRoleTableSchema;
        InsertQuery iq = new InsertQuery(roleTable.getTableName());
        iq.addColumn(roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLENAME), roleName);
        iq.addColumn(roleTable.getDBColumn(MedSavantDatabase.UserRoleTableSchema.COLUMNNAME_OF_ROLE_DESCRIPTION), roleDescription);

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        PreparedStatement stmt = null;
        ResultSet res = null;
        int roleId = -1;
        try {
            stmt = conn.prepareStatement(iq.toString(), Statement.RETURN_GENERATED_KEYS);
            stmt.execute();
            res = stmt.getGeneratedKeys();
            res.next();
            roleId = res.getInt(1);

            return new UserRole(roleId, roleName, roleDescription, thisDatabase);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (res != null) {
                res.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    

    @Override
    public void dropRolesForUser(String sessID, String user, Set<UserRole> roles) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        checkAdmin(sessID);
          //Check if any of the roles given are already assigned, and if so remove them from the
        //roles to register.
        Set<UserRole> assignedRoles = getRolesForUser(sessID, user);
        if (assignedRoles.containsAll(roles)) {
            return;
        } else if (assignedRoles.size() > 0) {
            roles.removeAll(assignedRoles);
        }

        //register the remaining roles.        
        TableSchema raTable = MedSavantDatabase.UserRoleAssignmentTableSchema;
        for (UserRole role : roles) {
            DeleteQuery dq = new DeleteQuery(raTable.getTableName());          
            dq.addCondition(BinaryCondition.equalTo(raTable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_USERNAME), user));            
            dq.addCondition(BinaryCondition.equalTo(raTable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_ROLE_ID), role.getRoleId()));            
            ConnectionController.executeUpdate(sessID, dq.toString());          
        }
    }

    @Override
    public void registerRoleForUser(String sessID, String user, Set<UserRole> roles) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        checkAdmin(sessID);

        //Check if any of the roles given are already assigned, and if so remove them from the
        //roles to register.
        Set<UserRole> assignedRoles = getRolesForUser(sessID, user);
        if (assignedRoles.containsAll(roles)) {
            return;
        } else if (assignedRoles.size() > 0) {
            roles.removeAll(assignedRoles);
        }

        //register the remaining roles.        
        TableSchema raTable = MedSavantDatabase.UserRoleAssignmentTableSchema;
        for (UserRole role : roles) {
            InsertQuery iq = new InsertQuery(raTable.getTableName());
            iq.addColumn(raTable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_ROLE_ID), role.getRoleId());
            iq.addColumn(raTable.getDBColumn(MedSavantDatabase.UserRoleAssignmentTableSchema.COLUMNNAME_OF_USERNAME), user);
            ConnectionController.executeUpdate(sessID, iq.toString());
        }

    }

    @Override
    public Set<UserRole> getUserRoles(String sessID, String user) throws RemoteException, SessionExpiredException, SQLException, SecurityException {
        String thisUser = SessionManager.getInstance().getUserForSession(sessID);
        String thisDatabase = SessionManager.getInstance().getDatabaseForSession(sessID);

        //check that this user ibelongs to this database
        if (!isUserOfThisDatabase(sessID)) {
            LOG.error("User " + thisUser + " is not a member of the database " + thisDatabase + ", and can't query roles for user " + user);
            throw new SecurityException("Can't get roles for user " + user + " on this database.  User making request is not a user of this database.");

        }
        //Check that this user is either requesting his own roles, OR is an administrator.
        if (user.equals(thisUser) || isAdmin(sessID)) {
            //ok to proceed
            return getRolesForUser(sessID, user);
        } else {
            String err = "User " + user + " does not have administrative permission to request roles available for user " + user;
            LOG.error(err);
            throw new SecurityException(err);
        }
    }

    @Override
    public Set<UserRole> getRolesForUser(String sessID) throws SQLException, SessionExpiredException, RemoteException {
        return getRolesForUser(sessID, SessionManager.getInstance().getUserForSession(sessID));
    }

    public Set<String> getAllUserNames(String sessID) throws SQLException, SessionExpiredException {
        Set<String> results = new HashSet<String>();
        ResultSet rs = ConnectionController.executePreparedQuery(sessID, "SELECT DISTINCT user FROM mysql.user");

        while (rs.next()) {
            String user = rs.getString(1);
            results.add(user);
            //results.add(rs.getString(1));
        }
        return results;
    }

    @Override
    public String[] getUserNames(String sessID) throws SQLException, SessionExpiredException {
        
        Map<String, String> validUsers = null;
        try {
            validUsers = SettingsManager.getInstance().getSettingsForKeyPrefix(sessID, DATABASE_USER_KEY_PREFIX);
        } catch (RemoteException re) {
            throw new SQLException("Unable to find valid users for this database ", re);
        }

        if (validUsers == null) {
            return new String[]{};
        }

        Set<String> users = getAllUserNames(sessID);
        List<String> results = new ArrayList<String>();

        /* for(String au : users){
         System.out.println("All user: "+au);
         }
         for(String vu : validUsers.keySet()){
         System.out.println("Valid user: "+vu);
         }*/
        for (String user : users) {
            if (validUsers.containsKey(user) && !user.equalsIgnoreCase("root")) {
                results.add(user);
            }
        }

        return results.toArray(new String[0]);
    }

    public boolean isUserOfThisDatabase(String sessID) throws SQLException, SessionExpiredException, RemoteException {
        String thisUser = SessionManager.getInstance().getUserForSession(sessID);
        String[] users = getUserNames(sessID);
        for (String user : users) {
            if (user.equalsIgnoreCase(thisUser)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean userExists(String sessID, String user) throws SQLException, SessionExpiredException {
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
    public synchronized void addUser(String sessID, String user, char[] pass, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            if (user.startsWith(DATABASE_USER_KEY_PREFIX)) {
                throw new SQLException("Can't create user " + user + " -- illegal username");
            }
            // TODO: Transactions aren't supported for MyISAM, so this has no effect.
            conn.setAutoCommit(false);

            conn.executePreparedUpdate("CREATE USER ?@'%' IDENTIFIED BY ?", user, new String(pass));
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

    @Override
    public synchronized void changePassword(String sessID, String userName, char[] oldPass, char[] newPass) throws SQLException, RemoteException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.setAutoCommit(true);

            //Check that old password is valid.
            ConnectionController.revalidate(userName, new String(oldPass), sessID);

            //TODO: Check the new password against the current mysql password policy.                                                
            //Change the password
            conn.executePreparedUpdate("SET PASSWORD FOR ? = PASSWORD(?)", userName, new String(newPass));
        } finally {
            for (int i = 0; i < oldPass.length; ++i) {
                oldPass[i] = 0;
            }
            for (int i = 0; i < newPass.length; ++i) {
                newPass[i] = 0;
            }
            conn.close();
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     *
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            String dbName = ConnectionController.getDBName(sessID);
            LOG.info("Granting " + level + " privileges to " + name + " on " + dbName + "...");
            switch (level) {
                case ADMIN:
                    conn.executePreparedUpdate("GRANT ALTER, RELOAD, CREATE, CREATE VIEW, CREATE TEMPORARY TABLES, CREATE USER, DELETE, DROP, FILE, GRANT OPTION, INSERT, SELECT, UPDATE ON *.* TO ?", name);
                    conn.executePreparedUpdate(String.format("GRANT GRANT OPTION ON %s.* TO ?", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT ALTER, CREATE, CREATE VIEW, CREATE TEMPORARY TABLES, DELETE, DROP, INSERT, SELECT, UPDATE ON %s.* TO ?", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.user TO ?", name);
                    conn.executePreparedUpdate("GRANT SELECT ON mysql.db TO ?", name);
                    break;
                case USER:
                    conn.executePreparedUpdate(String.format("GRANT CREATE TEMPORARY TABLES, SELECT ON %s.* TO ?", dbName), name);

                    //grant read/write/delete on region sets.
                    conn.executePreparedUpdate(String.format("GRANT SELECT,INSERT,UPDATE,DELETE ON %s.region_set TO ?", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT SELECT,INSERT,UPDATE,DELETE ON %s.region_set_membership TO ?", dbName), name);

                    //Grant read/write/delete on cohorts.
                    conn.executePreparedUpdate(String.format("GRANT INSERT,SELECT,UPDATE,DELETE ON %s.cohort TO ?", dbName), name);
                    conn.executePreparedUpdate(String.format("GRANT INSERT,SELECT,UPDATE,DELETE ON %s.cohort_membership TO ?", dbName), name);

                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?", name);
                    conn.executePreparedUpdate("GRANT FILE ON *.* TO ?", name);
                    break;
                case GUEST:
                    conn.executePreparedUpdate(String.format("GRANT SELECT ON %s.* TO ?", dbName), name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_user_priv) ON mysql.user TO ?", name);
                    conn.executePreparedUpdate("GRANT SELECT (user, Create_tmp_table_priv) ON mysql.db TO ?", name);

                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.server_log TO ?", dbName), name);
                    // Grant permissions to write comments
                    conn.executePreparedUpdate(String.format("GRANT INSERT ON %s.variant_starred TO ?", dbName), name);

                    conn.executePreparedUpdate("GRANT FILE ON *.* TO ?", name);

                    break;
            }

            SettingsManager.getInstance().addSetting(sessID, DATABASE_USER_KEY_PREFIX + name, level.name());
            LOG.info("... granted.");
        } catch (Exception ex) {
            LOG.error("Problem creating user", ex);
            throw new SQLException("Can't setup privileges for user " + name, ex);
        } finally {
            conn.executeQuery("FLUSH PRIVILEGES");
            conn.close();
        }
    }

    @Override
    public UserLevel getSessionUsersLevel(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        // username for this session
        String name = SessionManager.getInstance().getUserForSession(sessID);
        return getUserLevel(sessID, name);
    }

    @Override
    public UserLevel getUserLevel(String sessID, String name) throws SQLException, SessionExpiredException {

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
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException, RemoteException {
        PooledConnection conn = ConnectionController.connectPooled(sid);
        conn.executePreparedUpdate("DROP USER ?", name);
        conn.executeQuery("FLUSH PRIVILEGES");
        SettingsManager.getInstance().removeSetting(sid, DATABASE_USER_KEY_PREFIX + name);
        
    }

    /**
     * Check whether the user associated with the session is an administrator.
     *
     * @param sessionID The session to check.
     * @return Whether the user associated with the session is an administrator.
     */
    public boolean isAdmin(String sessionID) throws SQLException, RemoteException, SessionExpiredException {
        try {
            return isAdmin(sessionID, false);
        } catch (UnauthorizedException ex) {
            // this will never happen because we specified not to throw exceptions above
            return false;
        }
    }

    /**
     * Check whether the user associated with the session is an administrator.
     *
     * @param sessionID The session to check.
     * @param throwUnauthorizedExceptionIfNot Whether or not to throw an exception if the
     * user is not an admin.
     * @return Whether the user associated with the session is an administrator.
     */
    public boolean isAdmin(String sessionID, boolean throwUnauthorizedExceptionIfNot) throws SQLException, RemoteException, SessionExpiredException, UnauthorizedException {
        boolean isAdmin = getSessionUsersLevel(sessionID) == UserLevel.ADMIN;
        if (isAdmin) {
            return true;
        } else {
            if (throwUnauthorizedExceptionIfNot) {
                throw new UnauthorizedException("You do not have administrative priviledges.");
            } else {
                return false;
            }
        }
    }
}
