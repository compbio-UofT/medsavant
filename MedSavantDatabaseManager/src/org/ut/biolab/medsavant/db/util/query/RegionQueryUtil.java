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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.RegionSetTableSchema;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.RegionSetMembershipTableSchema;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.model.BEDRecord;
import org.ut.biolab.medsavant.db.model.GenomicRegion;
import org.ut.biolab.medsavant.db.model.Range;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.util.BinaryConditionMS;
import org.ut.biolab.medsavant.db.util.ConnectionController;

/**
 *
 * @author Andrew
 */
public class RegionQueryUtil {
    
    public static void addRegionList(String geneListName, int genomeId, Iterator<String[]> i) throws NonFatalDatabaseException, SQLException {
        
        Connection conn = ConnectionController.connectPooled();       
        TableSchema regionSetTable = MedSavantDatabase.RegionsetTableSchema;     
        TableSchema regionMemberTable = MedSavantDatabase.RegionsetmembershipTableSchema;
        
        conn.setAutoCommit(false);

        //add region set
        InsertQuery query1 = new InsertQuery(regionSetTable.getTable());
        query1.addColumn(regionSetTable.getDBColumn(RegionSetTableSchema.COLUMNNAME_OF_NAME), geneListName);
        
        PreparedStatement stmt = conn.prepareStatement(query1.toString(), Statement.RETURN_GENERATED_KEYS);       
        stmt.execute();
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        
        int regionSetId = rs.getInt(1);
        
        //add regions
        while (i.hasNext() && !Thread.currentThread().isInterrupted()){
            String[] line = i.next();
            InsertQuery query = new InsertQuery(regionMemberTable.getTable());
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_GENOME_ID), genomeId);
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId);
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_CHROM), line[0]);
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_START), line[1]);
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_END), line[2]);
            query.addColumn(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_DESCRIPTION), line[3]);
            
            conn.createStatement().executeUpdate(query.toString());
        }
        if (Thread.currentThread().isInterrupted()) {
            conn.rollback();
        } else {
            conn.commit();
        }
        conn.setAutoCommit(true);
    }
    
    public static void removeRegionList(int regionSetId) throws SQLException {
        
        TableSchema regionMemberTable = MedSavantDatabase.RegionsetmembershipTableSchema;
        TableSchema regionSetTable = MedSavantDatabase.RegionsetTableSchema;

        Connection c = ConnectionController.connectPooled();
        
        //remove members
        DeleteQuery q1 = new DeleteQuery(regionMemberTable.getTable());
        q1.addCondition(BinaryConditionMS.equalTo(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        c.createStatement().execute(q1.toString());
        
        //remove from region regionSetTable
        DeleteQuery q2 = new DeleteQuery(regionSetTable.getTable());
        q2.addCondition(BinaryConditionMS.equalTo(regionSetTable.getDBColumn(RegionSetTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        c.createStatement().execute(q2.toString());
    }
    
    public static List<RegionSet> getRegionSets() throws SQLException {
        
        TableSchema table = MedSavantDatabase.RegionsetTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<RegionSet> result = new ArrayList<RegionSet>();
        while(rs.next()){
            result.add(new RegionSet(rs.getInt(RegionSetTableSchema.COLUMNNAME_OF_REGION_SET_ID), rs.getString(RegionSetTableSchema.COLUMNNAME_OF_NAME)));
        }
        return result;
    }
    
    public static int getNumberRegions(int regionSetId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.countAll());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));

        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        rs.next();
        return rs.getInt(1);
    }

    public static List<String> getRegionNamesInRegionSet(int regionSetId, int limit) throws SQLException {
        
        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString() + " LIMIT " + limit);

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            result.add(rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_DESCRIPTION));
        }
        return result;
    }

    public static List<GenomicRegion> getRegionsInRegionSet(int regionSetId) throws SQLException {
        
        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString());
        
        List<GenomicRegion> result = new ArrayList<GenomicRegion>();
        while(rs.next()){
            result.add(new GenomicRegion(
                    rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_CHROM), 
                    new Range(rs.getDouble(RegionSetMembershipTableSchema.COLUMNNAME_OF_START), rs.getDouble(RegionSetMembershipTableSchema.COLUMNNAME_OF_END))));
        }
        return result;
    }
    
    public static List<BEDRecord> getBedRegionsInRegionSet(int regionSetId, int limit) throws NonFatalDatabaseException, SQLException {

        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;
        
        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        
        ResultSet rs = ConnectionController.connectPooled().createStatement().executeQuery(query.toString() + " LIMIT " + limit);
        
        List<BEDRecord> result = new ArrayList<BEDRecord>();
        while(rs.next()){
            result.add(new BEDRecord(
                    rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_CHROM), 
                    rs.getInt(RegionSetMembershipTableSchema.COLUMNNAME_OF_START), 
                    rs.getInt(RegionSetMembershipTableSchema.COLUMNNAME_OF_END), 
                    rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_DESCRIPTION)));
        }
        return result;           
    }
    
    
}
