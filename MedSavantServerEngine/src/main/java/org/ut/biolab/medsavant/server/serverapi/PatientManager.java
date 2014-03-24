/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.serverapi;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
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
import org.apache.commons.logging.Log;
import org.ut.biolab.medsavant.shared.db.MedSavantDatabaseExtras;
import org.ut.biolab.medsavant.shared.db.TableSchema;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase.PatientFormatTableSchema;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.PatientTablemapTableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.server.db.util.CustomTables;
import org.ut.biolab.medsavant.server.db.util.DBSettings;
import org.ut.biolab.medsavant.server.db.util.DBUtils;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.model.Range;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.PatientManagerAdapter;


/**
 *
 * @author Andrew
 */
public class PatientManager extends MedSavantServerUnicastRemoteObject implements PatientManagerAdapter, BasicPatientColumns {

    Log LOG = org.apache.commons.logging.LogFactory.getLog(PatientManager.class);
    
    private static PatientManager instance;

    @Override
    public void test(CustomField[] c) throws RemoteException{
        System.out.println("Got custom field "+c[0]);
        System.out.println("Alias: "+c[0].getAlias());
        System.out.println("Col Name: "+c[0].getColumnName());
        System.out.println("Col Length: "+c[0].getColumnLength());
    }
    
    private PatientManager() throws RemoteException, SessionExpiredException {
    }

