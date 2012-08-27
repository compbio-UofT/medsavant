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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.RegionSetColumns;
import org.ut.biolab.medsavant.db.MedSavantDatabase.RegionSetMembershipColumns;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.connection.PooledConnection;
import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.importing.ImportDelimitedFile;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;


/**
 *
 * @author Andrew
 */
public class RegionSetManager extends MedSavantServerUnicastRemoteObject implements RegionSetManagerAdapter, RegionSetMembershipColumns {
    private static final Log LOG = LogFactory.getLog(RegionSetManager.class);

    private static RegionSetManager instance;

    private RegionSetManager() throws RemoteException {
    }

    public static synchronized RegionSetManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new RegionSetManager();
        }
        return instance;
    }

    @Override
    public void addRegionSet(String sessID, String regionSetName, int genomeID, char delim, FileFormat fileFormat, int numHeaderLines, int fileID) throws IOException, SQLException, RemoteException {

        Connection conn = ConnectionController.connectPooled(sessID);

        try {
            // TODO: Since we're using the MyISAM engine for this table, rolling back has no effect.
            conn.setAutoCommit(false);

            //add region set
            InsertQuery query = MedSavantDatabase.RegionSetTableSchema.preparedInsert(RegionSetColumns.NAME);
            PreparedStatement prep = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
            prep.setString(1, regionSetName);
            prep.executeUpdate();

            ResultSet rs = prep.getGeneratedKeys();
            rs.next();
            int regionSetID = rs.getInt(1);

            File f = NetworkManager.getInstance().getFileByTransferID(sessID, fileID);
            Iterator<String[]> i = ImportDelimitedFile.getFileIterator(f.getAbsolutePath(), delim, numHeaderLines, fileFormat);

            query = MedSavantDatabase.RegionSetMembershipTableSchema.preparedInsert(GENOME_ID, REGION_SET_ID, CHROM, START, END, DESCRIPTION);
            prep = conn.prepareStatement(query.toString());
            while (i.hasNext() && !Thread.currentThread().isInterrupted()){
                String[] line = i.next();
                LOG.info(StringUtils.join(line, '\t'));
                prep.setInt(1, genomeID);
                prep.setInt(2, regionSetID);
                prep.setString(3, line[0]);
                prep.setString(4, line[1]);
                prep.setString(5, line[2]);
                prep.setString(6, line[3]);
                prep.executeUpdate();
            }

            // Since this table is defined with the MyISAM engine, the rollback doesn't actually do anything.
            if (Thread.currentThread().isInterrupted()) {
                conn.rollback();
            } else {
                conn.commit();
            }
            conn.setAutoCommit(true);
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeRegionSet(String sessID, int regionSetID) throws SQLException {

        Connection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.createStatement().executeUpdate(MedSavantDatabase.RegionSetMembershipTableSchema.delete(REGION_SET_ID, regionSetID).toString());
            conn.createStatement().executeUpdate(MedSavantDatabase.RegionSetTableSchema.delete(RegionSetColumns.REGION_SET_ID, regionSetID).toString());
        } finally {
            conn.close();
        }
    }

    @Override
    public List<RegionSet> getRegionSets(String sessID) throws SQLException {

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            SelectQuery query = MedSavantDatabase.RegionSetMembershipTableSchema.groupBy(REGION_SET_ID).leftJoin(MedSavantDatabase.RegionSetTableSchema, "region_set_id").select(REGION_SET_ID, "NAME", "COUNT(*)");
            LOG.info("getRegionSets: " + query);
            ResultSet rs = conn.createStatement().executeQuery(query.toString());
            List<RegionSet> result = new ArrayList<RegionSet>();
            while (rs.next()) {
                result.add(new RegionSet(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
            return result;
        } finally {
            conn.close();
        }
    }

    @Override
    public List<GenomicRegion> getRegionsInSet(String sessID, RegionSet set) throws SQLException {

        Connection conn = ConnectionController.connectPooled(sessID);

        try {
            SelectQuery query = MedSavantDatabase.RegionSetMembershipTableSchema.where(REGION_SET_ID, set.getID()).select(DESCRIPTION, CHROM, START, END);
            LOG.info("getRegionsInSet(" + set.getName() + "): " + query);
            ResultSet rs = conn.createStatement().executeQuery(query.toString());

            List<GenomicRegion> result = new ArrayList<GenomicRegion>();
            while (rs.next()) {
                result.add(new GenomicRegion(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
            }
            return result;
        } finally {
            conn.close();
        }
    }

    @Override
    public List<GenomicRegion> getRegionsInSets(String sessID, Collection<RegionSet> sets) throws SQLException {

        PooledConnection conn = ConnectionController.connectPooled(sessID);

        try {
            List<Integer> ids = new ArrayList<Integer>(sets.size());
            int maxRegions = 0;
            for (RegionSet s: sets) {
                ids.add(s.getID());
                maxRegions += s.getSize();
            }
            SelectQuery query = MedSavantDatabase.RegionSetMembershipTableSchema.distinct().whereIn(REGION_SET_ID, ids).select(DESCRIPTION, CHROM, START, END);
            ResultSet rs = conn.executeQuery(query.toString());

            List<GenomicRegion> result = new ArrayList<GenomicRegion>();
            while (rs.next()) {
                result.add(new GenomicRegion(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
            }
            return result;
        } finally {
            conn.close();
        }
    }

    @Override
    public void addToRegionSet(String sessID, RegionSet set, int genomeID, String chrom, int start, int end, String desc) throws SQLException, RemoteException{
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try{
            InsertQuery query = MedSavantDatabase.RegionSetMembershipTableSchema.insert(GENOME_ID, genomeID, REGION_SET_ID, set.getID(), CHROM, chrom, START, start, END, end, DESCRIPTION, desc);
            conn.executeUpdate(query.toString());
        } finally{
            conn.close();
        }

    }
}
