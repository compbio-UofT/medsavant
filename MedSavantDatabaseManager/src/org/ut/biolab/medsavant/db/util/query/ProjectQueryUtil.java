package org.ut.biolab.medsavant.db.util.query;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Action;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Status;
import org.ut.biolab.medsavant.db.model.ProjectDetails;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ProjectTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.ReferenceTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.CustomField.Category;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.DBSettings;

/**
 *
 * @author mfiume
 */
public class ProjectQueryUtil {

    public static List<String> getProjectNames() throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        List<String> results = new ArrayList<String>();

        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }

    public static boolean containsProject(String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addAllColumns();
        query.addFromTable(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        return rs.next();
    }

    public static int getProjectId(String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }

    public static void removeReferenceForProject(int project_id, int ref_id) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query1 = new SelectQuery();
        query1.addFromTable(table.getTable());
        query1.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query1.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query1.toString());

        while (rs.next()) {
            String tableName = rs.getString(1);
            DBUtil.dropTable(tableName);
        }

        DeleteQuery query2 = new DeleteQuery(table.getTable());
        query2.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), project_id),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), ref_id)));
    }

    public static String getProjectName(int projectid) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));

        ResultSet rs1 = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        if (rs1.next()) {
            return rs1.getString(1);
        } else {
            return null;
        }
    }

    public static String createVariantTable(int projectid, int referenceid, int updateid) throws SQLException {
        return createVariantTable(projectid, referenceid, updateid, null, false);
    }

    public static String createVariantTable(int projectid, int referenceid, int updateid, int[] annotationIds, boolean isStaging) throws SQLException {

        String variantTableInfoName = isStaging ? DBSettings.createVariantStagingTableName(projectid, referenceid, updateid) : DBSettings.createVariantTableName(projectid, referenceid, updateid);

        Connection c = (ConnectionController.connectPooled());

        String query =
                "CREATE TABLE `" + variantTableInfoName + "` ("
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
                + "`" + DefaultVariantTableSchema.COLUMNNAME_OF_CUSTOM_INFO + "` varchar(500) COLLATE latin1_bin DEFAULT NULL,";

        //add custom vcf fields
        if(!isStaging){
            List<CustomField> customFields = getCustomVariantFields(projectid);
            for(CustomField f : customFields){
                query += f.generateSchema(true);
            }
        }

        //add each annotation
        if(annotationIds != null){
            for(int annotationId : annotationIds){
                query += getAnnotationSchema(annotationId);
            }
        }

        query = query.substring(0, query.length()-1); //remove last comma
        query += ") ENGINE=BRIGHTHOUSE;";

        c.createStatement().execute(query);

        if(!isStaging){
            TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;

            //remove existing entries
            DeleteQuery d = new DeleteQuery(table.getTable());
            d.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
            d.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid));
            c.createStatement().execute(d.toString());

            //create new entry
            InsertQuery query1 = new InsertQuery(table.getTable());
            query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceid);
            query1.addColumn(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME), variantTableInfoName);
            c.createStatement().execute(query1.toString());

            //set annotation ids
            /*String s = "";
            if(annotationIds != null && annotationIds.length > 0){
                for(Integer i : annotationIds){
                    s += i + ",";
                }
                s = s.substring(0, s.length()-1);
            }
            setAnnotations(projectid, referenceid, s);   */
        }

        return variantTableInfoName;
    }

    private static String getAnnotationSchema(int annotationId){

        AnnotationFormat format = null;
        try {
            format = AnnotationQueryUtil.getAnnotationFormat(annotationId);
        } catch (Exception e){
            e.printStackTrace();
        }

        return format.generateSchema();
    }

    public static int getNumberOfRecordsInVariantTable(int projectid, int refid) throws SQLException {
        String variantTableName = ProjectQueryUtil.getVariantTablename(projectid,refid);
        return DBUtil.getNumRecordsInTable(variantTableName);
    }

    public static String getVariantTablename(int projectid, int refid) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_VARIANT_TABLENAME));
        query.addCondition(ComboCondition.and(
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid),
                BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid)));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        if (rs.next()) {
            return rs.getString(1);
        } else {
            return null;
        }
    }


    public static int addProject(String name, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), name);

        PreparedStatement stmt = (ConnectionController.connectPooled()).prepareStatement(query.toString(),
                Statement.RETURN_GENERATED_KEYS);

        stmt.execute();
        ResultSet res = stmt.getGeneratedKeys();
        res.next();

        int projectid = res.getInt(1);

        PatientQueryUtil.createPatientTable(projectid, fields);

        return projectid;
    }

    public static void removeProject(String projectName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), projectName));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        if (rs.next()) {
            removeProject(rs.getInt(1));
        }
    }


    public static void removeProject(int projectid) throws SQLException {


        Connection c = ConnectionController.connectPooled();

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

        ResultSet rs1 = ConnectionController.connectPooled().createStatement().executeQuery(q2.toString());

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

        ResultSet rs2 = c.createStatement().executeQuery(q5.toString());

        while(rs2.next()) {
            String variantTableName = rs2.getString(1);
            c.createStatement().execute("DROP TABLE IF EXISTS " + variantTableName);
        }

        //remove from variant tablemap
        DeleteQuery q6 = new DeleteQuery(variantMapTable.getTable());
        q6.addCondition(BinaryConditionMS.equalTo(variantMapTable.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid));
        c.createStatement().execute(q6.toString());

        //remove cohort entries
        List<Integer> cohortIds = CohortQueryUtil.getCohortIds(projectid);
        for(Integer cohortId : cohortIds){
            CohortQueryUtil.removeCohort(cohortId);
        }

    }

    public static void setAnnotations(int projectid, int refid, String annotation_ids, boolean logEntry) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttablemapTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS), annotation_ids);
        query.addCondition(ComboCondition.and(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid)));
        query.addCondition(ComboCondition.and(BinaryConditionMS.equalTo(table.getDBColumn(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID), refid)));

        (ConnectionController.connectPooled()).createStatement().execute(query.toString());

        if(logEntry){
            AnnotationLogQueryUtil.addAnnotationLogEntry(projectid, refid, Action.UPDATE_TABLE, Status.PENDING);
        }
    }

    public static List<ProjectDetails> getProjectDetails(int projectId) throws SQLException {

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

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        List<ProjectDetails> result = new ArrayList<ProjectDetails>();
        while(rs.next()){
            result.add(new ProjectDetails(
                    rs.getInt(VariantTablemapTableSchema.COLUMNNAME_OF_REFERENCE_ID),
                    rs.getString(ReferenceTableSchema.COLUMNNAME_OF_NAME),
                    rs.getString(VariantTablemapTableSchema.COLUMNNAME_OF_ANNOTATION_IDS)));
        }

        return result;
    }

    public static void renameProject(int projectId, String newName) throws SQLException {

        TableSchema table = MedSavantDatabase.ProjectTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_NAME), newName);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(ProjectTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString());
    }

    public static void setCustomVariantFields(int projectId, List<CustomField> fields, boolean firstSet) throws SQLException {

        Connection c = ConnectionController.connectPooled();
        TableSchema table = MedSavantDatabase.VariantformatTableSchema;

        //clear the current fields
        DeleteQuery clearQuery = new DeleteQuery(table.getTable());
        clearQuery.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        c.createStatement().executeUpdate(clearQuery.toString());

        c.setAutoCommit(false);
        for(int i = 0; i < fields.size(); i++){
            CustomField f = fields.get(i);
            InsertQuery insertQuery = new InsertQuery(table.getTable());
            insertQuery.addColumn(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
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

        if(!firstSet){
            List<Integer> referenceIds = ReferenceQueryUtil.getReferenceIdsForProject(projectId);
            for(Integer id : referenceIds){
                AnnotationLogQueryUtil.addAnnotationLogEntry(projectId, id, Action.UPDATE_TABLE, Status.PENDING);
            }
        }

    }

    //Get the most up-to-date custom fields, as specified in variant_format table
    public static List<CustomField> getCustomVariantFields(int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.VariantformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(VariantFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());

        List<CustomField> result = new ArrayList<CustomField>();
        while(rs.next()){
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

    public static AnnotationFormat getActualCustomFieldAnnotationFormat(int projectId) throws SQLException {

        List<CustomField> customFields = getCustomVariantFields(projectId);
        return new AnnotationFormat(
                "custom vcf", "custom vcf", 0, "", true, true, AnnotationType.POSITION, customFields);
    }

}
