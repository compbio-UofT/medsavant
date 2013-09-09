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
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.model.AnnotationLog;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Action;
import org.ut.biolab.medsavant.shared.model.AnnotationLog.Status;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.Entity;
import org.ut.biolab.medsavant.shared.util.SQLUtils;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author Andrew
 */
public class AnnotationLogManager {

    private static final Log LOG = LogFactory.getLog(AnnotationLogManager.class);

    private static AnnotationLogManager instance;
    private static EntityManager entityManager = EntityManagerFactory.getEntityManager();
    private static QueryManager queryManager = QueryManagerFactory.getQueryManager();

    public static synchronized AnnotationLogManager getInstance() {
        if (instance == null) {
            instance = new AnnotationLogManager();
        }
        return instance;
    }

    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action) throws SQLException, RemoteException, SessionExpiredException {
        return addAnnotationLogEntry(sid,projectId,referenceId,action,Status.STARTED);
    }

    public int addAnnotationLogEntry(String sid,int projectId, int referenceId, Action action, Status status) throws SQLException, RemoteException, SessionExpiredException {

        String projectName = ProjectManager.getInstance().getProjectName(sid, projectId);
        String referenceName = ReferenceManager.getInstance().getReferenceName(sid,referenceId);
        String user = SessionController.getInstance().getUserForSession(sid);
        Timestamp sqlDate = SQLUtils.getCurrentTimestamp();

        int updateId = DBUtils.generateId("upload_id", Entity.ANNOTATION_LOG);
        AnnotationLog annotationLog = new AnnotationLog(projectName, referenceName, action, status,sqlDate,user, updateId);

        try {
            entityManager.persist(annotationLog);
        } catch (InitializationException e) {
            LOG.error("Error adding annotation log entry");
        }
        return updateId;
    }

    public void removeAnnotationLogEntry(String sid,int updateId) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Delete from AnnotationLog l where l.upload_id = :updateId");
        query.setParameter("updateId", updateId);
        query.executeDelete();
    }

    public void setAnnotationLogStatus(String sid,int updateId, Status status) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Update AnnotationLog l set l.status = :status where l.upload_id = :updateId");
        query.setParameter("status", status);
        query.setParameter("updateId", updateId);
        query.executeUpdate();
    }

    public void setAnnotationLogStatus(String sid,int updateId, Status status, Timestamp sqlDate) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Update AnnotationLog l set l.status = :status, l.timestamp = :timestamp where l.upload_id = :updateId");
        query.setParameter("status", status);
        query.setParameter("timestamp", sqlDate);
        query.setParameter("updateId", updateId);
        query.executeUpdate();
    }
}
