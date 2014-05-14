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
package org.ut.biolab.medsavant.client.ontology;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import com.healthmarketscience.sqlbuilder.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.region.RegionSetFilter;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;


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
        Map<OntologyTerm, String[]> allTermsGenes;
        try {
            allTermsGenes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.getSessionID(), appliedTerms.toArray(new OntologyTerm[0]), ReferenceController.getInstance().getCurrentReferenceName());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;
        }
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
