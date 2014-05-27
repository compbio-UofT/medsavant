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
package org.ut.biolab.medsavant.server.ontology;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.server.MedSavantServerJob;
import org.ut.biolab.medsavant.server.MedSavantServerEngine;

import org.ut.biolab.medsavant.server.db.MedSavantDatabase;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns;
import org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyInfoColumns;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.server.db.ConnectionController;
import org.ut.biolab.medsavant.server.db.PooledConnection;
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.serverapi.OntologyManagerAdapter;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.ALT_IDS;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.DEF;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.ID;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.NAME;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.ONTOLOGY;
import static org.ut.biolab.medsavant.server.db.MedSavantDatabase.OntologyColumns.PARENTS;
import org.ut.biolab.medsavant.server.serverapi.SessionManager;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.util.RemoteFileCache;
import org.ut.biolab.medsavant.shared.util.WebResources;

/**
 * Concrete implementation of ontology manager.
 *
 * @author tarkvara
 */
public class OntologyManager extends MedSavantServerUnicastRemoteObject implements OntologyManagerAdapter, OntologyColumns, OntologyInfoColumns {

    private static final Log LOG = LogFactory.getLog(OntologyManager.class);

    private static OntologyManager instance;

    private PooledConnection connection;
    private final TableSchema ontologySchema;
    private final TableSchema ontologyInfoSchema;

