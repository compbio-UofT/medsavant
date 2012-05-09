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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.ut.biolab.medsavant.model.Block;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;


/**
 * Public interface to the server's standard gene sets.
 *
 * @author tarkvara
 */
public interface GeneSetAdapter extends Remote {
    /**
     * Get a list of all available gene sets.
     */
    public List<GeneSet> getGeneSets(String sessID) throws SQLException, RemoteException;

    /**
     * Get a list of all available gene sets for the given reference.
     */
    public List<GeneSet> getGeneSets(String sessID, String refName) throws SQLException, RemoteException;

    /**
     * Get a list of all genes for the given set.
     */
    public List<Gene> getGenes(String sessID, GeneSet geneSet) throws SQLException, RemoteException;
    
    /**
     * Get a list of all transcripts for the given set.
     */
    public List<Gene> getTranscripts(String sessID, GeneSet geneSet) throws SQLException, RemoteException;
    
    /**
     * Get all blocks associated with the given gene.  Not currently presented to the user in any fashion.
     */
    public List<Block> getBlocks(String sessID, Gene gene) throws SQLException, RemoteException;
    
}
