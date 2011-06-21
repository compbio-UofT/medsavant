/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author Nirvana Nursimulu
 */
public class Node implements Comparable{
    
    /**
     * Child of this node.
     */
    private TreeSet<Node> children;
    
    /**
     * Identifier of this node. (Acts as some kind of key)
     */
    private String identifier;
    
    /**
     * Description of this node.
     */
    private String description;
    
    /**
     * Keep track of the genome locations associated with this node, if any.
     */
    private ArrayList<ArrayList<String>> locs;
    
    
    /**
     * Useful for the GUI implementation; says if this node has already been 
     * selected earlier.
     */
    private boolean hasBeenSelected;
    
    
    
    
    /**
     * Constructor of this node given an identifier.
     * @param identifier  "key" of this node.
     */
    public Node(String identifier){
        
        this.children = new TreeSet<Node>();
        this.identifier = identifier;
        this.description = null;
        this.locs = null;
        this.hasBeenSelected = false;
    }
    
    
    
    
    public void setLocs(ArrayList<ArrayList<String>> locs){
        
        this.locs = locs;
    }
    
    public ArrayList<ArrayList<String>> getLocs(){
        
        return this.locs;
    }
    
        
    /**
     * Adds a child to this node.
     * @param child 
     */
    public void addChild(Node child){
        
        this.children.add(child);
    }
    
    
    /**
     * Removes a certain child.
     * @param child the child to be removed.
     * @return true iff the child was removed.
     */
    public boolean removeChild(Node child){
        
        // If this is not the root node, this operation is not enabled.
        if (!this.getIdentifier().equals("ROOT")){
            
            return false;
        }
        else{
            
            return this.children.remove(child);
        }
    }
    
    /**
     * Returns the child of this node.
     * @return 
     */
    public TreeSet<Node> getChildren(){
        
        return this.children;
    }
    
    /**
     * Copy info from this node into the designated node 
     * (except for children info)
     * @param node node to copy info to. 
     */
    public void copyInfoExceptChildrenTo(Node node){
        
        node.description = this.description;
        node.identifier = this.identifier;
        node.locs = this.locs;
    }
    
    /**
     * Get the identifier of this node.
     * @return the identifier of this node.
     */
    public String getIdentifier(){
        
        return this.identifier;
    }
    
    /**
     * Sets the identifier of this node.
     * @param identifier 
     */
    public void setIdentifier(String identifier){
        
        this.identifier = identifier;
    }
    
    /**
     * Set the description of this node.
     * @param description 
     */
    public void setDescription(String description){
        
        this.description = description;
    }
    
    /**
     * Get the description of this node.
     * @return description.
     */
    public String getDescription(){
        
        return this.description;
    }
    
    @Override
    public String toString(){
        
        return this.description;
    }

    
    /**
     * Select this node; useful in the GUI implementation
     */
    public void select(){
        
        this.hasBeenSelected = true;
    }
    
    /**
     * Says if this node has been selected.
     * @return true iff this node has been selected.
     */
    public boolean isSelected(){
        
        return this.hasBeenSelected;
    }

    @Override
    /**
     * How does this node compare to this other object, which is potentially a 
     * node.
     */
    public int compareTo(Object o) {
        
        try{
            Node node = (Node)o;
            return this.description.compareTo(node.description);
        }
        catch(Exception e){
            
            return 0;
        }
    }
    
}
