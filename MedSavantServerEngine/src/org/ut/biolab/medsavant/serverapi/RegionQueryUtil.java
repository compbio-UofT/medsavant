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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.sqlbuilder.DeleteQuery;
import com.healthmarketscience.sqlbuilder.FunctionCall;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.RegionSetTableSchema;
import org.ut.biolab.medsavant.db.MedSavantDatabase.RegionSetMembershipTableSchema;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.util.FileServer;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportDelimitedFile;
import org.ut.biolab.medsavant.model.BEDRecord;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class RegionQueryUtil extends MedSavantServerUnicastRemoteObject implements RegionQueryUtilAdapter {

    private static RegionQueryUtil instance;

    public static synchronized RegionQueryUtil getInstance() throws RemoteException {
        if (instance == null) {
            instance = new RegionQueryUtil();
        }
        return instance;
    }

    public RegionQueryUtil() throws RemoteException {super();}


    @Override
    public void addRegionList(String sid,String geneListName, int genomeId,  RemoteInputStream fileStream, char delim, FileFormat fileFormat, int numHeaderLines) throws NonFatalDatabaseException, SQLException, RemoteException {

        Connection conn = ConnectionController.connectPooled(sid);
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

        //prepare
        Iterator<String[]> i = null;
        try {
            File f = FileServer.getInstance().sendFile(fileStream);
            i = ImportDelimitedFile.getFileIterator(f.getAbsolutePath(), delim, numHeaderLines, fileFormat);
        } catch (IOException ex) {
            Logger.getLogger(RegionQueryUtil.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

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
        conn.close();
    }

    @Override
    public void removeRegionList(String sid,int regionSetId) throws SQLException {

        TableSchema regionMemberTable = MedSavantDatabase.RegionsetmembershipTableSchema;
        TableSchema regionSetTable = MedSavantDatabase.RegionsetTableSchema;

        //remove members
        DeleteQuery q1 = new DeleteQuery(regionMemberTable.getTable());
        q1.addCondition(BinaryConditionMS.equalTo(regionMemberTable.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        ConnectionController.execute(sid, q1.toString());

        //remove from region regionSetTable
        DeleteQuery q2 = new DeleteQuery(regionSetTable.getTable());
        q2.addCondition(BinaryConditionMS.equalTo(regionSetTable.getDBColumn(RegionSetTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));
        ConnectionController.execute(sid, q2.toString());
    }

    @Override
    public List<RegionSet> getRegionSets(String sid) throws SQLException {

        TableSchema table = MedSavantDatabase.RegionsetTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<RegionSet> result = new ArrayList<RegionSet>();
        while(rs.next()){
            result.add(new RegionSet(rs.getInt(RegionSetTableSchema.COLUMNNAME_OF_REGION_SET_ID), rs.getString(RegionSetTableSchema.COLUMNNAME_OF_NAME)));
        }
        return result;
    }

    @Override
    public int getNumberRegions(String sid, int regionSetId) throws SQLException {

        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addCustomColumns(FunctionCall.countAll());
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        rs.next();
        return rs.getInt(1);
    }

    @Override
    public List<String> getRegionNamesInRegionSet(String sid, int regionSetId, int limit) throws SQLException {

        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + limit);

        List<String> result = new ArrayList<String>();
        while(rs.next()){
            result.add(rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_DESCRIPTION));
        }
        return result;
    }

    @Override
    public List<GenomicRegion> getRegionsInRegionSet(String sid, int regionSetId) throws SQLException {

        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());

        List<GenomicRegion> result = new ArrayList<GenomicRegion>();
        while(rs.next()){
            result.add(new GenomicRegion(
                    rs.getString(RegionSetMembershipTableSchema.COLUMNNAME_OF_CHROM),
                    new Range(rs.getDouble(RegionSetMembershipTableSchema.COLUMNNAME_OF_START), rs.getDouble(RegionSetMembershipTableSchema.COLUMNNAME_OF_END))));
        }
        return result;
    }

    @Override
    public List<BEDRecord> getBedRegionsInRegionSet(String sid, int regionSetId, int limit) throws NonFatalDatabaseException, SQLException {

        TableSchema table = MedSavantDatabase.RegionsetmembershipTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetMembershipTableSchema.COLUMNNAME_OF_REGION_SET_ID), regionSetId));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString() + " LIMIT " + limit);

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

    @Override
    public boolean listNameExists(String sid, String name) throws SQLException {

        TableSchema table = MedSavantDatabase.RegionsetTableSchema;

        SelectQuery query = new SelectQuery();
        query.addFromTable(table.getTable());
        query.addAllColumns();
        query.addCondition(BinaryConditionMS.equalTo(table.getDBColumn(RegionSetTableSchema.COLUMNNAME_OF_NAME), name));

        ResultSet rs = ConnectionController.executeQuery(sid, query.toString());
        return rs.next();
    }
}
