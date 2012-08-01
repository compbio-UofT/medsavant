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

package org.ut.biolab.medsavant.serverapi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;

import org.ut.biolab.medsavant.db.*;
import org.ut.biolab.medsavant.db.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.DBUtils;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.format.PatientFormat;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class PatientManager extends MedSavantServerUnicastRemoteObject implements PatientManagerAdapter {

    private static PatientManager instance;

    private PatientManager() throws RemoteException {
    }

    public static synchronized PatientManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new PatientManager();
        }
        return instance;
    }

    @Override
    public List<Object[]> getBasicPatientInfo(String sid, int projectId, int limit) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sid,projectId);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER),
                table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            result.add(new Object[] {
                rs.getInt(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD),
                rs.getInt(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER),
                rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS)
            });
        }
        return result;
    }

    @Override
    public List<Object[]> getPatients(String sid, int projectId) throws SQLException, RemoteException {
        String tablename = getPatientTableName(sid,projectId);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] o = new Object[rs.getMetaData().getColumnCount()];
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                try {
                    o[i] = rs.getObject(i+1);
                } catch (SQLException e) {
                    //ignore...probably invalid input (ie. date 0000-00-00)
                }
            }
            result.add(o);
        }
        return result;
    }

    @Override
    public Object[] getPatientRecord(String sid, int projectId, int patientId) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sid,projectId);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), patientId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        Object[] v = new Object[rs.getMetaData().getColumnCount()];
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            try {
                v[i - 1] = rs.getObject(i);
            } catch (SQLException e) {
                //ignore...probably invalid input (ie. date 0000-00-00)
            }
        }
        return v;
    }

    @Override
    public List<String> getPatientFieldAliases(String sid, int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<String> result = new ArrayList<String>();

        for (CustomField af : PatientFormat.getDefaultAnnotationFormat()) {
            result.add(af.getAlias());
        }

        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    @Override
    public CustomField[] getPatientFields(String sessID, int projID) throws SQLException {
        CustomField[] defaultFields = PatientFormat.getDefaultAnnotationFormat();
        CustomField[] customFields = getCustomPatientFields(sessID, projID);
        CustomField[] result = new CustomField[defaultFields.length + customFields.length];
        System.arraycopy(defaultFields, 0, result, 0, defaultFields.length);
        System.arraycopy(customFields, 0, result, defaultFields.length, customFields.length);
        return result;
    }

    @Override
    public CustomField[] getCustomPatientFields(String sessID, int projID) throws SQLException {

        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<CustomField> result = new ArrayList<CustomField>();
        while (rs.next()) {
            result.add(new CustomField(
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE),
                    rs.getBoolean(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS),
                    rs.getString(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }
        return result.toArray(new CustomField[0]);
    }

    @Override
    public String getPatientTableName(String sid, int projectId) throws SQLException {

        TableSchema table = MedSavantDatabase.PatienttablemapTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        return rs.getString(1);
    }


    @Override
    public void createPatientTable(String sid, int projectid, CustomField[] fields) throws SQLException {

        String patientTableName = DBSettings.createPatientTableName(projectid);
        Connection c = ConnectionController.connectPooled(sid);


        //create basic fields
        String query =
                "CREATE TABLE `" + patientTableName + "` ("
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "` int(11) unsigned NOT NULL AUTO_INCREMENT,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD + "` varchar(100) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_GENDER + "` int(11) unsigned DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_AFFECTED + "` int(11) unsigned DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS + "` varchar(1000) COLLATE latin1_bin DEFAULT NULL,"
                + "`" + DefaultPatientTableSchema.COLUMNNAME_OF_BAM_URL + "` varchar(5000) COLLATE latin1_bin DEFAULT NULL,";

        for (CustomField field : fields) {
            query += field.generateSchema();
        }

        query += "PRIMARY KEY (`" + DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID + "`)"
                + ") ENGINE=MyISAM;";

        //create patientFormatTable
        c.createStatement().execute(query);

        //add to tablemap
        TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
        InsertQuery query1 = new InsertQuery(patientMapTable.getTable());
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
        query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME), patientTableName);
        c.createStatement().executeUpdate(query1.toString());

        //populate format patientFormatTable
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
        c.setAutoCommit(false);
        for (int i = 0; i < fields.length; i++) {
            CustomField a = fields[i];
            InsertQuery query2 = new InsertQuery(patientFormatTable.getTable());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectid);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i);
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), a.getColumnName());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), a.getColumnTypeString());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (a.isFilterable() ? "1" : "0"));
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), a.getAlias());
            query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), a.getDescription());
            c.createStatement().executeUpdate(query2.toString());
        }
        c.commit();
        c.setAutoCommit(true);
        c.close();

    }

    @Override
    public void removePatient(String sid, int projectId, int[] patientIds) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        Connection c = ConnectionController.connectPooled(sid);
        c.setAutoCommit(false);
        for (int id : patientIds) {
            //remove all references
            CohortManager.getInstance().removePatientReferences(sid,projectId, id);

            //remove from patient patientFormatTable
            DeleteQuery query = new DeleteQuery(table.getTable());
            query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), id));
            c.createStatement().executeUpdate(query.toString());
        }
        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    @Override
    public void addPatient(String sid, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        InsertQuery query = new InsertQuery(table.getTable());
        for (int i = 0; i < Math.min(cols.size(), values.size()); i++) {
            query.addColumn(new DbColumn(table.getTable(), cols.get(i).getColumnName(), cols.get(i).getColumnTypeString(), 100), values.get(i));
        }

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public Map<Object, List<String>> getDNAIDsForValues(String sessID, int projID, String columnName) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sessID,projID);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID,tablename);

        DbColumn currentDNAID = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAID, testColumn);

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        Map<Object, List<String>> map = new HashMap<Object, List<String>>();
        while (rs.next()) {
            Object o = rs.getObject(columnName);
            if (o == null) o = "";
            if (map.get(o) == null) map.put(o, new ArrayList<String>());
            String dnaIdsString = rs.getString(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
            if (dnaIdsString == null) continue;
            String[] dnaIds = dnaIdsString.split(",");
            for (String id : dnaIds) {
                if (!map.get(o).contains(id)) {
                    map.get(o).add(id);
                }
            }
        }
        return map;
    }

    @Override
    public List<String> getDNAIDsWithValuesInRange(String sessID, int projID, String columnName, Range r) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sessID,projID);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        DbColumn currentDNAID = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAID);
        q.addCondition(BinaryCondition.greaterThan(testColumn, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(testColumn, r.getMax(), true));
        
        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<String> result = new ArrayList<String>();
        while (rs.next()) {
            String s = rs.getString(1);
            String[] dnaIDs;
            if (s == null) {
                dnaIDs = new String[]{"null"};
            } else {
                dnaIDs = s.split(",");
            }
            for (String id : dnaIDs) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getDNAIDsForStringList(String sessID, TableSchema table, List<String> list, String columnName) throws SQLException {

        DbColumn currentDNAID = table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAID);

        Condition[] conditions = new Condition[list.size()];
        for (int i = 0; i < list.size(); i++) {
            conditions[i] = BinaryConditionMS.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        List<String> result = new ArrayList<String>();
        while (rs.next()) {
            String current = rs.getString(1);
            if (current == null) continue;
            String[] dnaIDs = current.split(",");
            for (String id : dnaIDs) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public void updateFields(String sessID, int projID, CustomField[] newFields) throws SQLException, RemoteException {

        List<CustomField> currentFields = Arrays.asList(getCustomPatientFields(sessID, projID));
        List<CustomField> fields = Arrays.asList(newFields);

        String tablename = getPatientTableName(sessID, projID);
        //TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(tablename);
        TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;

        Connection c = ConnectionController.connectPooled(sessID);
        c.setAutoCommit(false);

        //remove unused fields
        for (CustomField f : currentFields) {
            if (!fields.contains(f)) {
                DeleteQuery q = new DeleteQuery(patientFormatTable.getTable());
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName()));
                c.createStatement().execute(q.toString());

                String q1 = "ALTER TABLE `" + tablename + "` DROP COLUMN `" + f.getColumnName() + "`";
                c.createStatement().execute(q1);
            }
        }

        //modify old fields, add new fields
        int tempPos = 5002;
        for (CustomField f : fields) {
            if (currentFields.contains(f)) {
                UpdateQuery q = new UpdateQuery(patientFormatTable.getTable());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), f.getAlias());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), f.getDescription());
                q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (f.isFilterable() ? "1" : "0"));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
                q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName()));
                c.createStatement().executeUpdate(q.toString());
            } else {
                InsertQuery q = new InsertQuery(patientFormatTable.getTable());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID);
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), tempPos++);
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), f.getColumnName());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), f.getColumnTypeString());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (f.isFilterable() ? "1" : "0"));
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), f.getAlias());
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), f.getDescription());
                c.createStatement().executeUpdate(q.toString());

                String q1 = "ALTER TABLE `" + tablename + "` ADD " + f.generateSchema().replaceAll(",", "");
                c.createStatement().execute(q1);
            }
        }

        c.commit();
        c.setAutoCommit(true);

        TableSchema patientTable = CustomTables.getInstance().getCustomTableSchema(sessID,tablename, true);
        List<DbColumn> columns = patientTable.getColumns();
        List<DbColumn> defaultColumns = MedSavantDatabase.DefaultPatientTableSchema.getColumns();
        c.setAutoCommit(false);
        int i = 0;
        for (DbColumn col : columns) {
            boolean isDefault = false;
            for (DbColumn a : defaultColumns) {
                if (col.getColumnNameSQL().equals(a.getColumnNameSQL())) {
                    isDefault = true;
                }
            }
            if (isDefault) continue;

            UpdateQuery q = new UpdateQuery(patientFormatTable.getTable());
            q.addSetClause(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i++);
            q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID));
            q.addCondition(BinaryConditionMS.equalTo(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), col.getColumnNameSQL()));
            c.createStatement().executeUpdate(q.toString());

        }

        c.commit();
        c.setAutoCommit(true);
        c.close();
    }

    /*
     * Given a list of values for field A, get the corresponding values from field B
     */
    @Override
    public List<Object> getValuesFromField(String sid, int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(columnNameB));
        Condition[] conditions = new Condition[values.size()];
        for (int i = 0; i < values.size(); i++) {
            conditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(columnNameA), values.get(i));
        }
        query.addCondition(ComboCondition.or(conditions));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Object> result = new ArrayList<Object>();
        while (rs.next()) {
            result.add(rs.getObject(1));
        }

        return result;
    }

    @Override
    public List<String> getDNAIDsFromField(String sessID, int projID, String columnNameA, List<Object> values) throws SQLException, RemoteException {

        List<Object> l1 = getValuesFromField(sessID,projID, columnNameA, DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS, values);
        List<String> result = new ArrayList<String>();
        for (Object o : l1) {
            String[] dnaIds = ((String) o).split(",");
            for (String id : dnaIds) {
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> getValuesFromDNAIDs(String sessID, int projID, String columnNameB, List<String> ids) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(columnNameB));
        Condition[] conditions = new Condition[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            conditions[i] = BinaryCondition.like(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS), "%" + ids.get(i) + "%");
        }
        query.addCondition(ComboCondition.or(conditions));


        String s = query.toString();
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<String> result = new ArrayList<String>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }

        return result;
    }

    @Override
    public List<Object[]> getFamily(String sessID, int projID, String famID) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());

        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID));

        // TODO: replace with OO names
        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIOMOM));
        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_IDBIODAD));
        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID));
        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_GENDER));
        query.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_AFFECTED));

        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID), famID));

        String s = query.toString();
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] r = new Object[6];
            r[0] = rs.getString(1);
            r[1] = rs.getString(2);
            r[2] = rs.getString(3);
            r[3] = rs.getInt(4);
            r[4] = rs.getInt(5);
            r[5] = rs.getInt(6);
            result.add(r);
        }

        return result;
    }


    @Override
    public List<Object[]> getFamilyOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException {

        String famID = getFamilyIDOfPatient(sessID, projID, patID);
        if (famID == null) {
            return new ArrayList<Object[]>();
        }

        return getFamily(sessID,projID, famID);
    }

    @Override
    public String getFamilyIDOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException {
        String tablename = getPatientTableName(sessID,projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID,tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_PATIENT_ID), patID));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, q1.toString());

        if (!rs1.next()) {
            return null;
        }

        return rs1.getString(1);
    }

    @Override
    public List<String> getFamilyIDs(String sessID, int projID) throws SQLException, RemoteException {

        String tableName = getPatientTableName(sessID,projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableName);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID));

        q1.setIsDistinct(true);

        ResultSet rs1 = ConnectionController.executeQuery(sessID, q1.toString());

        List<String> ids = new ArrayList<String>();
        while (rs1.next()) {
            ids.add(rs1.getString(1));
        }

        ids.remove(null);

        return ids;
    }


    //SELECT `dna_ids` FROM `z_patient_proj1` WHERE `family_id` = 'AB0001' AND `dna_ids` IS NOT null;
    @Override
    public Map<String,String> getDNAIDsForFamily(String sessID, int projID, String famID) throws SQLException, RemoteException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_HOSPITAL_ID));
        q1.addColumns(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_DNA_IDS));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(DefaultPatientTableSchema.COLUMNNAME_OF_FAMILY_ID), famID));


        ResultSet rs1 = ConnectionController.executeQuery(sessID, q1.toString());

        Map<String,String> patientIDToDNAIDMap = new HashMap<String,String>();

        //List<String> ids = new ArrayList<String>();
        while (rs1.next()) {
            String patientID = rs1.getString(1);
            String DNAIDString = rs1.getString(2);
            if (DNAIDString != null && !DNAIDString.isEmpty()) {
                patientIDToDNAIDMap.put(patientID, DNAIDString);
            }
        }

        return patientIDToDNAIDMap;
    }

    @Override
    public void clearPatients(String sessID, int projID) throws SQLException, RemoteException{

        String tableName = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableName);

        DeleteQuery query = new DeleteQuery(table.getTable());

        ConnectionController.executeUpdate(sessID, query.toString());
    }

    @Override
    public List<String> parseDNAIDs(String s) {
        List<String> result = new ArrayList<String>();
        if (s == null) return result;
        String[] dnaIDs = s.split(",");
        for (String id : dnaIDs) {
            if (!result.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    @Override
    public List<String> getDNAIDsForHPOID(String sessID, int projID, String id) throws SQLException, RemoteException {

        //TODO: make a prepared statement
        String query = "SELECT dna_ids FROM " + getPatientTableName(sessID, projID)  + " WHERE " + MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO + "='" + id + "';";

        ResultSet rs = ConnectionController.executeQuery(sessID, query);

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }

        return results;
    }

    @Override
    public boolean hasOptionalField(String sessID, int pid, String fieldName) throws SQLException {
        String tableName = getPatientTableName(sessID, pid);
        return DBUtils.fieldExists(sessID, tableName, MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO);
    }
}
