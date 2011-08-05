/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.view.annotations.AnnotationsSection;
import org.ut.biolab.medsavant.view.genetics.GeneticsSection;
import org.ut.biolab.medsavant.view.genetics.filter.GOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.HPOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.CreateMappingsFile;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.GOTree;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.GOTreeReadyController;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.XMLontology;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOParser;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOTreeReadyController;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.patients.PatientsSection;
import org.ut.biolab.medsavant.view.subview.SectionView;


/**
 *
 * @author mfiume
 */
public class LoggedInView extends JPanel {
    
    private ViewController viewController;
    private static boolean initiated = false;

    public LoggedInView() {
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());
        initViewContainer();
        initTabs();
        // added by nnursimulu
        initGO();
        initHPO();
    }
    
    /**
     * @author nnursimulu
     */
    private void initGO(){
        
        class GOTask extends SwingWorker{
            
            private boolean doneGettingTree;

            @Override
            protected Object doInBackground() throws Exception {
                doneGettingTree = false;
                while (!doneGettingTree){
                    try{
                        String destination = CreateMappingsFile.getMappings();
                        GOTree goTree = XMLontology.makeTree(destination);
                        doneGettingTree = true;
                        GOTreeReadyController.addGOTree(goTree);
                    }  catch(Exception e){
                        System.out.println("Problem with constructing GO tree:\n" + e);
                        try{
                            // Let's just wait for a while.
                            Thread.sleep(3000);
                        } catch(InterruptedException ex){
                        }
                    }
                }
                return null;
            }            
        }
        
        GOTask task = new GOTask();
        task.execute();
    }
    
    /**
     * @author nnursimulu
     */
    private void initHPO(){
        
        class HPOTask extends SwingWorker{
            
            private boolean doneGettingTree = false;

            @Override
            protected Object doInBackground() throws Exception {
                doneGettingTree = false;
                while (!doneGettingTree){
                    try{
                        Tree tree = HPOParser.getHPOTree();
                        doneGettingTree = true;
                        HPOTreeReadyController.addHPOTree(tree);
                    } catch(Exception e){
                        System.out.println("Problem with constructing HPO tree:\n" + e);
                        try{
                            // Let's just wait for a while.
                            Thread.sleep(3000);
                        } catch(InterruptedException ex){                        
                        }
                    }
                }                
                return null;
            }            
        }
        
        HPOTask task = new HPOTask();
        task.execute();
    }

    private void initViewContainer() {
        viewController = ViewController.getInstance();
        this.add(viewController,BorderLayout.CENTER);
    }
    
    private void addSection(SectionView view) {
        viewController.addSection(view);
    }

    private void initTabs() {
        if (!initiated) {
            addSection(new PatientsSection());
            addSection(new AnnotationsSection());
            addSection(new GeneticsSection());
        }
        initiated = true;
    }
    
}
