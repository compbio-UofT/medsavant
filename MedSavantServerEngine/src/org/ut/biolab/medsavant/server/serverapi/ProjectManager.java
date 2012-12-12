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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantFormatTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.DBSettings;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.server.db.variants.VariantManager;
import org.ut.biolab.medsavant.server.db.variants.VariantManagerUtils;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;

/**
 *
 * @author mfiume
 */
public class ProjectManager extends MedSavantServerUnicastRemoteObject implements ProjectManagerAdapter {

    private static final Log LOG = LogFactory.getLog(ProjectManager.class);

    private static ProjectManager instance;


    private ProjectManager() throws RemoteException {
    }

    public static synchronized ProjectManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    @Override
    public String[] getProjectNames(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<String> result = new ArrayList<String>();

        while (rs.next()) {
            result.add(rs.getString(1));
        }

        return result.toArray(new String[0]);
    }

    @Override
    public boolean containsProject(String sid, String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addAllColumns();
        query.addFromTable(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        return rs.next();
    }

    @Override
    public int getProjectID(String sid, String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }

    @Override
    public void removeReferenceForProject(String sessID, int projID, int refID) throws SQLException {

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
            SelectQuery query1 = new SelectQuery();
            query1.addFromTable(table.getTable());
            query1.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
            query1.addCondition(ComboCondition.and(
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID),
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID)));

            ResultSet rs = conn.executeQuery(query1.toString());

            while (rs.next()) {
                String tableName = rs.getString(1);
                DBUtils.dropTable(sessID, tableName);
            }

