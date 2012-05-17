/*
 *    Copyright 2012 University of Toronto
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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.MedSavantDatabase.GeneSetColumns;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.model.Block;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 * Server-side implementation of class which managed GeneSets.
 *
 * @author tarkvara
 */
public class GeneSetManager extends MedSavantServerUnicastRemoteObject implements GeneSetManagerAdapter, GeneSetColumns {
    private static final Log LOG = LogFactory.getLog(GeneSetManager.class);

    private static GeneSetManager instance;

    public static synchronized GeneSetManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new GeneSetManager();
        }
        return instance;
    }

    public GeneSetManager() throws RemoteException {
    }


    /**
     * Get a list of all available gene sets.
     * @param sessID
     * @return
     * @throws SQLException 
     */
    @Override
    public List<GeneSet> getGeneSets(String sessID) throws SQLException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.distinct().select(GENOME, TYPE);
        LOG.info("getGeneSets:" + query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<GeneSet> result = new ArrayList<GeneSet>();
        while (rs.next()) {
            result.add(new GeneSet(rs.getString(1), rs.getString(2)));
        }

        return result;
    }

    /**
     * Get a list of all gene sets for the given reference genome.
     * @param sessID session ID
     * @param refName reference name (not ID)
     * @return
     * @throws SQLException 
     */
    @Override
    public List<GeneSet> getGeneSets(String sessID, String refName) throws SQLException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.distinct().where(GENOME, refName).select(TYPE);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<GeneSet> result = new ArrayList<GeneSet>();
        while (rs.next()) {
            result.add(new GeneSet(refName, rs.getString(1)));
        }

        return result;
    }
    
    @Override
    public List<Gene> getGenes(String sessID, GeneSet geneSet) throws SQLException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, geneSet.getGenome(), TYPE, geneSet.getType()).groupBy(NAME).select(NAME, CHROM, "MIN(start)", "MAX(end)", "MIN(codingStart)", "MAX(codingEnd)");
        LOG.debug(query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Gene> result = new ArrayList<Gene>();
        while (rs.next()) {
            result.add(new Gene(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), null));
        }

        return result;
    }

    @Override
    public List<Gene> getTranscripts(String sessID, GeneSet geneSet) throws SQLException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, geneSet.getGenome(), TYPE, geneSet.getType()).select(NAME, CHROM, START, END, CODING_START, CODING_END, TRANSCRIPT);
        LOG.debug(query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Gene> result = new ArrayList<Gene>();
        while (rs.next()) {
            result.add(new Gene(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getString(7)));
        }

        return result;
    }

    @Override
    public List<Block> getBlocks(String sessID, Gene gene) throws SQLException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
