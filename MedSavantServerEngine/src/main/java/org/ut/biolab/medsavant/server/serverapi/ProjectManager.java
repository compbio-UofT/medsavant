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
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.ut.biolab.medsavant.server.db.variants.VariantManagerUtils;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.ProjectDetails;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.db.admin.SetupMedSavantDatabase;
import org.ut.biolab.medsavant.shared.model.AnnotationDownloadInformation;
import org.ut.biolab.medsavant.shared.model.Reference;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.ProjectManagerAdapter;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.VersionSettings;

/**
 *
 * @author mfiume
 */
public class ProjectManager extends MedSavantServerUnicastRemoteObject implements ProjectManagerAdapter {

    private static final Log LOG = LogFactory.getLog(ProjectManager.class);
    private static ProjectManager instance;

    private ProjectManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized ProjectManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    //This method will be removed from the public API. API users do not need to know database details.
    @Override    
    @Deprecated
    public String getVariantTableName(String sessID, int updateID, int projectid, int refid) throws SQLException, SessionExpiredException, RemoteException {
        return DBSettings.getVariantViewName(projectid, refid);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getProjectNames(String sid) throws SQLException, SessionExpiredException {

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
    public boolean containsProject(String sid, String projectName) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addAllColumns();
        query.addFromTable(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        return rs.next();
    }

    @Override
    public int getProjectID(String sid, String projectName) throws SQLException, SessionExpiredException {

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
    public void removeReferenceForProject(String sessID, int projID, int refID) throws SQLException, SessionExpiredException {

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

            table = MedSavantDatabase.VariantformatTableSchema;
            query2 = new DeleteQuery(table.getTable());
            query2.addCondition(ComboCondition.and(
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID),
                    BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID)));
            conn.executeUpdate(query2.toString());
        } finally {
            conn.close();
        }
    }

