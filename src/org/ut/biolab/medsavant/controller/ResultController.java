/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.controller;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;

/**
 *
 * @author Andrew
 */
public class ResultController {

    private Vector filteredVariants;
    
    private static final int DEFAULT_LIMIT = 1000;
    private int limit = -1;
    private int filterSetId = -1;
    
    private int projectId;
    private int referenceId;

    private static ResultController instance;
    
    public ResultController() throws NonFatalDatabaseException {
        updateFilteredVariantDBResults(DEFAULT_LIMIT);
    }

    public static ResultController getInstance() throws NonFatalDatabaseException {
        if (instance == null) {
            instance = new ResultController();
        }
        return instance;
    }


    public Vector getAllVariantRecords() {
        return filteredVariants;
    }

    public Vector getFilteredVariantRecords(int limit) {
        if(filterSetId != FilterController.getCurrentFilterSetID() || this.limit < limit ||
                ProjectController.getInstance().getCurrentProjectId() != projectId ||
                ProjectController.getInstance().getCurrentReferenceId() != referenceId){
            try {
                updateFilteredVariantDBResults(limit);
                this.limit = limit;
            } catch (NonFatalDatabaseException ex) {
                ex.printStackTrace();
            }
            projectId = ProjectController.getInstance().getCurrentProjectId();
            referenceId = ProjectController.getInstance().getCurrentReferenceId();
        }      
        return filteredVariants;
    }
    
    private void updateFilteredVariantDBResults(int limit) throws NonFatalDatabaseException {
        
        filterSetId = FilterController.getCurrentFilterSetID();
        
        try {
            filteredVariants = VariantQueryUtil.getVariants(ProjectController.getInstance().getCurrentProjectId(), ProjectController.getInstance().getCurrentReferenceId(), FilterController.getQueryFilterConditions(), limit);
        } catch (SQLException ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*private void updateFilteredVariantDBResults1(int limit) throws NonFatalDatabaseException {
        
        filterSetId = FilterController.getCurrentFilterSetID();

        TableSchema variant = MedSavantDatabase.getInstance().getVariantTableSchema();
        VariantAnnotationSiftTableSchema sift = MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        VariantAnnotationPolyphenTableSchema polyphen = MedSavantDatabase.getInstance().getVariantPolyphenTableSchema();
        VariantAnnotationGatkTableSchema gatk = MedSavantDatabase.getInstance().getVariantGatkTableSchema();
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(variant.getTable());
        query.addAllColumns();

        List<QueryFilter> filters = FilterController.getQueryFilters();
        for (QueryFilter f : filters) {
            query.addCondition(ComboCondition.or(f.getConditions()));
        }
        
        String queryString = query.toString() + " LIMIT " + limit;
        
        ResultSet r1;
        try {
            r1 = ConnectionController.connect().createStatement().executeQuery(queryString);
        } catch (SQLException ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
            throw new FatalDatabaseException(ex.getMessage());
        }

        List<Vector> results;
        Object[][] columnGridVariant = variant.getColumnGrid();
        Object[][] columnGridSift = sift.getJoinedColumnGrid();
        Object[][] columnGridPolyphen = polyphen.getJoinedColumnGrid();
        Object[][] columnGridGatk = gatk.getJoinedColumnGrid();
        Object[][] columnGrid = new Object[columnGridVariant.length + columnGridSift.length + columnGridPolyphen.length + columnGridGatk.length][3];
        
        //copy variant
        System.arraycopy(columnGridVariant, 0, columnGrid, 0, columnGridVariant.length);
        
        int numFields = variant.getNumFields();
        
        //copy sift
        for(int i = 0; i < columnGridSift.length; i++){
            columnGridSift[i][0] = ((Integer)columnGridSift[i][0]) + numFields; 
        }      
        System.arraycopy(columnGridSift, 0, columnGrid, numFields, columnGridSift.length);
        numFields += columnGridSift.length;
        
        //copy polyphen
        for(int i = 0; i < columnGridPolyphen.length; i++){
            columnGridPolyphen[i][0] = ((Integer)columnGridPolyphen[i][0]) + numFields; 
        }      
        System.arraycopy(columnGridPolyphen, 0, columnGrid, numFields, columnGridPolyphen.length);
        numFields += columnGridPolyphen.length;
        
        //copy gatk
        for(int i = 0; i < columnGridGatk.length; i++){
            columnGridGatk[i][0] = ((Integer)columnGridGatk[i][0]) + numFields; 
        }      
        System.arraycopy(columnGridGatk, 0, columnGrid, numFields, columnGridGatk.length);
        numFields += columnGridGatk.length;

        try {
            results = DBUtil.parseResultSet(columnGrid, r1);
        } catch (Exception ex) {
            Logger.getLogger(ResultController.class.getName()).log(Level.SEVERE, null, ex);
            throw new FatalDatabaseException(ex.getMessage());
        }
        filteredVariants1 = Util.convertVectorsToVariantRecords(results);

    }*/
}
