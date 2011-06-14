/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter.ontology;

import org.ut.biolab.medsavant.view.filter.geneontology.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Nirvana Nursimulu
 */
public class Tree {
    
    
    /**
     * Fake root of the tree.  Useful since this is actually a forest of trees.
     */
    protected Node fakeRoot; 
    
    /**
     * Dictionary from an identifier to a node.
     */
    protected HashMap<String, Node> identifierToNode;
    
    /**
     * Map from GO ID to locations
     */
    protected HashMap<String, ArrayList<ArrayList<String>>> goToLocs;
    
    /**
     * Constructor
     * @param fileGenLocation the location of the file mapping GO IDs to 
     * genome locations.
     */
    public Tree() throws Exception{
        
        fakeRoot = new Node("ROOT");
        identifierToNode = new HashMap<String, Node>();
        identifierToNode.put("ROOT", fakeRoot);  
        goToLocs = new HashMap<String, ArrayList<ArrayList<String>>>();
    }
    
    /**
     * Get the root nodes of this tree.
     * @return the actual roots of this forest (XNode objects).
     */
    public Set<Node> getRootNodes(){
        
        return fakeRoot.getChildren();
    }
    
    /**
     * Get the children of the node with the provided identifier.
     * @param identifier the identifier of the parent.
     * @return the TreeSet of children nodes of this parent.
     */
    public TreeSet<Node> getChildrenNodes(String identifier){
        
        return identifierToNode.get(identifier).getChildren();
    }
    
    /**
     * Mark the locations of this node.
     * @param node the node whose locations are to be marked.
     */
    private void markLocations(Node node){
        
        // If the locations have already been marked, do not do anything.
        if (node.getLocs() != null){
            return;
        }
        ArrayList<ArrayList<String>> locs = 
                goToLocs.get(node.getIdentifier().replace(':', '_'));
        if (locs == null){
            
            locs = new ArrayList< ArrayList<String> >();
        }
        node.setLocs(locs);
    }
    
    /**
     * Add this "node" to the tree.  Assumes that this method is called only 
     * once for each child node.
     * @param child the child in this relationship.
     * @param parentID the parent's identifier in this relationship.
     */
    public void addNode(Node child, List<String> parentIDs){
        
        // First of all, get the child node if it already exists from the 
        // dictionary.
        // If the child node does not already exist, add to the list of nodes
        // to watch out for.  Note that the information in this node should
        // be complete since this node is being encountered as a child.
        Node childref = identifierToNode.get(child.getIdentifier());
        
        if (childref != null){
            
            child.copyInfoExceptChildrenTo(childref);
        }
        else{
            
            // Put into dictionary if node not seen yet.
            childref = child;
            identifierToNode.put(childref.getIdentifier(), childref);            
        }
        
        markLocations(childref);
        
        // For each parent, mark this as their child.
        for (String parentID: parentIDs){
        
            // If the parent has already been encountered, retrieve the record.
            // Otherwise, create a record in the dictionary.
            Node parentRef = identifierToNode.get(parentID);
            if (parentRef == null){
                
                parentRef = new Node(parentID);
                identifierToNode.put(parentID, parentRef);
            }
            
            // Mark the child.  This is the easy part.
            parentRef.addChild(childref);
            
                        
        }

    }
    
    /**
     * Add a (true) root to this tree.
     * @param root 
     */
    public void addRoot(Node root){
        
        // See if we already have this root in a map.  If so, use it; otherwise,
        // put the root into the dictionary.
        Node rootRef = identifierToNode.get(root.getIdentifier());
        if (rootRef == null){
            
            rootRef = root;
            identifierToNode.put(rootRef.getIdentifier(), rootRef);
        }
        
        // Add this node as a child to the fake root.
        fakeRoot.addChild(root);
        
        // Mark the location of this node.
        markLocations(root);
    }
    

}
