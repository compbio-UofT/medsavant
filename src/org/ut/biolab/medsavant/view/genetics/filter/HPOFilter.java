/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.jidesoft.swing.CheckBoxTree;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOTreeReadyController;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPOTreeReadyListener;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.CheckBoxTreeNew;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ClassifiedPositionInfo;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Node;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOFilter implements HPOTreeReadyListener{
    
    public static final String NAME_FILTER = "Human Phenotype";
    
    public static String NAME_TREE = "HPO TREE";
    
    private ClassifiedPositionInfo classifiedPos;
    
    private JPanel container;
    
    private boolean pathHasBeenRemoved = false;
    
    private static HPOFilter instance;
    
    private HPOFilter(){
        HPOTreeReadyController.addHPOTreeReadyListener(this);
    }
    
    public static HPOFilter getInstance(){
        if (instance == null){
            instance = new HPOFilter();
        }
        return instance;
    }
            
    /**
     * Returns the filterview object associated with the human phenotype 
     * ontology.
     * @return 
     */
    public FilterView getHPOntologyFilterView(){
        
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView hpontologyView = new FilterView(NAME_FILTER, container);
        showProgressBar();
        return hpontologyView;
    }
    
    private void showProgressBar(){
        Tree hpTree;
        if ((hpTree = HPOTreeReadyController.getHPOTree()) != null){
            showTree(hpTree);
            return;
        }
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        container.add(progressBar);
    }
    
    
    /**
     * Display the jTree in the container, once it has been loaded.
     * @param container JPanel object
     * @param tree the tree containing the ontology information.
     */
    private void showTree(final Tree tree){
        
        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        
        final JLabel numberSelected = new JLabel();
        
        // To keep track of the locations in question.
        final HashSet<String> locations = new HashSet<String>();
        
        // Now that we have the tree, construct jTree, and display it.
        // Enable multiple discontinuous selection.
        final JTree jTree = ConstructJTree.getTree(tree, false, true, true);   
                
        jTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                
                // If elements were added programmatically, do NOT do anything AND paths have been added...
                if (((CheckBoxTreeNew)jTree).undergoingProgrammaticChange() && e.isAddedPath() || HPOFilter.instance.pathHasBeenRemoved){
                    HPOFilter.instance.pathHasBeenRemoved = false;
                    return;
                }
                
                HPOFilter.instance.pathHasBeenRemoved = true;
                locations.clear();
                System.out.println("Selected elements from the Human Phenotype Filter.");
                TreePath[] paths = 
                        ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().
                        getSelectionPaths();
                
                if (paths != null){
                    for (TreePath path: paths){
//                        System.out.println(path);
                        DefaultMutableTreeNode currVisualNode = 
                                (DefaultMutableTreeNode)path.getLastPathComponent();
                        Node currNode = (Node)currVisualNode.getUserObject();
                        
                        HashSet<String> arrayLocs = currNode.getLocs();
//                        System.out.println("For this path\n" + arrayLocs);
                        for (String arrayLoc: arrayLocs){
                            
                            String[] split = arrayLoc.split("\t");
                            // Note: BED format fixed already so get locations as they are.
                            String str = split[0] + "\t" + split[1] + "\t" + split[2];
                            locations.add(str);
                        }
                    }
                    
                    if (locations.size() != 1){
                        numberSelected.setText(ViewUtil.numToString(locations.size()) + 
                                " gene location ranges selected");
                    }
                    else{
                        numberSelected.setText(ViewUtil.numToString(locations.size()) + 
                                " gene location range selected");                        
                    }
                }
                else{
                    numberSelected.setText("");
                }
                
                applyButton.setEnabled(true); 
//                System.out.println("Locations\n" + locations);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(jTree);
        scrollPane.setAlignmentX(0F);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(Box.createVerticalBox());
        container.add(scrollPane);
        
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
//                FilterController.removeFilter("position human phenotype ont");
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
                // Select query statement for GO.
                classifiedPos = new ClassifiedPositionInfo();

                System.out.println("Pressed apply for gene ontology filter");
                TreePath[] paths = ((CheckBoxTree)jTree).getCheckBoxTreeSelectionModel().getSelectionPaths();
                
                for (String location: locations){

                    String[] split = location.split("\t");
                    Double start = Integer.parseInt(split[1]) + 0.0;
                    Double end = Integer.parseInt(split[2]) + 0.0;
                    classifiedPos.addCondition(split[0], start, end);
                } 

                // If there are no conditions at all, do not display
                // anything (most intuitive). So, create bogus condition that 
                // will never be satisfied.
                if (classifiedPos.getConditions().isEmpty() && paths != null){
                    classifiedPos.addCondition("chr1", 23, 22); 
                }
                final HashMap<String, List<Range>> map = classifiedPos.getConditions();
                
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
                                        (MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(ClassifiedPositionInfo.POSITION_COL), range.getMin(), true);
                                BinaryCondition innerCond2 = BinaryCondition.lessThan
                                        (MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(ClassifiedPositionInfo.POSITION_COL), range.getMax(), true);
                                BinaryCondition[] condTogether = {innerCond1, innerCond2};
                                listInnerCond.add(ComboCondition.and(condTogether));
                            } // for each range for the chromosome of interest.
                            BinaryCondition chrCond = BinaryCondition.equalTo
                                    (MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(ClassifiedPositionInfo.CHROM_COL), key);
                            conds[i++] = ComboCondition.and(chrCond, ComboCondition.or(listInnerCond.toArray()));
                        } // for each chromosome.
                        return conds;
                    }

                    @Override
                    public String getName() {
                        return NAME_FILTER;
                    }
                };
                System.out.println("Adding Filter " + f.getName());
                FilterController.addFilter(f);
            }
        });
    }

    public void hpoTreeReady() {
        container.removeAll();
        showTree(HPOTreeReadyController.getHPOTree());
    }
    
}
