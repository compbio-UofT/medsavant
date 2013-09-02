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

package org.ut.biolab.medsavant.server.serverapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.util.PersistenceUtil;
import org.ut.biolab.medsavant.server.mail.CryptoUtils;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.User;
import org.ut.biolab.medsavant.shared.model.UserLevel;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.serverapi.UserManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author mfiume
 */
public class UserManager extends MedSavantServerUnicastRemoteObject implements UserManagerAdapter {

    private static final Log LOG = LogFactory.getLog(UserManager.class);

    private static UserManager instance;
    private static EntityManager entityManager;
    private static QueryManager queryManager;

    public static synchronized UserManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private UserManager() throws RemoteException, SessionExpiredException {
        entityManager = EntityManagerFactory.getEntityManager();
        queryManager = QueryManagerFactory.getQueryManager();
    }
    
    @Override
    public String[] getUserNames(String sessID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select u.name from User u");

        List<String> results = new ArrayList<String>();
        List<User> userList = query.execute();
        for (User u : userList) {
            results.add(u.getName());
        }

        return results.toArray(new String[0]);
    }

    @Override
    public boolean userExists(String sessID, String user) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select u from User u");

        return (query.execute().size() > 0 ) ? true : false;
    }

    @Override
    public boolean tryLoginUser(String username, String encryptedPassword) throws RemoteException {
        Query query = queryManager.createQuery("Select u from User u where u.name = :name and u.password = :password");
        query.setParameter("name", username);
        query.setParameter("password", encryptedPassword);

        return (query.execute().size() > 0 ) ? true : false;
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

        String encryptedPassword = CryptoUtils.encrypt(new String(pass));
        User newUser = new User(user, encryptedPassword, level);
        try {
            entityManager.persist(newUser);
            PersistenceUtil.addUser(sessID, user, pass, level);
        } catch (InitializationException e) {
            LOG.error("Error adding new user");
        }
    }

    /**
     * Grant the user the privileges appropriate to their level
     * @param name user name from <code>mysql.user</code> table
     * @param level ADMIN, USER, or GUEST
     * @throws SQLException
     */
    @Override
    public void grantPrivileges(String sessID, String name, UserLevel level) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Update User u set u.level = :level where u.name = :name");
        query.setParameter("name", name);
        query.setParameter("level", level);
        query.executeUpdate();
        PersistenceUtil.grantPrivileges(sessID,name,level);
    }

    @Override
    public UserLevel getUserLevel(String sessID, String name) throws SQLException, SessionExpiredException {
        UserLevel level = UserLevel.NONE;

        Query query = queryManager.createQuery("Select u from User u");
        User user = query.getFirst();
        if (user != null) {
            level = user.getLevel();
        }

        return level;
    }

    @Override
    public void removeUser(String sid, String name) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Delete from User u where u.name = :name");
        query.setParameter("user", name);
        query.executeDelete();
        PersistenceUtil.removeUser(sid, name);
    }
}