    public static OntologyManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new OntologyManager();
        }
        return instance;
    }

    private OntologyManager() throws RemoteException {
        ontologySchema = MedSavantDatabase.OntologyTableSchema;
        ontologyInfoSchema = MedSavantDatabase.OntologyInfoTableSchema;
    }

    @Override
    public void addOntology(String sessID, String name, OntologyType type, URL oboData, URL mappingData) throws IOException, SQLException, SessionExpiredException {

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

        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            conn.executePreparedUpdate(ontologyInfoSchema.delete(ONTOLOGY_NAME, name).toString());
            conn.executePreparedUpdate(ontologyInfoSchema.preparedInsert(TYPE, ONTOLOGY_NAME, OBO_URL, MAPPING_URL).toString(), type.toString(), name, oboData.toString(), mappingData.toString());
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeOntology(String sessID, String ontName) throws IOException, SQLException, RemoteException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Ontology[] getOntologies(String sessID) throws SQLException, RemoteException, SessionExpiredException {
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            List<Ontology> results = new ArrayList<Ontology>();
            ResultSet rs = conn.executePreparedQuery(ontologyInfoSchema.distinct().select(TYPE, ONTOLOGY_NAME, OBO_URL, MAPPING_URL).toString());
            while (rs.next()) {
                String name = rs.getString(2);
                try {
                    results.add(new Ontology(Enum.valueOf(OntologyType.class, rs.getString(1)), rs.getString(2), new URL(rs.getString(3)), new URL(rs.getString(4))));
                } catch (MalformedURLException ex) {
                    LOG.warn(String.format("Error parsing URL for %s: %s", name, MiscUtils.getMessage(ex)), ex);
                }
            }
            return results.toArray(new Ontology[0]);
        } finally {
            conn.close();
        }
    }

    @Override
    public OntologyTerm[] getAllTerms(String sessID, OntologyType ont) throws InterruptedException, SQLException, SessionExpiredException {
        makeProgress(sessID, "Connecting...", 0.0);
        List<OntologyTerm> result = new ArrayList<OntologyTerm>();
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            double prog = 0.2;
            makeProgress(sessID, "Executing query...", prog);
            String q = ontologySchema.where(ONTOLOGY, ont.toString()).whereNotNull(GENES).orderBy(ID).select(ID, NAME, DEF, ALT_IDS, PARENTS).toString();
            LOG.info("Getting all ontology terms: " + q);
            ResultSet rs = conn.executePreparedQuery(q);
            while (rs.next()) {
                prog = 0.5 + prog * 0.5;    // Just for fun, to converge on 1.0
                makeProgress(sessID, "Retrieving ontology terms...", prog);
                result.add(new OntologyTerm(ont, rs.getString(1), rs.getString(2), rs.getString(3), StringUtils.split(rs.getString(4), ','), StringUtils.split(rs.getString(5), ',')));
            }
        } finally {
            conn.close();
        }
        return result.toArray(new OntologyTerm[0]);
    }

    @Override
    public String[] getGenesForTerm(String sessID, OntologyTerm term, String refID) throws SQLException, SessionExpiredException {
        List<String> result = new ArrayList<String>();
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            ResultSet rs = conn.executePreparedQuery(ontologySchema.where(ID, term.getID(), ONTOLOGY, term.getOntology().toString()).select(GENES));

            // Only expecting one row for each term.
            if (rs.next()) {
                String geneString = rs.getString(1);
                if (geneString != null && geneString.length() > 2) {
                    // Gene-string should be of the form "|gene1|gene2|...|geneN|".
                    // We start at position 1 to avoid an empty string at the start.
                    result.addAll(Arrays.asList(geneString.substring(1).split("\\|")));
                }
            }
        } finally {
            conn.close();
        }
        return result.toArray(new String[0]);
    }

    /**
     * When loading an ontology it's more efficient to do a single big-ass query
     * instead of a whack of small ones.
     *
     * @param terms an array of terms whose genes are to be fetched
     * @return an map of term IDs to arrays of genes
     */
    @Override
    public Map<OntologyTerm, String[]> getGenesForTerms(String sessID, OntologyTerm[] terms, String refID) throws SQLException, SessionExpiredException {
        Map<OntologyTerm, String[]> result = new HashMap<OntologyTerm, String[]>();
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            StringBuilder termsString = new StringBuilder("\'");
            if (terms.length > 0) {
                termsString.append(terms[0].getID());
                for (int i = 1; i < terms.length; i++) {
                    termsString.append("\', \'");
                    termsString.append(terms[i].getID());
                }
                termsString.append('\'');
                ResultSet rs = conn.executePreparedQuery(String.format("SELECT id,genes FROM ontology WHERE ontology=? and id IN (%s)", termsString), terms[0].getOntology().toString());

                while (rs.next()) {
                    OntologyTerm term = findTermByID(terms, rs.getString(1));

                    String genesString = rs.getString(2);
                    if (genesString != null && genesString.length() > 2) {
                        // Gene-string should be of the form "|gene1|gene2|...|geneN|".
                        // We start splitting at position 1 to avoid an empty string at the start.
                        result.put(term, genesString.substring(1).split("\\|"));
                    }
                }
            }
        } finally {
            conn.close();
        }
        return result;
    }

    /**
     * @param sessID login session
     * @param ont ontology to be searched (pass null to search across all
     * ontologies)
     * @param geneName name of the gene whose terms we want
     * @throws SQLException
     */
    @Override
    public OntologyTerm[] getTermsForGene(String sessID, OntologyType ont, String geneName) throws SQLException, SessionExpiredException {
        List<OntologyTerm> result = new ArrayList<OntologyTerm>();
        PooledConnection conn = ConnectionController.connectPooled(sessID);
        try {
            if (ont != null) {
                ResultSet rs = conn.executePreparedQuery("SELECT id,name,def,alt_ids,parents FROM ontology WHERE ontology=? AND INSTR(genes, ?)", ont.toString(), "|" + geneName + "|");
                while (rs.next()) {
                    result.add(new OntologyTerm(ont, rs.getString(1), rs.getString(2), rs.getString(3), StringUtils.split(rs.getString(4), ','), StringUtils.split(rs.getString(5), ',')));
                }
            } else {
                ResultSet rs = conn.executePreparedQuery("SELECT id,ontology.name,def,alt_ids,parents,type FROM ontology RIGHT JOIN ontology_info ON ontology = ontology_info.name WHERE INSTR(genes, ?)", "|" + geneName + "|");
                while (rs.next()) {
                    result.add(new OntologyTerm(OntologyType.valueOf(rs.getString(6)), rs.getString(1), rs.getString(2), rs.getString(3), StringUtils.split(rs.getString(4), ','), StringUtils.split(rs.getString(5), ',')));
                }
            }
        } finally {
            conn.close();
        }
        return result.toArray(new OntologyTerm[0]);
    }

    public OntologyTerm getOntologyTerm(String sessID, OntologyType ont, String ontologyId) throws SQLException, SessionExpiredException{
        PooledConnection conn = null;
        ResultSet rs = null;
        try {
            conn = ConnectionController.connectPooled(sessID);
            rs = conn.executePreparedQuery("SELECT id,name,def,alt_ids,parents FROM ontology WHERE ontology=? AND id=?", ont.toString(), ontologyId);
            if (rs.next()) {
               return new OntologyTerm(ont, rs.getString(1), rs.getString(2), rs.getString(3), StringUtils.split(rs.getString(4), ','), StringUtils.split(rs.getString(5), ','));
            } else {
                return null;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Populate the tables with Gene Ontology data
     *
     * @param oboData an OBO file containing gene ontology terms
     * @param goToGeneData a gzipped GAF file
     * (http://www.geneontology.org/GO.format.gaf-2_0.shtml)
     * @throws IOException
     */
    private void populateGOTables(String sessID, String name, URL oboData, URL goToGeneData) throws IOException, SQLException, SessionExpiredException {
        LOG.info("Parsing OBO " + oboData);
        Map<String, OntologyTerm> terms = new OBOParser(OntologyType.GO).load(oboData);

        connection = ConnectionController.connectPooled(sessID);
        LOG.info("Session " + sessID + " made connection");
        try {
            populateTable(name, terms);

            Map<String, Set<String>> allGenes = new HashMap<String, Set<String>>();
            // Expecting a GZIPped tab-delimited text file in GAF (GO Annotation File) format.
            // We are only interested in columns 2 (gene), 3 (qualifier), and 4 (GO term).

            LOG.info("Reading annotation file " + goToGeneData + " (Cache: " + RemoteFileCache.getCacheFile(goToGeneData) + ")");
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
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE ontology SET genes=? WHERE id=?");
                for (String t : allGenes.keySet()) {
                    Set<String> termGenes = allGenes.get(t);
                    String geneString = StringUtils.join(termGenes, '|');
                    connection.executePreparedUpdate(updateStatement, "|" + geneString + "|", t);
                }
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
            for (String p : term2.getParentIDs()) {
                addGenesToTerm(allTerms, allGenes, gene, p);
            }
        }
    }

    private void populateHPOTables(String sessID, String name, URL oboData, URL hpoToGeneData) throws IOException, SQLException, SessionExpiredException {
        Map<String, OntologyTerm> terms = new OBOParser(OntologyType.HPO).load(oboData);
        connection = ConnectionController.connectPooled(sessID);
        try {
            populateTable(name, terms);

            PreparedStatement updStmt = connection.prepareStatement("UPDATE ontology SET genes=? WHERE id=?");

            // Mapping file from charite.de has HP terms one per line.
            BufferedReader reader = new BufferedReader(new FileReader(RemoteFileCache.getCacheFile(hpoToGeneData)));
            String line;
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
                            String geneString = "|";
                            for (String g : genes) {
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
                        for (String g : hpoGenes) {
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
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void populateTable(String name, Map<String, OntologyTerm> terms) throws SQLException {
        String backupTableName = null;

        // Insert records for all the terms.  Different prepared statement used depending on whether we have parents or not.
        PreparedStatement prep4 = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF).toString());
        PreparedStatement prep5a = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, ALT_IDS).toString());
        PreparedStatement prep5b = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, PARENTS).toString());
        PreparedStatement prep6 = connection.prepareStatement(ontologySchema.preparedInsert(ONTOLOGY, ID, NAME, DEF, ALT_IDS, PARENTS).toString());
        int mostAltIDs = 1;
        for (OntologyTerm t : terms.values()) {
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
        }
    }

    private static OntologyTerm findTermByID(OntologyTerm[] terms, String termID) {
        for (OntologyTerm t : terms) {
            if (t.getID().equals(termID)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Called from <code>createDatabase()</code> to create all the ontology
     * tables on a BLOCKING background thread.
     *
     * @param sessID
     */
    public void populate(final String sessID) {
        try {
            MedSavantServerEngine.submitShortJob(new MedSavantServerJob(SessionManager.getInstance().getUserForSession(sessID), "Ontology Populator", null) {
                @Override
                public boolean run() {
                    try {
                        LOG.info("dbname for connection: " + ConnectionController.getDBName(sessID));
                        LOG.info("Adding GO Ontology");
                        addOntology(sessID, OntologyType.GO.toString(), OntologyType.GO, WebResources.GO_OBO_URL, WebResources.GO_TO_GENES_URL);
                        LOG.info("Adding HPO Ontology");
                        addOntology(sessID, OntologyType.HPO.toString(), OntologyType.HPO, WebResources.HPO_OBO_URL, WebResources.HPO_TO_GENES_URL);
                        LOG.info("Adding OMIM Ontology");
                        addOntology(sessID, OntologyType.OMIM.toString(), OntologyType.OMIM, WebResources.OMIM_OBO_URL, WebResources.OMIM_TO_HPO_URL);
                        SessionManager.getInstance().unregisterSession(sessID);
                        return true;
                    } catch (Exception ex) {
                        LOG.error("Error populating ontology tables.", ex);
                        return false;
                    }
                }
            });
        } catch (Exception ex) {
            LOG.error("Error populating ontology tables.", ex);
        }
    }
}
