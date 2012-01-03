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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VarianttagTableSchema;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.shared.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.DBUtil;
import org.ut.biolab.medsavant.db.util.query.api.VariantQueryUtilAdapter;

/**
 *
 * @author Andrew
 */
public class VariantQueryUtil extends java.rmi.server.UnicastRemoteObject implements VariantQueryUtilAdapter {

    private static VariantQueryUtil instance;

    public static VariantQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new VariantQueryUtil();
        }
        return instance;
    }

    public VariantQueryUtil() throws RemoteException {}


    @Override
    public TableSchema getCustomTableSchema(String sessionId, int projectId, int referenceId) throws SQLException, RemoteException {
        return CustomTables.getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId));
    }


    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, int start, int limit) throws SQLException, RemoteException {
        return getVariants(sessionId,projectId, referenceId, new Condition[1][], start, limit);
    }

    public List<Object[]> getVariants(String sessionId,int projectId, int referenceId, Condition[][] conditions, int start, int limit) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sessionId,ProjectQueryUtil.getInstance().getVariantTablename(sessionId,projectId, referenceId));
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        addConditionsToQuery(query, conditions);

        Connection conn = ConnectionController.connectPooled(sessionId);

        long startTime = System.currentTimeMillis();

        System.out.println(query.toString() + " LIMIT " + start + ", " + limit);

        ResultSet rs = conn.createStatement().executeQuery(query.toString() + " LIMIT " + start + ", " + limit);

        System.out.println("Time to execute query: " + (((double) System.currentTimeMillis() - startTime) / 1000) + "s");
        startTime = System.currentTimeMillis();

        ResultSetMetaData rsMetaData = rs.getMetaData();
        int numberColumns = rsMetaData.getColumnCount();

        List<Object[]> result = new ArrayList<Object[]>();
        while (rs.next()) {
            Object[] v = new Object[numberColumns];
            for (int i = 1; i <= numberColumns; i++) {
                v[i - 1] = rs.getObject(i);
            }
            result.add(v);
        }

        System.out.println("Time to parse results: " + (((double) System.currentTimeMillis() - startTime) / 1000) + "s");

        return result;
    }

    public double[] getExtremeValuesForColumn(String sid,String tablename, String columnname) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.min().addColumnParams(table.getDBColumn(columnname)));
        query.addCustomColumns(FunctionCall.max().addColumnParams(table.getDBColumn(columnname)));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());
        rs.next();
        return new double[]{rs.getDouble(1), rs.getDouble(2)};
    }

    public List<String> getDistinctValuesForColumn(String sid, String tablename, String columnname) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,tablename);

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.setIsDistinct(true);
        query.addColumns(table.getDBColumn(columnname));
        query.addOrdering(table.getDBColumn(columnname), Dir.ASCENDING);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString());

        List<String> result = new ArrayList<String>();
        while (rs.next()) {
            String val = rs.getString(1);
            if (val == null) {
                result.add("");
            } else {
                result.add(val);
            }
        }

        return result;
    }

    public int getNumFilteredVariants(String sid, int projectId, int referenceId) throws SQLException, RemoteException {
        return getNumFilteredVariants(sid,projectId, referenceId, new Condition[0][]);
    }

    public int getNumFilteredVariants(String sid,int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException {

        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId);

        if (name == null) {
            return -1;
        }

        TableSchema table = CustomTables.getCustomTableSchema(sid,name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public int getNumVariantsForDnaIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds) throws SQLException, RemoteException {
        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId);

        if (name == null) {
            return -1;
        }

        if(dnaIds.isEmpty()){
            return 0;
        }

        TableSchema table = CustomTables.getCustomTableSchema(sid,name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);

        Condition[] dnaConditions = new Condition[dnaIds.size()];
        for(int i = 0; i < dnaIds.size(); i++){
            dnaConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
        }
        q.addCondition(ComboCondition.or(dnaConditions));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public int getFilteredFrequencyValuesForColumnInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String columnname, double min, double max) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId));

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(columnname), min, true));
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(columnname), max, false));
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());
        rs.next();

        return rs.getInt(1);
    }

    public Map<String, Integer> getFilteredFrequencyValuesForColumn(String sid, int projectId, int referenceId, Condition[][] conditions, String columnAlias) throws SQLException, RemoteException {

        TableSchema tableSchema = CustomTables.getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId));
        DbTable table = tableSchema.getTable();
        DbColumn col = tableSchema.getDBColumnByAlias(columnAlias);

        return getFilteredFrequencyValuesForColumn(sid,table, conditions, col);
    }

    public Map<String, Integer> getFilteredFrequencyValuesForColumn(String sid, DbTable table, Condition[][] conditions, DbColumn column) throws SQLException {

        SelectQuery q = new SelectQuery();
        q.addFromTable(table);
        q.addColumns(column);
        q.addCustomColumns(FunctionCall.countAll());
        addConditionsToQuery(q, conditions);
        q.addGroupings(column);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            String key = rs.getString(1);
            if (key == null) {
                key = "";
            }
            map.put(key, rs.getInt(2));
        }

        return map;
    }

    public int getNumVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId));

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());
        q.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        q.addCondition(BinaryCondition.greaterThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start, true));
        q.addCondition(BinaryCondition.lessThan(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end, false));
        addConditionsToQuery(q, conditions);

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        rs.next();
        return rs.getInt(1);
    }

    public Map<String, Map<Range, Integer>> getChromosomeHeatMap(String sid, int projectId, int referenceId, Condition[][] conditions, int binsize) throws SQLException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());

        queryBase.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM));

        String roundFunction = "ROUND(" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "/" + binsize + ",0)";

        queryBase.addCustomColumns(FunctionCall.countAll());
        queryBase.addGroupings(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM));


        addConditionsToQuery(queryBase, conditions);

        String query = queryBase.toString().replace("COUNT(*)", "COUNT(*)," + roundFunction) + "," + roundFunction;

        Connection conn = ConnectionController.connectPooled(sid);
        ResultSet rs = conn.createStatement().executeQuery(query);

        Map<String, Map<Range, Integer>> results = new HashMap<String, Map<Range, Integer>>();
        while (rs.next()) {

            String chrom = rs.getString(1);

            Map<Range, Integer> chromMap;
            if (!results.containsKey(chrom)) {
                chromMap = new HashMap<Range, Integer>();
            } else {
                chromMap = results.get(chrom);
            }

            int binNo = rs.getInt(3);
            Range binRange = new Range(binNo * binsize, (binNo + 1) * binsize);

            int count = rs.getInt(2);

            chromMap.put(binRange, count);
            results.put(chrom, chromMap);
        }

        return results;


        //TODO
        /*
        String query = "select y.range as `range`, count(*) as `number of occurences` "
        + "from ("
        + "select case ";
        int pos = 0;
        for(int i = 0; i < numbins; i++) {
        query += "when `" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "` between " + pos + " and " + (pos+binsize) + " then " + i + " ";
        pos += binsize;
        }

        query += "end as `range` "
        + "from (";
        query += queryBase.toString();
        query += ") x ) y "
        + "group by y.`range`";


        Connection conn = ConnectionController.connectPooled();
        ResultSet rs = conn.createStatement().executeQuery(query.toString());

        int[] numRows = new int[numbins];
        for(int i = 0; i < numbins; i++) numRows[i] = 0;
        while (rs.next()) {
        int index = rs.getInt(1);
        numRows[index] = rs.getInt(2);
        }
        return numRows;
         *
         */
    }

    public int[] getNumVariantsForBins(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException, RemoteException {

        TableSchema table = CustomTables.getCustomTableSchema(sid,ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId));

        SelectQuery queryBase = new SelectQuery();
        queryBase.addFromTable(table.getTable());
        queryBase.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION));
        queryBase.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom));
        addConditionsToQuery(queryBase, conditions);

        /*String queryBase =
        "SELECT `" + VariantTable.FIELDNAME_POSITION + "`" +
        " FROM " + ProjectQueryUtil.getVariantTablename(projectId, referenceId) + " t0" +
        " WHERE `" + VariantTable.FIELDNAME_CHROM + "`=\"" + chrom + "\"";
        if(!conditions.isEmpty()) {
        queryBase += " AND ";
        }
        queryBase += conditionsToStringOr(conditions);*/


        //TODO
        String query = "select y.range as `range`, count(*) as `number of occurences` "
                + "from ("
                + "select case ";
        int pos = 0;
        for (int i = 0; i < numbins; i++) {
            query += "when `" + DefaultVariantTableSchema.COLUMNNAME_OF_POSITION + "` between " + pos + " and " + (pos + binsize) + " then " + i + " ";
            pos += binsize;
        }

        query += "end as `range` "
                + "from (";
        query += queryBase.toString();
        query += ") x ) y "
                + "group by y.`range`";


        Connection conn = ConnectionController.connectPooled(sid);
        ResultSet rs = conn.createStatement().executeQuery(query.toString());

        int[] numRows = new int[numbins];
        for (int i = 0; i < numbins; i++) {
            numRows[i] = 0;
        }
        while (rs.next()) {
            int index = rs.getInt(1);
            numRows[index] = rs.getInt(2);
        }
        return numRows;
    }

    public void uploadFileToVariantTable(String sid, File file, String tableName) throws SQLException {

        // TODO: for some reason the connection is closed going into this function
        Connection c = ConnectionController.connectPooled(sid);

        System.out.println("Uploading file to variant table: "
                + "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");

        Statement s = c.createStatement();
        s.setQueryTimeout(60 * 60); // 1 hour
        s.execute(
                "LOAD DATA LOCAL INFILE '" + file.getAbsolutePath().replaceAll("\\\\", "/") + "' "
                + "INTO TABLE " + tableName + " "
                + "FIELDS TERMINATED BY ',' ENCLOSED BY '\"' "
                + "LINES TERMINATED BY '\\r\\n';");
    }

    public int getNumPatientsWithVariantsInRange(String sid, int projectId, int referenceId, Condition[][] conditions, String chrom, int start, int end) throws SQLException, RemoteException {

        TableSchema table = getCustomTableSchema(sid,projectId, referenceId);
        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns("COUNT(DISTINCT " + DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID + ")");
        addConditionsToQuery(q, conditions);

        Condition[] cond = new Condition[3];
        cond[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM), chrom);
        cond[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), start);
        cond[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION), end);
        q.addCondition(ComboCondition.and(cond));

        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");

        Statement s = ConnectionController.connectPooled(sid).createStatement();
        ResultSet rs = s.executeQuery(query);
        rs.next();

        int numrows = rs.getInt(1);

        return numrows;
    }

    public void addConditionsToQuery(SelectQuery query, Condition[][] conditions) {
        Condition[] c = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            c[i] = ComboCondition.and(conditions[i]);
        }
        query.addCondition(ComboCondition.or(c));
    }

    public Map<String, List<String>> getSavantBookmarkPositionsForDNAIds(String sid, int projectId, int referenceId, Condition[][] conditions, List<String> dnaIds, int limit) throws SQLException, RemoteException {

        Map<String, List<String>> results = new HashMap<String, List<String>>();

        TableSchema table = getCustomTableSchema(sid,projectId, referenceId);
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID),
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM),
                table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION));
        addConditionsToQuery(query, conditions);
        Condition[] dnaIdConditions = new Condition[dnaIds.size()];
        for (int i = 0; i < dnaIds.size(); i++) {
            dnaIdConditions[i] = BinaryConditionMS.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        query.addCondition(ComboCondition.or(dnaIdConditions));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(query.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));

        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3) - 100) + "-" + (rs.getLong(3) + 100));
        }

        return results;
    }

    public Map<String, Integer> getNumVariantsInFamily(String sid, int projectId, int referenceId, String familyId, Condition[][] conditions) throws SQLException, RemoteException {

        String name = ProjectQueryUtil.getInstance().getVariantTablename(sid,projectId, referenceId);

        if (name == null) {
            return null;
        }

        TableSchema table = CustomTables.getCustomTableSchema(sid,name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));
        q.addCustomColumns(FunctionCall.countAll());
        q.addGroupings(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID));
        addConditionsToQuery(q, conditions);

        Map<String, String> patientToDNAIDMap = PatientQueryUtil.getInstance().getDNAIdsForFamily(sid,projectId, familyId);
        Map<String, List<String>> betterPatientToDNAIDMap = new HashMap<String, List<String>>();

        List<String> dnaIDs = new ArrayList<String>();
        for (String patientID : patientToDNAIDMap.keySet()) {
            String dnaIDString = patientToDNAIDMap.get(patientID);
            List<String> idList = new ArrayList<String>();
            for (String dnaID : dnaIDString.split(",")) {
                if (dnaID != null && !dnaID.isEmpty()) {
                    dnaIDs.add(dnaID);
                    idList.add(dnaID);
                }
            }
            betterPatientToDNAIDMap.put(patientID, idList);
        }
        patientToDNAIDMap = null; // we don't need it anymore; use betterPatientToDNAIDMap instead

        Map<String, Integer> dnaIDsToCountMap = new HashMap<String, Integer>();

        if (!dnaIDs.isEmpty()) {

            Condition[] dnaIDConditions = new Condition[dnaIDs.size()];

            int i = 0;
            for (String dnaID : dnaIDs) {
                dnaIDConditions[i] = BinaryCondition.equalTo(table.getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_DNA_ID), dnaID);
                i++;
            }

            q.addCondition(ComboCondition.or(dnaIDConditions));

            System.out.println(q);

            ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

            while (rs.next()) {
                dnaIDsToCountMap.put(rs.getString(1), rs.getInt(2));
            }
        } else {
            System.out.println("No DNA IDS in family");
        }

        Map<String, Integer> patientIDTOCount = new HashMap<String, Integer>();
        for (String patientID : betterPatientToDNAIDMap.keySet()) {
            int count = 0;
            for (String dnaID : betterPatientToDNAIDMap.get(patientID)) {
                if (dnaIDsToCountMap.containsKey(dnaID)) {
                    count += dnaIDsToCountMap.get(dnaID);
                }
            }
            patientIDTOCount.put(patientID, count);
            //System.out.println("Number of variants for: " + patientID + " = " + count);
        }

        return patientIDTOCount;
    }

    public void cancelUpload(String sid,int uploadId, String tableName) {
        try {

            //remove log entry
            AnnotationLogQueryUtil.getInstance().removeAnnotationLogEntry(sid,uploadId);

            //drop staging table
            DBUtil.dropTable(sid,tableName);

        } catch (RemoteException ex) {
            Logger.getLogger(VariantQueryUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(VariantQueryUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addTagsToUpload(String sid, int uploadID, String[][] variantTags) throws SQLException {

        Connection conn = ConnectionController.connectPooled(sid);
        TableSchema variantTagTable = MedSavantDatabase.VarianttagTableSchema;

        conn.setAutoCommit(false);

        //add tags
        for (int i = 0; i < variantTags.length && !Thread.currentThread().isInterrupted(); i++) {
            InsertQuery query = new InsertQuery(variantTagTable.getTable());
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID), uploadID);
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), variantTags[i][0]);
            query.addColumn(variantTagTable.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE), variantTags[i][1]);

            System.out.println(query);

            conn.createStatement().executeUpdate(query.toString());
        }
        if (Thread.currentThread().isInterrupted()) {
            conn.rollback();
        } else {
            conn.commit();
        }
        conn.setAutoCommit(true);
    }

    public List<String> getDistinctTagNames(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        List<String> tagNames = new ArrayList<String>();
        while (rs.next()) {
            tagNames.add(rs.getString(1));
        }

        return tagNames;
    }

    public List<String> getValuesForTagName(String sid, String tagName) throws SQLException {

        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.setIsDistinct(true);
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE));
        q.addCondition(BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), tagName));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        List<String> tagValues = new ArrayList<String>();
        while (rs.next()) {
            tagValues.add(rs.getString(1));
        }

        return tagValues;

    }

    public List<Integer> getUploadIDsMatchingVariantTags(String sid, String[][] variantTags) throws SQLException {
        TableSchema table = MedSavantDatabase.VarianttagTableSchema;

        /*
         * SELECT upload_id FROM (SELECT t17.upload_id, COUNT(*) AS count FROM variant_tag t17 WHERE (((t17.tagkey = 'Sequencer') AND (t17.tagvalue = 'SOLID')) OR ((t17.tagkey = 'Sequencer Version') AND (t17.tagvalue = '5500'))) GROUP BY t17.upload_id) as tbl WHERE count = 2;
         */


        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addColumns(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID));

        Condition[] orConditions = new Condition[variantTags.length];

        Set<String> seenConditions = new HashSet<String>();
        int duplicates = 0;

        for (int i = 0; i < variantTags.length; i++) {

            String strRepresentation = variantTags[i][0] + ":" + variantTags[i][1];

            if (seenConditions.contains(strRepresentation)) {
                duplicates++;
            } else {

                orConditions[i] = ComboCondition.and(new Condition[]{
                            BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGKEY), variantTags[i][0]),
                            BinaryCondition.equalTo(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_TAGVALUE), variantTags[i][1])});
                seenConditions.add(strRepresentation);
            }
        }

        q.addCondition(ComboCondition.or(orConditions));
        q.addGroupings(table.getDBColumn(VarianttagTableSchema.COLUMNNAME_OF_UPLOAD_ID));
        q.addHaving(BinaryCondition.equalTo(FunctionCall.countAll(), variantTags.length-duplicates));

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        List<Integer> results = new ArrayList<Integer>();
        while (rs.next()) {
            results.add(rs.getInt(1));
        }

        return results;
    }
}
