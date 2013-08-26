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

package org.ut.biolab.medsavant.server.ontology;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.server.SessionController;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyInfoColumns;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.persistence.EntityManager;
import org.ut.biolab.medsavant.shared.persistence.EntityManagerFactory;
import org.ut.biolab.medsavant.shared.query.Query;
import org.ut.biolab.medsavant.shared.query.QueryManager;
import org.ut.biolab.medsavant.shared.query.QueryManagerFactory;
import org.ut.biolab.medsavant.shared.query.ResultRow;
import org.ut.biolab.medsavant.shared.serverapi.OntologyManagerAdapter;
import org.ut.biolab.medsavant.shared.solr.exception.InitializationException;
import org.ut.biolab.medsavant.shared.util.RemoteFileCache;

import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * Concrete implementation of ontology manager.
 *
 * @author tarkvara
 */
public class OntologyManager extends MedSavantServerUnicastRemoteObject implements OntologyManagerAdapter, OntologyColumns, OntologyInfoColumns {

    //ToDo add inserts
    private static final Log LOG = LogFactory.getLog(OntologyManager.class);

    private static OntologyManager instance;

    private PooledConnection connection;
    private final TableSchema ontologySchema;
    private final TableSchema ontologyInfoSchema;
    private static QueryManager queryManager;
    private static EntityManager entityManager;

