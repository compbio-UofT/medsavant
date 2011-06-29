/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.db;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.ut.biolab.medsavant.db.table.SubjectTableSchema;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.table.CohortViewTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListTableSchema;
import org.ut.biolab.medsavant.db.table.GeneListViewTableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;


/**
 *
 * @author mfiume
 */
public class QueryUtil {

    
    public static List<String> getDistinctValuesForColumn(Connection conn, TableSchema t, DbColumn col) throws SQLException {

        if (t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get distinct values for numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(col);
        q.addFromTable(t.getTable());
        
        Statement s = conn.createStatement();
        ResultSet rs = s.executeQuery(q.toString());

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
                default:
                    throw new FatalDatabaseException("Unhandled column type: " + type);
            }
        }

        return new Range(min,max);

    }

    public static List<String> getDistinctDNAIds() throws SQLException, NonFatalDatabaseException {
        return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getVariantTableSchema(),
                    MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(VariantTableSchema.ALIAS_DNAID));
    }

    public static List<String> getDistinctPatientIDs() throws SQLException, NonFatalDatabaseException {
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
    }
    
    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, String id) throws SQLException {
        return getRecordsMatchingID(conn,t,col,id,-1);
    }

    private static List<Vector> getRecordsMatchingID(Connection conn, TableSchema t, DbColumn col, String id, int limit) throws SQLException {
        
        SelectQuery q = new SelectQuery();
        q.addAllColumns();
        q.addFromTable(t.getTable());
        
        q.addCondition(new BinaryCondition(BinaryCondition.Op.EQUAL_TO,col,id));

        Statement s = conn.createStatement();
        
        System.out.println("Querying for: " + q.toString()  + ((limit == -1) ? "" : (" LIMIT " + limit)));
        
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

    public static List<String> getDistinctCohortNames() throws NonFatalDatabaseException, SQLException {
         return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getCohortTableSchema(),
                    MedSavantDatabase.getInstance().getCohortTableSchema().getDBColumn(CohortViewTableSchema.ALIAS_COHORTNAME));
    }

    public static List<Vector> getPatientsInCohort(String cohortName) throws SQLException, NonFatalDatabaseException {
        return QueryUtil.getRecordsMatchingID(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getCohortViewTableSchema(),
                    MedSavantDatabase.getInstance().getCohortViewTableSchema().getDBColumn(CohortViewTableSchema.ALIAS_COHORTNAME),
                    cohortName);
    }
    
    public static List<Vector> getRegionsInRegionSet(String regionName, int limit) throws SQLException, NonFatalDatabaseException {
        return QueryUtil.getRecordsMatchingID(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema(),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema().getDBColumn(GeneListViewTableSchema.ALIAS_REGIONSETNAME),
                    regionName,
                    limit);
    }

    public static List<String> getDistinctRegionLists() throws NonFatalDatabaseException, SQLException {
        return QueryUtil.getDistinctValuesForColumn(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema(),
                    MedSavantDatabase.getInstance().getGeneListTableSchema().getDBColumn(GeneListTableSchema.ALIAS_NAME));
    }

    public static int getNumRegionsInRegionSet(String regionName) throws NonFatalDatabaseException, SQLException {
        return QueryUtil.getNumRowsInTable(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getGeneListViewTableSchema().getTable());
    }
    
    public static int getNumVariantsInRange(Connection c, TableSchema t, String chrom, long start, long end) throws SQLException, NonFatalDatabaseException {
        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = getCurrentBaseFilterQuery(t);
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
    
    public static int getNumFilteredVariants(Connection c, TableSchema t) throws SQLException {
        FunctionCall count = FunctionCall.countAll();
        SelectQuery q = getCurrentBaseFilterQuery(t);     
        q.addCustomColumns(count);
       
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery(q.toString());
        rs.next();

        int numrows = rs.getInt(1);
        s.close();
        
        return numrows;
    }
    
    public static SelectQuery getCurrentBaseFilterQuery(TableSchema t) {
        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        
        List<QueryFilter> filters = FilterController.getQueryFilters();
        for (QueryFilter f : filters) {
            q.addCondition(ComboCondition.or(f.getConditions()));
        }
        
        return q;
    }
}