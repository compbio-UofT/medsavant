/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.serverapi;

import java.io.IOException;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Map;
import org.ut.biolab.medsavant.shared.model.Ontology;

import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.Modifier;
import static org.ut.biolab.medsavant.shared.util.ModificationType.*;

import org.ut.biolab.medsavant.shared.util.NetworkUtils;


/**
 * Interface used to access an ontology hierarchy.
 *
 * @author tarkvara
 */
public interface OntologyManagerAdapter extends Remote {

    /**
     * Check the status of a lengthy process, giving the user the option to cancel.
     */
    ProgressStatus checkProgress(String sessID, boolean userCancelled) throws RemoteException, SessionExpiredException;

    /**
     * As part of the maintenance process, populate the tables for the given ontology.
     *
     * @param sessID the login session
     * @param ontName name of ontology to be added
     * @param ont type of ontology to be added
     * @param oboData URL of OBO file containing the ontology
     * @param geneData URL of text file defining mapping between terms and genes (format may vary)
     */
    @Modifier(type=ONTOLOGY)
    void addOntology(String sessID, String ontName, OntologyType ont, URL oboData, URL mappingData) throws IOException, InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * As part of the maintenance process, remove the tables for a given ontology.
     *
     * @param sessID the login session
     * @param ontName the ontology to be removed
     */
    @Modifier(type=ONTOLOGY)
    void removeOntology(String sessID, String ontName) throws IOException, InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * Retrieve a list of all available ontologies.
     */
    Ontology[] getOntologies(String sessID) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all terms in the given ontology that are associated with at least one gene.
     * If limit is given, the number of terms returned is limited to the first 'limit'.
     */
    OntologyTerm[] getAllTerms(String sessID, OntologyType type) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;    

    /**
     * Get the names of all genes corresponding to the given term.
     * @param sessID the login session
     * @param term the term being looked for
     * @param refName the reference being looked for         
     * @return genes corresponding to <code>term</code>
     */
    String[] getGenesForTerm(String sessID, OntologyTerm term, String refName) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;    
        
    /**
     * Get the names of all genes corresponding to the given terms.  When loading a large number of terms, this can be more efficient
     * than calling <code>getGenesForTerm</code> separately for each term.
     *
     * @param sessID the login session
     * @param terms array of terms being looked for
     * @param refID the current reference
     * @return a map associating terms with their genes
     * @throws SQLException
     * @throws RemoteException
     */
    Map<OntologyTerm, String[]> getGenesForTerms(String sessID, OntologyTerm[] terms, String refID) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all terms of the given ontology corresponding to the given gene.
     * @param ont the ontology to be searched
     * @param geneName name of the gene being looked for
     * @return ontology terms corresponding to <code>gene</code>
     */
    OntologyTerm[] getTermsForGene(String sessID, OntologyType ont, String geneName) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;
}
