/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.jidesoft.swing.CheckBoxTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RangeFilter;
import org.ut.biolab.medsavant.model.RangeSet;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.*;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.CheckBoxTreeNew;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class GOFilter implements GOTreeReadyListener{
    
    public static final String NAME_FILTER = "Gene Ontology";
    
    public static String NAME_TREE = "GO TREE";
    
    private JPanel container;
    
    private static GOFilter instance;
    
    private GOFilter(){
        GOTreeReadyController.addGOTreeReadyListener(this);
    }
    
    public static GOFilter getInstance(){
        if (instance == null){
            instance = new GOFilter();
        }
        return instance;
    }
        
    /**
     * Return the filter view for the gene ontology filter.
     * @return 
     */
    public FilterView getGOntologyFilterView(){
        
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView gontologyFilterView = new FilterView(NAME_FILTER, container);
        
        // Start loading the information at the beginning.
        showProgressBar();

        return gontologyFilterView;
    }
    
    private void showProgressBar(){
        
        GOTree goTree;
        if ((goTree = GOTreeReadyController.getGOTree()) != null){
            showTree(goTree);
            return;
        }
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        container.add(progressBar);
    }
    
    /**
     * Set up the panel so that the jtree component can be seen, along with the
     * button asking to "apply" a filter.
     * @param container
     * @param xtree 
     */
    private void showTree(final GOTree xtree){
        
        final JButton applyButton = new JButton("Apply");

        // label will show number of locations.
        final JLabel numberSelected = new JLabel();
        // Construct jtree from xtree that has been made.
        // Put tree in scrollpane, and scrollpane in panel.
        // For the tree, we want to use a checkboxtree, a dummy root, and to add 
        // a package we will be calling "genes" to every single node.
        
        final JTree jTree = ConstructJTree.getTree(xtree, true, true, true);
        
        // to keep track of the locations of the places selected.
        final HashSet<String> locations = new HashSet<String>();

        // Add a listener to the tree.  Note: tree can allow non-contiguous
        // multiple selections.
        ((CheckBoxTreeNew)jTree).getCheckBoxTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            
            public void valueChanged(TreeSelectionEvent e) {
                
                // If values were added programmatically, do NOT do anything AND paths have been added.
                if (((CheckBoxTreeNew)jTree).undergoingProgrammaticChange() && e.isAddedPath()){
                    return;
                }

                locations.clear();
                System.out.println("Selected elements for gene ontology filter.");
//                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
               
                
                if (paths != null){
 
                    for (TreePath path: paths){
                   
                        DefaultMutableTreeNode currNode = 
                                (DefaultMutableTreeNode)path.getLastPathComponent();
                        Node currXnode = (Node) currNode.getUserObject();
                        
                        HashSet<String> arrayLocs = currXnode.getLocs();

                        for (String arrayLoc: arrayLocs){
                            
                            String[] split = arrayLoc.split("\t"); 
                            // Need to subtract 1 because of BED format.
                            int formattedEnd = 
                                    Integer.parseInt(split[3].trim());

                            String str = split[1].trim() + "_" + 
                                    split[2].trim() + "_" + formattedEnd;
                            if (!split[1].matches(".*_.*")){
                                locations.add(str);
                            }
                        }
                    }
                    if (locations.size() != 1){
                        numberSelected.setText(ViewUtil.numToString(locations.size()) + 
                                " transcript location ranges selected");
                    }
                    else{
                        numberSelected.setText(ViewUtil.numToString(locations.size()) + 
                                " transcript location range selected");
                    }
                }
                else{
                    numberSelected.setText("");
                }
                applyButton.setEnabled(true);                
            }
        }); 

        JScrollPane scrollpane = new JScrollPane(jTree);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(Box.createVerticalBox());
        container.add(scrollpane);
        scrollpane.setAlignmentX(0F);
        
        container.add(numberSelected);

        JPanel bottomContainer = new JPanel();
        
        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // select all the nodes in the tree.
                System.out.println("All nodes in the tree selected");
                ((CheckBoxTreeNew)jTree).selectAllFromRoot();
            }
        });
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.add(selectAll);
        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");
        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // deselect any node in the tree.
                ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().clearSelection();
//                FilterController.removeFilter("position gene Ont");
            }
        });
        bottomContainer.add(selectNone);
        
        bottomContainer.add(Box.createGlue());
        bottomContainer.add(applyButton);
        bottomContainer.setAlignmentX(0F);

        container.add(bottomContainer);
        
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            // Note that an action could have been performed only if some tree
            // element has been selected.
            public void actionPerformed(ActionEvent e) {
            
                applyButton.setEnabled(false); 

                System.out.println("Pressed apply for gene ontology filter");
                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
//                System.out.println("N: " + paths);
                
                Filter f = new RangeFilter() {

                    @Override
                    public String getName() {
                        return NAME_FILTER;
                    }
                };
                
                RangeSet rangeSet = ((RangeFilter)f).getRangeSet();
                
                for (String location: locations){

                    String[] split = location.split("_");
                    String chrom = split[0];
                    Double start = Integer.parseInt(split[1]) + 0.0;
                    Double end = Integer.parseInt(split[2]) + 0.0;
                    Range range = new Range(start, end);
                    rangeSet.addRange(chrom, range);
                } 

                // If there are no conditions at all, do not display
                // anything (most intuitive). So, create bogus condition that 
                // will never be satisfied.
                if (rangeSet.isEmpty() && paths != null){
                    Range range = new Range(23.0, 22.0);
                    rangeSet.addRange("chr1", range);
                }
                
                if (rangeSet.isEmpty() && paths == null){
                    FilterController.removeFilter(NAME_FILTER);
                }
                else{
                    FilterController.addFilter(f);
                    System.out.println("Adding Filter " + f.getName());
                }
            }
        });        
    }

    public void goTreeReady() {
        container.removeAll();
        showTree(GOTreeReadyController.getGOTree());
    }
    
}
