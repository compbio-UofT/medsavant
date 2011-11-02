/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.db.util.query;

import java.io.IOException;
import java.sql.Connection;
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
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.xml.sax.SAXException;

import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.CustomTables;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultPatientTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.format.AnnotationField;
import org.ut.biolab.medsavant.db.format.AnnotationField.Category;
import org.ut.biolab.medsavant.db.format.PatientFormat;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.DBSettings;

/**
 *
 * @author Andrew
 */
public class PatientQueryUtil {
    
    public static List<Object[]> getBasicPatientInfo(int projectId, int limit) throws SQLException, NonFatalDatabaseException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()){
            result.add(new Object[] {
                rs.getInt(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS)
            });          
        }
        return result;
    }
    
    public static Object[] getPatientRecord(int projectId, int patientId) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), patientId));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        rs.next();
        Object[] v = new Object[rs.getMetaData().getColumnCount()];
        for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
            v[i - 1] = rs.getObject(i);
        }
        return v;
    }
    
    public static List<String> getPatientFieldAliases(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<String> result = new ArrayList<String>();

        for (AnnotationField af : PatientFormat.getDefaultAnnotationFormat()) {
            result.add(af.getAlias());
        }
        
        while(rs.next()){
            result.add(rs.getString(1));
        }
        return result;
    }
    
    public static List<AnnotationField> getPatientFields(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<AnnotationField> result = new ArrayList<AnnotationField>();
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "int(11)", false, DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID, "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID, "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID, "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "varchar(100)", false, DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID, "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, "varchar(1000)", false, DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, "", Category.PATIENT));
        result.add(new AnnotationField(DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, "varchar(5000)", false, DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL, "", Category.PATIENT));
        
        while(rs.next()){
            result.add(new AnnotationField(
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), 
                    rs.getBoolean(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), 
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), 
                    Category.PATIENT));
        }
        return result;
    }
    
    public static String getPatientTablename(int projectId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.PatienttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        rs.next();
        return rs.getString(1);
    }
    
     
    public static void createPatientTable(int projectid, List<CustomField> fields) throws SQLException, ParserConfigurationException, SAXException, IOException {

        String patientTableName = DBSettings.createPatientTableName(projectid);        
        Connection c = ConnectionController.connectPooled();

        //create basic fields
        String query = 
                "CREATE TABLE `" + patientTableName + "` ("
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_PEDIGREE_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL + "` varchar(5000) COLLATE latin1_bin DEFAULT NULL,";
        
        for(CustomField field : fields){
            query += field.generateSchema();
        }
        
        query += "PRIMARY KEY (`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "`)"
                + ") ENGINE=MyISAM;";
        
        //create table
        c.createStatement().execute(query);

        //add to tablemap
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        InsertQuery query1 = new InsertQuery(patientMapTable.getTable());
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME), patientTableName);
        c.createStatement().executeUpdate(query1.toString());
        
        //populate format table
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        c.setAutoCommit(false);
        for(int i = 0; i < fields.size(); i++){
            CustomField a = fields.get(i);
            InsertQuery query2 = new InsertQuery(patientFormatTable.getTable());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), a.getColumnName());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), a.getColumnType());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (a.isFilterable() ? "1" : "0"));
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), a.getAlias());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), a.getDescription());
            c.createStatement().executeUpdate(query2.toString());
        }
        c.commit();
        c.setAutoCommit(true);   
        
    }
    
    public static void removePatient(int projectId, int[] patientIds) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        
        Connection c = ConnectionController.connectPooled();
        c.setAutoCommit(false);       
        for(int id : patientIds){
            //remove all references
            CohortQueryUtil.removePatientReferences(projectId, id); 
            
            //remove from patient table
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
        }
        c.commit();
        c.setAutoCommit(true);
    }
    
    public static void addPatient(int projectId, List<CustomField> cols, List<String> values) throws SQLException {
        
        String tablename = getPatientTablename(projectId);
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        
        InsertQuery query = new InsertQuery(table.getTable());
        for(int i = 0; i < Math.min(cols.size(), values.size()); i++){
            query.addColumn(new DbColumn(table.getTable(), cols.get(i).getColumnName(), cols.get(i).getColumnType(), 100), values.get(i));
        }
        
        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString()); 
    }

    public static List<String> getDNAIdsWithValuesInRange(int projectId, String columnName, Range r) throws NonFatalDatabaseException, SQLException {
        
        String tablename = getPatientTablename(projectId);
        
        TableSchema table = CustomTables.getCustomTableSchema(tablename);
        
        DbColumn currentDNAId = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        q.addCondition(BinaryCondition.greaterThan(testColumn, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(testColumn, r.getMax(), true));
        
        Statement s = ConnectionController.connectPooled().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        
        List<String> result = new ArrayList<String>();
        while(rs.next()){          
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }
    
    public static List<String> getDNAIdsForStringList(TableSchema table, List<String> list, String columnname) throws NonFatalDatabaseException, SQLException {
 
        DbColumn currentDNAId = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnname);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        
        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryConditionMS.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));   
        
        Statement s = ConnectionController.connectPooled().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> result = new ArrayList<String>();
        while(rs.next()){          
            String[] dnaIds = rs.getString(1).split(",");
            for(String id : dnaIds){
                if(!result.contains(id)){
                    result.add(id);
                }
            }
        }
        return result;
    }
}