            DeleteQuery query2 = new DeleteQuery(table.getTable());
            query2.addCondition(ComboCondition.and(
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID),
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID)));
            conn.executeUpdate(query2.toString());
        } finally {
            conn.close();
        }
    }

    @Override
    public String getProjectName(String sessID, int projID) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, query.toString());

        if (rs1.next()) {
            return rs1.getString(1);
        } else {
            return null;
        }
    }

    @Override
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging) throws RemoteException, SQLException {
        return createVariantTable(sessID, projID, refID, updID, annIDs, staging, false);
    }

    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging, boolean sub) throws RemoteException, SQLException {

        // Create basic fields.
        String tableName = DBSettings.getVariantTableName(projID, refID, updID);
        if (sub) {
            tableName += "_sub";
        }
        TableSchema variantSchema = new TableSchema(MedSavantDatabase.schema, tableName, BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        for (CustomField f: getCustomVariantFields(sessID, projID, refID, updID)) {
            variantSchema.addColumn(f);
        }

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            for (int ann: annIDs) {
                AnnotationFormat annFmt = AnnotationManager.getInstance().getAnnotationFormat(sessID, ann);
                for (CustomField f: annFmt.getCustomFields()) {
                    variantSchema.addColumn(f);
                }
            }
            String updateString;
            if (MedSavantServerEngine.USE_INFINIDB_ENGINE) {
                updateString = "";
            } else {
                updateString = variantSchema.getCreateQuery() + " ENGINE=INFINIDB;";
            }
            System.out.println(updateString);
            conn.executeUpdate(variantSchema.getCreateQuery() + " ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;");
        } finally {
            conn.close();
        }
        return tableName;
    }

    @Override
    public void addTableToMap(String sessID, int projID, int refID, int updID, boolean published, String tableName, int[] annotationIDs, String subTableName) throws SQLException, RemoteException{


        TableSchema variantTableMap = MedSavantDatabase.VarianttablemapTableSchema;
        InsertQuery query = new InsertQuery(variantTableMap.getTable());
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID);

        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), published);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), tableName);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);

        float subMultiplier = 1.0f;
        if (subTableName != null) {
            subMultiplier = getMultiplier(sessID, tableName, subTableName);
        }
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), subMultiplier);


        // add annotation ids
        //LOG.info("\n\n\n\n\n\n\n");
        //LOG.info(annotationIDs);
        if (annotationIDs.length > 0) {
            String annIDString = "";
            for (int id : annotationIDs) {
                annIDString += id + ",";
            }
            //LOG.info(annIDString);
            annIDString = annIDString.substring(0,annIDString.length()-1); // remove the last comma
            query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annIDString);
        }

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    /*
     * Get the name of the most up-to-date variant table, or the most up-to-date,
     * published variant table.
     */
    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published) throws SQLException {
        return getVariantTableName(sid, projectid, refid, published, false);
    }

    @Override
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn((sub ? VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME : VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME)));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            return rs.getString(1);
        } else {
            return null;
        }
    }

    public Object[] getVariantTableInfo(String sid, int projectid, int refid, boolean published) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME),
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME),
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            return new Object[]{rs.getString(1), rs.getString(2), rs.getFloat(3)};
        } else {
            return null;
        }
    }

    public void addSubsetInfoToMap(String sessID, int projID, int refID, int updID, String subTableName, float multiplier) throws SQLException{

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), multiplier);
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID));

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    private float getMultiplier(String sid, String table, String subTable) throws SQLException, RemoteException {
        int numerator = VariantManager.getInstance().getNumFilteredVariantsHelper(sid, table, new Condition[0][]);
        int denominator = VariantManager.getInstance().getNumFilteredVariantsHelper(sid, subTable, new Condition[0][]);
        if (denominator == 0) denominator = 1;
        return (float)numerator / (float)denominator;
    }

    @Override
    public int addProject(String sessID, String name, CustomField[] fields) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), name);

        Connection c = ConnectionController.connectPooled(sessID);
        PreparedStatement stmt = c.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int projID = res.getInt(1);

        PatientManager.getInstance().createPatientTable(sessID, projID, fields);

        c.close();
        return projID;
    }

    @Override
    public void removeProject(String sid, String projectName) throws SQLException, RemoteException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            removeProject(sid, rs.getInt(1));
        }
    }

    @Override
    public void removeProject(String sid, int projectid) throws SQLException, RemoteException {


        Connection c = ConnectionController.connectPooled(sid);

        TableSchema projectTable = MedSavantDatabase.ProjectTableSchema;
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;

        //remove from project table
        DeleteQuery q1 = new DeleteQuery(projectTable.getTable());
        q1.addCondition(BinaryConditionMS.equalTo(projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q1.toString());

        //remove patient table
        SelectQuery q2 = new SelectQuery();
        q2.addFromTable(patientMapTable.getTable());
        q2.addColumns(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        q2.addCondition(BinaryConditionMS.equalTo(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs1 = ConnectionController.executeQuery(sid, q2.toString());

        rs1.next();
        String patientTableName = rs1.getString(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME);
        c.createStatement().execute("DROP TABLE IF EXISTS " + patientTableName);

        //remove from patient format table
        DeleteQuery q3 = new DeleteQuery(patientFormatTable.getTable());
        q3.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q3.toString());

        //remove from patient tablemap
        DeleteQuery q4 = new DeleteQuery(patientMapTable.getTable());
        q4.addCondition(BinaryConditionMS.equalTo(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q4.toString());

        //remove variant tables
        SelectQuery q5 = new SelectQuery();
        q5.addFromTable(variantMapTable.getTable());
        q5.addColumns(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        q5.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs2 = ConnectionController.executeQuery(sid, q5.toString());

        while (rs2.next()) {
            String variantTableName = rs2.getString(1);
            c.createStatement().execute("DROP TABLE IF EXISTS " + variantTableName);
        }

        //remove from variant tablemap
        DeleteQuery q6 = new DeleteQuery(variantMapTable.getTable());
        q6.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q6.toString());

        //remove cohort entries
        int[] cohIDs = CohortManager.getInstance().getCohortIDs(sid, projectid);
        for (int cohID : cohIDs) {
            CohortManager.getInstance().removeCohort(sid, cohID);
        }

        c.close();
    }

    @Override
    public void setAnnotations(String sessID, int projID, int refID, int updID, String annIDs) throws SQLException {

        String tablename = getVariantTableName(sessID, projID, refID, true);

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annIDs);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), tablename)); //should only affect published table

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    @Override
    public ProjectDetails[] getProjectDetails(String sessID, int projID) throws SQLException {

        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(variantMapTable.getTable());
        query.addAllColumns();
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                variantMapTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        query.addOrdering(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Integer> refs = new ArrayList<Integer>();
        List<ProjectDetails> result = new ArrayList<ProjectDetails>();
        while (rs.next()) {
            if (!refs.contains(rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID))) { // should be only one per reference (most recent and published)

                // Annotation IDs are stored as a comma-delimited string.
                String annString = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS);
                int[] annIDs;
                if (annString != null) {
                    String[] anns = annString.split(",");
                    annIDs = new int[anns.length];
                    for (int i = 0; i < anns.length; i++) {
                        annIDs[i] = Integer.parseInt(anns[i]);
                    }
                } else {
                    annIDs = new int[0];
                }

                result.add(new ProjectDetails(
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID),
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID),
                        rs.getBoolean(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED),
                        null,
                        rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                        annIDs));
                refs.add(rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID));
            }
        }

        return result.toArray(new ProjectDetails[0]);
    }

    @Override
    public void renameProject(String sid, int projectId, String newName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), newName);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public void setCustomVariantFields(String sid, int projectId, int referenceId, int updateId, CustomField[] fields) throws SQLException {

        Connection c = ConnectionController.connectPooled(sid);
        TableSchema table = MedSavantDatabase.VariantformatTableSchema;

        c.setAutoCommit(false);
        for (int i = 0; i < fields.length; i++) {
            CustomField f = fields[i];
            InsertQuery insertQuery = new InsertQuery(table.getTable());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), f.getTypeString());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (f.isFilterable() ? "1" : "0"));
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_ALIAS), f.getAlias());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), f.getDescription());
            c.createStatement().execute(insertQuery.toString());
        }
        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    //Get the most up-to-date custom fields, as specified in variant_format table
    @Override
    public CustomField[] getCustomVariantFields(String sid, int projectId, int referenceId, int updateId) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        query.addOrdering(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<CustomField> result = new ArrayList<CustomField>();
        while (rs.next()) {
            result.add(new CustomField(
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME).toLowerCase(),
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                    rs.getBoolean(VariantFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_ALIAS),
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }
        return result.toArray(new CustomField[0]);
    }

    @Override
    public int getNewestUpdateID(String sid, int projectId, int referenceId, boolean published) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        return rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID);
    }

    public void publishVariantTable(PooledConnection conn, int projID, int refID, int updID) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID));

        conn.executeUpdate(query.toString());
    }

    @Override
    public ProjectDetails[] getUnpublishedChanges(String sessID) throws SQLException {

        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema projectTable = MedSavantDatabase.ProjectTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(variantMapTable.getTable());
        query.addAliasedColumn(projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), "A");
        query.addAliasedColumn(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME), "B");
        query.addAllTableColumns(variantMapTable.getTable());
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                variantMapTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addJoin(
                SelectQuery.JoinType.LEFT_OUTER,
                variantMapTable.getTable(),
                projectTable.getTable(),
                BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectTable.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID)));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        Map<Integer, Map<Integer, ProjectDetails>> map = new HashMap<Integer, Map<Integer, ProjectDetails>>();

        List<ProjectDetails> unpublished = new ArrayList<ProjectDetails>();
        while (rs.next()) {
            int projID = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID);
            int refID = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID);
            int updID = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID);
            boolean published = rs.getBoolean(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED);

            if (!map.containsKey(projID)) {
                map.put(projID, new HashMap<Integer, ProjectDetails>());
            }

            if (!map.get(projID).containsKey(refID) || map.get(projID).get(refID).getUpdateID() < updID) {
                ProjectDetails pd = new ProjectDetails(projID, refID, updID, published, rs.getString("A"), rs.getString("B"), null);
                map.get(projID).put(refID, pd);
                if (!pd.isPublished()) {
                    unpublished.add(pd);
                }
            }
        }

        return unpublished.toArray(new ProjectDetails[0]);
    }

    @Override
    public String[] getReferenceNamesForProject(String sessID, int projID) throws SQLException {

        TableSchema refTable = MedSavantDatabase.ReferenceTableSchema;
        TableSchema variantMapTable = MedSavantDatabase.VarianttablemapTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(variantMapTable.getTable());
        query.addColumns(refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_NAME));
        query.addJoin(SelectQuery.JoinType.LEFT_OUTER,
                variantMapTable.getTable(),
                refTable.getTable(),
                BinaryConditionMS.equalTo(
                        refTable.getDBColumn(ReferenceTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID)));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<String> references = new ArrayList<String>();
        while (rs.next()) {
            if (!references.contains(rs.getString(1))){
                references.add(rs.getString(1));
            }
        }

        return references.toArray(new String[0]);
    }

    @Override
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Integer> references = new ArrayList<Integer>();
        while (rs.next()) {
            references.add(rs.getInt(1));
        }

        return ArrayUtils.toPrimitive(references.toArray(new Integer[0]));
    }


    /**
     * Remove any variant tables with updateMin <= updateId <= updateMax
     */
    public void removeTables(String sessID, int projID, int refID, int updateMax, int updateMin) throws SQLException {

        //find update ids
        TableSchema mapTable = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(mapTable.getTable());
        query.addColumns(
                mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID),
                mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        query.addCondition(ComboCondition.and(
                BinaryCondition.lessThan(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateMax, true),
                BinaryCondition.greaterThan(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateMin, true)));

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            while (rs.next()) {
                int updateId = rs.getInt(1);
                String tableName = rs.getString(2);

                //remove variant table
                VariantManagerUtils.dropTableIfExists(sessID, tableName);

                //remove from variant tablemap
                DeleteQuery dq1 = new DeleteQuery(mapTable.getTable());
                dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
                dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
                dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
                conn.executeUpdate(dq1.toString());

                //remove from variant format
                TableSchema formatTable = MedSavantDatabase.VariantformatTableSchema;
                DeleteQuery dq2 = new DeleteQuery(formatTable.getTable());
                dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
                dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
                dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
            }
        } finally {
            conn.close();
        }
    }
}
