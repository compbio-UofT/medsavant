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
package org.ut.biolab.medsavant.client.app.api;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.user.UserController;
import org.ut.biolab.medsavant.client.user.UserEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.format.UserRole;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.UserLevel;

/**
 *
 * @author jim
 */
public class AppRoleManagerBuilder {

    private static Log LOG = LogFactory.getLog(AppRoleManagerBuilder.class);
    
    private class RoleInfo {

        private final String shortname;
        private final String description;
        private final Set<UserLevel> userlevels;

        public RoleInfo(String shortname, String description, Set<UserLevel> userlevels) {
            this.shortname = shortname;
            this.description = description;
            this.userlevels = userlevels;
        }
    }

    private final List<RoleInfo> roleInfo = new ArrayList<RoleInfo>();
    private final Set<String> defaultRoleNames = new HashSet<String>();
    private boolean assignRolesByUserLevel = true;
    //Adds a 'default' role -- i.e. a role that everyone is assumed to have, even if not explicitly 
    //set in the database.
    public AppRoleManagerBuilder addDefaultRole(String shortName, String description, Set<UserLevel> userlevels) {
        RoleInfo ri = new RoleInfo(shortName, description, userlevels);
        roleInfo.add(ri);
        defaultRoleNames.add(shortName);
        return this;
    }

    public AppRoleManagerBuilder addRole(String shortName, String description, Set<UserLevel> userlevels) {
        roleInfo.add(new RoleInfo(shortName, description, userlevels));
        return this;
    }

    public AppRoleManagerBuilder autoAssignRolesToExistingUsers(boolean f){
        assignRolesByUserLevel = f;
        return this;
    }
    
    public AppRoleManager build() throws SQLException, SessionExpiredException, RemoteException, SecurityException, AppNotInitializedException {
        return new AppRoleManager(defaultRoleNames);
    }

    public class AppRoleManager implements Listener<UserEvent> {

        //Maps each userlevel to the set of roles associated to that user level by default.
        private final Map<UserLevel, Set<UserRole>> userLevelRoleMap = new EnumMap<UserLevel, Set<UserRole>>(UserLevel.class);
        private final Set<UserRole> rolesForThisUser;
        private final Set<String> defaultRoleNames;

        private AppRoleManager(Set<String> defaultRoleNames) throws SQLException, SessionExpiredException, RemoteException, SecurityException, AppNotInitializedException {
            boolean allRolesWereNew = false;
            if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) {
                allRolesWereNew = createRoles();
            }
            initRoleMaps();
            if (assignRolesByUserLevel && allRolesWereNew) {
                String[] usersForThisDatabase = MedSavantClient.UserManager.getUserNames(LoginController.getSessionID());
                for (String user : usersForThisDatabase) {
                    UserLevel l = MedSavantClient.UserManager.getUserLevel(LoginController.getSessionID(), user);
                    setRoleForUser(user, l); //NO!  Requires userLevelRoleMap to be initialized!
                }
            }
            rolesForThisUser = MedSavantClient.UserManager.getRolesForUser(LoginController.getSessionID());
            this.defaultRoleNames = defaultRoleNames;
            UserController.getInstance().addListener(this);

        }

        private boolean createRoles() throws SQLException, SessionExpiredException, RemoteException, SecurityException {
            int n = 0;
            for (RoleInfo ri : roleInfo) {
                UserRole r = MedSavantClient.UserManager.getRoleByName(LoginController.getSessionID(), ri.shortname);
                //If the role already exists, do nothing.  Otherwise, create the role, and upgrade all 
                //existing users based on their userlevel to the new role.
                if (r == null) {
                    MedSavantClient.UserManager.addRole(LoginController.getSessionID(), ri.shortname, ri.description);
                    n++;
                }
            }
            if(assignRolesByUserLevel && (n > 0 && n < roleInfo.size())){
                LOG.info("Some roles used by an App already existed, and some did not.  Users will not be autoassigned roles based on their userlevel.");
            }
            //if n==0, all the roles needed by this app already exist.  In this case, do not auto-assign existing users to roles            
            return (n == roleInfo.size());
        }

