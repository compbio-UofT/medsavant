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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.SettingsTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.db.Settings;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.Setting;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.SettingsManagerAdapter;


/**
 *
 * @author Andrew
 */
public class SettingsManager extends MedSavantServerUnicastRemoteObject implements SettingsManagerAdapter {
    private static SettingsManager instance;
    private boolean lockReleased = false;
    private static QueryManager queryManager;
    private static EntityManager entityManager;
    private static final Log LOG = LogFactory.getLog(SettingsManager.class);

    private SettingsManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    public static synchronized SettingsManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    @Override
    public void addSetting(String sid, String key, String value) throws SQLException, SessionExpiredException {
        Setting setting = new Setting(key, value);
        try {
            entityManager.persist(setting);
        } catch (InitializationException e) {
            LOG.error("Error persisting setting");
        }
    }

    @Override
    public String getSetting(String sid, String key) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select s.value from Setting s where s.key= :key");
        query.setParameter("key", "\"" + key + "\"" );
        List<ResultRow> resultRowList = query.executeForRows();

        String result = null;
        if (resultRowList.size() > 0) {
            result = String.valueOf(resultRowList.get(0).getObject("value"));
        }

        return result;
    }

    @Override
    public void updateSetting(String sessID, String key, String value) throws SQLException, SessionExpiredException {
        updateSetting(key, value);
    }

    private void updateSetting(String key, String value) {
        Setting setting = new Setting(key, value);
        try {
            entityManager.persist(setting);
        } catch (InitializationException e) {
            LOG.error("Error persisting setting");
        }
    }

    private void updateSetting(Connection conn, String key, String value) throws SQLException, SessionExpiredException {
        updateSetting(key, value);
    }

    @Override
    public synchronized boolean getDBLock(String sessID) throws SQLException, SessionExpiredException {

        System.out.print(sessID + " getting lock");

        String value = getSetting(sessID, Settings.KEY_DB_LOCK);
        if (Boolean.parseBoolean(value) && !lockReleased) {
            System.out.println(" - FAILED");
            return false;
        }
        updateSetting(sessID, Settings.KEY_DB_LOCK, Boolean.toString(true));
        System.out.println(" - SUCCESS");
        lockReleased = false;
        return true;
    }

    public synchronized void releaseDBLock(Connection conn) throws SQLException, SessionExpiredException {
        System.out.println("Server releasing lock");
        try {
            updateSetting(conn, Settings.KEY_DB_LOCK, Boolean.toString(false));
        } finally {
            lockReleased = true;
        }
    }

    @Override
    public void releaseDBLock(String sessID) throws SQLException, SessionExpiredException {
        Connection conn = ConnectionController.connectPooled(sessID);
        if (conn != null) {
            try {
                releaseDBLock(conn);
            } finally {
                conn.close();
            }
        }
    }
}
