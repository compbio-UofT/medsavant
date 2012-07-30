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

package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.*;
import org.ut.biolab.medsavant.controller.GeneSetController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;

/**
 *
 * @author khushi
 */
public class GeneSetFetcher {

    Map<String, Gene> geneDictionary;

    public GeneSetFetcher() {
        geneDictionary = new HashMap<String, Gene>();
        try {
            initializeGeneDictionary();
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to initialise gene dictionary: %s", ex);
        }
    }

    public Map<String, Gene> getGeneDictionary() {
        return geneDictionary;
    }

    private void initializeGeneDictionary() throws SQLException, RemoteException {
        for (Gene currentGene: GeneSetController.getInstance().getCurrentGenes()) {
            geneDictionary.put(currentGene.getName(), currentGene);
        }
    }

    public List<Gene> getGenesByNumVariants(List<String> relatedGenes) {
        List<Gene> genes = getGenes(relatedGenes);
        Collections.sort(genes, new Comparator<Gene>() {
            @Override
            public int compare (Gene gene1, Gene gene2) {
                try {
                    int numVariantsInGene1 = MedSavantClient.VariantManager.getVariantCountInRange(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID() , FilterController.getInstance().getQueryFilterConditions(), gene1.getChrom(), gene1.getStart(), gene1.getEnd());
                    int numVariantsInGene2 = MedSavantClient.VariantManager.getVariantCountInRange(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID() , FilterController.getInstance().getQueryFilterConditions(), gene2.getChrom(), gene2.getStart(), gene2.getEnd());
                    if (numVariantsInGene1 == numVariantsInGene2) {
                        return 0;
                    } else if (numVariantsInGene1 < numVariantsInGene2) {
                        return -1;
                    }
                    return 1;
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to initialise gene dictionary: %s", ex);
                    return 0;
                }
            }
        });
        return genes;
    }

    public List<Gene> getGenes(List<String> geneNames) {
        List<Gene> genes = new ArrayList<Gene>();
        Iterator<String> itr = geneNames.iterator();
        Gene currGene;
        itr.next();//skip the first one which is the queried gene itself
        while (itr.hasNext()) {
            String name = itr.next();
            currGene = getGene(name);
            if (currGene!= null)
                genes.add(currGene);
        }
        return genes;
    }

    public Gene getGene(String geneName) {
        return geneDictionary.get(geneName);
    }
}
