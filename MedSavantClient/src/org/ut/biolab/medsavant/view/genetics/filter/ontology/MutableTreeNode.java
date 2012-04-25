/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter.ontology;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Nirvana Nursimulu
 */
public class MutableTreeNode extends DefaultMutableTreeNode{
    
    private boolean isToString;
    
    public MutableTreeNode(){
        super();
    }
    
    public MutableTreeNode(Node userObject){
        super(userObject);
    }
    
    public MutableTreeNode(Node userObject, boolean allowsChildren){
        super(userObject, allowsChildren);
    }
    
    /**
     * Do you want the string part only or the entire value of the description.
     * @param isToString 
     */
    public void setToString(boolean isToString){
        this.isToString = isToString;
    }
    
    @Override
    public String toString(){
        if (isToString){
            return super.toString();
        }
        else{
            return ((Node)this.userObject).toValue();
        }
    }
}
