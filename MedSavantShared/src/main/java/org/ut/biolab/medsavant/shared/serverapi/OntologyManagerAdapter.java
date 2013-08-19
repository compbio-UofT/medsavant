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
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;


/**
 * Interface used to access an ontology hierarchy.
 *
 * @author tarkvara
 */
public interface OntologyManagerAdapter extends Remote {


    public static final String basePath = "http://medsavant.com/serve/ontology/";

    public static final URL GO_OBO_URL = NetworkUtils.getKnownGoodURL(basePath + "gene_ontology.1_2.obo");
    public static final URL HPO_OBO_URL = NetworkUtils.getKnownGoodURL(basePath + "human-phenotype-ontology.obo");
    public static final URL OMIM_OBO_URL = NetworkUtils.getKnownGoodURL(basePath + "omim.obo");

    public static final URL GO_TO_GENES_URL = NetworkUtils.getKnownGoodURL(basePath + "gene_association.goa_human.gz");
    public static final URL HPO_TO_GENES_URL = NetworkUtils.getKnownGoodURL(basePath + "phenotype_to_genes.txt");
    public static final URL OMIM_TO_HPO_URL = NetworkUtils.getKnownGoodURL(basePath + "phenotype_annotation.tab");


    /*
    public static final URL GO_OBO_URL = NetworkUtils.getKnownGoodURL("http://geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo");
    public static final URL HPO_OBO_URL = NetworkUtils.getKnownGoodURL("http://compbio.charite.de/svn/hpo/trunk/src/ontology/human-phenotype-ontology.obo");
    public static final URL OMIM_OBO_URL = NetworkUtils.getKnownGoodURL("http://obo.svn.sourceforge.net/viewvc/obo/phenotype-commons/annotations/OMIM/omim.obo");

    public static final URL GO_TO_GENES_URL = NetworkUtils.getKnownGoodURL("http://www.geneontology.org/gene-associations/gene_association.goa_human.gz");
    public static final URL HPO_TO_GENES_URL = NetworkUtils.getKnownGoodURL("http://compbio.charite.de/svn/hpo/trunk/src/annotation/phenotype_to_genes.txt");
    public static final URL OMIM_TO_HPO_URL = NetworkUtils.getKnownGoodURL("http://compbio.charite.de/svn/hpo/trunk/src/annotation/phenotype_annotation.tab");
    *
    */
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
    void addOntology(String sessID, String ontName, OntologyType ont, URL oboData, URL mappingData) throws IOException, InterruptedException, SQLException, RemoteException, SessionExpiredException, InitializationException;

    /**
     * As part of the maintenance process, remove the tables for a given ontology.
     *
     * @param sessID the login session
     * @param ontName the ontology to be removed
     */
    void removeOntology(String sessID, String ontName) throws IOException, InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * Retrieve a list of all available ontologies.
     */
    Ontology[] getOntologies(String sessID) throws InterruptedException, SQLException, RemoteException, SessionExpiredException;

    /**
     * Get a list of all terms in the given ontology.
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
