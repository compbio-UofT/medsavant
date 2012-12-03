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

package org.ut.biolab.medsavant.client.ontology;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.region.RegionSetFilter;


/**
 *
 * @author tarkvara
 */
public class OntologyFilter extends RegionSetFilter {

    private static final Log LOG = LogFactory.getLog(OntologyFilter.class);

    private final List<OntologyTerm> appliedTerms;
    private final OntologyType ontology;

    public OntologyFilter(List<OntologyTerm> applied, OntologyType ont) {
        appliedTerms = applied;
        ontology = ont;
    }

    @Override
    public Condition[] getConditions() throws InterruptedException, SQLException, RemoteException {
        Set<Gene> genes = new HashSet<Gene>();
        Map<OntologyTerm, String[]> allTermsGenes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.sessionId, appliedTerms.toArray(new OntologyTerm[0]), ReferenceController.getInstance().getCurrentReferenceName());
        for (String[] termGenes: allTermsGenes.values()) {
            for (String geneName: termGenes) {
                Gene g = GeneSetController.getInstance().getGene(geneName);
                if (g != null) {
                    genes.add(g);
                } else {
                    LOG.info("Non-existent gene " + geneName + " referenced by " + ontology);
                }
            }
        }
        List<GenomicRegion> regions = new ArrayList<GenomicRegion>(genes.size());
        int i = 0;
        for (Gene g: genes) {
            regions.add(new GenomicRegion(g.getName(), g.getChrom(), g.getStart(), g.getEnd()));
        }
        return getConditions(regions);
    }

    @Override
    public String getID() {
        return ontologyToFilterID(ontology);
    }

    @Override
    public String getName() {
        return ontologyToTitle(ontology);
    }

    public static String ontologyToTitle(OntologyType ontology) {
        switch (ontology) {
            case GO:
                return "GO – Gene Ontology";
            case HPO:
                return "HPO – Human Phenotype Ontology";
            case OMIM:
// The full title is too long, and screws up the layout of the filter view.
//              return "OMIM – Online Mendelian Inheritance in Man";
                return "OMIM";
            default:
                return null;
        }
    }

    public static String ontologyToFilterID(OntologyType ontology) {
        return ontology.toString();
    }

    public static OntologyType filterIDToOntology(String filterID) {
        return Enum.valueOf(OntologyType.class, filterID);
    }
}
