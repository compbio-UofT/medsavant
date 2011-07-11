/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import com.jidesoft.swing.CheckBoxTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Nirvana Nursimulu
 */
public class ConstructJTree {
    
    /**
     * Obtain the jTree object to be made from this ontology.
     * @param tree the Tree object containing ontology information.
     * @param isForest true iff there is more than one root, in which case, a
     * default root is created. Note that there is a packageToAdd, and isForest
     * is true, the root will not have a child with the package to be added.
     * @param isCheckBoxTree true iff the tree is to be a check box tree.
     * @param useToString true if you want the toString part of the node to be
     * returned, false if you want the toValue part.
     * @return the jTree object made.
     */
    public static JTree getTree(Tree tree, boolean isForest, boolean isCheckBoxTree, boolean useToString){

        if (tree == null){
            return null;
        }
        MutableTreeNode actualRoot;
        if (isForest){
            // "dummy" root of the tree.  No special node here...
//            Node root = new Node("...", null);
//            root.setDescription("...");
            Node root = tree.fakeRoot;
            actualRoot = new MutableTreeNode(root);
        }
        else{
            actualRoot = 
                    new MutableTreeNode(tree.getRootNodes().toArray(new Node[1])[0]);
        }
        actualRoot.setToString(useToString);
        
        
        // Add the nodes beneath the root node to this tree.
        addNodes(actualRoot, tree, isForest, useToString);
        JTree displayedTree = null;
        
        if (!isCheckBoxTree){
            displayedTree = new JTree(actualRoot);
        }
        else{
            displayedTree = new CheckBoxTreeNew(actualRoot);
        }
        
        return displayedTree;
    }
    
    
    /**
     * Add the nodes to form part of the jTree.
     * @param actualRoot the actual root of the tree.
     * @param tree the tree containing all ontology information.
     * @param isForest true iff the tree has multiple roots.
     */
    private static void addNodes
            (MutableTreeNode actualRoot, Tree tree, boolean isForest, boolean useToString){
        
        Set<Node> roots;
        // To contain the roots of the tree.
        if (isForest){
            roots = tree.getRootNodes();
        }
        else{
            roots = ((Node)actualRoot.getUserObject()).getChildren();
        }

        // Get the name of the children while going down the tree.
        TreeSet<Node> children;
        
        // The child in consideration in context.
        MutableTreeNode child;
        
        // To contain the parent nodes (to be used when displaying) in question.
        List<MutableTreeNode> parentNodes = 
                new ArrayList<MutableTreeNode>();
        
        
        // To contain the children nodes in question.
        List<MutableTreeNode> childrenNodes = 
                new ArrayList<MutableTreeNode>();
        
        
        // Add all roots to the tree.
        for (Node root: roots){
        
            // Connect the root to its children.
            child = new MutableTreeNode(root); 
            child.setToString(useToString);
            actualRoot.add(child);
            // The future parents to be considered.
            parentNodes.add(child);
        }

        // While we still have children nodes...
        while(!parentNodes.isEmpty()){
            
            // Go through the tree in a breadth-first manner.
            for (MutableTreeNode parent: parentNodes){

                // Get the set of children, and have the parents accept their
                // children.
                children = tree.getChildrenNodes
                        (((Node)parent.getUserObject()).getIdentifier());

                for (Node child2: children){

                    child = new MutableTreeNode(child2); 
                    child.setToString(useToString);
                    childrenNodes.add(child);
                    parent.add(child);
                }
            }

            // Now have the children become parents.
            parentNodes.clear();
            parentNodes.addAll(childrenNodes);
            childrenNodes.clear();   
        }

    }
    
}
