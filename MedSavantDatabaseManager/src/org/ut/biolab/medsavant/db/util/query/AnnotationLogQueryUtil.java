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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.VariantPendingUpdateTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;
import org.ut.biolab.medsavant.db.util.DBUtil;

/**
 *
 * @author Andrew
 */
public class AnnotationLogQueryUtil {

   
    public static enum Action {ADD_VARIANTS, UPDATE_TABLE};
    public static enum Status {PREPROCESS, PENDING, INPROGRESS, ERROR, COMPLETE}; 
    
    private static int actionToInt(Action action){
        switch(action){
            case UPDATE_TABLE:
                return 0;
            case ADD_VARIANTS:
                return 1;
            default:
                return -1;
        }
    }
    
    public static Action intToAction(int action){
        switch(action){
            case 0:
                return Action.UPDATE_TABLE;
            case 1:
                return Action.ADD_VARIANTS;
            default:
                return null;
        }
    }
    
    private static int statusToInt(Status status){
        switch(status){
            case PREPROCESS:
                return 0;
            case PENDING:
                return 1;
            case INPROGRESS:
                return 2;
            case ERROR:
                return 3;
            case COMPLETE:
                return 4;
            default:
                return -1;
        }
    }
    
    public static Status intToStatus(int status){
        switch(status){
            case 0:
                return Status.PREPROCESS;
            case 1:
                return Status.PENDING;
            case 2:
                return Status.INPROGRESS;
            case 3:
                return Status.ERROR;
            case 4:
                return Status.COMPLETE;
            default:
                return null;
        }
    }
    
    public static int addAnnotationLogEntry(int projectId, int referenceId, Action action) throws SQLException{    
        return addAnnotationLogEntry(projectId,referenceId,action,Status.PREPROCESS);
    }
    
    public static int addAnnotationLogEntry(int projectId, int referenceId, Action action, Status status) throws SQLException {
        
        if(action == Action.UPDATE_TABLE && existsDuplicateAnnotation(projectId, referenceId)) return -1;
        
        Timestamp sqlDate = DBUtil.getCurrentTimestamp();
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        InsertQuery query = new InsertQuery(table.getTable());
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId);
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId);
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION), actionToInt(action));
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), statusToInt(status));
        query.addColumn(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);

        PreparedStatement stmt = (ConnectionController.connectPooled()).prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
        stmt.execute();
        
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }
    
    public static void removeAnnotationLogEntry(int updateId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        DeleteQuery query = new DeleteQuery(table.getTable());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        
        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString());
    }
    
    public static ResultSet getPendingUpdates() throws SQLException, IOException{
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), statusToInt(Status.PENDING)));
        query.addOrdering(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION), OrderObject.Dir.ASCENDING);
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        return rs;
    }
    
    public static void setAnnotationLogStatus(int updateId, Status status) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), statusToInt(status));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        
        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString());
    }
    
    public static void setAnnotationLogStatus(int updateId, Status status, Timestamp sqlDate) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        UpdateQuery query = new UpdateQuery(table.getTable());
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), statusToInt(status));
        query.addSetClause(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_TIMESTAMP), sqlDate);
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPDATE_ID), updateId));
        
        ConnectionController.connectPooled().createStatement().executeUpdate(query.toString());
    }
    
    private static boolean existsDuplicateAnnotation(int projectId, int referenceId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.VariantpendingupdateTableSchema;
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addColumns(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_UPDATE_ID));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_PROJECT_ID), projectId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_REFERENCE_ID), referenceId));
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_ACTION), actionToInt(Action.UPDATE_TABLE)));
        query.addCondition(BinaryCondition.lessThan(table.getDBColumn(VariantPendingUpdateTableSchema.COLUMNNAME_OF_STATUS), statusToInt(Status.PENDING), true));
        
        String q = query.toString();
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        return rs.next();
    }
    
}