    public static synchronized PatientManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new PatientManager();
        }
        return instance;
    }

    @Override
    public List<Object[]> getBasicPatientInfo(String sid, int projectId, int limit) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sid,projectId);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(PATIENT_ID),
                table.getDBColumn(FAMILY_ID),
                table.getDBColumn(HOSPITAL_ID),
                table.getDBColumn(IDBIOMOM),
                table.getDBColumn(IDBIODAD),
                table.getDBColumn(GENDER),
                table.getDBColumn(AFFECTED),
                table.getDBColumn(DNA_IDS),
                table.getDBColumn(PHENOTYPES));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            result.add(new Object[] {
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getInt(6),
                rs.getInt(7),
                rs.getString(8),
                rs.getString(9)
            });
        }
        return result;
    }

    @Override
    public List<Object[]> getPatients(String sid, int projectId) throws SQLException, RemoteException, SessionExpiredException {
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
    public Object[] getPatientRecord(String sid, int projectId, int patientId) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sid,projectId);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PATIENT_ID), patientId));

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
    public List<String> getPatientFieldAliases(String sid, int projectId) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.PatientformatTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addOrdering(table.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), Dir.ASCENDING);

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<String> result = new ArrayList<String>();

        for (CustomField af: REQUIRED_PATIENT_FIELDS) {
            result.add(af.getAlias());
        }

        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    @Override
    public CustomField[] getPatientFields(String sessID, int projID) throws SQLException, SessionExpiredException {
        CustomField[] defaultFields = REQUIRED_PATIENT_FIELDS;
        CustomField[] customFields = getCustomPatientFields(sessID, projID);
        CustomField[] result = new CustomField[defaultFields.length + customFields.length];
        System.arraycopy(defaultFields, 0, result, 0, defaultFields.length);
        System.arraycopy(customFields, 0, result, defaultFields.length, customFields.length);
        return result;
    }

    @Override
    public CustomField[] getCustomPatientFields(String sessID, int projID) throws SQLException, SessionExpiredException {

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
    public String getPatientTableName(String sid, int projectId) throws SQLException, SessionExpiredException {

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
    public void createPatientTable(String sessID, int projID, CustomField[] fields) throws SQLException, SessionExpiredException {

        // Create basic fields.
        String tableName = DBSettings.createPatientTableName(projID);
        TableSchema patientSchema = new TableSchema(MedSavantDatabase.schema, tableName, BasicPatientColumns.class);
        for (CustomField field: fields) {
            patientSchema.addColumn(field.getColumnName(), field.getColumnType(), field.getColumnLength(), field.getColumnScale());
        }

        // make hospital id unique
        patientSchema.getDBColumn(BasicPatientColumns.HOSPITAL_ID).unique();

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.executeUpdate(patientSchema.getCreateQuery() + " ENGINE=MyISAM;");


            //add to tablemap
            TableSchema patientMapTable = MedSavantDatabase.PatienttablemapTableSchema;
            InsertQuery query1 = new InsertQuery(patientMapTable.getTable());
            query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PROJECT_ID), projID);
            query1.addColumn(patientMapTable.getDBColumn(PatientTablemapTableSchema.COLUMNNAME_OF_PATIENT_TABLENAME), tableName);
            conn.executeUpdate(query1.toString());

            //populate format patientFormatTable
            TableSchema patientFormatTable = MedSavantDatabase.PatientformatTableSchema;
            conn.setAutoCommit(false);
            for (int i = 0; i < fields.length; i++) {
                CustomField a = fields[i];
                InsertQuery query2 = new InsertQuery(patientFormatTable.getTable());
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_PROJECT_ID), projID);
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_POSITION), i);
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_NAME), a.getColumnName());
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), a.getTypeString());
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_FILTERABLE), (a.isFilterable() ? "1" : "0"));
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_ALIAS), a.getAlias());
                query2.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_DESCRIPTION), a.getDescription());
                conn.createStatement().executeUpdate(query2.toString());
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    @Override
    public void removePatient(String sid, int projectId, int[] patientIds) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        PooledConnection conn = ConnectionController.connectPooled(sid);
        try {
            conn.setAutoCommit(false);
            for (int id : patientIds) {
                //remove all references
                CohortManager.getInstance().removePatientReferences(sid,projectId, id);

                //remove from patient patientFormatTable
                DeleteQuery query = new DeleteQuery(table.getTable());
                query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(PATIENT_ID), id));
                conn.createStatement().executeUpdate(query.toString());
            }
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    
    // TODO: write JavaDoc, variables names cols and values are meaningless
    @Override
    public void addPatient(String sid, int projectId, List<CustomField> cols, List<String> values) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        InsertQuery query = new InsertQuery(table.getTable());
        for (int i = 0; i < Math.min(cols.size(), values.size()); i++) {
            query.addColumn(new DbColumn(table.getTable(), cols.get(i).getColumnName(), cols.get(i).getTypeString(), 100, 0), values.get(i));
        }

        ConnectionController.executeUpdate(sid,  query.toString());
    }
    
    @Override
    public void updatePatient(String sid, int projectId, int patientID, List<CustomField> cols, List<String> values) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sid,projectId);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sid,tablename);

        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(PATIENT_ID), patientID));
        for (int i = 0; i < Math.min(cols.size(), values.size()); i++) {
            query.addSetClause(table.getDBColumn(cols.get(i).getColumnName()), values.get(i));
        }

        ConnectionController.executeUpdate(sid,  query.toString());
    }

    @Override
    public Map<Object, List<String>> getDNAIDsForValues(String sessID, int projID, String columnName) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID,projID);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID,tablename);

        DbColumn currentDNAID = table.getDBColumn(DNA_IDS);
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
            String dnaIdsString = rs.getString(DNA_IDS.getColumnName());
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
    public List<String> getDNAIDsWithValuesInRange(String sessID, int projID, String columnName, Range r) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID,projID);

        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        DbColumn currentDNAID = table.getDBColumn(DNA_IDS);
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
    public List<String> getDNAIDsForStringList(String sessID, TableSchema table, List<String> list, String columnName, boolean allowInexactMatch) throws SQLException, SessionExpiredException {

        DbColumn currentDNAID = table.getDBColumn(DNA_IDS);
        DbColumn testColumn = table.getDBColumn(columnName);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAID);

        Condition[] conditions = new Condition[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String val = list.get(i);
            if (val.length() == 0) {
                // Users are humans (not computer programmers), so we treat empty strings as equivalent to null.
                conditions[i] = ComboCondition.or(BinaryCondition.equalTo(testColumn, ""), UnaryCondition.isNull(testColumn));
            } else {
                if (allowInexactMatch) {
                    conditions[i] = BinaryConditionMS.like(testColumn, "%" + val + "%");
                } else {
                    conditions[i] = BinaryConditionMS.equalTo(testColumn, val);
                }
            }
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
    public void updateFields(String sessID, int projID, CustomField[] newFields) throws SQLException, RemoteException, SessionExpiredException {

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
                q.addColumn(patientFormatTable.getDBColumn(PatientFormatTableSchema.COLUMNNAME_OF_COLUMN_TYPE), f.getTypeString());
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
        c.setAutoCommit(false);
        int i = 0;
        for (DbColumn col : columns) {
            boolean isDefault = false;
            for (CustomField a: BasicPatientColumns.REQUIRED_PATIENT_FIELDS) {
                if (col.getColumnNameSQL().equals(a.getColumnName())) {
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
    public List<Object> getValuesFromField(String sid, int projectId, String columnNameA, String columnNameB, List<Object> values) throws SQLException, RemoteException, SessionExpiredException {

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
    public List<String> getDNAIDsFromField(String sessID, int projID, String columnNameA, List<Object> values) throws SQLException, RemoteException, SessionExpiredException {

        List<Object> l1 = getValuesFromField(sessID,projID, columnNameA, DNA_IDS.getColumnName(), values);
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
    public Map<String,String> getValuesFromDNAIDs(String sessID, int projID, String columnNameB, List<String> ids) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(DNA_IDS));
        query.addColumns(table.getDBColumn(columnNameB));
        Condition[] conditions = new Condition[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            conditions[i] = BinaryCondition.like(table.getDBColumn(DNA_IDS), "%" + ids.get(i) + "%");
        }
        query.addCondition(ComboCondition.or(conditions));

        String s = query.toString();
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        Map<String,String> result = new HashMap<String,String>();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getString(2));
        }

        return result;
    }

    @Override
    public List<Object[]> getFamily(String sessID, int projID, String famID) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());

        query.addColumns(table.getDBColumn(HOSPITAL_ID));
        query.addColumns(table.getDBColumn(IDBIOMOM));
        query.addColumns(table.getDBColumn(IDBIODAD));
        query.addColumns(table.getDBColumn(PATIENT_ID));
        query.addColumns(table.getDBColumn(GENDER));
        query.addColumns(table.getDBColumn(AFFECTED));
        query.addColumns(table.getDBColumn(DNA_IDS));

        query.addCondition(BinaryCondition.equalTo(table.getDBColumn(FAMILY_ID), famID));

        String s = query.toString();
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] r = new Object[7];
            r[0] = rs.getString(1);
            r[1] = rs.getString(2);
            r[2] = rs.getString(3);
            r[3] = rs.getInt(4);
            r[4] = rs.getInt(5);
            r[5] = rs.getInt(6);
            r[6] = rs.getString(7);
            result.add(r);
        }

        return result;
    }


    @Override
    public List<Object[]> getFamilyOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException, SessionExpiredException {

        String famID = getFamilyIDOfPatient(sessID, projID, patID);
        if (famID == null) {
            return new ArrayList<Object[]>();
        }

        return getFamily(sessID,projID, famID);
    }

    @Override
    public String getFamilyIDOfPatient(String sessID, int projID, int patID) throws SQLException, RemoteException, SessionExpiredException {
        String tablename = getPatientTableName(sessID,projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID,tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(FAMILY_ID));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(PATIENT_ID), patID));

        ResultSet rs1 = ConnectionController.executeQuery(sessID, q1.toString());

        if (!rs1.next()) {
            return null;
        }

        return rs1.getString(1);
    }

    @Override
    public List<String> getFamilyIDs(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException {

        String tableName = getPatientTableName(sessID,projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tableName);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(FAMILY_ID));

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
    public Map<String,String> getDNAIDsForFamily(String sessID, int projID, String famID) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery q1 = new SelectQuery();
        q1.addFromTable(table.getTable());
        q1.addColumns(table.getDBColumn(HOSPITAL_ID));
        q1.addColumns(table.getDBColumn(DNA_IDS));
        q1.addCondition(BinaryCondition.equalTo(table.getDBColumn(FAMILY_ID), famID));


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
    public void clearPatients(String sessID, int projID) throws SQLException, RemoteException, SessionExpiredException{

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
    public List<String> getDNAIDsForHPOID(String sessID, int projID, String id) throws SQLException, RemoteException, SessionExpiredException {

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
    public boolean hasOptionalField(String sessID, int pid, String fieldName) throws SQLException, SessionExpiredException {
        String tableName = getPatientTableName(sessID, pid);
        return DBUtils.fieldExists(sessID, tableName, MedSavantDatabaseExtras.OPTIONAL_PATIENT_FIELD_HPO);
    }

    @Override
    public String getReadAlignmentPathForDNAID(String sessID, int projID, String dnaID) throws SQLException, RemoteException, SessionExpiredException {

        String tablename = getPatientTableName(sessID, projID);
        TableSchema table = CustomTables.getInstance().getCustomTableSchema(sessID, tablename);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(BAM_URL));
        q.addCondition(BinaryCondition.like(table.getDBColumn(DNA_IDS), dnaID));

        ResultSet rs = ConnectionController.executeQuery(sessID, q.toString());

        String bamURL = null;

        //List<String> ids = new ArrayList<String>();
        while (rs.next()) {
            bamURL = rs.getString(1);
            if ("".equals(bamURL)) {
                bamURL = null;
            }
        }

        return bamURL;
    }
}