        private void initRoleMaps() throws AppNotInitializedException, SQLException, SessionExpiredException, RemoteException, SecurityException {
            Map<String, RoleInfo> appRoleMap = new HashMap<String, RoleInfo>();
            for (RoleInfo ri : roleInfo) {
                appRoleMap.put(ri.shortname, ri);
            }

            Set<UserRole> allRoles = MedSavantClient.UserManager.getAllRoles(LoginController.getSessionID());
            for (UserRole role : allRoles) {
                //make sure we have a role for every role added in the builder.
                if (appRoleMap.keySet().contains(role.getRoleName())) {

                    RoleInfo ri = appRoleMap.get(role.getRoleName());
                    if (ri.userlevels != null) {
                        for (UserLevel l : ri.userlevels) {
                            Set<UserRole> roleSet = userLevelRoleMap.get(l);
                            if (roleSet == null) {
                                roleSet = new HashSet<UserRole>();
                            }
                            roleSet.add(role);
                            userLevelRoleMap.put(l, roleSet);
                        }
                    }
                    appRoleMap.remove(role.getRoleName());
                }
            }
            if (appRoleMap.size() > 0) {
                throw new AppNotInitializedException("Cannot initialize role manager for app -- roles missing from database: " + StringUtils.join(appRoleMap.keySet().iterator(), ","));
            }

        }

        private void setRoleForUser(String user, UserLevel l) {
            //These events should only ever be triggered by admins.
            if (LoginController.getInstance().getUserLevel() == UserLevel.ADMIN) { //assert                           
                if (l == UserLevel.ADMIN || l == UserLevel.GUEST || l == UserLevel.USER) {
                    try {
                        clearRolesForUser(user);
                        Set<UserRole> rolesToRegister = userLevelRoleMap.get(l);
                        if (rolesToRegister != null) {
                            MedSavantClient.UserManager.registerRoleForUser(LoginController.getSessionID(), user, rolesToRegister);
                        }
                    } catch (Exception ex) {
                        LOG.error("Unable to register new role for user ", ex);
                    }
                } else {
                    LOG.error("Unexpected user level " + l + ". " + this.getClass().getName() + " will not modify user roles.");
                }
            } else {
                LOG.error("Unexpected user change event from non-admin user");
            }
        }

        @Override
        public void handleEvent(UserEvent event) {
            //These events should only ever be triggered by admins.
            if(event.getType() == UserEvent.Type.ADDED){
                setRoleForUser(event.getName(), event.getUserLevel());
            }else if(event.getType() == UserEvent.Type.REMOVED){
                try{
                    clearRolesForUser(event.getName());
                }catch(Exception ex){
                    //If we can't clear the roles for a user
                    LOG.error(ex); 
                }
            }else{
                LOG.error("Unexpected UserEvent type "+event.getType());
            }
        }

        //clears all roles used by this app that are associated to this user.
        private void clearRolesForUser(String username) throws RemoteException, SQLException, SessionExpiredException, SecurityException {
            for (Set<UserRole> roles : userLevelRoleMap.values()) {
                MedSavantClient.UserManager.dropRolesForUser(LoginController.getSessionID(), username, roles);
            }
        }

        public boolean checkRole(String roleName) {
            if (defaultRoleNames.contains(roleName)) {
                return true;
            }

            for (UserRole role : rolesForThisUser) {
                if (role.getRoleName().equals(roleName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkRole(UserRole r) {
            if (defaultRoleNames.contains(r.getRoleName())) {
                return true;
            }
            for (UserRole role : rolesForThisUser) {
                if (role.equals(r)) {
                    return true;
                }
            }
            return false;
        }
    }
}
