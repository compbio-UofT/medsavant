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

import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Table;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.TableSchema;
import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.model.Block;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.util.MedSavantServerUnicastRemoteObject;


/**
 * Server-side implementation of class which managed GeneSets.
 *
 * @author tarkvara
 */
public class GeneSetManager extends MedSavantServerUnicastRemoteObject implements GeneSetAdapter {

    private static GeneSetManager instance;

    public static synchronized GeneSetManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new GeneSetManager();
        }
        return instance;
    }

    public GeneSetManager() throws RemoteException {
    }


    @Override
    public List<GeneSet> getGeneSets(String sessID) throws SQLException {

        ResultSet rs = ConnectionController.executeQuery(sessID, MedSavantDatabase.GeneSetTableSchema.getListQuery().toString());

        List<GeneSet> result = new ArrayList<GeneSet>();
        while (rs.next()) {
            result.add(new GeneSet(rs.getString(1), rs.getString(2)));
        }

        return result;
    }
    
    @Override
    public List<Gene> getGenes(String sessID, GeneSet geneSet) throws SQLException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where("genome", geneSet.getGenome(), "type", geneSet.getType()).select("name", "chrom", "start", "end", "codingStart", "codingEnd");

        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<Gene> result = new ArrayList<Gene>();
        while (rs.next()) {
            result.add(new Gene(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)));
        }

        return result;
    }

    @Override
    public List<Block> getBlocks(String sessID, Gene gene) throws SQLException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
