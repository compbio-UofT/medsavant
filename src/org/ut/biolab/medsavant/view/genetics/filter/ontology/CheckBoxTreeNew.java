/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import com.jidesoft.swing.CheckBoxTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JTree;
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
    
    private HashSet<TreePath> selectedPaths;

    public CheckBoxTreeNew(TreeNode root){
        super(root);
        this.selectedPaths = new HashSet<TreePath>();
        super.setClickInCheckBoxOnly(false);
        super.setDigIn(false);
        super.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.initListeners();
    }
    
    public CheckBoxTreeNew(TreeModel model){
        super(model);
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
    
    /**
     * Initialise listeners for special dig-in appearance.
     */
    private void initListeners(){
        
        final JTree tree = this;
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
                
                CheckBoxTreeNew.changeSelections(true, event.getPath(), (CheckBoxTreeNew)tree);                
            }

            // So far, nothing will be done here.
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        // Add an expansion listener to this tree.
        this.getCheckBoxTreeSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                
                boolean pathWasAdded = e.isAddedPath();
                CheckBoxTreeNew.changeSelections(pathWasAdded, e.getPath(), (CheckBoxTreeNew)tree);
            }
        });
    }
    
    /**
     * Change selections in the tree.
     * @param addPaths if paths have been added.
     * @param sourcePath the source from which selection are to be changed
     */
    private static void changeSelections(boolean pathWasAdded, TreePath sourcePath, CheckBoxTreeNew tree){
        
        if (pathWasAdded){
            List<TreePath> listPaths = new ArrayList<TreePath>();
            TreeUtils.getPaths(tree, sourcePath, true, listPaths);
            for (TreePath path: listPaths){
                tree.selectedPaths.add(path);                
//                tree.getCheckBoxTreeSelectionModel().addSelectionPath(path);
            }
            tree.getCheckBoxTreeSelectionModel().addSelectionPaths(listPaths.toArray(new TreePath[0]));
        }
        else{
            HashSet<TreePath> removedPaths = new HashSet<TreePath>();
            for (TreePath selectedPath: tree.selectedPaths){
                if (sourcePath.isDescendant(selectedPath)){
                    removedPaths.add(selectedPath);
                }
            }
            for (TreePath removedPath: removedPaths){
                tree.selectedPaths.remove(removedPath);
            }
            
            tree.getCheckBoxTreeSelectionModel().removeSelectionPaths(removedPaths.toArray(new TreePath[0]));
        }
    }
}