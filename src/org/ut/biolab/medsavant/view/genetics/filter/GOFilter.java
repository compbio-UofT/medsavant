/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.ut.biolab.medsavant.db.Database;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.view.genetics.filter.geneontology.*;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class GOFilter {
    
    private static SelectQueryGO selectStatementGO;
        
    /**
     * Return the filter view for the gene ontology filter.
     * @return 
     */
    public static FilterView getGOntologyFilterView(){
        
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView gontologyFilterView = new FilterView("Gene Ontology", container);
        // Add button to ask whether the person wants to see the tree.
//        final JButton buttonShowTree = new JButton("Show tree");
//        container.add(buttonShowTree);
//        buttonShowTree.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) { 
//                container.remove(buttonShowTree);
//                // show progress bar while loading data, and then show the tree.
//                loadData(container);
//            }
//        });
        
        // Start loading the information at the beginning.
        loadData(container);

        return gontologyFilterView;
    }
    
    /**
     * Load data once the user has indicated the wish to show the tree (ie, 
     * pressed on the button asking to show the tree).
     * @param container 
     */
    private static void loadData(final JPanel container){
                
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        container.add(progressBar);
        
          class Task extends SwingWorker{
              
              XTree xtree;

                @Override
                protected Object doInBackground() throws Exception {
                    setProgress(0);
                    // Create the mappings file at a certain destination 
                    // then show the tree.
                    try{
                        String destination = CreateMappingsFile.getMappings();
                        xtree = XMLontology.makeTree(destination);
                    }
                    catch(Exception e){
                    }
                    setProgress(100);
                    return xtree;
                }
            }

        try{

            final Task task = new Task();
            
            task.addPropertyChangeListener(new PropertyChangeListener() {

                // To detect progress with the loading of the tree.
                public void propertyChange(PropertyChangeEvent evt) {

                    System.out.println("Event name: " + evt.getPropertyName());
                    System.out.println("Event value: " + evt.getNewValue());

//                    if ("state".equals(evt.getPropertyName()) && 
//                            "DONE".equals(evt.getNewValue() + "")){
                    if (task.getState() == Task.StateValue.DONE){

                        progressBar.setIndeterminate(false);
                        progressBar.setVisible(false);
                        container.remove(progressBar);
                        // When we're done loading the information, show tree.
                        try{
                            showTree(container, task.xtree);
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
            
            // Try to load the information into the tree (i.e., download XML 
            // file etc)
            task.execute();
        }
        // If we could not show the tree (load data), say that we could not.
        catch(Exception e){

            container.remove(progressBar);
            container.add(new JLabel("Could not display the tree"));
            System.out.println("Could not display the tree.");
        }
    }
    
    /**
     * Set up the panel so that the jtree component can be seen, along with the
     * button asking to "apply" a filter.
     * @param container
     * @param xtree 
     */
    private static void showTree(JPanel container, XTree xtree){

        final JButton applyButton = new JButton("Apply");

        // label will show number of locations.
        final JLabel numberSelected = new JLabel();
        // Construct jtree from xtree that has been made.
        // Put tree in scrollpane, and scrollpane in panel.
        final JTree jTree = ConstructJTree.getTree(xtree, true, false);
        jTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        // to keep track of the locations of the places selected.
        final HashSet<String> locations = new HashSet<String>();
        // Add a listener to the tree.  Note: tree can allow non-contiguous
        // multiple selections.
        jTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {

                locations.clear();
                System.out.println("Pressed apply for gene ontology filter");
                TreePath[] paths = jTree.getSelectionPaths();

                                
                if (paths != null){
                    for (TreePath path: paths){
                        if (path.getPathCount() == 1){
                            continue;
                        }
                        applyButton.setEnabled(true);
                        DefaultMutableTreeNode currNode = 
                                (DefaultMutableTreeNode)path.getLastPathComponent();
                        Node currXnode = (Node) currNode.getUserObject();
                        ArrayList<ArrayList<String>> arrayLocs = currXnode.getLocs();

                        for (ArrayList<String> arrayLoc: arrayLocs){
                            // Need to subtract 1 because of BED format.
                            int formattedEnd = 
                                    Integer.parseInt(arrayLoc.get(3).trim()) - 1;

                            String str = arrayLoc.get(1).trim() + "_" + 
                                    arrayLoc.get(2).trim() + "_" + formattedEnd;
                            if (!arrayLoc.get(1).matches(".*_.*")){
                                locations.add(str);
                            }
                        }
                    }
                    if (locations.size() != 1){
                        numberSelected.setText(locations.size() + " gene location ranges selected");
                    }
                    else{
                        numberSelected.setText(locations.size() + " gene location range selected");
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
                System.out.println("All nodes in the tree selected; not yet implemented");
            }
        });
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.add(selectAll);
        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");
        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // deselect any node in the tree.
                jTree.clearSelection();
                FilterController.removeFilter("position gene Ont");
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
                // wilhelm
                // Select query statement for GO.
                selectStatementGO = new SelectQueryGO();

                System.out.println("Pressed apply for gene ontology filter");
                TreePath[] paths = jTree.getSelectionPaths();
                
                for (String location: locations){

                    String[] split = location.split("_");
                    Double start = Integer.parseInt(split[1]) + 0.0;
                    Double end = Integer.parseInt(split[2]) + 0.0;
                    selectStatementGO.addCondition(split[0], start, end);
                } 

                // If there are no conditions at all, do not display
                // anything (most intuitive). So, create bogus condition that 
                // will never be satisfied.
                if (selectStatementGO.getConditions().isEmpty() && paths != null){
                    selectStatementGO.addCondition("chr1", 23, 22); 
                }
                final HashMap<String, List<Range>> map = selectStatementGO.getConditions();
                
                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        
                        Condition[] conds = new Condition[map.keySet().size()];
                        int i = 0;
                       
                        for (String key: map.keySet()){
                            
                            List<ComboCondition> listInnerCond = 
                                    new ArrayList<ComboCondition>();
                            List<Range> ranges = map.get(key);
                            for (Range range: ranges){

                                BinaryCondition innerCond1 = BinaryCondition.greaterThan
                                        (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.POSITION_COL), range.getMin(), true);
                                BinaryCondition innerCond2 = BinaryCondition.lessThan
                                        (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.POSITION_COL), range.getMax(), true);
                                BinaryCondition[] condTogether = {innerCond1, innerCond2};
                                listInnerCond.add(ComboCondition.and(condTogether));
                            } // for each range for the chromosome of interest.
                            BinaryCondition chrCond = BinaryCondition.equalTo
                                    (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.CHROM_COL), key);
                            conds[i++] = ComboCondition.and(chrCond, ComboCondition.or(listInnerCond.toArray()));
                        } // for each chromosome.
                        return conds;
                    }

                    @Override
                    public String getName() {
                        return " position gene Ont";
                    }
                };
                System.out.println("Adding Filter" + f.getName());
                FilterController.addFilter(f);
            }
        });        
    }
    
}
