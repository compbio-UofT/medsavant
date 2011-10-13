/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.olddb;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.OrderObject.Dir;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.SettingsController;
import org.ut.biolab.medsavant.olddb.table.AlignmentTableSchema;
import org.ut.biolab.medsavant.olddb.table.CohortTableSchema;
import org.ut.biolab.medsavant.olddb.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListTableSchema;
import org.ut.biolab.medsavant.olddb.table.GeneListViewTableSchema;
import org.ut.biolab.medsavant.olddb.table.GenomeTableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.record.BEDRecord;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.Range;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class QueryUtil {

    public static List<String> getDistinctValuesForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {
        return getDistinctValuesForColumn(conn, t, col, -1);
    }
    
    public static List<String> getDistinctValuesForColumn(Connection conn, TableSchema t, DbColumn col, int limit) throws SQLException {

        if (t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get distinct values for numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(col);
        q.addFromTable(t.getTable());
        
        Statement s = conn.createStatement();    
        String queryString = q.toString();
        if(limit > 0) queryString = queryString + " LIMIT " + limit;
        ResultSet rs = s.executeQuery(queryString);
        
        List<String> distinctValues = new ArrayList<String>();

        while(rs.next()) {
            String val = rs.getString(1);
            if(val == null){
                distinctValues.add("");
            } else {
                distinctValues.add(val);
            }
        }

        Collections.sort(distinctValues);

        return distinctValues;
    }
    
    /*
     * Only use this if you are sure the numeric column has a small number of values. 
     */
    public static List<String> getDistinctNumericValuesForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(col);
        q.addFromTable(t.getTable());
        
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<Integer> distinctValues = new ArrayList<Integer>();

        while(rs.next()) {
            distinctValues.add(rs.getInt(1));
        }
        
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2) {
                if((Integer) o1 < (Integer) o2) return -1;
                return 1;
            }
        };

        Collections.sort(distinctValues, c);
        
        List<String> distinctStringValues = new ArrayList<String>();
        for(Integer i : distinctValues){
            distinctStringValues.add(i.toString());
        }

        return distinctStringValues;
    }
    
    public static List<Vector> getDistinctValuesForColumns(Connection conn, TableSchema t, DbColumn[] cols, Object[][] columnTypeIndices, DbColumn orderColumn, Dir dir, int limit) throws SQLException {

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(cols);
        q.addFromTable(t.getTable());
        q.addOrdering(orderColumn, dir);
        
        Statement s = conn.createStatement();
        String queryString = q.toString();
        if(limit > 0) queryString = queryString + " LIMIT " + limit;
        ResultSet rs = s.executeQuery(queryString);

        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(columnTypeIndices, rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException(ex.getMessage());
        }

        return results;
    }
    

    public static int getNumRowsInTable(Connection c, Table t) throws SQLException {

        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = new SelectQuery();
        q.addFromTable(t);
        q.addCustomColumns(count);

        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }

    public static Range getExtremeValuesForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {

        if (!t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get extreme values for non-numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addCustomColumns(FunctionCall.min().addColumnParams(col));
        q.addCustomColumns(FunctionCall.max().addColumnParams(col));

        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        double min = 0;
        double max = 0;

        ColumnType type = t.getColumnType(col);

        while(rs.next()) {
            switch(type) {
                case INTEGER:
                    min = rs.getInt(1);
                    max = rs.getInt(2);
                    break;
                case FLOAT:
                    min = rs.getFloat(1);
                    max = rs.getFloat(2);
                    break;
                case DECIMAL:
                    min = rs.getDouble(1);
                    max = rs.getDouble(2);
                    break;
                default:
                    throw new FatalDatabaseException("Unhandled column type: " + type);
            }
        }

        return new Range(min,max);

    }
    
    public static int getMaxValueForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {
        
        if (!t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get extreme values for non-numeric field : " + col.getAbsoluteName());
        }
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addCustomColumns(FunctionCall.max().addColumnParams(col));
        
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();
        
        return rs.getInt(1);
    }

    public static List<String> getDistinctDNAIds() throws SQLException, NonFatalDatabaseException {
        return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getVariantTableSchema(),
                    MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_DNAID));
    }

    /*public static List<String> getDistinctPatientIDs() throws SQLException, NonFatalDatabaseException {
                return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getSubjectTableSchema(),
                    MedSavantDatabase.getInstance().getSubjectTableSchema().getDBColumn(SubjectTableSchema.ALIAS_HOSPITALID));
    }

    public static List<Vector> getPatientRecord(String pid) throws NonFatalDatabaseException, SQLException {
        return QueryUtil.getRecordsMatchingID(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getSubjectTableSchema(),
                    MedSavantDatabase.getInstance().getSubjectTableSchema().getDBColumn(SubjectTableSchema.ALIAS_HOSPITALID),
                    pid);
    }*/
    
    public static List<String> getDistinctPatientIDs() throws SQLException, NonFatalDatabaseException {
                
        //TODO:dbref
        return new ArrayList<String>();
    }

    public static List<Vector> getPatientRecord(String pid) throws NonFatalDatabaseException, SQLException {
        //TODO:dbref
        return new ArrayList<Vector>();
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, String id) throws SQLException {
        return getRecordsMatchingID(conn,t,col,id,-1);
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, int id) throws SQLException {
        return getRecordsMatchingID(conn,t,col,id,-1);
    }

    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, String id, int limit) throws SQLException {
        
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(t.getTable());
        
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,col,id));

        Statement s = conn.createStatement();
        
        ResultSet rs = s.executeQuery(q.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));
        
        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(t.getColumnGrid(), rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException(ex.getMessage());
        }
        
        return results;
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, int id, int limit) throws SQLException {
        
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(t.getTable());
        
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,col,id));

        Statement s = conn.createStatement();
        
        ResultSet rs = s.executeQuery(q.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));
        
        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(t.getColumnGrid(), rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException(ex.getMessage());
        }
        
        return results;
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, DbColumn orderby, int id, int limit) throws SQLException {
        
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(t.getTable());
        q.addOrdering(orderby, Dir.ASCENDING);
        
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,col,id));

        Statement s = conn.createStatement();
        
        ResultSet rs = s.executeQuery(q.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));
        
        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(t.getColumnGrid(), rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException(ex.getMessage());
        }
        
        return results;
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, DbColumn orderby, String id, int limit) throws SQLException {
        
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(t.getTable());
        q.addOrdering(orderby, Dir.ASCENDING);
        
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,col,id));

        Statement s = conn.createStatement();
        
        ResultSet rs = s.executeQuery(q.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));
        
        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(t.getColumnGrid(), rs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDatabaseException(ex.getMessage());
        }
        
        return results;
    }

    public static List<String> getDistinctCohortNames(int limit) throws NonFatalDatabaseException, SQLException {
         return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getCohortTableSchema(),
                    MedSavantDatabase.getInstance().getCohortTableSchema().getDBColumn(CohortTableSchema.ALIAS_COHORTNAME),
                    limit);
    }
    
    public static List<String> getDistinctGeneListNames() throws SQLException, NonFatalDatabaseException {
        return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema().getDBColumn(GeneListTableSchema.ALIAS_NAME));
    }

    public static List<Vector> getPatientsInCohort(String cohortName) throws SQLException, NonFatalDatabaseException {
        int cohortId = getCohortIdFromCohortName(cohortName);
        return QueryUtil.getRecordsMatchingID(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getCohortViewTableSchema(),
                    MedSavantDatabase.getInstance().getCohortViewTableSchema().getDBColumn(CohortViewTableSchema.ALIAS_COHORTID),
                    cohortId);
    }
    
    public static List<Vector> getRegionsInRegionSet(String regionName, int limit) throws SQLException, NonFatalDatabaseException {
        int regionId = getRegionIdFromRegionName(regionName);
        return QueryUtil.getRecordsMatchingID(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema(),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema().getDBColumn(GeneListViewTableSchema.ALIAS_REGIONSETID),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema().getDBColumn(GeneListViewTableSchema.ALIAS_DESCRIPTION),
                    regionId,
                    limit);
    }

    public static List<String> getDistinctRegionLists(int limit) throws NonFatalDatabaseException, SQLException {
        return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema().getDBColumn(GeneListTableSchema.ALIAS_NAME),
                    limit);
    }
    
    public static List<String> getDistinctValuesFromPatientTable(String columnAlias, boolean isNumeric) throws NonFatalDatabaseException, SQLException {
        
        //TODO:dbref
        
        if(isNumeric){
            return new ArrayList<String>();
            /*
            return QueryUtil.getDistinctNumericValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getPatientTableSchema(),
                    MedSavantDatabase.getInstance().getPatientTableSchema().getDBColumn(columnAlias));
             * 
             */
        } else {
            return new ArrayList<String>();
            /*
            return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getPatientTableSchema(),
                    MedSavantDatabase.getInstance().getPatientTableSchema().getDBColumn(columnAlias));
             * 
             */
        }
    }

    public static int getNumRegionsInRegionSet(String regionName) throws NonFatalDatabaseException, SQLException {
        
        int regionId = getRegionIdFromRegionName(regionName);
        
        TableSchema t = MedSavantDatabase.getInstance().getGeneListViewTableSchema();

        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addCustomColumns(count);
        q.addCondition(BinaryCondition.equalTo(t.getDBColumn(GeneListViewTableSchema.ALIAS_REGIONSETID), regionId));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }
    
    public static int getNumVariantsInRange(Connection c, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException {
        
        TableSchema t = MedSavantDatabase.getInstance().getVariantTableSchema();

        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = getCurrentBaseVariantFilterQuery();
        q.addCustomColumns(count);
        
        Condition[] conditions = new Condition[3];
        conditions[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_CHROM), chrom);
        conditions[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_POSITION), start);
        conditions[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, t.getDBColumn(VariantTableSchema.ALIAS_POSITION), end);       
        q.addCondition(ComboCondition.and(conditions));        
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }
    
    public static int[] getNumVariantsForBins(Connection c, String chrom, int binsize, int numbins) throws SQLException, NonFatalDatabaseException {
        
        TableSchema t = MedSavantDatabase.getInstance().getVariantTableSchema();

        SelectQuery base = getCurrentBaseVariantFilterQuery();
        base.addColumns(t.getDBColumn(VariantTableSchema.ALIAS_POSITION));
        base.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_CHROM), chrom)); 

        String q1 = base.toString();
        
        String q = "select y.range as `range`, count(*) as `number of occurences` "
                + "from ("
                + "select case ";
        int pos = 0;
        for(int i = 0; i < numbins; i++){
            q += "when `position` between " + pos + " and " + (pos+binsize) + " then " + i + " ";
            pos += binsize;
        }
        
        q += "end as `range` "
                + "from (";
        q += q1;
        q += ") x ) y "
                + "group by y.`range`";
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q);
        
        int[] numRows = new int[numbins];
        for(int i = 0; i < numbins; i++) numRows[i] = 0;
        while(rs.next()){
            int index = rs.getInt(1);
            numRows[index] = rs.getInt(2);
        }
              
        s.close();
        
        return numRows;
    }
    
    public static int getNumPatientsWithVariantsInRange(Connection connect, String chrom, int start, int end) throws SQLException {
        VariantTableSchema t = (VariantTableSchema) MedSavantDatabase.getInstance().getVariantTableSchema();

        FunctionCall count = FunctionCall.count();
        SelectQuery q = getCurrentBaseVariantFilterQuery();
        q.addCustomColumns("COUNT(DISTINCT " + t.DBFIELDNAME_DNAID + ")");
        //q.addColumns(t.getDBColumn(t.ALIAS_DNAID));
        
        Condition[] conditions = new Condition[3];
        conditions[0] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_CHROM), chrom);
        conditions[1] = new BinaryCondition(BinaryCondition.Op.GREATER_THAN_OR_EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_POSITION), start);
        conditions[2] = new BinaryCondition(BinaryCondition.Op.LESS_THAN, t.getDBColumn(VariantTableSchema.ALIAS_POSITION), end);       
        q.addCondition(ComboCondition.and(conditions));        
        
        String query = q.toString();
        query = query.replaceFirst("'", "").replaceFirst("'", "");
        
        Statement s = connect.createStatement();
        ResultSet rs = s.executeQuery(query);
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }
    
    public static int getNumFilteredVariants(Connection c) throws SQLException {
        return 1;
        /*FunctionCall count = FunctionCall.countAll();
        SelectQuery q = getCurrentBaseVariantFilterQuery();   
        q.addCustomColumns(count);
       
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());      
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;*/
    }
    
    public static SelectQuery getCurrentBaseVariantFilterQuery() {
        
        /*TableSchema variant = MedSavantDatabase.getInstance().getVariantTableSchema();
        TableSchema sift = MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        
        SelectQuery q = new SelectQuery();
        
        q.addFromTable(variant.getTable());
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX add back
        //q.addJoin(
        //        SelectQuery.JoinType.LEFT_OUTER, 
        //        variant.getTable(), 
        //        sift.getTable(), 
        //        new BinaryCondition(BinaryCondition.Op.EQUAL_TO,variant.getDBColumn(VariantTableSchema.ALIAS_ANNOTATIONSIFT),sift.getDBColumn(VariantAnnotationSiftTableSchema.ALIAS_ANNOTATIONID))); 
        
        List<QueryFilter> filters = FilterController.getQueryFilters();
        for (QueryFilter f : filters) {
            q.addCondition(ComboCondition.or(f.getConditions()));
        }

        return q;*/
        return new SelectQuery();
    }
    
    /*
    public static SelectQuery getCurrentBaseVariantFilterQueryWithNoColumns() {
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(MedSavantDatabase.getInstance().getVariantTableSchema().getTable());
        
        List<QueryFilter> filters = FilterController.getQueryFilters();
        for (QueryFilter f : filters) {
            q.addCondition(ComboCondition.or(f.getConditions()));
        }
        
        return q;
    }
     * 
     */


    public static Map<String, Integer> getFrequencyValuesForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {

        SelectQuery q = new SelectQuery();
        FunctionCall count = FunctionCall.countAll();
        q.addColumns(col);
        q.addCustomColumns(count);
        q.addFromTable(t.getTable());
        q.addGroupings(col);

        Statement s = conn.createStatement();

        ResultSet rs = s.executeQuery(q.toString());

        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            map.put(rs.getString(1), rs.getInt(2));
        }

        return map;
    }

    public static int getFrequencyValuesForColumnInRange(Connection conn, TableSchema t, DbColumn col, Range r) throws SQLException {

        SelectQuery q = new SelectQuery();
        FunctionCall count = FunctionCall.countAll();
        q.addCustomColumns(count);
        q.addFromTable(t.getTable());
        q.addCondition(getRangeCondition(col,r));

        Statement s = conn.createStatement();

        ResultSet rs = s.executeQuery(q.toString());

        rs.next();
        
        return rs.getInt(1);
    }
    
    
    public static Map<String, Integer> getFilteredFrequencyValuesForColumn(Connection conn, DbColumn col) throws SQLException {

        SelectQuery q = QueryUtil.getCurrentBaseVariantFilterQuery();
        FunctionCall count = FunctionCall.countAll();
        q.addColumns(col);
        q.addCustomColumns(count);
        q.addGroupings(col);

        Statement s = conn.createStatement();

        ResultSet rs = s.executeQuery(q.toString());

        Map<String, Integer> map = new HashMap<String, Integer>();

        while (rs.next()) {
            map.put(rs.getString(1), rs.getInt(2));
        }

        return map;
    }

    public static int getFilteredFrequencyValuesForColumnInRange(Connection conn, DbColumn col, Range r) throws SQLException {

        SelectQuery q = QueryUtil.getCurrentBaseVariantFilterQuery();
        FunctionCall count = FunctionCall.countAll();
        q.addCustomColumns(count);
        q.addCondition(getRangeCondition(col,r));

        Statement s = conn.createStatement();

        ResultSet rs = s.executeQuery(q.toString());

        rs.next();
        
        return rs.getInt(1);
    }

    public static Condition getRangeCondition(DbColumn col, Range r) {
        Condition[] results = new Condition[2];
        results[0] = BinaryCondition.greaterThan(col, r.getMin(), true);
        results[1] = BinaryCondition.lessThan(col, r.getMax(), false);

        return ComboCondition.and(results);
    }
    
    public static List<String> getDNAIdsForIndividualsInCohort(String cohortName) throws NonFatalDatabaseException, SQLException {
        
        //TODO:dbref
        return new ArrayList<String>();
        
        /*
        int cohortId = getCohortIdFromCohortName(cohortName);
        
        PatientTableSchema tsubject = (PatientTableSchema) MedSavantDatabase.getInstance().getPatientTableSchema();
        DbColumn currentDNAId = tsubject.getDBColumn(PatientTableSchema.ALIAS_DNAID);
        DbColumn subjecthospitalId = tsubject.getDBColumn(PatientTableSchema.ALIAS_PATIENTID);
        
        CohortViewTableSchema tcohort = (CohortViewTableSchema) MedSavantDatabase.getInstance().getCohortViewTableSchema();
        DbColumn cohorthospitalId = tcohort.getDBColumn(CohortViewTableSchema.ALIAS_HOSPITALID);
        DbColumn cohortIdField = tcohort.getDBColumn(CohortViewTableSchema.ALIAS_COHORTID);
               
        SelectQuery q = new SelectQuery();
        q.addColumns(currentDNAId);
        q.setIsDistinct(true);
        q.addFromTable(tsubject.getTable());
        q.addJoin(SelectQuery.JoinType.INNER, tsubject.getTable(), tcohort.getTable(), BinaryCondition.equalTo(subjecthospitalId, cohorthospitalId));
        q.addCondition(BinaryCondition.equalTo(cohortIdField, cohortId));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;
         * 
         */
    }

    public static List<String> getDNAIdsForGender(int gender) throws NonFatalDatabaseException, SQLException {
        
        //todo:dbref
        return new ArrayList<String>();
        
        /*
        PatientTableSchema tsubject = (PatientTableSchema) MedSavantDatabase.getInstance().getPatientTableSchema();
        DbColumn currentDNAId = tsubject.getDBColumn(PatientTableSchema.ALIAS_DNAID);
        DbColumn subjectGender = tsubject.getDBColumn(PatientTableSchema.ALIAS_GENDER);
        
        SelectQuery q = new SelectQuery();
        q.addColumns(currentDNAId);
        q.setIsDistinct(true);
        q.addFromTable(tsubject.getTable());
        q.addCondition(BinaryCondition.equalTo(subjectGender, gender));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;    
         * 
         */
    }
    
    public static List<String> getDNAIdsForList(TableSchema table, List<String> list, String columnAlias) throws NonFatalDatabaseException, SQLException {
        
        //todo:dbref
        return new ArrayList<String>();
        
        /*
        PatientTableSchema tpatient = MedSavantDatabase.getInstance().getPatientTableSchema();
        PhenotypeTableSchema tphenotype = MedSavantDatabase.getInstance().getPhenotypeTableSchema();
        DbColumn currentDNAId = tpatient.getDBColumn(PatientTableSchema.ALIAS_DNAID);
        DbColumn testColumn = table.getDBColumn(columnAlias);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(tpatient.getTable());
        q.addFromTable(tphenotype.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        
        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryCondition.equalTo(testColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));   
        
        DbColumn patientid = tpatient.getDBColumn(PatientTableSchema.ALIAS_PATIENTID);
        DbColumn phenotypeid = tphenotype.getDBColumn(PhenotypeTableSchema.ALIAS_PATIENTID);
        q.addCondition(BinaryCondition.equalTo(patientid, phenotypeid));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;  
         * 
         */
    }
    
    /*public static List<String> getDNAIdsForPatientList(List<String> list, String columnAlias) throws NonFatalDatabaseException, SQLException {
        
        PatientTableSchema tsubject = (PatientTableSchema) MedSavantDatabase.getInstance().getPatientTableSchema();
        DbColumn currentDNAId = tsubject.getDBColumn(PatientTableSchema.ALIAS_DNAID);
        DbColumn subjectColumn = tsubject.getDBColumn(columnAlias);
        
        SelectQuery q = new SelectQuery();
        q.addColumns(currentDNAId);
        q.setIsDistinct(true);
        q.addFromTable(tsubject.getTable());
        Condition[] conditions = new Condition[list.size()];
        for(int i = 0; i < list.size(); i++){
            conditions[i] = BinaryCondition.equalTo(subjectColumn, list.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));    
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;  
    }*/
 
    public static List<String> getAllDNAIds() throws NonFatalDatabaseException, SQLException {
        
        //todo:dbref
        return new ArrayList<String>();
        
        /*
        PatientTableSchema tsubject = (PatientTableSchema) MedSavantDatabase.getInstance().getPatientTableSchema();
        DbColumn currentDNAId = tsubject.getDBColumn(PatientTableSchema.ALIAS_DNAID);     

        SelectQuery q = new SelectQuery();
        q.addColumns(currentDNAId);
        q.setIsDistinct(true);
        q.addFromTable(tsubject.getTable());
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;
         * 
         */
    }

    public static List<GenomicRegion> getGenomicRangesForRegionList(String geneListName) throws SQLException, NonFatalDatabaseException {
        
        int regionId = getRegionIdFromRegionName(geneListName);
        
        GeneListViewTableSchema t = (GeneListViewTableSchema) MedSavantDatabase.getInstance().getGeneListViewTableSchema();
        DbColumn name = t.getDBColumn(GeneListViewTableSchema.ALIAS_REGIONSETID);     

        SelectQuery q = new SelectQuery();
        q.addColumns(t.getDBColumn(GeneListViewTableSchema.ALIAS_CHROM));
        q.addColumns(t.getDBColumn(GeneListViewTableSchema.ALIAS_START));
        q.addColumns(t.getDBColumn(GeneListViewTableSchema.ALIAS_END));
        q.addFromTable(t.getTable());
        q.addCondition(BinaryCondition.equalTo(name, regionId)); 
        
        Statement s = ConnectionController.connect().createStatement();

        ResultSet rs = s.executeQuery(q.toString());

        List<GenomicRegion> results = new ArrayList<GenomicRegion>();
        while (rs.next()) {
            results.add(new GenomicRegion(rs.getString(1), new Range(rs.getInt(2), rs.getInt(3))));
        }
        
        return results;
    }
    
    public static Map<String, List<String>> getSavantBookmarkPositionsForDNAIds(Connection c, List<String> dnaIds, int limit) throws SQLException, NonFatalDatabaseException {
        
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        
        TableSchema t = MedSavantDatabase.getInstance().getVariantTableSchema();
        SelectQuery q = getCurrentBaseVariantFilterQuery();
        q.addColumns(t.getDBColumn(VariantTableSchema.ALIAS_DNAID), t.getDBColumn(VariantTableSchema.ALIAS_CHROM), t.getDBColumn(VariantTableSchema.ALIAS_POSITION));
        
        Condition[] conditions = new Condition[dnaIds.size()];
        for(int i = 0; i < dnaIds.size(); i++){
            conditions[i] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_DNAID), dnaIds.get(i));
            results.put(dnaIds.get(i), new ArrayList<String>());
        }
        q.addCondition(ComboCondition.or(conditions));    
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString() + ((limit == -1) ? "" : (" LIMIT " + limit)));

        while (rs.next()) {
            results.get(rs.getString(1)).add(rs.getString(2) + ":" + (rs.getLong(3)-100) + "-" + (rs.getLong(3)+100));  
        }
        
        return results;
    }
    
    public static List<String> getBAMFilesForDNAIds(Connection c, List<String> dnaIds) throws SQLException, NonFatalDatabaseException {
        
        TableSchema t = MedSavantDatabase.getInstance().getAlignmentTableSchema();
        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(t.getDBColumn(AlignmentTableSchema.ALIAS_ALIGNMENTPATH));
        q.addFromTable(t.getTable());

        Condition[] conditions = new Condition[dnaIds.size()];
        for(int i = 0; i < dnaIds.size(); i++){
            conditions[i] = new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(VariantTableSchema.ALIAS_DNAID), dnaIds.get(i));
        }
        q.addCondition(ComboCondition.or(conditions));
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());

        List<String> results = new ArrayList<String>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;      
    }
    
    public static String getGenomeBAMPathForVersion(Connection c, String genomeVersion) throws SQLException, NonFatalDatabaseException {
        
        TableSchema t = MedSavantDatabase.getInstance().getGenomeTableSchema();
        SelectQuery q = new SelectQuery();
        q.addColumns(t.getDBColumn(GenomeTableSchema.ALIAS_BAMPATH));
        q.addFromTable(t.getTable());
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO, t.getDBColumn(GenomeTableSchema.ALIAS_VERSION), genomeVersion));
        
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString() + " LIMIT 1");
        
        if(rs.next()){
            return rs.getString(1);
        } else {
            return "";
        }
        
    }

    public static List<BEDRecord> getRegionsInRegionList(String geneListName, int limit) throws NonFatalDatabaseException, SQLException {
        
        int regionId = getRegionIdFromRegionName(geneListName);
        
        //select chrom,start,end,description from region_set_view where name = 'ASD Genes';
        GeneListViewTableSchema t = (GeneListViewTableSchema) MedSavantDatabase.getInstance().getGeneListViewTableSchema();
        
        DbColumn chrom = t.getDBColumn(GeneListViewTableSchema.ALIAS_CHROM);
        DbColumn start = t.getDBColumn(GeneListViewTableSchema.ALIAS_START);
        DbColumn end = t.getDBColumn(GeneListViewTableSchema.ALIAS_END);
        DbColumn description = t.getDBColumn(GeneListViewTableSchema.ALIAS_DESCRIPTION);
        DbColumn name = t.getDBColumn(GeneListViewTableSchema.ALIAS_REGIONSETID);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addColumns(chrom);
        q.addColumns(start);
        q.addColumns(end);
        q.addColumns(description);
        q.addOrdering(description, Dir.ASCENDING);
        q.addCondition(BinaryCondition.equalTo(name, regionId));
         
        Statement s = ConnectionController.connect().createStatement();
        String queryString = q.toString();
        if(limit > 0) queryString = queryString + " LIMIT " + limit;
        
        ResultSet rs = s.executeQuery(queryString);
        
        List<BEDRecord> results = new ArrayList<BEDRecord>();
        
        while(rs.next()) {
            
            results.add(new BEDRecord(
                    rs.getString(1),
                    rs.getInt(2),
                    rs.getInt(3),
                    rs.getString(4)
                    ));
        }
        
        return results;
    }
    
    public static List<String> getDNAIdsWithValuesInRange(TableSchema table, String columnName, Range r) throws NonFatalDatabaseException, SQLException {
        
        return new ArrayList<String>();
        //todo:dbref
        
        /*
        PatientTableSchema tpatient = MedSavantDatabase.getInstance().getPatientTableSchema();
        PhenotypeTableSchema tphenotype = MedSavantDatabase.getInstance().getPhenotypeTableSchema();
        DbColumn currentDNAId = tpatient.getDBColumn(PatientTableSchema.ALIAS_DNAID);
        DbColumn testColumn = table.getDBColumn(columnName);
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(tpatient.getTable());
        q.addFromTable(tphenotype.getTable());
        q.setIsDistinct(true);
        q.addColumns(currentDNAId);
        q.addCondition(BinaryCondition.greaterThan(testColumn, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(testColumn, r.getMax(), true));
        
        DbColumn patientid = tpatient.getDBColumn(PatientTableSchema.ALIAS_PATIENTID);
        DbColumn phenotypeid = tphenotype.getDBColumn(PhenotypeTableSchema.ALIAS_PATIENTID);
        q.addCondition(BinaryCondition.equalTo(patientid, phenotypeid));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        
        List<String> results = new ArrayList<String>();
        while(rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results; 
         * 
         */
    }
    
    /*public static List<String> getPatientsWithValuesInRange(String alias_any_numeric_field, Range r) throws NonFatalDatabaseException, SQLException {
             
        PatientTableSchema t = (PatientTableSchema) MedSavantDatabase.getInstance().getPatientTableSchema();
        DbColumn col = t.getDBColumn(alias_any_numeric_field);

        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addColumns(t.getDBColumn(t.ALIAS_DNAID));
        q.addCondition(BinaryCondition.greaterThan(col, r.getMin(), true));
        q.addCondition(BinaryCondition.lessThan(col, r.getMax(), true));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        
        List<String> results = new ArrayList<String>();
        while(rs.next()) {
            results.add(rs.getString(1));
        }
        
        return results;     
    }*/
    
    public static List<Vector> getDistinctBasicPatientInfo(int limit) throws SQLException, NonFatalDatabaseException {
        
        //TODO:dbref
        return new ArrayList<Vector>();
        
        /*
        TableSchema t = MedSavantDatabase.getInstance().getPatientTableSchema();
        Object[][] columnTypeIndices = {{1,null,ColumnType.VARCHAR},{2,null,ColumnType.VARCHAR},{3,null,ColumnType.VARCHAR}};
        //DbColumn[] cols = {t.getDBColumn(PatientTableSchema.ALIAS_INDEXID), t.getDBColumn(PatientTableSchema.ALIAS_DNA1), t.getDBColumn(PatientTableSchema.ALIAS_FAMNUM)};
        DbColumn[] cols = {t.getDBColumn(PatientTableSchema.ALIAS_PATIENTID), t.getDBColumn(PatientTableSchema.ALIAS_DNAID), t.getDBColumn(PatientTableSchema.ALIAS_FAMILYID)};
        return QueryUtil.getDistinctValuesForColumns(
            ConnectionController.connect(),
            t,
            cols,
            columnTypeIndices,
            t.getDBColumn(PatientTableSchema.ALIAS_PATIENTID),
            Dir.ASCENDING,
            limit);
         * 
         */
    }
    
    public static Date getUpdateTimeForTable(String dbName, String tableName) throws SQLException, NonFatalDatabaseException {  
        String query = 
                "SELECT UPDATE_TIME " +
                "FROM information_schema.tables " +
                "WHERE TABLE_SCHEMA = '" + dbName + "' " +
                "AND TABLE_NAME = '" + tableName + "'";
        
        Statement s = ConnectionController.connect().createStatement();             
        ResultSet rs = s.executeQuery(query);
        
        rs.next();
        Date date = rs.getDate(1);
        return date;
    }
    
    /*
     * Check the latest update time for any table that is used in FilterCache. 
     */
    /*public static Timestamp getMaxUpdateTimeForCache() throws SQLException, NonFatalDatabaseException {  
        
        String query = 
                "SELECT MAX(UPDATE_TIME) " +
                "FROM information_schema.tables " +
                "WHERE TABLE_SCHEMA = '" + SettingsController.getInstance().getDBName() + "' " +
                "AND (" + 
                "TABLE_NAME = '" + MedSavantDatabase.getInstance().getPatientTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getVariantTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getCohortTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getPhenotypeTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getGeneListTableSchema().getTable().getTableNameSQL() + "' " + 
                ")";
        
        Statement s = ConnectionController.connect().createStatement();             
        ResultSet rs = s.executeQuery(query);
        
        rs.next();
        Timestamp timestamp = rs.getTimestamp(1);
        return timestamp;
    }*/
    
    public static Map<String, Timestamp> getUpdateTimesForCache() throws SQLException, NonFatalDatabaseException {  
        
        //todo:dbref
        return new HashMap<String,Timestamp>();
        
        /*
        String query = 
                "SELECT TABLE_NAME, UPDATE_TIME " +
                "FROM information_schema.tables " + 
                "WHERE TABLE_SCHEMA = '" + SettingsController.getInstance().getDBName() + "' " + 
                "AND (" + 
                "TABLE_NAME = '" + MedSavantDatabase.getInstance().getPatientTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getVariantTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getCohortTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getPhenotypeTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getGeneListTableSchema().getTable().getTableNameSQL() + "' " + 
                "OR TABLE_NAME = '" + MedSavantDatabase.getInstance().getVariantSiftTableSchema().getTable().getTableNameSQL() + "' " +
                ")";
        
        Statement s = ConnectionController.connect().createStatement();             
        ResultSet rs = s.executeQuery(query);
        
        Map<String, Timestamp> result = new HashMap<String, Timestamp>();
        while(rs.next()){
            result.put(rs.getString(1), rs.getTimestamp(2));
        }
        return result;
         * 
         */
    }
    
    
    //TODO: should really be using join instead of two separate queries
    public static int getIdFromName(TableSchema t, DbColumn nameColumn, DbColumn idColumn, String name) throws SQLException, NonFatalDatabaseException {
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addColumns(idColumn);
        q.addCondition(BinaryCondition.equalTo(nameColumn, name));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        if(rs.next()){
            return rs.getInt(1);
        } else {
            return -1;
        }
    }
    
    public static int getCohortIdFromCohortName(String cohortName) throws SQLException, NonFatalDatabaseException {
        TableSchema t = MedSavantDatabase.getInstance().getCohortTableSchema();
        return getIdFromName(
                t, 
                t.getDBColumn(CohortTableSchema.ALIAS_COHORTNAME),
                t.getDBColumn(CohortTableSchema.ALIAS_COHORTID),
                cohortName);
    }
    
    public static int getRegionIdFromRegionName(String regionName) throws SQLException, NonFatalDatabaseException {
        TableSchema t = MedSavantDatabase.getInstance().getGeneListTableSchema();
        return getIdFromName(
                t, 
                t.getDBColumn(GeneListTableSchema.ALIAS_NAME),
                t.getDBColumn(GeneListTableSchema.ALIAS_ID),
                regionName);
    }
    
    
    /*public static int getCohortIdFromCohortName(String cohortName) throws SQLException, NonFatalDatabaseException {       
        TableSchema t = MedSavantDatabase.getInstance().getCohortTableSchema();
        
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addColumns(t.getDBColumn(CohortTableSchema.ALIAS_COHORTID));
        q.addCondition(BinaryCondition.equalTo(t.getDBColumn(CohortTableSchema.ALIAS_COHORTNAME), cohortName));
        
        Statement s = ConnectionController.connect().createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        if(rs.next()){
            return rs.getInt(1);
        } else {
            return -1;
        }
    }*/

    
    
}