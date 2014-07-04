/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.serverapi;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.healthmarketscience.sqlbuilder.SelectQuery;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.GeneSetColumns;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.shared.model.Block;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.serverapi.GeneSetManagerAdapter;
import org.ut.biolab.medsavant.shared.util.BinaryConditionMS;

/**
 * Server-side implementation of class which managed GeneSets.
 *
 * @author tarkvara
 */
public class GeneSetManager extends MedSavantServerUnicastRemoteObject implements GeneSetManagerAdapter, GeneSetColumns {

    private static final Log LOG = LogFactory.getLog(GeneSetManager.class);

    private static GeneSetManager instance;

    public static synchronized GeneSetManager getInstance() throws RemoteException, SessionExpiredException {
        if (instance == null) {
            instance = new GeneSetManager();
        }
        return instance;
    }

    private GeneSetManager() throws RemoteException, SessionExpiredException {
    }

    /**
     * Get a list of all available gene sets.
     *
     * @param sessID
     * @return
     * @throws SQLException
     */
    @Override
    public GeneSet[] getGeneSets(String sessID) throws SQLException, SessionExpiredException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.distinct().groupBy(GENOME).select(GENOME, TYPE, "COUNT(DISTINCT name)");
        LOG.info("getGeneSets:" + query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        List<GeneSet> result = new ArrayList<GeneSet>();
        while (rs.next()) {
            result.add(new GeneSet(rs.getString(1), rs.getString(2), rs.getInt(3)));
        }

        return result.toArray(new GeneSet[0]);
    }

    /**
     * Get the gene set for the given reference genome.
     *
     * @param sessID session ID
     * @param refName reference name (not ID)
     * @return
     * @throws SQLException
     */
    @Override
    public GeneSet getGeneSet(String sessID, String refName) throws SQLException, SessionExpiredException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.distinct().where(GENOME, refName).select(TYPE, "COUNT(DISTINCT name)");
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        if (rs.next()) {
            return new GeneSet(refName, rs.getString(1), rs.getInt(2));
        }
        return null;
    }

    public static void main(String[] argv) {
        TableSchema table = MedSavantDatabase.GeneSetTableSchema;
        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, "hg19", TYPE, "RefSeq").groupBy(CHROM).groupBy(NAME).select(NAME, CHROM, "MIN(start)", "MAX(end)", "MIN(codingStart)", "MAX(codingEnd)");
        BinaryCondition dumbChrsCondition1 = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\_%");
        query.addCondition(dumbChrsCondition1);
        BinaryCondition dumbChrsCondition2 = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\-%");
        query.addCondition(dumbChrsCondition2);
    }

    @Override
    public Gene[] getGenes(String sessID, GeneSet geneSet) throws SQLException, SessionExpiredException {

        TableSchema table = MedSavantDatabase.GeneSetTableSchema;
        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, geneSet.getReference(), TYPE, geneSet.getType()).groupBy(CHROM).groupBy(NAME).select(NAME, CHROM, "MIN(start)", "MAX(end)", "MIN(codingStart)", "MAX(codingEnd)");

        BinaryCondition dumbChrsCondition = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\_%");
        query.addCondition(dumbChrsCondition);
        BinaryCondition dumbNameCondition = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.NAME), "%-%");
        query.addCondition(dumbNameCondition);

        return getGenes(sessID, table, geneSet, query);     
    }

    @Override
    public Gene[] getGenesInRegion(String sessID, GeneSet geneSet, String chrom, int start_position, int end_position) throws SQLException, SessionExpiredException {
        TableSchema table = MedSavantDatabase.GeneSetTableSchema;
        ComboCondition restrictToRegion
                = ComboCondition.and(
                        BinaryCondition.equalTo(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), chrom),
                        ComboCondition.or(
                                ComboCondition.and(
                                        BinaryCondition.lessThan(table.getDBColumn(MedSavantDatabase.GeneSetColumns.START), start_position, true),
                                        BinaryCondition.greaterThan(table.getDBColumn(MedSavantDatabase.GeneSetColumns.END), start_position, true)
                                ),
                                ComboCondition.and(
                                        BinaryCondition.lessThan(table.getDBColumn(MedSavantDatabase.GeneSetColumns.START), end_position, true),
                                        BinaryCondition.greaterThan(table.getDBColumn(MedSavantDatabase.GeneSetColumns.END), end_position, true)
                                )
                        ),
                        BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.NAME), "%-%") //dumb name condition
                );
        
        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, geneSet.getReference(), TYPE, geneSet.getType()).groupBy(CHROM).groupBy(NAME).select(NAME, CHROM, "MIN(start)", "MAX(end)", "MIN(codingStart)", "MAX(codingEnd)");
        query.addCondition(restrictToRegion);
        return getGenes(sessID, table, geneSet, query);
    }

    private Gene[] getGenes(String sessID, TableSchema table, GeneSet geneSet, SelectQuery query) throws SQLException, SessionExpiredException {
        BinaryCondition dumbChrsCondition = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.CHROM), "%\\_%");
        query.addCondition(dumbChrsCondition);
        BinaryCondition dumbNameCondition = BinaryConditionMS.notlike(table.getDBColumn(MedSavantDatabase.GeneSetColumns.NAME), "%-%");
        query.addCondition(dumbNameCondition);

        LOG.info(query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        Gene[] result = new Gene[geneSet.getSize()];
        int i = 0;
        while (rs.next()) {
            Gene g = new Gene(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), null);
            result[i++] = g;
        }
        if (i != result.length) {
            LOG.info("There were " + result.length + " genes, but only " + i + " were loaded.");
        }
        result = Arrays.copyOf(result, i);

        return result;
    }

    @Override
    public Gene[] getTranscripts(String sessID, GeneSet geneSet) throws SQLException, SessionExpiredException {

        SelectQuery query = MedSavantDatabase.GeneSetTableSchema.where(GENOME, geneSet.getReference(), TYPE, geneSet.getType()).select(NAME, CHROM, START, END, CODING_START, CODING_END, TRANSCRIPT);
        LOG.debug(query);
        ResultSet rs = ConnectionController.executeQuery(sessID, query.toString());

        Gene[] result = new Gene[geneSet.getSize()];
        int i = 0;
        while (rs.next()) {
            result[i++] = new Gene(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getString(7));
        }

        return result;
    }

    @Override
    public Block[] getBlocks(String sessID, Gene gene) throws SQLException, RemoteException, SessionExpiredException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
