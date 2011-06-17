/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.view.filter.hpontology.HPOParser;
import org.ut.biolab.medsavant.view.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOFilter {
    
    /**
     * Returns the filterview object associated with the human phenotype 
     * ontology.
     * @return 
     */
    public static FilterView getHPOntologyFilterView(){
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView hpontologyView = new FilterView("Human Phenotype", container);
        loadData(container);
        return hpontologyView;
    }
    
    /**
     * Load the tree to the JPanel container.
     * @param container the JPanel cto contain the tree.
     */
    private static void loadData(final JPanel container){
        
        // Add progress bar.
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        container.add(progressBar);
        
        // the task of loading info.
            class Task extends SwingWorker{
                
                Tree tree;

                @Override
                protected Object doInBackground() throws Exception {
                    setProgress(0);
                    // get the tree here.
                    try{
                        tree = HPOParser.getHPOTree();
                    }
                    catch(Exception e){
                    }
//                    Thread.sleep(10000);
                    setProgress(100);
                    return null;
                }  
            } // end of Task class.

        try{    
            final Task task = new Task();
            task.execute();

            task.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {

                    if (task.getState() == Task.StateValue.DONE){

                        progressBar.setIndeterminate(false);
                        progressBar.setVisible(false);
                        container.remove(progressBar);
                        // When we're done loading the information, show tree.
                        try{
                            showTree(container, task.tree);
                        }
                        catch(Exception e){
                            container.remove(progressBar);
                            container.add(new JLabel("Could not display the tree"));
                            System.out.println("Could not display the tree.");                            
                        }
                        task.removePropertyChangeListener(this);
                    }   
                    else {

                        progressBar.setIndeterminate(true);
                    }                
                }
            });
        }
        catch (Exception e){
            container.remove(progressBar);
            container.add(new JLabel("Could not display the tree"));
            System.out.println("Could not display the tree.");            
        }
    }
    
    /**
     * Display the jtree in the container, once it has been loaded.
     * @param container JPanel object
     * @param tree the tree containing the ontology information.
     */
    private static void showTree(JPanel container, Tree tree){
        
        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        
        // Now that we have the tree, construct jTree, and display it.
        // Enable multiple discontinuous selection.
        final JTree jtree = ConstructJTree.getTree(tree, false);
        jtree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        jtree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                applyButton.setEnabled(true); 
                
                // do something here.

            }
        });
        
        JScrollPane scrollPane = new JScrollPane(jtree);
        scrollPane.setAlignmentX(0F);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(Box.createVerticalBox());
        container.add(scrollPane);
        
        JPanel bottomContainer = new JPanel();
        
        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // select all the nodes in the tree.
                System.out.println("All nodes in the tree selected; not yet implemented");
            }
        });
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.add(selectAll);
        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");
        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // deselect any node in the tree.
                jtree.clearSelection();
                FilterController.removeFilter("position gene Ont");
            }
        });
        bottomContainer.add(selectNone);
        
        bottomContainer.add(Box.createGlue());
        bottomContainer.add(applyButton);
        bottomContainer.setAlignmentX(0F);
        container.add(bottomContainer);    
        
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(false);
                
                // And do something here...
            }
        });
    }
    
}
