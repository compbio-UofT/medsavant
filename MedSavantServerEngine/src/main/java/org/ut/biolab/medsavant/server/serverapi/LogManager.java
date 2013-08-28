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

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ServerLogTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.serverapi.LogManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.shared.util.Entity;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;


/**
 *
 * @author mfiume
 */
public class LogManager extends MedSavantServerUnicastRemoteObject implements LogManagerAdapter {

    private static final Log LOG = LogFactory.getLog(LogManager.class);
    private static final String SERVER_UNAME = "server";

    private static LogManager instance;
    private static EntityManager entityManager;
    private static QueryManager queryManager;

    private LogManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    public static synchronized LogManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    @Override
    public List<GeneralLog> getClientLog(String sid, int start, int limit) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select l from GeneralLog l where l.user <> :user order by l.timestamp");
        query.setParameter("user", "server");
        query.setStart(start);
        query.setLimit(limit);
        List<GeneralLog> generalLogList = query.execute();
        return generalLogList;
    }

    @Override
    public List<GeneralLog> getServerLog(String sid, int start, int limit) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select l from GeneralLog l where l.user= :user order by l.timestamp");
        query.setParameter("user", "server");
        query.setStart(start);
        query.setLimit(limit);
        List<GeneralLog> generalLogList = query.execute();
        return generalLogList;
    }

    @Override
    public List<AnnotationLog> getAnnotationLog(String sid, int start, int limit) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select l from AnnotationLog l where l.user= :user order by l.timestamp");
        query.setStart(start);
        query.setLimit(limit);
        List<AnnotationLog> annotationLogList = query.execute();
        return annotationLogList;

    }

    @Override
    public int getAnnotationLogSize(String sid) throws SQLException, SessionExpiredException {
        return getLogSize(sid,Entity.ANNOTATION_LOG, null);
    }

    @Override
    public int getServerLogSize(String sid) throws SQLException, SessionExpiredException {
        return getLogSize(sid,Entity.GENERAL_LOG, BinaryConditionMS.equalTo("user", SERVER_UNAME));
    }

    @Override
    public int getClientLogSize(String sid) throws SQLException, SessionExpiredException {
        return getLogSize(sid, Entity.GENERAL_LOG, BinaryCondition.notEqualTo("user", SERVER_UNAME));
    }

    private static int getLogSize(String sid, String entity, Condition c) throws SQLException, SessionExpiredException {
        StringBuilder statement =  new StringBuilder(String.format("Select l from %s l", entity));
        if (c != null) {
            statement.append(c);
        }
        Query query = queryManager.createQuery(statement.toString());
        return (int) query.count();
    }

    @Override
    public void addServerLog(String sid, LogType t, String description) throws SessionExpiredException {
        addLog(sid,SERVER_UNAME, t, description);
    }

    @Override
    public void addLog(String sessID, String user, LogType type, String desc) throws SessionExpiredException {

        Timestamp sqlDate = new java.sql.Timestamp((new java.util.Date()).getTime());
        GeneralLog generalLog = new GeneralLog(user, type.toString(), desc,sqlDate );
        try {
            entityManager.persist(generalLog);
        } catch (InitializationException e) {
            LOG.error("Error persisting general log");
        }
    }

    @Override
    public Date getDateOfLastServerLog(String sid) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select l from GeneralLog where l.user= :user order by l.timestamp");
        query.setParameter("user", "server");
        query.setStart(0);
        query.setLimit(1);
        List<GeneralLog> generalLogList = query.execute();
        return new Date(generalLogList.get(0).getTimestamp().getTime());
    }}
