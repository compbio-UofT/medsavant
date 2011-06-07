/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import fiume.vcf.VariantRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import medsavant.db.ConnectionController;
import medsavant.db.Database;
import medsavant.db.DBUtil;
import medsavant.db.table.TableSchema;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.Util;

/**
 *
 * @author mfiume
 */
public class ResultController implements FiltersChangedListener {

    private List<VariantRecord> filteredVariants;

    private static ResultController instance;
    
    public ResultController() {
        FilterController.addFilterListener(this);
        updateFilteredVariantDBResults();
    }

    public static ResultController getInstance() {
        if (instance == null) {
            instance = new ResultController();
        }
        return instance;
    }


    public List<VariantRecord> getAllVariantRecords() {
        return filteredVariants;
    }

    public List<VariantRecord> getFilteredVariantRecords() {
        return filteredVariants;
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException {
        updateFilteredVariantDBResults();
    }

    private void updateFilteredVariantDBResults() {

        TableSchema tableSchema = Database.getInstance().getVariantTableSchema();
        SelectQuery query = new SelectQuery();
        query.addFromTable(tableSchema.getTable());
        query.addAllColumns();

        List<QueryFilter> filters = FilterController.getQueryFilters();
        System.out.println("Number of query filters: " + filters.size());
        for (QueryFilter f : filters) {
            query.addCondition(ComboCondition.or(f.getConditions()));
        }

        String queryString = query.toString() + " LIMIT 5000";
        System.out.println(queryString);

        ResultSet r1;
        try {
            r1 = ConnectionController.connect().createStatement().executeQuery(queryString);
        } catch (SQLException ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
            throw new FatalDatabaseException(ex.getMessage());
        }

        List<DbColumn> columns = tableSchema.getColumns();
        List<Vector> results;
        try {
            results = DBUtil.parseResultSet(tableSchema.getColumnGrid(), r1);
        } catch (Exception ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
            throw new FatalDatabaseException(ex.getMessage());
        }
        filteredVariants = Util.convertVectorsToVariantRecords(results);

        /*
        for (Vector row : results) {
            for (Object f : row) {
                System.out.print(f + "\t");
            }
            System.out.println();
        }
         * 
         */

    }




    /*
    public static void clearVariants() {
        variants = new ArrayList<VariantRecord>();
    }
     * 
     */

    /*
    public static void addVariantSet(VariantSet s) {
        variants.addAll(s.getRecords());
    }
     *
     */

}
