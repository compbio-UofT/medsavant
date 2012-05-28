/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.*;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;

/**
 *
 * @author khushi
 */
public class GeneSetFetcher {
    Map<String, Gene> geneDictionary;
    
    public GeneSetFetcher(){
        geneDictionary= new HashMap<String, Gene>();
        try {
            initializeGeneDictionary();
        } catch (SQLException ex) {
            Logger.getLogger(GeneSetFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(GeneSetFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Map<String, Gene> getGeneDictionary(){
        return geneDictionary;
    }
    
    private void initializeGeneDictionary() throws SQLException, RemoteException{
        Iterator<Gene> geneIterator = GeneSetController.getInstance().getCurrentGeneSet().iterator();
        Gene currentGene;
        while (geneIterator.hasNext()){
            currentGene = geneIterator.next();
            geneDictionary.put(currentGene.getName(), currentGene);
        }
    }
    
    public List<Gene> getGenesByNumVariants(List<String> relatedGenes){
        List<Gene> genes = getGenes(relatedGenes);
        Collections.sort(genes, new Comparator<Gene>(){
            public int compare (Gene gene1, Gene gene2){
                try {
                    int numVariantsInGene1 = MedSavantClient.VariantManager.getNumVariantsInRange(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID() , FilterController.getQueryFilterConditions(), gene1.getChrom(), gene1.getStart(), gene1.getEnd());
                    int numVariantsInGene2 = MedSavantClient.VariantManager.getNumVariantsInRange(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID() , FilterController.getQueryFilterConditions(), gene2.getChrom(), gene2.getStart(), gene2.getEnd());
                    if(numVariantsInGene1 == numVariantsInGene2)
                        return 0;
                    else if (numVariantsInGene1 <numVariantsInGene2)
                        return -1;
                    return 1;
                } catch (SQLException ex) {
                    Logger.getLogger(GeneSetFetcher.class.getName()).log(Level.SEVERE, null, ex);
                    return 0;
                } catch (RemoteException ex) {
                    Logger.getLogger(GeneSetFetcher.class.getName()).log(Level.SEVERE, null, ex);
                    return 0;
                }
            }
        });
        return genes;
    }
    
    public List<Gene> getGenes(List<String> geneNames){
        List<Gene> genes = new ArrayList<Gene>();
        Iterator<String> itr = geneNames.iterator();
        Gene currGene;
        itr.next();//skip the first one which is the queried gene itself
        while (itr.hasNext()){
            String name = itr.next();
            currGene = geneDictionary.get(name);
            if (currGene!= null)
                genes.add(currGene);
        }
        return genes;
    }
}
