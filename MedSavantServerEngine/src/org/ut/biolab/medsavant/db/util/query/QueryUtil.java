/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.util.query;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.CustomTables;
import org.ut.biolab.medsavant.db.util.query.api.QueryUtilAdapter;

/**
 *
 * @author Andrew
 */
public class QueryUtil extends java.rmi.server.UnicastRemoteObject implements QueryUtilAdapter {

    private static QueryUtil instance;

    public static QueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new QueryUtil();
        }
        return instance;
    }

    public QueryUtil() throws RemoteException {}


    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException {
        return getDistinctValuesForColumn(sid,t, col, -1);
    }

    public List<String> getDistinctValuesForColumn(String sid,TableSchema t, DbColumn col, int limit) throws SQLException {

        if (t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get distinct values for numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.setIsDistinct(true);
        q.addColumns(col);
        q.addFromTable(t.getTable());

        Statement s = ConnectionController.connectPooled(sid).createStatement();
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


    public Range getExtremeValuesForColumn(String sid,TableSchema t, DbColumn col) throws SQLException {

        if (!t.isNumeric(t.getColumnType(t.getColumnIndex(col)))) {
            throw new FatalDatabaseException("Can't get extreme values for non-numeric field : " + col.getAbsoluteName());
        }

        SelectQuery q = new SelectQuery();
        q.addFromTable(t.getTable());
        q.addCustomColumns(FunctionCall.min().addColumnParams(col));
        q.addCustomColumns(FunctionCall.max().addColumnParams(col));

        Statement s = ConnectionController.connectPooled(sid).createStatement();
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


    public List<String> getBAMFilesForDNAIds(List<String> dnaIds) throws SQLException, NonFatalDatabaseException {


        /*TableSchema t = OMedSavantDatabase.getInstance().getAlignmentTableSchema();
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
        }*/

        System.err.println("NOT IMPLEMENTED YET");

        return new ArrayList<String>();
    }

    public String getGenomeBAMPathForVersion(String genomeVersion) throws SQLException, NonFatalDatabaseException {

        //todo:dbref
        throw new UnsupportedOperationException("dbref");

        /*
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
        *
        */

    }



    public Condition getRangeCondition(DbColumn col, Range r) {
        Condition[] results = new Condition[2];
        results[0] = BinaryCondition.greaterThan(col, r.getMin(), true);
        results[1] = BinaryCondition.lessThan(col, r.getMax(), false);

        return ComboCondition.and(results);
    }

    public int getNumRecordsInTable(String sid,String name) throws SQLException, RemoteException {

        if (name == null) { return -1; }

        TableSchema table = CustomTables.getCustomTableSchema(sid,name);

        SelectQuery q = new SelectQuery();
        q.addFromTable(table.getTable());
        q.addCustomColumns(FunctionCall.countAll());

        ResultSet rs = ConnectionController.connectPooled(sid).createStatement().executeQuery(q.toString());

        rs.next();
        return rs.getInt(1);
    }


}
