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
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.util.PersistenceUtil;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.CustomColumn;
import org.ut.biolab.medsavant.shared.model.CustomColumnType;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.CustomFieldManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.persistence.solr.SolrCustomFieldManager;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class ProjectManager extends MedSavantServerUnicastRemoteObject implements ProjectManagerAdapter {

    private static final Log LOG = LogFactory.getLog(ProjectManager.class);

    private static ProjectManager instance;
    private static QueryManager queryManager;
    private static EntityManager entityManager;
    private static CustomFieldManager customFieldManager;


    private ProjectManager() throws RemoteException, SessionExpiredException {
        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
        customFieldManager = new SolrCustomFieldManager();
    }

    public static synchronized ProjectManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    @Override
    public String[] getProjectNames(String sid) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p.name from Project p");
        List<ResultRow> resultRowList = query.executeForRows();

        List<String> results = new ArrayList<String>();
        for (ResultRow resultRow : resultRowList) {
            results.add(String.valueOf(resultRow.getObject("name")));
        }

        return results.toArray(new String[0]);
    }

    @Override
    public boolean containsProject(String sid, String projectName) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project p where p.name= :name");
        query.setParameter("name", projectName);
        List<ResultRow> resultRows = query.executeForRows();

        boolean exists = (resultRows.size() > 0 ? true : false);

        return exists;
    }

    @Override
    public int getProjectID(String sid, String projectName) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select p.project_id from Project p where p.name= :name");
        query.setParameter("name", projectName);
        List<ResultRow> resultRows = query.executeForRows();

        if (resultRows.size() > 0) {
            return (Integer ) (resultRows.get(0).getObject("project_id"));
        } else {
            return -1;
        }
    }

    @Override
    public void removeReferenceForProject(String sessID, int projID, int refID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Delete from Variant v where v.project_id= :projectId and v.ref_id= :refId ");
        query.setParameter("projectId", projID);
        query.setParameter("refId", refID);
        query.executeDelete();
    }

    @Override
    public String getProjectName(String sessID, int projID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p.name from Project p where p.project_id = :projectId");
        query.setParameter("projectId", projID);
        return (String) query.getFirstRow().getObject("name");
    }

    @Override
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging) throws RemoteException, SQLException, SessionExpiredException {
        return PersistenceUtil.createVariantTable(sessID, projID, refID, updID, annIDs, staging, false);
    }

    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) throws RemoteException, SQLException, SessionExpiredException {
        return PersistenceUtil.createVariantTable(sessID, projID, refID, updID, annIDs, staging, sub);
    }

    @Override
    public void addTableToMap(String sessID, int projID, int refID, int updID, boolean published, String tableName, int[] annotationIDs, String subTableName) throws SQLException, RemoteException, SessionExpiredException{
        //Todo add reference + annotation info to project now  - not used by solr
        //Cannot update, since annotation ids is a multi valued field
        Query query = queryManager.createQuery("Select p from Project p where p.project_id = :projectId");
        query.setParameter("projectId", projID);

        String referenceName = ReferenceManager.getInstance().getReferenceName(sessID, refID);

        Query updateQuery = queryManager.createQuery("Update Project p set p.reference_id = :referenceId, " +
                "p.reference_name = :referenceName, p.update_id = :updateId, p.annotation_ids = :annotationIds " +
                "where p.project_id = :projectId");
        updateQuery.setParameter("projectId", projID);
        updateQuery.setParameter("referenceId", refID);
        updateQuery.setParameter("referenceName", referenceName);
        updateQuery.setParameter("updateId", updID);
        updateQuery.setParameter("annotationIds", annotationIDs);

        updateQuery.executeUpdate();
    }

    /*
     * Get the name of the most up-to-date variant table, or the most up-to-date,
     * published variant table.
     */
    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {
        return getVariantTableName(sid, projectid, refid, published, false);
    }

    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException {
        //Todo return Entity.VARIANT
        return PersistenceUtil.getVariantTableName(sid, projectid, refid, published, sub);
    }

    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {
        //FIXME implement
        //return new Object[] { "Variant" , "Variant" , "Variant" }
        return PersistenceUtil.getVariantTableInfo(sid, projectid, refid, published);

    }

    public void addSubsetInfoToMap(String sessID, int projID, int refID, int updID, String subTableName, float multiplier) throws SQLException, SessionExpiredException{
        //no longer used
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), multiplier);
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID));

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    private float getMultiplier(String sid, String table, String subTable) throws SQLException, RemoteException, SessionExpiredException {
        int numerator = VariantManager.getInstance().getNumFilteredVariantsHelper( new Condition[0][]);
        int denominator = VariantManager.getInstance().getNumFilteredVariantsHelper(new Condition[0][]);
        if (denominator == 0) denominator = 1;
        return (float)numerator / (float)denominator;
    }

    @Override
    public int addProject(String sessID, String name, CustomField[] fields) throws SQLException, RemoteException, SessionExpiredException {
        //Todo custom fields?
        ProjectDetails projectDetails = new ProjectDetails();

        int projectId = DBUtils.generateId("project_id", "Project");
        projectDetails.setProjectID(projectId);
        projectDetails.setProjectName(name);
        try {
            entityManager.persist(projectDetails);
        } catch (InitializationException e) {
            LOG.error("Error persisting project");
        }
        return projectId;
    }

    @Override
    public void removeProject(String sid, String projectName) throws SQLException, RemoteException, SessionExpiredException {

        int projectId = getProjectID(sid, projectName);
        removeProject(sid, projectId);
    }

    @Override
    public void removeProject(String sid, int projectid) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Delete from Project p where p.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

        query = queryManager.createQuery("Delete from Patient p where p.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

        query = queryManager.createQuery("Delete from Cohort c where c.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

        query = queryManager.createQuery("Delete from Variant v where v.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

        query = queryManager.createQuery("Delete from Comment c where c.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

        query = queryManager.createQuery("Delete from Variant_file v where v.project_id= :projectId");
        query.setParameter("projectId", projectid);
        query.executeDelete();

    }

    @Override
    public void setAnnotations(String sessID, int projID, int refID, int updID, String annIDs) throws SQLException, SessionExpiredException {
        //Todo
        Query query = queryManager.createQuery("Select p from Project p where p.project_id = :projectId AND " +
                "p.reference_id := referenceId");
        query.setParameter("projectId", projID);
        query.setParameter("referenceId", refID);

        ProjectDetails project = query.getFirst();

        Integer[] annotationIds = Arrays.copyOf(annIDs.split(","), annIDs.length(), Integer[].class);
        project.setAnnotationIDs(ArrayUtils.toPrimitive(annotationIds));

        try {
            entityManager.persist(project);
        } catch (InitializationException e) {
            LOG.error("Error persisting annotations");
        }
    }

    @Override
    public ProjectDetails[] getProjectDetails(String sessID, int projID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project p where p.project_id= :projectId");
        query.setParameter("projectId", projID);
        List<ProjectDetails> results = query.execute();

        return results.toArray(new ProjectDetails[results.size()]);

    }

    @Override
    public void renameProject(String sid, int projectId, String newName) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project p where p.project_id= :projectId");
        query.setParameter("projectId", projectId);
        List<ProjectDetails> results = query.execute();

        for (ProjectDetails projectDetails : results) {
            projectDetails.setProjectName(newName);
            try {
                entityManager.persist(projectDetails);
            } catch (InitializationException e) {
                LOG.error("Failed to persist project");
            }
        }
    }

    @Override
    public void setCustomVariantFields(String sid, int projectId, int referenceId, int updateId, CustomField[] fields) throws SQLException, SessionExpiredException, IOException, InitializationException, URISyntaxException {
        //Todo implement this

        List<CustomField> customColumnList = new ArrayList<CustomField>();
        ProjectDetails project =  ProjectManager.getInstance().getProjectDetails(sid, projectId)[0];
        int i = 0;
        for (CustomField customField : fields) {
            customColumnList.add(new CustomColumn(customField, project, CustomColumnType.VARIANT,i));
            i++;
        }

        entityManager.persistAll(customColumnList);
        customFieldManager.addCustomFields(customColumnList);
    }

    //Get the most up-to-date custom fields, as specified in variant_format table
    @Override
    public CustomField[] getCustomVariantFields(String sid, int projectId, int referenceId, int updateId) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select c from CustomColumn c where c.entity_name = :entityName and " +
                "c.project_id = :projectId and c.reference_id = :referenceId and c.update_id = :updateId");
        query.setParameter("entityName", CustomColumnType.VARIANT.name());
        query.setParameter("projectId", projectId);
        query.setParameter("referenceId", referenceId);
        query.setParameter("updateId", updateId);

        List<CustomColumn> results = query.execute();

        return results.toArray(new CustomField[0]);
    }

    @Override
    public int getNewestUpdateID(String sid, int projectId, int referenceId, boolean published) throws SQLException, SessionExpiredException {
        Query query = queryManager.createQuery("Select max(v.update_id) from Variant v " +
                "where v.project_id = :projectId and v.reference_id = :referenceId and v.published = :published");
        query.setParameter("projectId", projectId);
        query.setParameter("referenceId", referenceId);
        query.setParameter("published", published);
        ResultRow result = query.getFirstRow();
        return (Integer) result.getObject("max");
    }

    public void publishVariantTable(PooledConnection conn, int projID, int refID, int updID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Update Project p set p.published = :published " +
                "where p.project_id := projectId and p.reference_id = :referenceId and p.update_id = :updateId");
        query.setParameter("published", true);
        query.setParameter("projectId", projID);
        query.setParameter("referenceId", refID);
        query.setParameter("updateId", updID);
        query.executeUpdate();
    }

    @Override
    public ProjectDetails[] getUnpublishedChanges(String sessID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project p where p.published = :published");
        query.setParameter("published", true);

        return query.execute().toArray(new ProjectDetails[0]);
    }

    @Override
    public String[] getReferenceNamesForProject(String sessID, int projID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project p where p.project_id = :projectId");
        query.setParameter("projectId", projID);

        List<ProjectDetails> projectDetailsList = query.execute();

        List<String> referenceNames = new ArrayList<String>();
        for (ProjectDetails p : projectDetailsList) {
           referenceNames.add(p.getReferenceName());
        }

        return referenceNames.toArray(new String[0]);
    }

    @Override
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select p from Project where p.project_id = :projectId");
        query.setParameter("projectId", projID);

        List<ProjectDetails> projectDetailsList = query.execute();

        int[] referenceIds = new int[projectDetailsList.size()];

        int i = 0;
        for (ProjectDetails p : projectDetailsList) {
            referenceIds[i++] = p.getProjectID();
        }

        return referenceIds;
    }

    /**
     * Remove any variant tables with updateMin <= updateId <= updateMax
     */
    public void removeTables(String sessID, int projID, int refID, int updateMax, int updateMin) throws SQLException, SessionExpiredException {
        //ToDo table remove functionality does not belong to the abstraction layer, but rather to a persistence implementation
        //until then, just delete all variants with update_id between the 2 values

        Query query = queryManager.createQuery("Delete from Variant v where v.upload_id between :updateMin and :updateMax");
        query.setParameter("updateMin", updateMin);
        query.setParameter("updateMax", updateMax);
        query.executeDelete();
    }
}
