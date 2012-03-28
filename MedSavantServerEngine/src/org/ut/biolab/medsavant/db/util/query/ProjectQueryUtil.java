package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.CustomField.Category;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.shared.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.db.util.query.api.ProjectQueryUtilAdapter;
import org.ut.biolab.medsavant.db.variants.update.VariantManagerUtils;

/**
 *
 * @author mfiume
 */
public class ProjectQueryUtil extends MedSavantServerUnicastRemoteObject implements ProjectQueryUtilAdapter {

    private static ProjectQueryUtil instance;

    public static synchronized ProjectQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new ProjectQueryUtil();
        }
        return instance;
    }

    public ProjectQueryUtil() throws RemoteException {
        super();
    }

    public List<String> getProjectNames(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<String> results = new ArrayList<String>();

        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }

    public boolean containsProject(String sid, String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addAllColumns();
        query.addFromTable(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        return rs.next();
    }

    public int getProjectId(String sid, String projectName) throws SQLException {

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

    public void removeReferenceForProject(String sid, int project_id, int ref_id) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(table.getTable());
        query1.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query1.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));

        ResultSet rs = ConnectionController.executeQuery(sid, query1.toString());

        while (rs.next()) {
            String tableName = rs.getString(1);
            DBUtil.dropTable(sid, tableName);
        }

        DeleteQuery query2 = new DeleteQuery(table.getTable());
        query2.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));
        ConnectionController.execute(sid, query2.toString());
    }

    public String getProjectName(String sid, int projectid) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs1 = ConnectionController.executeQuery(sid, query.toString());

        if (rs1.next()) {
            return rs1.getString(1);
        } else {
            return null;
        }
    }

    /*public String createVariantTable(String sid, int projectid, int referenceid, int updateid) throws SQLException {
        return createVariantTable(sid, projectid, referenceid, updateid, null, false);
    }
     */



    public String createVariantTable(String sid, int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging) throws SQLException {
        return createVariantTable(sid, projectid, referenceid, updateid, annotationIds, isStaging, false);
    }

    public String createVariantTable(String sid, int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging, boolean isSub) throws SQLException {

        String variantTableName = DBSettings.getVariantTableName(projectid, referenceid, updateid);
        if(isSub){
            variantTableName += "_sub";
        }

        Connection c = (ConnectionController.connectPooled(sid));

        String query =
                "CREATE TABLE `" + variantTableName + "` ("
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_UPLOAD_ID + "` int(11) NOT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_FILE_ID + "` int(11) NOT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_ID + "` int(11) NOT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID + "` varchar(100) COLLATE latin1_bin NOT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_CHROM + "` varchar(5) COLLATE latin1_bin NOT NULL DEFAULT '',"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "` int(11) NOT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_DBSNP_ID + "` varchar(45) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_REF + "` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_ALT + "` varchar(30) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_QUAL + "` float(10,0) DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_FILTER + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_VARIANT_TYPE + "` varchar(10) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_ZYGOSITY + "` varchar(20) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_GT + "` varchar(10) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,";

        //add custom vcf fields
        List<CustomField> customFields = getCustomVariantFields(sid, projectid, referenceid, updateid);
        for (CustomField f : customFields) {
            query += f.generateSchema(true);
        }

        //add each annotation
        if (annotationIds != null) {
            for (int annotationId : annotationIds) {
                query += getAnnotationSchema(sid, annotationId);
            }
        }

        query = query.substring(0, query.length() - 1); //remove last comma
        query += ") ENGINE=BRIGHTHOUSE;";

        c.createStatement().execute(query);

        //create new entry in variant table map
        /*if(!isSub){
            TableSchema variantTableMap = MedSavantDatabase.VarianttablemapTableSchema;
            InsertQuery query1 = new InsertQuery(variantTableMap.getTable());
            query1.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query1.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid);
            query1.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateid);
            query1.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), !isStaging);
            query1.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), variantTableName);
            c.createStatement().execute(query1.toString());
        }*/

        c.close();
        return variantTableName;
    }
    
    public void addTableToMap(String sid, int projectid, int referenceid, int updateid, boolean published, String tablename, String subTablename) throws SQLException{
        TableSchema variantTableMap = MedSavantDatabase.VarianttablemapTableSchema;
        InsertQuery query = new InsertQuery(variantTableMap.getTable());
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateid);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), published);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), tablename);
        query.addColumn(variantTableMap.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTablename);
        ConnectionController.executeQuery(sid, query.toString());
    }

    private static String getAnnotationSchema(String sid, int annotationId) {

        AnnotationFormat format = null;
        try {
            format = AnnotationQueryUtil.getInstance().getAnnotationFormat(sid, annotationId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return format.generateSchema();
    }

    public int getNumberOfRecordsInVariantTable(String sid, int projectid, int refid) throws SQLException {
        try {
            String variantTableName = ProjectQueryUtil.getInstance().getVariantTablename(sid, projectid, refid, true);
            return DBUtil.getNumRecordsInTable(sid, variantTableName);
        } catch (RemoteException ex) {
            Logger.getLogger(ProjectQueryUtil.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    /*
     * Get the name of the most up-to-date variant table, or the most up-to-date,
     * published variant table.
     */
    public String getVariantTablename(String sid, int projectid, int refid, boolean published) throws SQLException {
        return getVariantTablename(sid, projectid, refid, published, false);
    }

    public String getVariantTablename(String sid, int projectid, int refid, boolean published, boolean sub) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn((sub ? VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME : VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME)));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        if(published){
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
        if(published){
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

    public void addSubsetInfoToMap(String sid, int projectId, int referenceId, int updateId, String subTableName, float multiplier) throws SQLException{

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_SUBSET_TABLENAME), subTableName);
        query.addCustomSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_SUBSET_MULTIPLIER), multiplier);
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));

        ConnectionController.execute(sid, query.toString());
    }

    public float getMultiplier(String sid, String table, String subTable) throws SQLException, RemoteException{
        int numerator = VariantQueryUtil.getInstance().getNumFilteredVariantsHelper(sid, table, new Condition[0][]);
        int denominator = VariantQueryUtil.getInstance().getNumFilteredVariantsHelper(sid, subTable, new Condition[0][]);
        if(denominator == 0) denominator = 1;
        return (float)numerator / (float)denominator;
    }

    public int addProject(String sid, String name, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), name);

        Connection c = ConnectionController.connectPooled(sid);
        PreparedStatement stmt = c.prepareStatement(query.toString(),
                Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int projectid = res.getInt(1);

        PatientQueryUtil.getInstance().createPatientTable(sid, projectid, fields);

        c.close();
        return projectid;
    }

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
        List<Integer> cohortIds = CohortQueryUtil.getInstance().getCohortIds(sid, projectid);
        for (Integer cohortId : cohortIds) {
            CohortQueryUtil.getInstance().removeCohort(sid, cohortId);
        }

        c.close();
    }

    public void setAnnotations(String sid, int projectid, int refid, int updateid, String annotation_ids) throws SQLException {

        String tablename = getVariantTablename(sid, projectid, refid, true);

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annotation_ids);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateid));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), tablename)); //should only affect published table

        ConnectionController.execute(sid, query.toString());
    }

    public List<ProjectDetails> getProjectDetails(String sid, int projectId) throws SQLException {

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
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        query.addOrdering(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Integer> refs = new ArrayList<Integer>();
        List<ProjectDetails> result = new ArrayList<ProjectDetails>();
        while (rs.next()) {
            if(!refs.contains(rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID))){ // should be only one per reference (most recent and published)
                result.add(new ProjectDetails(
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID),
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                        rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID),
                        rs.getBoolean(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED),
                        null,
                        rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                        rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS)));
                refs.add(rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID));
            }
        }

        return result;
    }

    public void renameProject(String sid, int projectId, String newName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), newName);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    public void setCustomVariantFields(String sid, int projectId, int referenceId, int updateId, List<CustomField> fields) throws SQLException {

        Connection c = ConnectionController.connectPooled(sid);
        TableSchema table = MedSavantDatabase.VariantformatTableSchema;

        c.setAutoCommit(false);
        for (int i = 0; i < fields.size(); i++) {
            CustomField f = fields.get(i);
            InsertQuery insertQuery = new InsertQuery(table.getTable());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), f.getColumnTypeString());
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
    public List<CustomField> getCustomVariantFields(String sid, int projectId, int referenceId, int updateId) throws SQLException {

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
                    rs.getString(VariantFormatTableSchema.COLUMNNAME_OF_DESCRIPTION),
                    Category.VARIANT));
        }
        return result;
    }

    public int getNewestUpdateId(String sid, int projectId, int referenceId, boolean published) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        if(published){
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true));
        }
        query.addOrdering(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), Dir.DESCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        return rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID);
    }

    public void publishVariantTable(Connection c, int projectId, int referenceId, int updateId) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED), true);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));

        c.createStatement().executeUpdate(query.toString());
    }

    public List<ProjectDetails> getUnpublishedTables(String sid) throws SQLException {

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

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        Map<Integer, Map<Integer, ProjectDetails>> map = new HashMap<Integer, Map<Integer, ProjectDetails>>();

        while(rs.next()){
            int projectId = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID);
            int referenceId = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID);
            int updateId = rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID);
            boolean published = rs.getBoolean(VariantTablemapTableSchema.COLUMNNAME_OF_PUBLISHED);

            if(!map.containsKey(projectId)){
                map.put(projectId, new HashMap<Integer, ProjectDetails>());
            }

            if(!map.get(projectId).containsKey(referenceId) || map.get(projectId).get(referenceId).getUpdateId() < updateId){
                map.get(projectId).put(referenceId, new ProjectDetails(projectId, referenceId, updateId, published, rs.getString("A"), rs.getString("B"), null));
            }
        }

        List<ProjectDetails> tables = new ArrayList<ProjectDetails>();
        for(Integer project : map.keySet()){
            for(ProjectDetails details : map.get(project).values()){
                if(!details.isPublished()){
                    tables.add(details);
                }
            }
        }

        return tables;
    }

    public boolean existsUnpublishedChanges(String sid, int projectId) throws SQLException, RemoteException {

        for(Integer referenceId : ReferenceQueryUtil.getInstance().getReferenceIdsForProject(sid, projectId)){
            if(existsUnpublishedChanges(sid, projectId, referenceId)){
                return true;
            }
        }
        return false;
    }

    public boolean existsUnpublishedChanges(String sid, int projectId, int referenceId) throws SQLException {
        int newestId = getNewestUpdateId(sid, projectId, referenceId, false);
        int newestPublishedId = getNewestUpdateId(sid, projectId, referenceId, true);
        return newestId > newestPublishedId;
    }

    /*
     * Remove any variant tables with updateMin <= updateId <= updateMax
     */
    public void removeTables(String sid, int projectId, int referenceId, int updateMax, int updateMin) throws SQLException {

        //find update ids
        TableSchema mapTable = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(mapTable.getTable());
        query.addColumns(
                mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID),
                mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(ComboCondition.and(
                BinaryCondition.lessThan(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateMax, true),
                BinaryCondition.greaterThan(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateMin, true)));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        while(rs.next()){
            int updateId = rs.getInt(1);
            String tableName = rs.getString(2);

            //remove variant table
            VariantManagerUtils.dropTableIfExists(sid, tableName);

            //remove from variant tablemap
            DeleteQuery dq1 = new DeleteQuery(mapTable.getTable());
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
            dq1.addCondition(BinaryConditionMS.equalTo(mapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
            ConnectionController.execute(sid, dq1.toString());

            //remove from variant format
            TableSchema formatTable = MedSavantDatabase.VariantformatTableSchema;
            DeleteQuery dq2 = new DeleteQuery(formatTable.getTable());
            dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
            dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
            dq2.addCondition(BinaryConditionMS.equalTo(formatTable.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        }

    }

}
