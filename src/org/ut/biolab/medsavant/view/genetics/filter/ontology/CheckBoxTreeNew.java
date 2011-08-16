/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import com.jidesoft.swing.CheckBoxTree;
import com.jidesoft.swing.CheckBoxTreeSelectionModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Nirvana Nursimulu
 */
public class CheckBoxTreeNew extends CheckBoxTree{
    
    /**
     * The set of all currently selected paths.
     */
    private HashSet<TreePath> selectedPaths;
    
    /**
     * True iff the change in selections is programmatic.
     */
    private boolean undergoingProgrammaticChange;

    public CheckBoxTreeNew(TreeNode root){
        super(root);
        this.undergoingProgrammaticChange = false;
        this.selectedPaths = new HashSet<TreePath>();
        super.setClickInCheckBoxOnly(false);
        super.setDigIn(false);
        super.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.initListeners();
    }
    
    public CheckBoxTreeNew(TreeModel model){
        super(model);
        this.undergoingProgrammaticChange = false;
        this.selectedPaths = new HashSet<TreePath>();
        super.setClickInCheckBoxOnly(false);
        super.setDigIn(false);
        super.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.initListeners();
    }

    /**
     * Selects from the root (currently expect to be in dig in mode).
     */
    public void selectAllFromRoot(){
         TreePath rootPath = this.getPathForRow(1).getParentPath();
         ((CheckBoxTree)this).getCheckBoxTreeSelectionModel().addSelectionPath(rootPath);
    }
    
    public boolean undergoingProgrammaticChange(){
        return undergoingProgrammaticChange;
    }
    
    /**
     * Initialize listeners for special dig-in appearance.
     */
    private void initListeners(){
        
        final CheckBoxTreeNew tree = this;
        // Add a selection listener to this tree.
        this.addTreeExpansionListener(new TreeExpansionListener() {

            // Upon expansion, look at nodes which have been selected.
            public void treeExpanded(TreeExpansionEvent event) {                
                
                // Look at the path which has been expanded, and see if the
                // last node in that path has been selected.
                // If so, select all its visible children nodes.                
                if (!selectedPaths.contains(event.getPath())){
                    return;
                }                
                CheckBoxTreeNew.changeSelections(true, event.getPath(), tree);    
            }

            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        // Add an expansion listener to this tree.
        this.getCheckBoxTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                
                boolean pathWasAdded = e.isAddedPath();
                // If the change is not programmatic, then go ahead.
                if (!tree.undergoingProgrammaticChange){
                    CheckBoxTreeNew.changeSelections(pathWasAdded, e.getPath(), tree);
                }
                else{
                    if (pathWasAdded){
                        tree.selectedPaths.add(e.getPath());
                    }
                    else{                        
                        HashSet<TreePath> removedPaths = new HashSet<TreePath>();
                        TreePath selectedPath = e.getPath();
                        for (TreePath path: tree.selectedPaths){
                            if (selectedPath.isDescendant(path)){
                                removedPaths.add(path);
                            }
                        }
                        for (TreePath path: removedPaths){
                            tree.selectedPaths.remove(path);
                        }
                        tree.selectedPaths.remove(e.getPath());
                    }
                }
            }
        });
    }
    
    /**
     * Change selections in the tree.
     * @param addPaths if paths have been added.
     * @param sourcePath the source from which selection are to be changed
     */
    private static void changeSelections(boolean pathWasAdded, TreePath sourcePath, CheckBoxTreeNew tree){
        
        tree.undergoingProgrammaticChange = true;
        
        if (pathWasAdded){
            List<TreePath> listPaths = new ArrayList<TreePath>();       
            TreeUtils.getPaths(tree, sourcePath, true, listPaths);            
                     
            tree.addPaths(listPaths.toArray(new TreePath[0]));
        }
        else{
            // Unselect any ancestors of the node which has just been unselected.
            HashSet<TreePath> removedPaths = new HashSet<TreePath>();
            for (TreePath path: tree.selectedPaths){
                if (path.isDescendant(sourcePath)){
                    removedPaths.add(path);
                }
            }

            for (TreePath selectedPath: tree.selectedPaths){
                if (sourcePath.isDescendant(selectedPath)){
                    removedPaths.add(selectedPath);
                }
            }
            removedPaths.add(sourcePath);
            tree.removePaths(removedPaths.toArray(new TreePath[0]));
        }
        tree.undergoingProgrammaticChange = false;
    }
    
    private void addPaths(TreePath[] pathsToAdd){
        for (TreePath path: pathsToAdd){
            this.selectedPaths.add(path);
        }        
        CheckBoxTreeSelectionModel model = this.getCheckBoxTreeSelectionModel();
        model.addSelectionPaths(pathsToAdd);
    }
    
    private void removePaths(TreePath[] pathsToRemove){
        for (TreePath path: pathsToRemove){
            this.selectedPaths.remove(path);
        }
        CheckBoxTreeSelectionModel model = this.getCheckBoxTreeSelectionModel();
        model.removeSelectionPaths(pathsToRemove);
    }
}