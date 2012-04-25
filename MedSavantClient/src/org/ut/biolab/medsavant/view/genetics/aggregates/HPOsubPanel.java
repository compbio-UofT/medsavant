/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.view.genetics.aggregates;

import javax.swing.JTree;

import org.ut.biolab.medsavant.view.genetics.OntologyPanelGenerator;
import org.ut.biolab.medsavant.view.genetics.filter.HPOFilter;
import org.ut.biolab.medsavant.view.genetics.filter.hpontology.HPTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.ConstructJTree;
import org.ut.biolab.medsavant.view.genetics.filter.ontology.Tree;
import org.ut.biolab.medsavant.view.genetics.storer.FilterObjectStorer;

/**
 *
 * @author Nirvana Nursimulu
 */
public class HPOsubPanel extends OntologySubPanel{
    
    private JTree jTree;
    OntologyPanelGenerator.OntologyPanel panel;
    
    public HPOsubPanel(OntologyPanelGenerator.OntologyPanel panel, String pageName){
        super(panel, pageName, 0, 1, 2);
        this.panel = super.panel;
    }

    
    @Override
    public String getName(){
        return "Human Phenotype Ontology";
    }
    
    @Override
    public boolean treeIsReadyToBeFetched(){
        return FilterObjectStorer.containsObjectWithName(HPOFilter.NAME_TREE);
    } 
    
    public Tree getTree(){
        return (Tree)FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
    }
    
    @Override
    public JTree getJTree(){
        if (jTree != null){
            return jTree;
        }
        else {
            //Tree tree = (Tree)FilterObjectStorer.getObject(HPOFilter.NAME_TREE);
            try {
                Tree tree = new HPTree();
                jTree = ConstructJTree.getTree(tree, false, true, false);
            } catch (Exception e){
                e.printStackTrace();
            }
            return jTree;
        }
    }

    @Override
    public void setUpdateRequired(boolean required) {
        //
    }
    
    
}