    public static OntologyManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new OntologyManager();
        }
        return instance;
    }

    private OntologyManager() throws RemoteException {
        ontologySchema = MedSavantDatabase.OntologyTableSchema;
        ontologyInfoSchema = MedSavantDatabase.OntologyInfoTableSchema;

        queryManager = QueryManagerFactory.getQueryManager();
        entityManager = EntityManagerFactory.getEntityManager();
    }

    @Override
    public void addOntology(String sessID, String name, OntologyType type, URL oboData, URL mappingData) throws IOException, SQLException, SessionExpiredException, InitializationException {

        switch (type) {
            case GO:
                populateGOTables(sessID, name, oboData, mappingData);
                break;
            case HPO:
                populateHPOTables(sessID, name, oboData, mappingData);
                break;
            case OMIM:
                // In the case of OMIM, the mappingData is actually a text-file mapping between OMIM terms and HPO
                // terms; to be useful, this assumes that populateHPOTables has already created the mapping between HPO
                // terms and genes.
                populateOMIMTables(sessID, name, oboData, mappingData);
                break;
        }

        //remove old entries
        removeOntology(sessID, name);

        //add new ones
        Ontology ontology = new Ontology(type,name,oboData,mappingData);
        entityManager.persist(ontology);
    }

    @Override
    public void removeOntology(String sessID, String ontName) throws IOException, SQLException, RemoteException, SessionExpiredException {
        Query query = queryManager.createQuery("Delete from Ontology o where o.name= :name");
        query.setParameter("name", ontName);
        query.executeDelete();


        query = queryManager.createQuery("Delete from OntologyTerm o where o.name= :name");
        query.setParameter("name", ontName);
        query.executeDelete();
    }

    @Override
    public Ontology[] getOntologies(String sessID) throws SQLException, RemoteException, SessionExpiredException {

        Query query = queryManager.createQuery("Select o from Ontology o");
        List<Ontology> ontologies = query.execute();
        return ontologies.toArray(new Ontology[ontologies.size()]);
    }

    @Override
    public OntologyTerm[] getAllTerms(String sessID, OntologyType ont) throws InterruptedException, SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select t from OntologyTerm t where t.type= :type");
        query.setParameter("type", ont);
        List<OntologyTerm> terms = query.execute();
        return terms.toArray(new OntologyTerm[terms.size()]);
    }


    @Override
    public String[] getGenesForTerm(String sessID, OntologyTerm term, String refID) throws SQLException, SessionExpiredException {

        Query query = queryManager.createQuery("Select t.genes from OntologyTerm t where t.id= :id");
        query.setParameter("id", term.getID());
        List<ResultRow> resultRows = query.executeForRows();

        if (resultRows.size() > 0) {
            return (String[] )resultRows.get(0).getObject("genes");
        } else {
            return null;
        }
    }

    /**
     * When loading an ontology it's more efficient to do a single big-ass query instead of a whack of small ones.
     * @param terms an array of terms whose genes are to be fetched
     * @return an map of term IDs to arrays of genes
     */
    @Override
    public Map<OntologyTerm, String[]> getGenesForTerms(String sessID, OntologyTerm[] terms, String refID) throws SQLException, SessionExpiredException {
        //this might not be necessary if the genes are stored on the OntologyTerm instances
        Map<OntologyTerm, String[]> result = new HashMap<OntologyTerm, String[]>();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < terms.length; i++) {
            sb.append("\"" + terms[i].getID() + "\"");
            if (i != terms.length - 1) {
                sb.append(",");
            }
        }

        String statement = String.format("Select t from OntologyTerm t where t.id IN (%s)", sb.toString());
        Query query = queryManager.createQuery(statement);
        List<OntologyTerm> results = query.execute();

        for (OntologyTerm term : results) {
            result.put(term, term.getGenes());
        }

        return result;
    }

    /**
     * @param sessID login session
     * @param ont ontology to be searched (pass null to search across all ontologies)
     * @param geneName name of the gene whose terms we want
     * @throws SQLException
     */
    @Override
    public OntologyTerm[] getTermsForGene(String sessID, OntologyType ont, String geneName) throws SQLException, SessionExpiredException {
        //this might not be necessary if the genes are stored on the OntologyTerm instances
        String statement = "Select t from OntologyTerm t where t.genes= :geneName";
        Query query = queryManager.createQuery(statement);
        query.setParameter("geneName", geneName);
        List<OntologyTerm> results = query.execute();

        return results.toArray(new OntologyTerm[results.size()]);
    }

    /**
     * Populate the tables with Gene Ontology data
     * @param oboData an OBO file containing gene ontology terms
     * @param goToGeneData a gzipped GAF file (http://www.geneontology.org/GO.format.gaf-2_0.shtml)
     * @throws IOException
     */
    private void populateGOTables(String sessID, String name, URL oboData, URL goToGeneData) throws IOException, SQLException, SessionExpiredException {
        Map<String, OntologyTerm> terms = new OBOParser(OntologyType.GO).load(oboData);

        connection = ConnectionController.connectPooled(sessID);
        LOG.info("Session " + sessID + " made connection");
        try {
            populateTable(name, terms);

            Map<String, Set<String>> allGenes = new HashMap<String, Set<String>>();
            // Expecting a GZIPped tab-delimited text file in GAF (GO Annotation File) format.
            // We are only interested in columns 2 (gene), 3 (qualifier), and 4 (GO term).

            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(RemoteFileCache.getCacheFile(goToGeneData)))));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0 && line.charAt(0) != '!') {
                        String[] fields = line.split("\t");
                        if (fields.length > 4 && !fields[3].equals("NOT")) {
                            addGenesToTerm(terms, allGenes, fields[2], fields[4]);
                        }
                    }
                }
                entityManager.persistAll(new ArrayList<OntologyTerm>(terms.values()));
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE ontology SET genes=? WHERE id=?");
                for (String t: allGenes.keySet()) {
                    Set<String> termGenes = allGenes.get(t);
                    String geneString = StringUtils.join(termGenes, '|');
                    //connection.executePreparedUpdate(updateStatement, "|" + geneString + "|", t);
                }
            } catch (InitializationException e) {
                LOG.error("Error persisting ontology terms.");
            } finally {
                reader.close();
            }

        } finally {
            connection.close();
            connection = null;
        }
    }

    private void addGenesToTerm(Map<String, OntologyTerm> allTerms, Map<String, Set<String>> allGenes, String gene, String term) {
        Set<String> termGenes = allGenes.get(term);
        if (termGenes == null) {
            termGenes = new HashSet<String>();
            allGenes.put(term, termGenes);
        }
        termGenes.add(gene);

        OntologyTerm term2 = allTerms.get(term);
        if (term2 != null) {
            for (String p: term2.getParentIDs()) {
                addGenesToTerm(allTerms, allGenes, gene, p);
            }
        }
    }

    private void populateHPOTables(String sessID, String name, URL oboData, URL hpoToGeneData) throws IOException, SQLException, SessionExpiredException, InitializationException {
        Map<String, OntologyTerm> terms = new OBOParser(OntologyType.HPO).load(oboData);
        connection = ConnectionController.connectPooled(sessID);
        try {
            populateTable(name, terms);

            PreparedStatement updStmt = connection.prepareStatement("UPDATE ontology SET genes=? WHERE id=?");

            // Mapping file from charite.de has HP terms one per line.
            BufferedReader reader = new BufferedReader(new FileReader(RemoteFileCache.getCacheFile(hpoToGeneData)));
            String line;
            OntologyTerm current = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    int hpPos = line.indexOf("(HP:");
                    if (hpPos > 0) {
                        String term = line.substring(hpPos + 1, hpPos + 11);

                        int genesStart = line.indexOf("\t[");
                        if (genesStart > 0) {
                            // The list of genes will be
                            line = line.substring(genesStart + 2, line.length() - 1);
                            String[] genes = line.split(", ");
                            current = terms.get(term);
                            current.setGenes(genes);
                            entityManager.persist(current);
                            String geneString = "|";
                            for (String g: genes) {
                                geneString += g.substring(0, g.indexOf('(')) + "|";
                            }
                            connection.executePreparedUpdate(updStmt, geneString, term);
                        }
                    }
                }
            }
        } finally {
            connection.close();
            connection = null;
        }
    }

    private void populateOMIMTables(String sessID, String name, URL oboData, URL omimToHPOData) throws IOException, SQLException, SessionExpiredException {
        Map<String, OntologyTerm> terms = new OBOParser(OntologyType.OMIM).load(oboData);
        connection = ConnectionController.connectPooled(sessID);
        try {
            populateTable(name, terms);

            PreparedStatement updStmt = connection.prepareStatement("UPDATE ontology SET genes=? WHERE id=?");
            PreparedStatement hpoSelStmt = connection.prepareStatement("SELECT genes FROM ontology where ontology='HPO' AND (id=? OR INSTR(alt_ids, ?))");
            Map<String, String[]> hpoGeneCache = new HashMap<String, String[]>();

            // OMIM mapping file is ordered by OMIM number, with one HPO term on each line.
            // When the OMIM number changes we write the accumulated genes out for the given term.
            BufferedReader reader = new BufferedReader(new FileReader(RemoteFileCache.getCacheFile(omimToHPOData)));
            String line;
            String omimTerm = null;
            String omimGenes = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("OMIM")) {
                    String[] fields = line.split("\t");
                    if (fields.length >= 5 && !fields[3].equals("NOT")) {
                        String newTerm = "MIM:" + fields[1];
                        if (!newTerm.equals(omimTerm)) {
                            if (omimGenes != null) {
                                connection.executePreparedUpdate(updStmt, omimGenes, omimTerm);
                                omimGenes = null;
                            }
                            omimTerm = newTerm;
                        }

                        String hpoTerm = fields[4];
                        String[] hpoGenes = hpoGeneCache.get(hpoTerm);
                        if (hpoGenes == null) {
                            ResultSet rs = connection.executePreparedQuery(hpoSelStmt, hpoTerm, hpoTerm);
                            if (rs.next()) {
                                String geneString = rs.getString(1);     // pipe-delimited list of genes
                                if (geneString != null) {
                                    hpoGenes = geneString.split("\\|");
                                } else {
                                    hpoGenes = new String[0];
                                }
                            } else {
                                hpoGenes = new String[0];
                                LOG.info("Unable to find HPO term " + hpoTerm + " for " + omimTerm);
                            }
                            hpoGeneCache.put(hpoTerm, hpoGenes);
                        }
                        for (String g: hpoGenes) {
                            if (g.length() > 0) {
                                g = "|" + g + "|";
                                if (omimGenes == null) {
                                    omimGenes = g;
                                } else if (!omimGenes.contains(g)) {
                                    omimGenes += g.substring(1);
                                }
                            }
                        }
                    }
                }
            }
            if (omimGenes != null) {
                connection.executePreparedUpdate(updStmt, omimGenes, omimTerm);
            }
        } finally {
            if (connection != null) { connection.close(); }
        }
    }

    private void populateTable(String name, Map<String, OntologyTerm> terms) throws SQLException {
        String backupTableName = null;
        try {
            entityManager.persistAll(new ArrayList<OntologyTerm>(terms.values()));
        } catch (InitializationException e) {
            LOG.error("Error persisting ontology terms");
        }
       /* // Insert records for all the terms.  Different prepared statement used depending on whether we have parents or not.
        PreparedStatement prep4 = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF).toString());
        PreparedStatement prep5a = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, ALT_IDS).toString());
        PreparedStatement prep5b = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, PARENTS).toString());
        PreparedStatement prep6 = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, ALT_IDS, PARENTS).toString());
        int mostAltIDs = 1;
        for (OntologyTerm t: terms.values()) {
            PreparedStatement prep;
            if (t.getAltIDs().length > 0) {
                if (t.getParentIDs().length > 0) {
                    prep = prep6;
                    prep.setString(6, StringUtils.join(t.getParentIDs(), ','));
                } else {
                    prep = prep5a;
                }
                prep.setString(5, StringUtils.join(t.getAltIDs(), ','));
                if (t.getAltIDs().length > mostAltIDs) {
                    mostAltIDs = t.getAltIDs().length;
                    LOG.info(t.getID() + " had " + mostAltIDs + " alt_ids.");   // For debug purposes.
                }
            } else if (t.getParentIDs().length > 0) {
                prep = prep5b;
                prep.setString(5, StringUtils.join(t.getParentIDs(), ','));
            } else {
                prep = prep4;
            }
            prep.setString(1, name);
            prep.setString(2, t.getID());
            prep.setString(3, t.getName());
            prep.setString(4, t.getDef());
            prep.executeUpdate();
        }
        LOG.debug(String.format("Inserted %d records.", terms.size()));

        // If we got here, drop the backup table.
        if (backupTableName != null) {
            connection.executeUpdate("DROP TABLE ontology_back");
            LOG.debug("Dropped ontology_back.");
        }*/
    }

    private static OntologyTerm findTermByID(OntologyTerm[] terms, String termID) {
        for (OntologyTerm t: terms) {
            if (t.getID().equals(termID)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Called from <code>createDatabase()</code> to create all the ontology tables on
     * a background thread.
     *
     * @param sessID
     */
    public void populate(final String sessID) {
        new Thread() {
            @Override
            public void run() {
                try {
                    LOG.info("dbname for connection: " + ConnectionController.getDBName(sessID));
                    addOntology(sessID, OntologyType.GO.toString(), OntologyType.GO, GO_OBO_URL, GO_TO_GENES_URL);
                    addOntology(sessID, OntologyType.HPO.toString(), OntologyType.HPO, HPO_OBO_URL, HPO_TO_GENES_URL);
                    addOntology(sessID, OntologyType.OMIM.toString(), OntologyType.OMIM, OMIM_OBO_URL, OMIM_TO_HPO_URL);
                    SessionController.getInstance().unregisterSession(sessID);
                } catch (Exception ex) {
                    LOG.error("Error populating ontology tables.", ex);
                }
            }
        }.start();
    }

    /**
     * As opposed to removeOntology(), this method only removes the entry in the ontology table, not the terms.
     * @param name
     */
    private void deleteOntology(String name) {
        Query query = queryManager.createQuery("Delete from Ontology o where o.name = :name");
        query.setParameter("name", name);
        query.executeDelete();
    }
}
