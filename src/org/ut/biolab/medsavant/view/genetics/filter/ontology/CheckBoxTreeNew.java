/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import com.jidesoft.swing.CheckBoxTree;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Nirvana Nursimulu
 */
public class CheckBoxTreeNew extends CheckBoxTree{
    

    public CheckBoxTreeNew(TreeNode root){

        super(root);
        super.setClickInCheckBoxOnly(false);
        super.setDigIn(true);
        super.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    /**
     * Selects from the root (currently expect to be in dig in mode).
     */
    public void selectAllFromRoot(){
         TreePath rootPath = this.getPathForRow(1).getParentPath();
         ((CheckBoxTree)this).getCheckBoxTreeSelectionModel().addSelectionPath(rootPath);
    }

}