    @Override
    public String getProjectName(String sessID, int projID) throws SQLException, SessionExpiredException {

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
    public int[] getDefaultAnnotationIDs(String sessID, int projID, int refID) throws RemoteException, SQLException, SessionExpiredException {
        List<Integer> defaultAnnIds = new ArrayList<Integer>();
        Reference[] refs = ReferenceManager.getInstance().getReferences(sessID);
        String refStr = null;
        for (Reference ref : refs) {
            if (ref.getID() == refID) {
                refStr = ref.getName();
                break;
            }
        }
        if (refStr == null) { //can't find reference in database.
            LOG.error("Reference id " + refID + " is invalid");
            throw new SQLException("Reference id " + refID + " is invalid");
        }
        try {
            List<AnnotationDownloadInformation> annotations = AnnotationDownloadInformation.getDownloadableAnnotations(VersionSettings.getVersionString(), refStr);
            for (AnnotationDownloadInformation adi : annotations) {
                if (adi.isDefault()) {
                    int id = AnnotationManager.getInstance().doInstallAnnotationForProject(sessID, projID, adi);
                    if (id == -1) {
                        LOG.error("Couldn't install annotation " + adi + " for project " + projID);
                    }
                    defaultAnnIds.add(id);
                    //break; //DEBUG CODE!  ONLY ALLOWS ONE ANNOTATION TO BE INSTALLED!
                }
            }
        } catch (Exception ex) {
            LOG.error(ex);
        }
        //return ids of these annotations.
        return ArrayUtils.toPrimitive(defaultAnnIds.toArray(new Integer[defaultAnnIds.size()]));
    }

    //The updID must always be 0, and so is ignored.  staging has no meaning and is ignored.  Both
    //arguments will be removed in the next API version.
    //This function will also be modified to add the default custom fields.  Another method identical to this 
    //one, but accepting a CustomField[] argument will be added for creating project with non-default customfields.
    @Override
    @Deprecated
    public String createVariantTable(String sessID, int projID, int refID, int updID, int[] annIDs, boolean staging) throws RemoteException, SQLException, SessionExpiredException {

        CustomField[] customFields = getCustomVariantFields(sessID, projID, refID, 0, true);
        String vt = addVariantTableToDatabase(sessID, projID, refID, 0, annIDs, customFields, false);
        String vts = addVariantTableToDatabase(sessID, projID, refID, 0, annIDs, customFields, true);
        addTableToMap(sessID, projID, refID, 0, false, vt, annIDs, vts);
        try {
            this.publishVariantTable(sessID, projID, refID);
        } catch (IOException iex) {
            LOG.error("Couldn't publish variant table", iex);
            throw new RemoteException("Couldn't publish variant table", iex);
        }
        return vt;
    }

    public String addVariantTableToDatabase(String sessID, int projID, int refID, int updID, int[] annIDs, CustomField[] customFields, boolean sub) throws RemoteException, SQLException, SessionExpiredException {
        // Create basic fields.        
        String tableName = DBSettings.getVariantTableName(projID, refID, updID);
        if (sub) {
            tableName += "_subset";
        }
        TableSchema variantSchema = new TableSchema(MedSavantDatabase.schema, tableName, BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
        for (CustomField f : customFields) {
            variantSchema.addColumn(f);
        }
       
        String s = "";
        for (DbColumn c : variantSchema.getColumns()) {
            s += c.getColumnNameSQL() + " ";
        }
        LOG.info("Creating variant table " + tableName + " with fields " + s);

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            int j = 0;
            for (int ann : annIDs) {
                annIDs[j++] = ann;
                AnnotationFormat annFmt = AnnotationManager.getInstance().getAnnotationFormat(sessID, ann);
                for (CustomField f : annFmt.getCustomFields()) {
                    variantSchema.addColumn(f);
                }
            }

            String updateString;
            if (MedSavantServerEngine.USE_INFINIDB_ENGINE) {

                updateString = variantSchema.getCreateQuery() + " ENGINE=INFINIDB;";
            } else {
                updateString = variantSchema.getCreateQuery() + " ENGINE=BRIGHTHOUSE DEFAULT CHARSET=latin1 COLLATE=latin1_bin;";
            }            
            conn.executeUpdate(updateString);
        } finally {
            conn.close();
        }

        return tableName;
    }

    public void setupTablesForVariantRemoval(String sessID, int projID, int refID, int updId, String newSubsetTableName) throws SQLException, RemoteException, SessionExpiredException {
        Object[] row = ProjectManager.getInstance().getVariantTableMapRecord(sessID, projID, refID, true);
        String tableName = (String) row[MedSavantDatabase.VarianttablemapTableSchema.INDEX_OF_VARIANT_TABLENAME];        
        int[] annotationIds = AnnotationManager.getInstance().getAnnotationIDs(sessID, projID, refID);       
        addTableToMap(sessID, projID, refID, updId, false, tableName, annotationIds, newSubsetTableName);

    }

    //This method will be removed from the RMI-exposed API (but retained as a public non-RMI method).  
    // published is redundant: it must always be false, and this argument will also be removed.
    @Deprecated
    @Override
    public void addTableToMap(String sessID, int projID, int refID, int updID, boolean published, String tableName, int[] annotationIDs, String subTableName) throws SQLException, RemoteException, SessionExpiredException {
        if (published != false) {//assert
            LOG.error("Can't add a published table to the variant table map");
            published = false;
        }
        TableSchema variantTableMap = MedSavantDatabase.VarianttablemapTableSchema;
        InsertQuery query = new InsertQuery(variantTableMap.getTable());
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updID);

        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), published);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), tableName);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);
        
        float subMultiplier = 0.0f;
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), subMultiplier);

       
       
        if (annotationIDs.length > 0) {
            //Duplicate annotation IDs should never be passed to this function.  Assert that they aren't 
            //with the below error check, but don't muck up the database or crash if they are.
             Set<Integer> annotationIdSet = new HashSet<Integer>();
             for(int id : annotationIDs){                 
                 annotationIdSet.add(id);
             }
                         
             if(annotationIdSet.size() < annotationIDs.length){
                 LOG.error("ERROR: Ignoring duplicate annotation ids in list "+StringUtils.join(annotationIDs, ","));
             }
            
            String annIDString = "";
            //for (int id : annotationIDs) {
            for(Integer id : annotationIdSet){
                annIDString += id + ",";
            }            
            annIDString = annIDString.substring(0, annIDString.length() - 1); // remove the last comma
            query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annIDString);
        }

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    private Object[] getVariantTableMapRecord(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if (published) {
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            Object[] results = new Object[rs.getMetaData().getColumnCount()];
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                results[i - 1] = rs.getObject(i);
            }
            return results;
        } else {
            return null;
        }
    }

    /*
     * Get the name of the most up-to-date variant table, or the most up-to-date,
     * published variant table.
     */
    public String getNameOfVariantTable(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException {
        Object[] row = getVariantTableMapRecord(sid, projectid, refid, published);
        return sub ? (String) row[VariantTablemapTableSchema.INDEX_OF_VARIANT_SUBSET_TABLENAME] : (String) row[VariantTablemapTableSchema.INDEX_OF_VARIANT_TABLENAME];        
    }

    //The published argument is ignored, and this method may be renamed in the future.
    //See below.
    @Override
    @Deprecated
    public String getVariantTableName(String sid, int projectid, int refid, boolean published) throws SQLException, SessionExpiredException {
        return getVariantTableName(sid, projectid, refid, published, false);
    }

    //The published argument is ignored and will be removed.  This method returns the current variant VIEW, 
    //which only applies to published tables.  This method may be renamed in the future.
    @Override
    @Deprecated
    public String getVariantTableName(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException, SessionExpiredException {
        return sub ? DBSettings.getVariantViewName(projectid, refid) : DBSettings.getVariantViewName(projectid, refid);       
    }   

    public Object[] getVariantTableViewInfo(String sid, int projectid, int refid) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(                
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME),
                table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));

        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        if (rs.next()) {
            //If the subset table doesn't exist, then the subset view cannot exist either.
            rs.getString(1);
            String subsetViewName = rs.wasNull() ? null : DBSettings.getVariantSubsetViewName(projectid, refid);

            return new Object[]{
                DBSettings.getVariantViewName(projectid, refid),
                subsetViewName,
                rs.getFloat(2)};
        } else {
            return null;
        }
    }

    public void addSubsetInfoToMap(String sessID, int projID, int refID, int updID, String subTableName, float multiplier) throws SQLException, SessionExpiredException {

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
        int numerator = VariantManager.getInstance().getNumFilteredVariantsHelper(sid, table, new Condition[0][]);
        int denominator = VariantManager.getInstance().getNumFilteredVariantsHelper(sid, subTable, new Condition[0][]);
        if (denominator == 0) {
            denominator = 1;
        }
        if (numerator > 0) {
            return (float) numerator / (float) denominator;
        } else {
            return 1.0f;
        }
    }

    @Override
    public int addProject(String sessID, String name, CustomField[] fields) throws SQLException, RemoteException, SessionExpiredException {

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
    public void removeProject(String sid, String projectName) throws SQLException, RemoteException, SessionExpiredException {

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
    public void removeProject(String sid, int projectid) throws SQLException, RemoteException, SessionExpiredException {

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
    public void setAnnotations(String sessID, int projID, int refID, int updID, String annIDs) throws SQLException, SessionExpiredException {

        String tablename = getNameOfVariantTable(sessID, projID, refID, true, false);

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
    public ProjectDetails[] getProjectDetails(String sessID, int projID) throws SQLException, SessionExpiredException {

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
    public void renameProject(String sid, int projectId, String newName) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), newName);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ConnectionController.executeUpdate(sid, query.toString());
    }

    @Override
    public void setCustomVariantFields(String sid, int projectId, int referenceId, int updateId, CustomField[] fields) throws SQLException, SessionExpiredException {

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

    //This method is temporary.  The boolean allows custom fields to be fetched from unpublished tables, which is only necessary when
    //creating a new project, as there is no mechanism for passing CustomFields on project creation.  The boolean will be removed and 
    //most of this method will replace the API-exposed getCustomVariantFields (it will be as if considerUnpublished is always false).
    @Deprecated
    private CustomField[] getCustomVariantFields(String sid, int projectId, int referenceId, int updateId, boolean considerUnpublished) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.VariantformatTableSchema;
        TableSchema variantTableMap = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();

        Column[] cols = new Column[table.getColumns().size()];
        int i = 0;
        for (DbColumn c : table.getColumns()) {
            cols[i++] = c;

        }
        query.addColumns(cols);

        //query.addFromTable(table.getTable());
        //reference, project, update, 
        if (!considerUnpublished) {
            Condition joinCondition
                    = ComboCondition.and(
                            BinaryCondition.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID)),
                            BinaryCondition.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID)),
                            BinaryCondition.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID))
                    );
            //restrict by published
            query.addJoin(SelectQuery.JoinType.INNER, table.getTable(), variantTableMap.getTable(), joinCondition);
        } else {
            query.addFromTable(table.getTable());
        }

        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        if (!considerUnpublished) {
            query.addCondition(BinaryCondition.equalTo(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 1));
        }
        query.addOrdering(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        //System.out.println("Query: "+query.toString());
        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<CustomField> result = new ArrayList<CustomField>();
        while (rs.next()) {
            result.add(new CustomField(
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME)/*.toLowerCase()*/, //why force lower case here?
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                    rs.getBoolean(VariantFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_ALIAS),
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }
        return result.toArray(new CustomField[0]);
    }

    //Get the most up-to-date custom fields, as specified in variant_format table.  Only published
    //custom fields are considered.
    @Override
    public CustomField[] getCustomVariantFields(String sid, int projectId, int referenceId, int updateId) throws SQLException, SessionExpiredException {
        return getCustomVariantFields(sid, projectId, referenceId, updateId, false);
    }

    @Override
    public int getNewestUpdateID(String sid, int projectId, int referenceId, boolean published) throws SQLException, SessionExpiredException {

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

    public void publishVariantTable(String sid, int projId, int refID) throws IOException, SQLException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sid);
        publishVariantTable(sid, conn, projId, refID);
    }

    public void publishVariantTable(String sid, PooledConnection conn, int projId, int refID) throws IOException, SQLException, SessionExpiredException {
        publishVariantTable(sid, conn, projId, new int[]{refID});

    }

    public void publishVariantTable(String sid, PooledConnection conn, int projID, int[] refIDs) throws IOException, SQLException, SessionExpiredException {
        TableSchema variantTableMapTable = MedSavantDatabase.VarianttablemapTableSchema;
        TableSchema variantFormatTable = MedSavantDatabase.VariantformatTableSchema;
        //Set Variant File IB Table <- Variant File MyISAM table
        SetupMedSavantDatabase.makeVariantFileIBTable(sid);
        Set<String> tablesToDrop = new HashSet<String>();
        for (int refID : refIDs) {
            String fileTableName = SetupMedSavantDatabase.getVariantFileIBTableName();
            String variantTableName = getNameOfVariantTable(sid, projID, refID, false, false);
            String subTableName = getNameOfVariantTable(sid, projID, refID, false, true);

            //Create a view into the table 
            String variantViewName = DBSettings.getVariantViewName(projID, refID);
            String variantSubViewName = DBSettings.getVariantSubsetViewName(projID, refID);
            //if (!DBUtils.tableExists(sid, variantViewName)) {
            conn.executeUpdate("DROP VIEW IF EXISTS " + variantViewName);
            String viewQuery = "CREATE VIEW " + variantViewName + " AS "
                    + "SELECT " + variantTableName + ".* FROM " + variantTableName + ", " + fileTableName + " WHERE ("
                    + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_FILE_ID + " = " + variantTableName + "." + BasicVariantColumns.FILE_ID.getColumnName()
                    + " AND "
                    + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID + " = " + variantTableName + "." + BasicVariantColumns.UPLOAD_ID.getColumnName()
                    + " AND "
                    + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_PROJECT_ID + " = " + projID
                    + " AND "
                    + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_REFERENCE_ID + " = " + refID + ")";

            LOG.info(viewQuery);
            conn.executeUpdate(viewQuery);
            //   }
            if (subTableName != null && !subTableName.isEmpty()/* && !DBUtils.tableExists(sid, variantSubViewName)*/) {
                conn.executeUpdate("DROP VIEW IF EXISTS " + variantSubViewName);
                viewQuery = "CREATE VIEW " + variantSubViewName + " AS "
                        + "SELECT " + subTableName + ".* FROM " + subTableName + ", " + fileTableName + " WHERE ("
                        + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_FILE_ID + " = " + subTableName + "." + BasicVariantColumns.FILE_ID.getColumnName()
                        + " AND "
                        + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_UPLOAD_ID + " = " + subTableName + "." + BasicVariantColumns.UPLOAD_ID.getColumnName()
                        + " AND "
                        + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_PROJECT_ID + " = " + projID
                        + " AND "
                        + fileTableName + "." + MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_REFERENCE_ID + " = " + refID + ")";
                conn.executeUpdate(viewQuery);
                LOG.info(viewQuery);
            }

            //Get the names of the current published tables.
            SelectQuery sq = new SelectQuery();
            sq.addFromTable(variantTableMapTable.getTable());
            sq.addAllColumns();
            sq.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
            sq.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            sq.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));

            //Queue all tables not named the same as the current table for later deletion, as the
            //last step of publication.
            ResultSet rs = conn.executeQuery(sq.toString());
            while (rs.next()) {
                String tn = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME);
                String sn = rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME);
                //System.out.println("Checking if tablename " + tn + " matches new tablename " + variantTableName);
                if (!tn.equals(variantTableName)) {
                    tablesToDrop.add(tn);
                }
                if (!sn.equals(subTableName)) {
                    tablesToDrop.add(sn);
                }
            }

            float multiplier = this.getMultiplier(sid, variantViewName, variantSubViewName);

           
            //update the table so everything points to the published version of the table, and the subset multiplier is set correctly.            
            UpdateQuery query = new UpdateQuery(variantTableMapTable.getTable());
            query.addSetClause(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), variantTableName);
            query.addSetClause(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);
            query.addSetClause(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), multiplier);
            query.addSetClause(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true);
            query.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
            query.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            conn.executeUpdate(query.toString());

            //Clean out variant_format table to contain only the format of the last created table.
            int u = getNewestUpdateID(sid, projID, refID, true);
            DeleteQuery dq = new DeleteQuery(variantFormatTable.getTable());
            dq.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
            dq.addCondition(BinaryConditionMS.equalTo(variantTableMapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            dq.addCondition(BinaryCondition.notEqualTo(variantTableMapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), u));
            conn.executeUpdate(dq.toString());
        }

        //delete unused tables.
        for (String tableToDrop : tablesToDrop) {
            DBUtils.dropTable(sid, tableToDrop);
        }

        //delete old genotype files.
        cleanStaleGenotypeFiles(sid, projID);
    }

    public boolean hasUnpublishedChanges(String sessId, int projId, int refId) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), false));
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sessId, query.toString());

        return rs.first();
    }

    @Override
    public ProjectDetails[] getUnpublishedChanges(String sessID) throws SQLException, SessionExpiredException {

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
    public String[] getReferenceNamesForProject(String sessID, int projID) throws SQLException, SessionExpiredException {

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
            if (!references.contains(rs.getString(1))) {
                references.add(rs.getString(1));
            }
        }

        return references.toArray(new String[0]);
    }

    @Override
    public int[] getReferenceIDsForProject(String sessID, int projID) throws SQLException, SessionExpiredException {

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

    public synchronized void cancelPublish(String sessID, int projID, int refID) throws SQLException, SessionExpiredException, IOException {

        //Drop all variant tables that are no longer being used.  i.e. those that do not have 
        //published=1.
        TableSchema mapTable = MedSavantDatabase.VarianttablemapTableSchema;

        String idRestriction = "("
                + VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID + "=" + refID
                + " AND "
                + VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID + "=" + projID
                + ")";

        //Select all tables with published=0 that do not also have published=1.
        String query = "SELECT DISTINCT "
                + mapTable.getTableName() + "." + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME + ","
                + mapTable.getTableName() + "." + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME
                + " FROM "
                + mapTable.getTableName() + " WHERE " + idRestriction + " AND "
                + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED + " = 0 AND "
                + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME + " NOT IN "
                + "(SELECT " + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME + " FROM "
                + mapTable.getTableName() + " WHERE " + idRestriction + " AND "
                + MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED + " = 1)";
        ResultSet rs = ConnectionController.executeQuery(sessID, query);
        List<Integer> droppedUpdateIDs = new ArrayList<Integer>();
        while (rs.next()) {
            String tableToDrop = rs.getString(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME);
            VariantManagerUtils.dropTableIfExists(sessID, tableToDrop);
            tableToDrop = rs.getString(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME);
            VariantManagerUtils.dropTableIfExists(sessID, tableToDrop);
            droppedUpdateIDs.add(rs.getInt(MedSavantDatabase.VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID));
        }

        //Delete unpublished tables from the variant table map.
        DeleteQuery dq1 = new DeleteQuery(mapTable.getTable());
        dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
        dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), 0));
        ConnectionController.executeUpdate(sessID, dq1.toString());
        LOG.info("Deleting unpublished tables from the variant table map: " + dq1.toString());

        TableSchema formatTable = MedSavantDatabase.VariantformatTableSchema;
        //Delete unpublished variant_format entries.        
        for (int uid : droppedUpdateIDs) {
            dq1 = new DeleteQuery(formatTable.getTable());
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), refID));
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), uid));
            LOG.info("Deleting unpublished entries from variant format table: " + dq1.toString());
            ConnectionController.executeUpdate(sessID, dq1.toString());
        }
       
        //Drop the main file table, and restore the "backup" copy from the Infobright table.
        restorePublishedFileTable(sessID);
        cleanStaleGenotypeFiles(sessID, projID);        
    }

    //Copies the published Infobright file table over the MyISAM file table.  This effectively 
    //undoes any changes that might be pending for unpublished variants.  This method is used to
    //cancel publication, and also to assert that updates to the variant table happen ONLY on 
    //published variants.  
    public void restorePublishedFileTable(String sessID) throws SQLException, SessionExpiredException, IOException {
        TableSchema fileTable = MedSavantDatabase.VariantFileTableSchema;
        //Drop the main file table, and restore the "backup" copy from the Infobright table.
        VariantManagerUtils.dropTableIfExists(sessID, fileTable.getTableName());
        SetupMedSavantDatabase.makeVariantFileTable(sessID, false);
        String query = "INSERT INTO " + fileTable.getTableName() + " SELECT * FROM " + SetupMedSavantDatabase.getVariantFileIBTableName();
        ConnectionController.executeUpdate(sessID, query);
    }

    private static synchronized void cleanStaleGenotypeFiles(String sessID, int projID) throws SQLException, SessionExpiredException, RemoteException {
        String database = SessionManager.getInstance().getDatabaseForSession(sessID);
        TableSchema fileTable = MedSavantDatabase.VariantFileTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(fileTable.getTable());
        query.addColumns(fileTable.getDBColumn(MedSavantDatabase.VariantFileTableSchema.COLUMNNAME_OF_FILE_NAME));
        query.addCondition(BinaryConditionMS.equalTo(fileTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());
        Set<String> fileNames = new HashSet<String>();
        while (rs.next()) {
            fileNames.add(rs.getString(1));
        }

        File genotypeDir = DirectorySettings.getGenoTypeDirectory(database, projID);
        File[] listOfFiles = genotypeDir.listFiles();
        for (File f : listOfFiles) {
            if (!fileNames.contains(f.getAbsolutePath())) {
                f.delete();
            }
        }
    }

    /**
     * Remove any variant tables with updateMin <= updateId <= updateMax.
     */
    @Deprecated
    public void removeTables(String sessID, int projID, int refID, int updateMax, int updateMin) throws SQLException, SessionExpiredException {

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
