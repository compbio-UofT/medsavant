/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import cytoscape.plugin.CytoscapePlugin;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author khushi
 */
public class GeneManiaCytoscapePlugin  extends CytoscapePlugin{
    public GeneManiaCytoscapePlugin(){
        try {
            GenemaniaInfoRetriever g = new GenemaniaInfoRetriever();
        } catch (Exception ex) {
            Logger.getLogger(GeneManiaCytoscapePlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
