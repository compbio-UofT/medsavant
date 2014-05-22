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
package org.ut.biolab.medsavant.shared.serverapi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

import org.ut.biolab.medsavant.shared.model.Block;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 * Public interface to the server's standard gene sets.
 *
 * @author tarkvara
 */
public interface GeneSetManagerAdapter extends Remote {

    /**
     * Get the gene-sets for the given reference. There should be exactly one.
     *
     * @param sessID the login session ID
     * @param refName the reference name (e.g. "hg18")
     */
    public GeneSet getGeneSet(String sessID, String refName) throws SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all available gene-sets for all references.
     */
    public GeneSet[] getGeneSets(String sessID) throws SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all genes for the given set.
     */
    public Gene[] getGenes(String sessID, GeneSet set) throws SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of genes that overlap the given region.
     *
     * @throws SQLException
     * @throws SessionExpiredException
     */
    public Gene[] getGenesInRegion(String sessID, GeneSet geneSet, String chrom, int start_position, int end_position) throws SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all transcripts for the given set.
     */
    public Gene[] getTranscripts(String sessID, GeneSet set) throws SQLException, RemoteException, SessionExpiredException;

    /**
     * Get all blocks associated with the given gene. Not currently presented to
     * the user in any fashion.
     */
    public Block[] getBlocks(String sessID, Gene gene) throws SQLException, RemoteException, SessionExpiredException;
}
