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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;

/**
 *
 * @author khushi
 */
public class GeneSetFetcher {

    Map<String, Gene> geneDictionary;
    private static GeneSetFetcher instance;
    private GeneSetFetcher() {
        geneDictionary = new HashMap<String, Gene>();
        try {
            initializeGeneDictionary();
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Unable to initialise gene dictionary: %s", ex);
        }
    }

    public static GeneSetFetcher getInstance(){
        if(instance == null){
            instance = new GeneSetFetcher();
        }
        return instance;
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
                    double normalizedVarFreq1 = getNormalizedVariantCount(gene1);
                    double normalizedVarFreq2 = getNormalizedVariantCount(gene2);
                    if (normalizedVarFreq1 == normalizedVarFreq2) {
                        return 0;
                    } else if (normalizedVarFreq1 < normalizedVarFreq2) {
                        return 1;
                    }
                    return -1;
                } catch (Exception ex) {
                    ClientMiscUtils.reportError("Unable to initialise gene dictionary: %s", ex);
                    return 0;
                }
            }
        });
        return genes;
    }

    public double getNormalizedVariantCount(Gene gene) throws SQLException, RemoteException, InterruptedException{
        double varCount = 0.0;
        try {
            varCount = MedSavantClient.VariantManager.getVariantCountInRange(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID() , FilterController.getInstance().getAllFilterConditions(), gene.getChrom(), gene.getStart(), gene.getEnd());
        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return 0.0;
        }
        double length = gene.getEnd()-gene.getStart();
        return ClientMiscUtils.round((varCount/length)*1000.00, 4);
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
